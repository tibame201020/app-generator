package com.tibame.app_generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;
    private final AgentTaskService agentTaskService;
    private final ObjectMapper objectMapper;

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
        // Edges might be empty if it's a single node workflow

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

        // Check required roles: PM, SA, PG, QA
        for (AgentType type : AgentType.values()) {
            if (!presentTypes.contains(type.name())) {
                 errors.add("Missing required agent role: " + type.name());
            }
        }

        return errors;
    }

    @Async
    public void compileAndRun(UUID projectId) {
        Workflow workflow = getWorkflow(projectId);
        List<String> validationErrors = validateWorkflow(workflow.getGraphData());
        if (!validationErrors.isEmpty()) {
            throw new IllegalStateException("Workflow validation failed: " + validationErrors);
        }

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) workflow.getGraphData().get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) workflow.getGraphData().get("edges");
        if (edges == null) edges = new ArrayList<>();

        // Topological Sort
        List<Map<String, Object>> sortedNodes = topologicalSort(nodes, edges);

        // Execute sequentially
        for (Map<String, Object> node : sortedNodes) {
             Map<String, Object> data = (Map<String, Object>) node.get("data");
             String agentTypeStr = (String) data.get("agentType");
             String label = (String) data.get("label");

             AgentType agentType;
             try {
                 agentType = AgentType.valueOf(agentTypeStr);
             } catch (IllegalArgumentException e) {
                 log.error("Invalid agent type: {}", agentTypeStr);
                 continue;
             }

             // Create Task
             AgentTask task = agentTaskService.createTask(projectId, agentType, label != null ? label : agentType.name() + " Task", data);

             // Run Task (Synchronously inside this Async method)
             runTaskSimulation(task);
        }
    }

    private void runTaskSimulation(AgentTask task) {
         try {
            log.info("Starting task execution for {}", task.getId());
            agentTaskService.startTask(task.getId());

            // Simulate steps
            int steps = 5;
            for (int i = 1; i <= steps; i++) {
                TimeUnit.SECONDS.sleep(1);
                int progress = i * 20;
                agentTaskService.updateProgress(task.getId(), progress, "Processing step " + i + "...");
            }

            agentTaskService.completeTask(task.getId(), "Task completed successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            agentTaskService.failTask(task.getId(), "Task interrupted.");
        } catch (Exception e) {
            log.error("Task execution failed", e);
            agentTaskService.failTask(task.getId(), "Task failed: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> topologicalSort(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
        // Build adjacency list
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();

        for (Map<String, Object> node : nodes) {
            String id = (String) node.get("id");
            nodeMap.put(id, node);
            adj.put(id, new ArrayList<>());
            inDegree.put(id, 0);
        }

        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            if (adj.containsKey(source) && inDegree.containsKey(target)) {
                adj.get(source).add(target);
                inDegree.put(target, inDegree.get(target) + 1);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String u = queue.poll();
            result.add(nodeMap.get(u));

            if (adj.containsKey(u)) {
                for (String v : adj.get(u)) {
                    inDegree.put(v, inDegree.get(v) - 1);
                    if (inDegree.get(v) == 0) {
                        queue.add(v);
                    }
                }
            }
        }

        if (result.size() != nodes.size()) {
            throw new IllegalStateException("Cycle detected in workflow graph");
        }

        return result;
    }
}
