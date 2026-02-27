package com.jules.factory.core.statemachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import com.jules.factory.dto.ArchitectureProposal;
import com.jules.factory.service.llm.PromptTemplateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
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

    @Mock
    private ChatModel chatModel;

    @Mock
    private PromptTemplateBuilder promptTemplateBuilder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SAStateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new SAStateHandler(projectRepository, conversationRepository, snowflakeIdGenerator, chatModel, promptTemplateBuilder, objectMapper, eventPublisher);
    }

    @Test
    void supports_ShouldReturnTrueForArchitectureDesign() {
        assertTrue(handler.supports(ProjectState.ARCHITECTURE_DESIGN));
        assertFalse(handler.supports(ProjectState.REQUIREMENT_GATHERING));
    }

    @Test
    void handle_FirstEntryFromPM_ShouldCallLLM_AndSaveJson() throws JsonProcessingException {
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

        // Mock PromptTemplateBuilder
        when(promptTemplateBuilder.buildFullPrompt(any(), any(), any())).thenReturn(Collections.emptyList());

        // Mock ChatModel response (valid JSON)
        String jsonResponse = """
                {
                  "backend": "Spring Boot",
                  "frontend": "React",
                  "database": "H2",
                  "message_queue": "Kafka",
                  "ai_integration": "Spring AI"
                }
                """;
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(jsonResponse)));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        handler.handle(context);

        // Verify LLM called
        verify(chatModel).call(any(Prompt.class));

        // Verify response saved
        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        Conversation saved = captor.getValue();

        assertEquals(AgentRole.SA.name(), saved.getSenderRole());

        // Ensure it's valid JSON and matches expected structure
        ArchitectureProposal savedProposal = objectMapper.readValue(saved.getContentText(), ArchitectureProposal.class);
        assertEquals("Spring Boot", savedProposal.backend());
        assertEquals("React", savedProposal.frontend());

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
        verify(chatModel, never()).call(any(Prompt.class));
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
