package com.tibame.app_generator.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.tibame.app_generator.config.DockerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DockerServiceTest {

    @Mock
    private DockerClient dockerClient;

    @Mock
    private DockerProperties dockerProperties;

    @Mock
    private CreateContainerCmd createContainerCmd;

    @Mock
    private CreateContainerResponse createContainerResponse;

    @Mock
    private StartContainerCmd startContainerCmd;

    @Mock
    private StopContainerCmd stopContainerCmd;

    @Mock
    private RemoveContainerCmd removeContainerCmd;

    @Mock
    private InspectContainerCmd inspectContainerCmd;

    @Mock
    private InspectContainerResponse inspectContainerResponse;

    private DockerService dockerService;

    @BeforeEach
    void setUp() {
        dockerService = new DockerService(dockerClient, dockerProperties);
    }

    @Test
    void createContainer_ShouldCreateContainerWithCorrectConfig(@TempDir Path tempDir) {
        // Arrange
        String projectId = "test-project-id";
        String imageName = "test-image";
        String expectedContainerId = "container-123";

        // Mock properties
        when(dockerProperties.getWorkspacePath()).thenReturn(tempDir.toString());
        when(dockerProperties.getDomain()).thenReturn("test.com");
        when(dockerProperties.getMemoryLimit()).thenReturn(512 * 1024 * 1024L);
        when(dockerProperties.getCpuPeriod()).thenReturn(100000L);
        when(dockerProperties.getCpuQuota()).thenReturn(50000L);
        when(dockerProperties.getContainerPort()).thenReturn("8080");
        when(dockerProperties.getContainerUser()).thenReturn("1000:1000");

        when(dockerClient.createContainerCmd(imageName)).thenReturn(createContainerCmd);
        when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withHostConfig(any(HostConfig.class))).thenReturn(createContainerCmd);
        when(createContainerCmd.withLabels(anyMap())).thenReturn(createContainerCmd);
        when(createContainerCmd.withUser(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(expectedContainerId);

        // Act
        String containerId = dockerService.createContainer(projectId, imageName);

        // Assert
        assertEquals(expectedContainerId, containerId);

        // Verify configuration
        verify(createContainerCmd).withName(projectId);
        verify(createContainerCmd).withUser("1000:1000");

        ArgumentCaptor<HostConfig> hostConfigCaptor = ArgumentCaptor.forClass(HostConfig.class);
        verify(createContainerCmd).withHostConfig(hostConfigCaptor.capture());
        HostConfig capturedConfig = hostConfigCaptor.getValue();
        assertEquals(100000L, capturedConfig.getCpuPeriod());
        assertEquals(50000L, capturedConfig.getCpuQuota());
        assertEquals(512 * 1024 * 1024L, capturedConfig.getMemory());

        ArgumentCaptor<Map<String, String>> labelsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(createContainerCmd).withLabels(labelsCaptor.capture());
        Map<String, String> capturedLabels = labelsCaptor.getValue();
        assertEquals("true", capturedLabels.get("traefik.enable"));
        assertEquals("Host(`" + projectId + ".test.com`)", capturedLabels.get("traefik.http.routers." + projectId + ".rule"));
        assertEquals("8080", capturedLabels.get("traefik.http.services." + projectId + ".loadbalancer.server.port"));
    }

    @Test
    void createContainer_ShouldThrowException_WhenProjectIdIsInvalid() {
        String invalidProjectId = "test/../../etc";
        String imageName = "test-image";

        assertThrows(IllegalArgumentException.class, () -> {
            dockerService.createContainer(invalidProjectId, imageName);
        });
    }

    @Test
    void startContainer_ShouldStartContainer() {
        String containerId = "container-123";
        when(dockerClient.startContainerCmd(containerId)).thenReturn(startContainerCmd);

        dockerService.startContainer(containerId);

        verify(startContainerCmd).exec();
    }

    @Test
    void stopContainer_ShouldStopContainer() {
        String containerId = "container-123";
        when(dockerClient.stopContainerCmd(containerId)).thenReturn(stopContainerCmd);

        dockerService.stopContainer(containerId);

        verify(stopContainerCmd).exec();
    }

    @Test
    void removeContainer_ShouldRemoveContainer() {
        String containerId = "container-123";
        when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeContainerCmd);
        when(removeContainerCmd.withForce(anyBoolean())).thenReturn(removeContainerCmd);

        dockerService.removeContainer(containerId);

        verify(removeContainerCmd).withForce(true);
        verify(removeContainerCmd).exec();
    }
}
