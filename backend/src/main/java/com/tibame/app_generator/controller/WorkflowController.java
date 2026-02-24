package com.tibame.app_generator.controller;

import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public ResponseEntity<Workflow> getWorkflow(@PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflow(projectId));
        } catch (IllegalArgumentException e) {
            // Return empty 200 or 404?
            // If it's a new project, maybe no workflow exists yet.
            // Frontend might expect 404 or empty object.
            // Let's return 404 and frontend can handle it.
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Workflow> saveWorkflow(@PathVariable UUID projectId, @RequestBody Map<String, Object> graphData) {
        return ResponseEntity.ok(workflowService.saveWorkflow(projectId, graphData));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateWorkflow(@PathVariable UUID projectId, @RequestBody Map<String, Object> graphData) {
        List<String> errors = workflowService.validateWorkflow(graphData);
        if (errors.isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.ok(Map.of("valid", false, "errors", errors));
        }
    }

    @PostMapping("/run")
    public ResponseEntity<?> runWorkflow(@PathVariable UUID projectId) {
        try {
            workflowService.compileAndRun(projectId);
            return ResponseEntity.accepted().body(Map.of("message", "Workflow execution started"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
