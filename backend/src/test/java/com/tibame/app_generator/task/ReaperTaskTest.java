package com.tibame.app_generator.task;

import com.tibame.app_generator.enums.ContainerStatus;
import com.tibame.app_generator.model.ContainerInstance;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.ContainerInstanceRepository;
import com.tibame.app_generator.service.DockerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaperTaskTest {

    @Mock
    private ContainerInstanceRepository containerInstanceRepository;

    @Mock
    private DockerService dockerService;

    @InjectMocks
    private ReaperTask reaperTask;

    @Test
    void checkIdleContainers_ShouldStopAndRemoveIdleContainer() {
        // Arrange
        UUID instanceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).build();

        ContainerInstance instance = ContainerInstance.builder()
                .id(instanceId)
                .project(project)
                .status(ContainerStatus.RUNNING)
                .lastAccessAt(ZonedDateTime.now().minusMinutes(20))
                .build();

        when(containerInstanceRepository.findByStatusAndLastAccessAtBefore(eq(ContainerStatus.RUNNING), any(ZonedDateTime.class)))
                .thenReturn(List.of(instance));

        // Act
        reaperTask.checkIdleContainers();

        // Assert
        verify(dockerService, times(1)).stopProjectContainer(projectId);
        verify(dockerService, times(1)).removeProjectContainer(projectId);
    }

    @Test
    void checkIdleContainers_ShouldDoNothingIfNoIdleContainers() {
        // Arrange
        when(containerInstanceRepository.findByStatusAndLastAccessAtBefore(eq(ContainerStatus.RUNNING), any(ZonedDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        reaperTask.checkIdleContainers();

        // Assert
        verify(dockerService, times(0)).stopProjectContainer(any());
        verify(dockerService, times(0)).removeProjectContainer(any());
    }
}
