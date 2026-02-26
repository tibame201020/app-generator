package com.jules.factory.core.statemachine;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import com.jules.factory.service.llm.PromptTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PMStateHandler implements StateHandler {

    private static final Logger logger = LoggerFactory.getLogger(PMStateHandler.class);

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ChatModel chatModel;
    private final PromptTemplateBuilder promptTemplateBuilder;

    public PMStateHandler(ProjectRepository projectRepository,
                          ConversationRepository conversationRepository,
                          SnowflakeIdGenerator snowflakeIdGenerator,
                          ChatModel chatModel,
                          PromptTemplateBuilder promptTemplateBuilder) {
        this.projectRepository = projectRepository;
        this.conversationRepository = conversationRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.chatModel = chatModel;
        this.promptTemplateBuilder = promptTemplateBuilder;
    }

    @Override
    public boolean supports(ProjectState state) {
        return state == ProjectState.REQUIREMENT_GATHERING;
    }

    @Override
    public void handle(StateContext context) {
        Long projectId = context.projectId();
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            logger.error("Project not found: {}", projectId);
            return;
        }

        Project project = projectOpt.get();
        List<Conversation> messages = context.currentMessages();

        boolean lastMessageIsUser = false;
        if (messages != null && !messages.isEmpty()) {
            Conversation lastMsg = messages.get(messages.size() - 1);
            if (AgentRole.USER.name().equals(lastMsg.getSenderRole())) {
                lastMessageIsUser = true;
            }
        }

        if (!lastMessageIsUser) {
            // PM initiates or continues asking
            if (messages == null || messages.isEmpty()) {
                logger.info("PM initiating conversation for project: {}", projectId);
                saveConversation(project, "Hello! I am your Project Manager. Please describe your project requirements.");
            } else {
                // If the last message was NOT from user (e.g. from PM/SA/etc), do nothing or log
                // to prevent infinite loop of bot talking to bot if not careful.
                logger.debug("Waiting for user input for project: {}", projectId);
            }
        } else {
            // User responded. Call LLM.
            logger.info("User input received. Calling LLM for project: {}", projectId);

            // Construct Prompt
            List<Message> promptMessages = promptTemplateBuilder.buildFullPrompt(AgentRole.PM, messages, null);
            Prompt prompt = new Prompt(promptMessages);

            // Call LLM
            ChatResponse response = chatModel.call(prompt);
            String responseText = response.getResult().getOutput().getContent();

            // Check for completion token
            if (responseText.contains("[REQUIREMENTS_GATHERED]")) {
                String cleanResponse = responseText.replace("[REQUIREMENTS_GATHERED]", "").trim();
                saveConversation(project, cleanResponse);

                logger.info("Requirements gathered. Transitioning to ARCHITECTURE_DESIGN for project: {}", projectId);
                project.setStatus(ProjectState.ARCHITECTURE_DESIGN);
                projectRepository.save(project);
            } else {
                saveConversation(project, responseText);
            }
        }
    }

    @Override
    public AgentRole getResponsibleRole() {
        return AgentRole.PM;
    }

    private void saveConversation(Project project, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Conversation conversation = new Conversation(
                snowflakeIdGenerator.nextId(),
                project,
                AgentRole.PM.name(),
                text,
                null
        );
        conversationRepository.save(conversation);
    }
}
