package com.tibame.app_generator.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OpsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MetricsService metricsService;

    @Mock
    private DockerClient dockerClient;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private OpsController opsController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(opsController).build();
    }

    @Test
    public void testHealth_Up() throws Exception {
        // Mock DB
        when(projectRepository.count()).thenReturn(10L);

        // Mock Docker
        PingCmd pingCmd = mock(PingCmd.class);
        when(dockerClient.pingCmd()).thenReturn(pingCmd);
        // PingCmd.exec() is void, so use doNothing() or simple implicit void behavior
        doNothing().when(pingCmd).exec();

        mockMvc.perform(get("/api/ops/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.db").value("UP"))
                .andExpect(jsonPath("$.docker").value("UP"));
    }

    @Test
    public void testHealth_Down() throws Exception {
        // Mock DB failure
        when(projectRepository.count()).thenThrow(new RuntimeException("DB Error"));

        // Mock Docker success (just to isolate DB failure)
        PingCmd pingCmd = mock(PingCmd.class);
        when(dockerClient.pingCmd()).thenReturn(pingCmd);
        doNothing().when(pingCmd).exec();

        mockMvc.perform(get("/api/ops/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.db").value("DOWN"))
                .andExpect(jsonPath("$.docker").value("UP"));
    }

    @Test
    public void testMetrics() throws Exception {
        when(metricsService.getMetrics()).thenReturn(Map.of("import.total", 5));

        mockMvc.perform(get("/api/ops/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['import.total']").value(5));
    }
}
