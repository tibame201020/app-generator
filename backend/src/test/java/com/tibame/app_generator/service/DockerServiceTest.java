package com.tibame.app_generator.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.NetworkSettings;
import com.tibame.app_generator.config.DockerProperties;
import com.tibame.app_generator.model.ContainerInstance;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.ContainerInstanceRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DockerServiceTest {

    @Mock
    private DockerClient dockerClient;

    @Mock
    private DockerProperties dockerProperties;

    @Mock
    private ContainerInstanceRepository containerInstanceRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CreateContainerCmd createContainerCmd;

    @Mock
    private CreateContainerResponse createContainerResponse;

    @Mock
    private StartContainerCmd startContainerCmd;

    @Mock
    private StopContainerCmd stopContainerCmd;

    @Mock
    private InspectContainerCmd inspectContainerCmd;

    @Mock
    private InspectContainerResponse inspectContainerResponse;

    @Mock
    private ContainerState containerState;

    @Mock
    private NetworkSettings networkSettings;

    @InjectMocks
    private DockerService dockerService;

    @Test
    void startProjectContainer_ShouldStartContainer_WhenExistsButStopped() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(dockerProperties.getDefaultImage()).thenReturn("ubuntu:latest");

        // Mock inspect: exists, state not running
        when(dockerClient.inspectContainerCmd(anyString())).thenReturn(inspectContainerCmd);
        when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);

        when(inspectContainerResponse.getState()).thenReturn(containerState);
        when(containerState.getRunning()).thenReturn(false);

        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);

        // Mock IP retrieval
        when(inspectContainerResponse.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getNetworks()).thenReturn(Collections.emptyMap()); // Just to avoid NPE, mocked logic returns 127.0.0.1

        dockerService.startProjectContainer(projectId);

        verify(dockerClient).startContainerCmd(projectId.toString());
        verify(containerInstanceRepository).save(any(ContainerInstance.class));
    }

    @Test
    void stopProjectContainer_ShouldStopContainer() {
        UUID projectId = UUID.randomUUID();

        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);

        dockerService.stopProjectContainer(projectId);

        verify(dockerClient).stopContainerCmd(projectId.toString());
        verify(stopContainerCmd).exec();
        verify(containerInstanceRepository).save(any(ContainerInstance.class));
    }
}
