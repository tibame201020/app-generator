package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, UUID> {
    List<WorkflowRun> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
