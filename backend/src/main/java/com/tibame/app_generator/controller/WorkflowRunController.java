package com.tibame.app_generator.controller;

import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.WorkflowRun;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.WorkflowRunRepository;
import com.tibame.app_generator.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkflowRunController {

    private final WorkflowService workflowService;
    private final WorkflowRunRepository workflowRunRepository;
    private final AgentTaskRepository agentTaskRepository;

    @GetMapping("/api/projects/{projectId}/runs")
    public ResponseEntity<List<WorkflowRun>> getProjectRuns(@PathVariable UUID projectId) {
        return ResponseEntity.ok(workflowRunRepository.findByProjectIdOrderByCreatedAtDesc(projectId));
    }

    @PostMapping("/api/projects/{projectId}/runs")
    public ResponseEntity<?> startRun(@PathVariable UUID projectId) {
        try {
            WorkflowRun run = workflowService.startRun(projectId);
            return ResponseEntity.accepted().body(run);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/runs/{runId}")
    public ResponseEntity<WorkflowRun> getRun(@PathVariable UUID runId) {
        return workflowRunRepository.findById(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/runs/{runId}/tasks")
    public ResponseEntity<List<AgentTask>> getRunTasks(@PathVariable UUID runId) {
        return ResponseEntity.ok(agentTaskRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(runId));
    }

    @PostMapping("/api/runs/{runId}/retry")
    public ResponseEntity<?> retryRun(@PathVariable UUID runId) {
        try {
            workflowService.resumeRun(runId);
            return ResponseEntity.accepted().body(Map.of("message", "Run resumed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/tasks/{taskId}/retry")
    public ResponseEntity<?> retryTask(@PathVariable UUID taskId) {
        try {
            workflowService.retryTask(taskId);
            return ResponseEntity.accepted().body(Map.of("message", "Task retry initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
