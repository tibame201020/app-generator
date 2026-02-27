package com.jules.factory.core.llm;

import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.enums.AgentRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptTemplateBuilderTest {

    private PromptTemplateBuilder promptTemplateBuilder;

    @BeforeEach
    void setUp() {
        promptTemplateBuilder = new PromptTemplateBuilder();
    }

    @Test
    void testSystemPrompt_NotEmpty() {
        assertFalse(PromptTemplateBuilder.PM_SYSTEM_PROMPT.isEmpty());
        assertFalse(PromptTemplateBuilder.SA_SYSTEM_PROMPT.isEmpty());
    }

    @Test
    void testBuildMessages_Success() {
        // Arrange
        String systemPrompt = "You are a test agent.";
        List<Conversation> history = new ArrayList<>();

        Conversation msg1 = new Conversation();
        msg1.setSenderRole(AgentRole.USER.name());
        msg1.setContentText("Hello, I need a website.");
        history.add(msg1);

        Conversation msg2 = new Conversation();
        msg2.setSenderRole(AgentRole.PM.name());
        msg2.setContentText("Sure, tell me more.");
        history.add(msg2);

        // Act
        List<Message> messages = promptTemplateBuilder.buildMessages(history, systemPrompt);

        // Assert
        assertEquals(3, messages.size());

        // Check System Message
        assertTrue(messages.get(0) instanceof SystemMessage);
        assertEquals(systemPrompt, messages.get(0).getContent());

        // Check User Message
        assertTrue(messages.get(1) instanceof UserMessage);
        assertEquals("Hello, I need a website.", messages.get(1).getContent());

        // Check Assistant Message
        assertTrue(messages.get(2) instanceof AssistantMessage);
        assertEquals("Sure, tell me more.", messages.get(2).getContent());
    }

    @Test
    void testBuildMessages_EmptyHistory() {
        // Arrange
        String systemPrompt = "System Prompt Only";
        List<Conversation> history = new ArrayList<>();

        // Act
        List<Message> messages = promptTemplateBuilder.buildMessages(history, systemPrompt);

        // Assert
        assertEquals(1, messages.size());
        assertTrue(messages.get(0) instanceof SystemMessage);
    }

    @Test
    void testBuildMessages_NullHistory() {
        // Arrange
        String systemPrompt = "System Prompt Only";

        // Act
        List<Message> messages = promptTemplateBuilder.buildMessages(null, systemPrompt);

        // Assert
        assertEquals(1, messages.size());
        assertTrue(messages.get(0) instanceof SystemMessage);
    }
}
