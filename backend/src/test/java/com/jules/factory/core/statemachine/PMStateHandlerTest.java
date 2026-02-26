package com.jules.factory.core.statemachine;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PMStateHandlerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private PMStateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PMStateHandler(projectRepository, conversationRepository, snowflakeIdGenerator);
    }

    @Test
    void supports_ShouldReturnTrueForRequirementGathering() {
        assertTrue(handler.supports(ProjectState.REQUIREMENT_GATHERING));
        assertFalse(handler.supports(ProjectState.ARCHITECTURE_DESIGN));
    }

    @Test
    void handle_InitialState_ShouldSendGreeting() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(100L);

        StateContext context = new StateContext(projectId, new ArrayList<>());

        handler.handle(context);

        verify(conversationRepository).save(argThat(c ->
            c.getProject().getId().equals(projectId) &&
            c.getSenderRole().equals(AgentRole.PM.name()) &&
            c.getContentText().contains("Hello")
        ));

        // State should remain REQUIREMENT_GATHERING
        assertEquals(ProjectState.REQUIREMENT_GATHERING, project.getStatus());
    }

    @Test
    void handle_UserReplied_ShouldTransitionState() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(101L);

        List<Conversation> messages = new ArrayList<>();
        // Previous PM message
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Hello", null));
        // User reply
        messages.add(new Conversation(101L, project, AgentRole.USER.name(), "Here are my requirements", null));

        StateContext context = new StateContext(projectId, messages);

        handler.handle(context);

        verify(conversationRepository).save(argThat(c ->
            c.getProject().getId().equals(projectId) &&
            c.getSenderRole().equals(AgentRole.PM.name()) &&
            c.getContentText().contains("Understood")
        ));

        // State should change to ARCHITECTURE_DESIGN
        verify(projectRepository).save(project);
        assertEquals(ProjectState.ARCHITECTURE_DESIGN, project.getStatus());
    }

    @Test
    void handle_WaitingForUser_ShouldDoNothing() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<Conversation> messages = new ArrayList<>();
        // Last message is PM
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Hello", null));

        StateContext context = new StateContext(projectId, messages);

        handler.handle(context);

        verify(conversationRepository, never()).save(any());
        verify(projectRepository, never()).save(any());
    }
}
