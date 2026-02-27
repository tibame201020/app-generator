package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
    List<ProjectMember> findByProjectId(UUID projectId);
}
