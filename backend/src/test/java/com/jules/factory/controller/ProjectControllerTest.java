package com.jules.factory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.dto.ChatRequest;
import com.jules.factory.dto.CreateProjectRequest;
import com.jules.factory.dto.ProjectResponse;
import com.jules.factory.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateProject() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest("Test Project", "Description");
        ProjectResponse response = new ProjectResponse(1L, "Test Project", "Description", ProjectState.REQUIREMENT_GATHERING, LocalDateTime.now());

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.status").value("REQUIREMENT_GATHERING"));
    }

    @Test
    public void testProcessChat() throws Exception {
        Long projectId = 1L;
        ChatRequest request = new ChatRequest("Hello, Jules!");

        mockMvc.perform(post("/api/projects/{id}/chat", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
