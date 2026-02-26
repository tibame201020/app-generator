package com.jules.factory.service.llm;

import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.enums.AgentRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateBuilderTest {

    private PromptTemplateBuilder promptTemplateBuilder;

    @BeforeEach
    void setUp() {
        promptTemplateBuilder = new PromptTemplateBuilder();
    }

    @Test
    void testBuildSystemMessageForPM() {
        SystemMessage systemMessage = promptTemplateBuilder.buildSystemMessage(AgentRole.PM);
        assertThat(systemMessage.getContent()).contains("You are a Project Manager");
        assertThat(systemMessage.getMessageType()).isEqualTo(MessageType.SYSTEM);
    }

    @Test
    void testBuildSystemMessageForSA() {
        SystemMessage systemMessage = promptTemplateBuilder.buildSystemMessage(AgentRole.SA);
        assertThat(systemMessage.getContent()).contains("You are a System Analyst");
        assertThat(systemMessage.getMessageType()).isEqualTo(MessageType.SYSTEM);
    }

    @Test
    void testBuildConversationHistory() {
        List<Conversation> history = new ArrayList<>();

        Conversation userConv = new Conversation();
        userConv.setSenderRole(AgentRole.USER.name());
        userConv.setContentText("I want a website.");
        history.add(userConv);

        Conversation pmConv = new Conversation();
        pmConv.setSenderRole(AgentRole.PM.name());
        pmConv.setContentText("What kind of website?");
        history.add(pmConv);

        List<Message> messages = promptTemplateBuilder.buildConversationHistory(history);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(UserMessage.class);
        assertThat(messages.get(0).getContent()).isEqualTo("I want a website.");
        assertThat(messages.get(1)).isInstanceOf(AssistantMessage.class);
        assertThat(messages.get(1).getContent()).isEqualTo("What kind of website?");
    }

    @Test
    void testBuildFullPrompt() {
        List<Conversation> history = new ArrayList<>();
        Conversation userConv = new Conversation();
        userConv.setSenderRole(AgentRole.USER.name());
        userConv.setContentText("Hello");
        history.add(userConv);

        String newUserMessage = "I need help.";

        List<Message> fullPrompt = promptTemplateBuilder.buildFullPrompt(AgentRole.PM, history, newUserMessage);

        assertThat(fullPrompt).hasSize(3);
        assertThat(fullPrompt.get(0)).isInstanceOf(SystemMessage.class); // System
        assertThat(fullPrompt.get(1)).isInstanceOf(UserMessage.class);   // History
        assertThat(fullPrompt.get(2)).isInstanceOf(UserMessage.class);   // New Message

        assertThat(fullPrompt.get(0).getContent()).contains("Project Manager");
        assertThat(fullPrompt.get(2).getContent()).isEqualTo("I need help.");
    }

    @Test
    void testBuildFullPrompt_EmptyUserMessage() {
         List<Conversation> history = new ArrayList<>();
         List<Message> fullPrompt = promptTemplateBuilder.buildFullPrompt(AgentRole.PM, history, null);

         assertThat(fullPrompt).hasSize(1);
         assertThat(fullPrompt.get(0)).isInstanceOf(SystemMessage.class);
    }
}
