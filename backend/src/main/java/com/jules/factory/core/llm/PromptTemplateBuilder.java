package com.jules.factory.core.llm;

import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.enums.AgentRole;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds Spring AI Prompt Messages from conversation history and system prompts.
 */
@Component
public class PromptTemplateBuilder {

    public static final String PM_SYSTEM_PROMPT = """
            You are an expert Project Manager (PM) in an autonomous software factory.
            Your goal is to gather clear, complete, and unambiguous requirements from the User.

            Responsibilities:
            1. Understand the User's initial idea.
            2. Ask clarifying questions to define the scope, features, and constraints.
            3. Identify potential edge cases or missing details.
            4. Once you have enough information, summarize the requirements and ask for confirmation.

            Behavior:
            - Be professional, concise, and helpful.
            - Do not start designing the architecture or writing code. That is for the SA and PG.
            - Focus on WHAT needs to be built, not HOW.
            - If the requirements are clear enough to proceed to technical design, explicitly state "Requirements gathered. Proceeding to Architecture Design."
            """;

    public static final String SA_SYSTEM_PROMPT = """
            You are an expert System Analyst (SA) in an autonomous software factory.
            Your goal is to design a robust, scalable, and modern technical architecture based on the requirements gathered by the PM.

            Responsibilities:
            1. Analyze the requirements provided by the PM and User.
            2. Propose a technology stack (Frontend, Backend, Database, Messaging, AI, etc.).
            3. Design the high-level system architecture.
            4. If required, output the architecture in a structured JSON format.

            Behavior:
            - Be technical, precise, and justify your choices.
            - Consider performance, security, and maintainability.
            - If you need to output a JSON structure, ensure it is valid JSON and strictly follows the requested schema.
            """;

    /**
     * Converts a list of Conversation entities into a list of Spring AI Messages,
     * prepended with the given System Prompt.
     *
     * @param history The conversation history.
     * @param systemPrompt The system prompt to set the context.
     * @return A list of Messages ready for the ChatClient.
     */
    public List<Message> buildMessages(List<Conversation> history, String systemPrompt) {
        List<Message> messages = new ArrayList<>();

        // 1. Add System Message
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(new SystemMessage(systemPrompt));
        }

        // 2. Add History
        if (history != null) {
            for (Conversation conv : history) {
                String role = conv.getSenderRole();
                String content = conv.getContentText();

                if (!StringUtils.hasText(content)) {
                    continue;
                }

                if (AgentRole.USER.name().equals(role)) {
                    messages.add(new UserMessage(content));
                } else {
                    // Treat all other agents (PM, SA, SYSTEM, etc.) as Assistant for the context of the LLM
                    messages.add(new AssistantMessage(content));
                }
            }
        }

        return messages;
    }
}
