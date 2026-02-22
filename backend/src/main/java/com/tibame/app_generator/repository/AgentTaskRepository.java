package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.AgentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTask, UUID> {
    List<AgentTask> findByProjectId(UUID projectId);
}
