package com.tibame.app_generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.enums.ContainerStatus;
import com.tibame.app_generator.service.DockerService;
import com.tibame.app_generator.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectRuntimeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private DockerService dockerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testRunProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        doNothing().when(dockerService).startProjectContainer(projectId);

        mockMvc.perform(post("/api/projects/{id}/run", projectId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project container started"));

        verify(dockerService, times(1)).startProjectContainer(projectId);
    }

    @Test
    @WithMockUser
    public void testStopProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        doNothing().when(dockerService).stopProjectContainer(projectId);

        mockMvc.perform(post("/api/projects/{id}/stop", projectId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project container stopped"));

        verify(dockerService, times(1)).stopProjectContainer(projectId);
    }

    @Test
    @WithMockUser
    public void testRestartProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        doNothing().when(dockerService).restartProjectContainer(projectId);

        mockMvc.perform(post("/api/projects/{id}/restart", projectId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project container restarted"));

        verify(dockerService, times(1)).restartProjectContainer(projectId);
    }

    @Test
    @WithMockUser
    public void testGetProjectStatus() throws Exception {
        UUID projectId = UUID.randomUUID();
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("status", ContainerStatus.RUNNING);
        statusMap.put("previewUrl", "/proxy/" + projectId + "/");
        statusMap.put("internalIp", "172.17.0.2");

        when(dockerService.getProjectContainerStatus(projectId)).thenReturn(statusMap);

        mockMvc.perform(get("/api/projects/{id}/status", projectId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.previewUrl").value("/proxy/" + projectId + "/"))
                .andExpect(jsonPath("$.internalIp").value("172.17.0.2"));

        verify(dockerService, times(1)).getProjectContainerStatus(projectId);
    }

    @Test
    @WithMockUser
    public void testRunProject_Error() throws Exception {
        UUID projectId = UUID.randomUUID();
        doThrow(new RuntimeException("Docker error")).when(dockerService).startProjectContainer(projectId);

        mockMvc.perform(post("/api/projects/{id}/run", projectId)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Docker error"));
    }
}
