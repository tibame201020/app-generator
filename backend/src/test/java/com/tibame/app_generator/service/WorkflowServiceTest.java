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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkflowServiceTest {

    @Mock private WorkflowRepository workflowRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private WorkflowRunRepository workflowRunRepository;
    @Mock private AgentTaskService agentTaskService;
    @Mock private AgentTaskRepository agentTaskRepository;
    @Mock private WorkflowExecutor workflowExecutor;

    @InjectMocks private WorkflowService workflowService;

    @Test
    public void testStartRun() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);

        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("id", "1");
        Map<String, Object> data = new HashMap<>();
        data.put("agentType", "PM");
        node.put("data", data);
        nodes.add(node);

        // Add other required roles
        String[] requiredRoles = {"SA", "PG", "QA"};
        for (int i = 0; i < requiredRoles.length; i++) {
            Map<String, Object> nextNode = new HashMap<>();
            nextNode.put("id", String.valueOf(i + 2));
            Map<String, Object> nextData = new HashMap<>();
            nextData.put("agentType", requiredRoles[i]);
            nextNode.put("data", nextData);
            nodes.add(nextNode);
        }

        graphData.put("nodes", nodes);

        Workflow workflow = new Workflow();
        workflow.setGraphData(graphData);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(workflowRepository.findByProjectId(projectId)).thenReturn(Optional.of(workflow));
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(i -> {
            WorkflowRun run = (WorkflowRun) i.getArguments()[0];
            run.setId(UUID.randomUUID());
            return run;
        });

        workflowService.startRun(projectId);

        verify(workflowRunRepository, times(1)).save(any(WorkflowRun.class));
        verify(workflowExecutor, times(1)).executeRunAsync(any(UUID.class), eq(projectId));
    }

    @Test
    public void testRetryTask() {
        UUID taskId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();

        AgentTask task = new AgentTask();
        task.setId(taskId);
        WorkflowRun run = new WorkflowRun();
        run.setId(runId);
        task.setWorkflowRun(run);

        when(agentTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        workflowService.retryTask(taskId);

        verify(agentTaskRepository, times(1)).save(task);
        // Async call retryTaskAsync is triggered
    }
}
