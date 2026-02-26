package com.tibame.app_generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.WorkflowRepository;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.service.llm.LlmAgentExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AgentTaskService agentTaskService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LlmAgentExecutionService llmAgentExecutionService;

    @InjectMocks
    private WorkflowService workflowService;

    private UUID projectId;
    private Project project;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        project = Project.builder().id(projectId).description("Test Project").build();
    }

    @Test
    void validateWorkflow_ShouldReturnErrors_WhenNodesMissing() {
        Map<String, Object> graphData = new HashMap<>();
        List<String> errors = workflowService.validateWorkflow(graphData);
        assertTrue(errors.contains("Invalid graph data structure.") || errors.contains("Workflow must contain at least one node."));
    }

    @Test
    void validateWorkflow_ShouldReturnErrors_WhenRolesMissing() {
        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("data", Map.of("agentType", "PM"));
        nodes.add(node);
        graphData.put("nodes", nodes);

        List<String> errors = workflowService.validateWorkflow(graphData);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Missing required agent role: SA")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Missing required agent role: PG")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Missing required agent role: QA")));
    }

    @Test
    void validateWorkflow_ShouldPass_WhenAllRolesPresent() {
        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(Map.of("id", "1", "data", Map.of("agentType", "PM")));
        nodes.add(Map.of("id", "2", "data", Map.of("agentType", "SA")));
        nodes.add(Map.of("id", "3", "data", Map.of("agentType", "PG")));
        nodes.add(Map.of("id", "4", "data", Map.of("agentType", "QA")));
        graphData.put("nodes", nodes);
        graphData.put("edges", new ArrayList<>());

        List<String> errors = workflowService.validateWorkflow(graphData);
        assertTrue(errors.isEmpty());
    }

    @Test
    void compileAndRun_ShouldSortAndExecute() {
        // Prepare graph: PM -> SA -> PG -> QA
        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(Map.of("id", "1", "data", Map.of("agentType", "PM")));
        nodes.add(Map.of("id", "2", "data", Map.of("agentType", "SA")));
        nodes.add(Map.of("id", "3", "data", Map.of("agentType", "PG")));
        nodes.add(Map.of("id", "4", "data", Map.of("agentType", "QA")));

        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(Map.of("source", "1", "target", "2")); // PM -> SA
        edges.add(Map.of("source", "2", "target", "3")); // SA -> PG
        edges.add(Map.of("source", "3", "target", "4")); // PG -> QA

        graphData.put("nodes", nodes);
        graphData.put("edges", edges);

        Workflow workflow = Workflow.builder().project(project).graphData(graphData).build();

        when(workflowRepository.findByProjectId(projectId)).thenReturn(Optional.of(workflow));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Mock createTask to return a mock task so logic can proceed
        when(agentTaskService.createTask(any(), any(), any(), any())).thenAnswer(invocation -> {
            return AgentTask.builder().id(UUID.randomUUID()).build();
        });

        // Mock llmAgentExecutionService
        when(llmAgentExecutionService.executeTask(any(), any())).thenReturn(new HashMap<>());

        workflowService.compileAndRun(projectId);

        verify(agentTaskService, times(4)).createTask(eq(projectId), any(AgentType.class), anyString(), anyMap());
        verify(llmAgentExecutionService, times(4)).executeTask(any(), any());
    }
}
