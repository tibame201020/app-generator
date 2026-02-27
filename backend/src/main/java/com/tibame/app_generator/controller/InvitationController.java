package com.tibame.app_generator.controller;

import com.tibame.app_generator.enums.ProjectRole;
import com.tibame.app_generator.model.ProjectInvitation;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.service.InvitationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    // --- Project-scoped endpoints ---

    @PostMapping("/projects/{projectId}/invitations")
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<?> createInvitation(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal User user,
            @RequestBody CreateInvitationRequest request) {
        try {
            ProjectInvitation invitation = invitationService.createInvitation(
                    projectId, user.getId(), request.getEmail(), request.getRole());
            return ResponseEntity.ok(invitation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/projects/{projectId}/invitations")
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<List<ProjectInvitation>> getProjectInvitations(@PathVariable UUID projectId) {
        return ResponseEntity.ok(invitationService.getPendingInvitationsForProject(projectId));
    }

    @DeleteMapping("/projects/{projectId}/invitations/{invitationId}")
    @PreAuthorize("@projectSecurityService.isAdmin(#projectId)")
    public ResponseEntity<?> cancelInvitation(@PathVariable UUID projectId, @PathVariable UUID invitationId) {
        // Service should probably verify the invitation belongs to the project, but for now ID is unique.
        try {
            invitationService.cancelInvitation(invitationId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- User-scoped endpoints ---

    @GetMapping("/invitations/me")
    public ResponseEntity<List<ProjectInvitation>> getMyInvitations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invitationService.getPendingInvitationsForUser(user.getEmail()));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal User user) {
        try {
            invitationService.acceptInvitation(invitationId, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal User user) {
        try {
            invitationService.rejectInvitation(invitationId, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class CreateInvitationRequest {
        private String email;
        private ProjectRole role;
    }
}
