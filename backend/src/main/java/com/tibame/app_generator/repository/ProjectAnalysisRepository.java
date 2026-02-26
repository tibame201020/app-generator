package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.ProjectAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectAnalysisRepository extends JpaRepository<ProjectAnalysis, UUID> {
    Optional<ProjectAnalysis> findByProjectId(UUID projectId);
}
