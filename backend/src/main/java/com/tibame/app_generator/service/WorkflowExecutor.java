package com.tibame.app_generator.service;

import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.model.WorkflowRun;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.WorkflowRepository;
import com.tibame.app_generator.repository.WorkflowRunRepository;
import com.tibame.app_generator.service.llm.LlmAgentExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutor {

    private final WorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final AgentTaskService agentTaskService;
    private final AgentTaskRepository agentTaskRepository;
    private final LlmAgentExecutionService llmAgentExecutionService;

    @Async
    public void executeRunAsync(UUID runId, UUID projectId) {
         try {
             WorkflowRun run = workflowRunRepository.findById(runId).orElseThrow();
             Workflow workflow = workflowRepository.findByProjectId(projectId)
                     .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

             // Initial context
             Project project = projectRepository.findById(projectId).orElseThrow();
             Map<String, Object> executionContext = new HashMap<>();
             if (project.getDescription() != null) {
                 executionContext.put("description", project.getDescription());
             }

             List<Map<String, Object>> sortedNodes = getSortedNodes(workflow.getGraphData());

             for (Map<String, Object> node : sortedNodes) {
                 executeNode(run, node, executionContext);
             }

             completeRun(runId);

         } catch (Exception e) {
             log.error("Run failed", e);
             failRun(runId);
         }
    }

    @Async
    public void continueRunAsync(UUID runId, Map<String, Object> context, int startNodeIndex) {
        try {
            WorkflowRun run = workflowRunRepository.findById(runId).orElseThrow();
            Workflow workflow = workflowRepository.findByProjectId(run.getProject().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

            List<Map<String, Object>> sortedNodes = getSortedNodes(workflow.getGraphData());

            for (int i = startNodeIndex; i < sortedNodes.size(); i++) {
                executeNode(run, sortedNodes.get(i), context);
            }

            completeRun(runId);
        } catch (Exception e) {
            log.error("Run continuation failed", e);
            failRun(runId);
        }
    }

    @Async
    public void retryTaskAsync(UUID taskId, UUID runId) {
        try {
            AgentTask task = agentTaskRepository.findById(taskId).orElseThrow();
            WorkflowRun run = workflowRunRepository.findById(runId).orElseThrow();

            // Re-execute task
            Map<String, Object> inputContext = task.getInputContext();
            Map<String, Object> result = llmAgentExecutionService.executeTask(task, inputContext);

            // If successful, continue the run
            Map<String, Object> nextContext = new HashMap<>(inputContext);
            if (result != null) {
                nextContext.putAll(result);
            }

            // Determine next node index
            Workflow workflow = workflowRepository.findByProjectId(run.getProject().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
            List<Map<String, Object>> sortedNodes = getSortedNodes(workflow.getGraphData());

            // Find task index by matching agent types in order
            List<AgentTask> tasks = agentTaskRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(run.getId());

            int taskIndex = -1;
            for(int i=0; i<tasks.size(); i++) {
                if(tasks.get(i).getId().equals(taskId)) {
                    taskIndex = i;
                    break;
                }
            }

            if (taskIndex >= 0 && taskIndex < sortedNodes.size() - 1) {
                // Continue with next node
                continueRunAsync(run.getId(), nextContext, taskIndex + 1);
            } else {
                // Last task, just complete run
                completeRun(run.getId());
            }

        } catch (Exception e) {
            log.error("Task retry failed", e);
            failRun(runId);
        }
    }

    private void executeNode(WorkflowRun run, Map<String, Object> node, Map<String, Object> context) {
         Map<String, Object> data = (Map<String, Object>) node.get("data");
         String agentTypeStr = (String) data.get("agentType");
         String label = (String) data.get("label");

         AgentType agentType;
         try {
             agentType = AgentType.valueOf(agentTypeStr);
         } catch (IllegalArgumentException e) {
             log.error("Invalid agent type: {}", agentTypeStr);
             throw e;
         }

         // Create Task
         AgentTask task = agentTaskService.createTask(run.getProject().getId(), run, agentType, label != null ? label : agentType.name() + " Task", data);

         // Execute
         Map<String, Object> result = llmAgentExecutionService.executeTask(task, context);

         // Update context for next steps
         if (result != null) {
             context.putAll(result);
         }
    }

    private void completeRun(UUID runId) {
        WorkflowRun run = workflowRunRepository.findById(runId).orElse(null);
        if (run != null) {
            run.setStatus(TaskStatus.SUCCESS);
            run.setEndedAt(ZonedDateTime.now());
            workflowRunRepository.save(run);
        }
    }

    private void failRun(UUID runId) {
        WorkflowRun run = workflowRunRepository.findById(runId).orElse(null);
        if (run != null) {
            run.setStatus(TaskStatus.FAIL);
            run.setEndedAt(ZonedDateTime.now());
            workflowRunRepository.save(run);
        }
    }

    private List<Map<String, Object>> getSortedNodes(Map<String, Object> graphData) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graphData.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) graphData.get("edges");
        if (edges == null) edges = new ArrayList<>();
        return topologicalSort(nodes, edges);
    }

    private List<Map<String, Object>> topologicalSort(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
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
