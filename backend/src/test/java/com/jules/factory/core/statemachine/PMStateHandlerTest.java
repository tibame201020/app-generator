package com.jules.factory.core.statemachine;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import com.jules.factory.service.llm.PromptTemplateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.Collections;
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

    @Mock
    private ChatModel chatModel;

    @Mock
    private PromptTemplateBuilder promptTemplateBuilder;

    private PMStateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PMStateHandler(projectRepository, conversationRepository, snowflakeIdGenerator, chatModel, promptTemplateBuilder);
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

        // ChatModel should NOT be called for initial greeting
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void handle_UserReplied_ShouldCallLLM_AndStayInState() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(101L);

        List<Conversation> messages = new ArrayList<>();
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Hello", null));
        messages.add(new Conversation(100L, project, AgentRole.USER.name(), "I need a website", null));

        StateContext context = new StateContext(projectId, messages);

        // Mock PromptTemplateBuilder
        when(promptTemplateBuilder.buildFullPrompt(any(), any(), any())).thenReturn(Collections.emptyList());

        // Mock ChatModel response
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation("Can you elaborate?")));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        handler.handle(context);

        // Verify LLM called
        verify(chatModel).call(any(Prompt.class));

        // Verify response saved
        verify(conversationRepository).save(argThat(c ->
            c.getProject().getId().equals(projectId) &&
            c.getSenderRole().equals(AgentRole.PM.name()) &&
            c.getContentText().equals("Can you elaborate?")
        ));

        // State should NOT change (no token)
        verify(projectRepository, never()).save(project);
        assertEquals(ProjectState.REQUIREMENT_GATHERING, project.getStatus());
    }

    @Test
    void handle_UserReplied_WithCompletionToken_ShouldTransition() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(snowflakeIdGenerator.nextId()).thenReturn(101L);

        List<Conversation> messages = new ArrayList<>();
        messages.add(new Conversation(100L, project, AgentRole.PM.name(), "Hello", null));
        messages.add(new Conversation(100L, project, AgentRole.USER.name(), "Here is everything.", null));

        StateContext context = new StateContext(projectId, messages);

        // Mock PromptTemplateBuilder
        when(promptTemplateBuilder.buildFullPrompt(any(), any(), any())).thenReturn(Collections.emptyList());

        // Mock ChatModel response with TOKEN
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation("Okay. [REQUIREMENTS_GATHERED]")));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        handler.handle(context);

        // Verify LLM called
        verify(chatModel).call(any(Prompt.class));

        // Verify response saved (Cleaned)
        verify(conversationRepository).save(argThat(c ->
            c.getProject().getId().equals(projectId) &&
            c.getSenderRole().equals(AgentRole.PM.name()) &&
            c.getContentText().equals("Okay.")
        ));

        // State should change to ARCHITECTURE_DESIGN
        verify(projectRepository).save(project);
        assertEquals(ProjectState.ARCHITECTURE_DESIGN, project.getStatus());
    }
}
