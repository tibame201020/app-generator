package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    Optional<Workflow> findByProjectId(UUID projectId);
}
