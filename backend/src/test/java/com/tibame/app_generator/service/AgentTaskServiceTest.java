package com.tibame.app_generator.service;

import com.tibame.app_generator.dto.TaskEventDTO;
import com.tibame.app_generator.dto.TaskEventType;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentTaskServiceTest {

    @Mock
    private AgentTaskRepository agentTaskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AgentTaskService agentTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_ShouldSaveTaskAndPublishEvent() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().build(); // Use builder if available, or setter
        // Assuming Project has setId, otherwise use reflection or constructor if available.
        // Let's use reflection to set ID since it might be private.
        // Or simpler, just mock Project.getId() if it's mocked. But here Project is POJO.
        // Let's assume Project has setId from Lombok @Data or similar.
        project.setId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(agentTaskRepository.save(any(AgentTask.class))).thenAnswer(invocation -> {
            AgentTask task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        AgentTask createdTask = agentTaskService.createTask(projectId, AgentType.PM, "Test Task", Collections.emptyMap());

        assertNotNull(createdTask);
        assertEquals(TaskStatus.PENDING, createdTask.getStatus());
        verify(agentTaskRepository).save(any(AgentTask.class));

        ArgumentCaptor<TaskEventDTO> captor = ArgumentCaptor.forClass(TaskEventDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/project/" + projectId + "/tasks"), captor.capture());

        TaskEventDTO event = captor.getValue();
        assertEquals(TaskEventType.QUEUED, event.getType());
        assertEquals(createdTask.getId(), event.getTaskId());
    }

    @Test
    void startTask_ShouldUpdateStatusAndPublishEvent() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().build();
        project.setId(projectId);

        AgentTask task = AgentTask.builder()
                .id(taskId)
                .project(project)
                .status(TaskStatus.PENDING)
                .build();

        when(agentTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        agentTaskService.startTask(taskId);

        assertEquals(TaskStatus.RUNNING, task.getStatus());
        verify(agentTaskRepository).save(task);

        ArgumentCaptor<TaskEventDTO> captor = ArgumentCaptor.forClass(TaskEventDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/project/" + projectId + "/tasks"), captor.capture());
        assertEquals(TaskEventType.RUNNING, captor.getValue().getType());
    }
}
