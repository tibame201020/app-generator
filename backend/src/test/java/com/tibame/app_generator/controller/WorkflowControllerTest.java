package com.tibame.app_generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.service.JwtService;
import com.tibame.app_generator.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getWorkflow_ShouldReturnWorkflow() throws Exception {
        UUID projectId = UUID.randomUUID();
        Workflow workflow = Workflow.builder().id(UUID.randomUUID()).build();
        when(workflowService.getWorkflow(projectId)).thenReturn(workflow);

        mockMvc.perform(get("/api/projects/{projectId}/workflow", projectId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void saveWorkflow_ShouldReturnSavedWorkflow() throws Exception {
        UUID projectId = UUID.randomUUID();
        Map<String, Object> graphData = Map.of("nodes", Collections.emptyList());
        Workflow workflow = Workflow.builder().id(UUID.randomUUID()).graphData(graphData).build();
        when(workflowService.saveWorkflow(eq(projectId), any())).thenReturn(workflow);

        mockMvc.perform(post("/api/projects/{projectId}/workflow", projectId)
                        .with(csrf()) // Important for POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(graphData)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void validateWorkflow_ShouldReturnValidationResult() throws Exception {
        UUID projectId = UUID.randomUUID();
        Map<String, Object> graphData = Map.of("nodes", Collections.emptyList());
        when(workflowService.validateWorkflow(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/projects/{projectId}/workflow/validate", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(graphData)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void runWorkflow_ShouldStartExecution() throws Exception {
        UUID projectId = UUID.randomUUID();
        // workflowService.compileAndRun returns void

        mockMvc.perform(post("/api/projects/{projectId}/workflow/run", projectId)
                        .with(csrf()))
                .andExpect(status().isAccepted());

        verify(workflowService).compileAndRun(projectId);
    }
}
