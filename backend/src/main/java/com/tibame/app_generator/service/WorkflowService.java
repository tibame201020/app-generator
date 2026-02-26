package com.tibame.app_generator.service;

import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.model.WorkflowRun;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.WorkflowRunRepository;
import com.tibame.app_generator.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final AgentTaskRepository agentTaskRepository;
    private final WorkflowExecutor workflowExecutor;

    @Transactional
    public Workflow saveWorkflow(UUID projectId, Map<String, Object> graphData) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Workflow workflow = workflowRepository.findByProjectId(projectId)
                .orElse(Workflow.builder().project(project).build());

        workflow.setGraphData(graphData);
        return workflowRepository.save(workflow);
    }

    public Workflow getWorkflow(UUID projectId) {
        return workflowRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for project: " + projectId));
    }

    public List<String> validateWorkflow(Map<String, Object> graphData) {
        List<String> errors = new ArrayList<>();
        if (graphData == null || !graphData.containsKey("nodes")) {
            errors.add("Invalid graph data structure.");
            return errors;
        }

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graphData.get("nodes");

        if (nodes == null || nodes.isEmpty()) {
            errors.add("Workflow must contain at least one node.");
            return errors;
        }

        Set<String> presentTypes = new HashSet<>();
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            if (data != null && data.containsKey("agentType")) {
                presentTypes.add(String.valueOf(data.get("agentType")));
            }
        }

        for (AgentType type : AgentType.values()) {
            if (!presentTypes.contains(type.name())) {
                 errors.add("Missing required agent role: " + type.name());
            }
        }

        return errors;
    }

    @Transactional
    public WorkflowRun startRun(UUID projectId) {
        Workflow workflow = getWorkflow(projectId);
        List<String> validationErrors = validateWorkflow(workflow.getGraphData());
        if (!validationErrors.isEmpty()) {
            throw new IllegalStateException("Workflow validation failed: " + validationErrors);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        WorkflowRun run = WorkflowRun.builder()
                .project(project)
                .status(TaskStatus.RUNNING)
                .startedAt(ZonedDateTime.now())
                .build();

        run = workflowRunRepository.save(run);

        // Trigger async execution via executor
        workflowExecutor.executeRunAsync(run.getId(), projectId);

        return run;
    }

    @Async
    public void compileAndRun(UUID projectId) {
        // Deprecated: Delegates to startRun but ignores return
        startRun(projectId);
    }

    @Transactional
    public void resumeRun(UUID runId) {
        WorkflowRun run = workflowRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        if (run.getStatus() == TaskStatus.RUNNING) {
             throw new IllegalStateException("Run is already running.");
        }

        List<AgentTask> tasks = agentTaskRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(runId);
        AgentTask failedTask = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.FAIL)
                .findFirst()
                .orElse(null);

        if (failedTask != null) {
            retryTask(failedTask.getId());
        } else {
            // If no failed task, maybe it was interrupted?
            // If status is FAIL but no failed task, maybe system crash.
            // We should try to find the last task and see if we can resume.
            log.warn("No failed task found to resume for run {}", runId);
            // If we want to support resuming interrupted runs (e.g. server restart), we'd need to check which tasks are PENDING or RUNNING and restart them.
            // For now, simpler to just say "No failed task".
        }
    }

    @Transactional
    public void retryTask(UUID taskId) {
        AgentTask task = agentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        WorkflowRun run = task.getWorkflowRun();
        if (run == null) {
            throw new IllegalStateException("Task is not associated with a workflow run.");
        }

        // Idempotency check
        if (task.getStatus() == TaskStatus.RUNNING) {
            throw new IllegalStateException("Task is already running.");
        }

        // Reset task status
        task.setStatus(TaskStatus.RUNNING);
        task.setRetryCount(task.getRetryCount() + 1);
        task.setErrorDetails(null);

        // Update retry metadata
        Map<String, Object> metadata = task.getRetryMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("timestamp", ZonedDateTime.now().toString());
        entry.put("reason", "User initiated retry"); // Hardcoded for now as we don't have user info passed
        // entry.put("userId", userId); // If we had userId

        // Use a list for history? Or just last retry?
        // Since retryMetadata is Map, maybe we store "history" list inside.
        List<Map<String, Object>> history = (List<Map<String, Object>>) metadata.get("history");
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(entry);
        metadata.put("history", history);
        metadata.put("lastRetry", entry);

        task.setRetryMetadata(metadata);

        agentTaskRepository.save(task);

        // Update run status if needed
        if (run.getStatus() != TaskStatus.RUNNING) {
            run.setStatus(TaskStatus.RUNNING);
            workflowRunRepository.save(run);
        }

        // Run async via executor
        workflowExecutor.retryTaskAsync(task.getId(), run.getId());
    }
}
