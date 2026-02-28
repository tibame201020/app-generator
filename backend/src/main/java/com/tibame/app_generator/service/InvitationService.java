package com.tibame.app_generator.service;

import com.tibame.app_generator.enums.ProjectRole;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.ProjectInvitation;
import com.tibame.app_generator.model.ProjectInvitation.InvitationStatus;
import com.tibame.app_generator.model.ProjectMember;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.repository.ProjectInvitationRepository;
import com.tibame.app_generator.repository.ProjectMemberRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectInvitation createInvitation(UUID projectId, UUID inviterId, String email, ProjectRole role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate: User isn't already a member (if they exist)
        userRepository.findByEmail(email).ifPresent(user -> {
            if (projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId()).isPresent()) {
                throw new IllegalArgumentException("User is already a member of this project");
            }
        });

        // Validate: No pending invitation exists for this email
        if (invitationRepository.findByProjectIdAndEmailAndStatus(projectId, email, InvitationStatus.PENDING).isPresent()) {
            throw new IllegalArgumentException("Pending invitation already exists for this email");
        }

        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .inviter(inviter)
                .email(email)
                .role(role)
                .status(InvitationStatus.PENDING)
                .build();

        return invitationRepository.save(invitation);
    }

    public List<ProjectInvitation> getPendingInvitationsForProject(UUID projectId) {
        return invitationRepository.findByProjectIdAndStatus(projectId, InvitationStatus.PENDING);
    }

    public List<ProjectInvitation> getPendingInvitationsForUser(String email) {
        return invitationRepository.findByEmailAndStatus(email, InvitationStatus.PENDING);
    }

    @Transactional
    public void acceptInvitation(UUID invitationId, UUID userId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new IllegalArgumentException("Invitation email does not match user email");
        }

        // Add member
        ProjectMember member = ProjectMember.builder()
                .project(invitation.getProject())
                .user(user)
                .role(invitation.getRole())
                .build();
        projectMemberRepository.save(member);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(UUID invitationId, UUID userId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new IllegalArgumentException("Invitation email does not match user email");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(UUID invitationId) {
         ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

         // Only pending invitations can be cancelled/deleted (or we just delete it)
         // For now, let's just delete it or mark rejected/cancelled?
         // Requirement implies "Cancel" -> Delete usually.
         invitationRepository.delete(invitation);
    }
}
