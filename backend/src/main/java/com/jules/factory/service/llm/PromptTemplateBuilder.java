package com.jules.factory.service.llm;

import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.enums.AgentRole;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PromptTemplateBuilder {

    public static final String PM_SYSTEM_PROMPT = """
            You are a Project Manager (PM) in a software factory.
            Your responsibility is Requirement Gathering.
            You need to clarify project goals and scope with the user.
            Ask questions to understand what the user wants to build.
            Once you have enough information, summarize the requirements.
            """;

    public static final String SA_SYSTEM_PROMPT = """
            You are a System Analyst (SA) in a software factory.
            Your responsibility is Architecture Design.
            Based on the requirements, you need to plan the technology stack, database schema, and API interfaces.
            Output your design in a structured format (e.g., JSON or clear sections).
            """;

    public SystemMessage buildSystemMessage(AgentRole role) {
        return switch (role) {
            case PM -> new SystemMessage(PM_SYSTEM_PROMPT);
            case SA -> new SystemMessage(SA_SYSTEM_PROMPT);
            default -> new SystemMessage("You are a helpful AI assistant.");
        };
    }

    public List<Message> buildConversationHistory(List<Conversation> history) {
        List<Message> messages = new ArrayList<>();
        if (history == null) {
            return messages;
        }

        for (Conversation conv : history) {
            String roleStr = conv.getSenderRole();
            String content = conv.getContentText();
            if (content == null) content = "";

            if (AgentRole.USER.name().equals(roleStr)) {
                messages.add(new UserMessage(content));
            } else {
                // Assuming all other roles (PM, SA, etc.) are treated as Assistant context
                // from the perspective of the current turn.
                messages.add(new AssistantMessage(content));
            }
        }
        return messages;
    }

    public List<Message> buildFullPrompt(AgentRole role, List<Conversation> history, String userMessage) {
        List<Message> fullPrompt = new ArrayList<>();

        // 1. System Prompt
        fullPrompt.add(buildSystemMessage(role));

        // 2. History
        fullPrompt.addAll(buildConversationHistory(history));

        // 3. New User Message (if provided)
        if (userMessage != null && !userMessage.isBlank()) {
            fullPrompt.add(new UserMessage(userMessage));
        }

        return fullPrompt;
    }
}
