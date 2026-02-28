package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.ProjectInvitation;
import com.tibame.app_generator.model.ProjectInvitation.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {

    // Find pending invitations for a specific email
    List<ProjectInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    // Find all invitations for a project
    List<ProjectInvitation> findByProjectId(UUID projectId);

    // Find pending invitations for a project
    List<ProjectInvitation> findByProjectIdAndStatus(UUID projectId, InvitationStatus status);

    // Check for duplicate pending invite
    Optional<ProjectInvitation> findByProjectIdAndEmailAndStatus(UUID projectId, String email, InvitationStatus status);
}
