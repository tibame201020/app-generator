package com.tibame.app_generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.model.WorkflowRun;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.WorkflowRunRepository;
import com.tibame.app_generator.service.JwtService;
import com.tibame.app_generator.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowRunController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for tests
public class WorkflowRunControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private WorkflowService workflowService;
    @MockBean private WorkflowRunRepository workflowRunRepository;
    @MockBean private AgentTaskRepository agentTaskRepository;
    @MockBean private JwtService jwtService;

    @Test
    public void testGetProjectRuns() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(workflowRunRepository.findByProjectIdOrderByCreatedAtDesc(projectId))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects/{projectId}/runs", projectId))
                .andExpect(status().isOk());
    }

    @Test
    public void testStartRun() throws Exception {
        UUID projectId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        run.setId(UUID.randomUUID());
        given(workflowService.startRun(projectId)).willReturn(run);

        mockMvc.perform(post("/api/projects/{projectId}/runs", projectId))
                .andExpect(status().isAccepted());
    }

    @Test
    public void testRetryRun() throws Exception {
        UUID runId = UUID.randomUUID();
        mockMvc.perform(post("/api/runs/{runId}/retry", runId))
                .andExpect(status().isAccepted());
    }

    @Test
    public void testRetryTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        mockMvc.perform(post("/api/tasks/{taskId}/retry", taskId))
                .andExpect(status().isAccepted());
    }
}
