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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SAStateHandlerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private SAStateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new SAStateHandler(projectRepository, conversationRepository, snowflakeIdGenerator);
    }

    @Test
    void supports_ShouldReturnTrueForArchitectureDesign() {
        assertTrue(handler.supports(ProjectState.ARCHITECTURE_DESIGN));
        assertFalse(handler.supports(ProjectState.REQUIREMENT_GATHERING));
    }

    @Test
    void handle_FirstEntryFromPM_ShouldProposeArchitecture() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.ARCHITECTURE_DESIGN);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(200L);

        List<Conversation> messages = new ArrayList<>();
        // Last message is from PM (transition message)
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Proceeding to Architecture Design", null));

        StateContext context = new StateContext(projectId, messages);

        handler.handle(context);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        Conversation saved = captor.getValue();

        assertEquals(AgentRole.SA.name(), saved.getSenderRole());
        assertTrue(saved.getContentText().contains("proposed_architecture"));
        assertTrue(saved.getContentText().contains("Spring Boot 3"));

        // State remains ARCHITECTURE_DESIGN
        assertEquals(ProjectState.ARCHITECTURE_DESIGN, project.getStatus());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void handle_WaitingForUser_ShouldDoNothing() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.ARCHITECTURE_DESIGN);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<Conversation> messages = new ArrayList<>();
        // PM message
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Proceeding...", null));
        // SA proposal
        messages.add(new Conversation(200L, project, AgentRole.SA.name(), "Proposed Architecture...", null));

        StateContext context = new StateContext(projectId, messages);

        handler.handle(context);

        verify(conversationRepository, never()).save(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void handle_UserApproves_ShouldTransitionToImplementation() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.ARCHITECTURE_DESIGN);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(201L);

        List<Conversation> messages = new ArrayList<>();
        // PM message
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Proceeding...", null));
        // SA proposal
        messages.add(new Conversation(200L, project, AgentRole.SA.name(), "Proposed Architecture...", null));
        // User approval
        messages.add(new Conversation(201L, project, AgentRole.USER.name(), "Looks good", null));

        StateContext context = new StateContext(projectId, messages);

        handler.handle(context);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        Conversation saved = captor.getValue();

        assertEquals(AgentRole.SA.name(), saved.getSenderRole());
        assertTrue(saved.getContentText().contains("Proceeding to Implementation"));

        // State changes to IMPLEMENTATION
        assertEquals(ProjectState.IMPLEMENTATION, project.getStatus());
        verify(projectRepository).save(project);
    }
}
