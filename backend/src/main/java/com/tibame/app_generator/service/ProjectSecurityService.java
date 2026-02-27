package com.tibame.app_generator.service;

import com.tibame.app_generator.enums.ProjectRole;
import com.tibame.app_generator.model.ProjectMember;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("projectSecurityService")
@RequiredArgsConstructor
public class ProjectSecurityService {

    private final ProjectMemberRepository projectMemberRepository;

    public boolean hasRole(UUID projectId, ProjectRole requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        User user = (User) authentication.getPrincipal();

        return projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .map(member -> member.getRole().ordinal() <= requiredRole.ordinal())
                .orElse(false);
    }

    public boolean isAdmin(UUID projectId) {
        return hasRole(projectId, ProjectRole.ADMIN);
    }

    public boolean isMember(UUID projectId) {
        return hasRole(projectId, ProjectRole.MEMBER);
    }

    public boolean isViewer(UUID projectId) {
        return hasRole(projectId, ProjectRole.VIEWER);
    }
}
