package com.tibame.app_generator.controller;

import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.service.AgentTaskService;
import com.tibame.app_generator.service.AgentTaskSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class AgentTaskController {

    private final AgentTaskService agentTaskService;
    private final AgentTaskSimulator agentTaskSimulator;

    @GetMapping
    public ResponseEntity<List<AgentTask>> listTasks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(agentTaskService.getTasksByProject(projectId));
    }

    @PostMapping("/simulate")
    public ResponseEntity<?> simulateTask(@PathVariable UUID projectId) {
        AgentTask task = agentTaskService.createTask(
                projectId,
                AgentType.PM, // Using PM for simulation
                "Simulated Task: Requirement Analysis",
                Map.of("simulation", true)
        );

        // Start the simulation asynchronously
        agentTaskSimulator.simulateTaskExecution(task.getId());

        return ResponseEntity.ok(task);
    }
}
