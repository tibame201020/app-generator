package com.tibame.app_generator.controller;

import com.tibame.app_generator.enums.ProjectRole;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.ProjectMember;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.repository.ProjectMemberRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("@projectSecurityService.isViewer(#projectId)")
    public ResponseEntity<List<ProjectMember>> getMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectMemberRepository.findByProjectId(projectId));
    }

    @PostMapping
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<?> addMember(@PathVariable UUID projectId, @RequestBody AddMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId()).isPresent()) {
            return ResponseEntity.badRequest().body("User is already a member");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(request.getRole())
                .build();

        return ResponseEntity.ok(projectMemberRepository.save(member));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestBody UpdateRoleRequest request) {

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        member.setRole(request.getRole());
        return ResponseEntity.ok(projectMemberRepository.save(member));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<?> removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        projectMemberRepository.delete(member);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class AddMemberRequest {
        private String email;
        private ProjectRole role;
    }

    @Data
    public static class UpdateRoleRequest {
        private ProjectRole role;
    }
}
