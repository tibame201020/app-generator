package com.jules.factory.core.statemachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.factory.common.event.AgentMessageEvent;
import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import com.jules.factory.dto.ArchitectureProposal;
import com.jules.factory.service.llm.PromptTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SAStateHandler implements StateHandler {

    private static final Logger logger = LoggerFactory.getLogger(SAStateHandler.class);

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ChatModel chatModel;
    private final PromptTemplateBuilder promptTemplateBuilder;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public SAStateHandler(ProjectRepository projectRepository,
                          ConversationRepository conversationRepository,
                          SnowflakeIdGenerator snowflakeIdGenerator,
                          ChatModel chatModel,
                          PromptTemplateBuilder promptTemplateBuilder,
                          ObjectMapper objectMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.conversationRepository = conversationRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.chatModel = chatModel;
        this.promptTemplateBuilder = promptTemplateBuilder;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean supports(ProjectState state) {
        return state == ProjectState.ARCHITECTURE_DESIGN;
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
        boolean lastMessageIsSA = false;

        if (messages != null && !messages.isEmpty()) {
            Conversation lastMsg = messages.get(messages.size() - 1);
            if (AgentRole.USER.name().equals(lastMsg.getSenderRole())) {
                lastMessageIsUser = true;
            } else if (AgentRole.SA.name().equals(lastMsg.getSenderRole())) {
                lastMessageIsSA = true;
            }
        }

        if (lastMessageIsUser) {
            // User responded. SA acknowledges and moves to implementation.
            logger.info("User approved architecture. Transitioning to IMPLEMENTATION for project: {}", projectId);

            saveConversation(project, "Great! Proceeding to Implementation.");

            project.setStatus(ProjectState.IMPLEMENTATION);
            projectRepository.save(project);
        } else {
            // Not from user. Check if SA already spoke.
            if (lastMessageIsSA) {
                // SA already spoke, waiting for user.
                logger.debug("SA waiting for user input for project: {}", projectId);
            } else {
                // First time SA speaks (e.g. after PM transition)
                logger.info("SA proposing architecture for project: {}", projectId);

                // 1. Prepare Converter
                BeanOutputConverter<ArchitectureProposal> converter = new BeanOutputConverter<>(ArchitectureProposal.class);
                String formatInstructions = converter.getFormat();

                // 2. Build Prompt
                String saTask = "Review the requirements gathered by PM and propose a technical architecture. " +
                        "You MUST output the result in strict JSON format matching the schema below.\n" +
                        formatInstructions;

                List<Message> promptMessages = promptTemplateBuilder.buildFullPrompt(AgentRole.SA, messages, saTask);
                Prompt prompt = new Prompt(promptMessages);

                // 3. Call LLM
                try {
                    eventPublisher.publishEvent(new AgentMessageEvent(
                            projectId, AgentRole.SA, "THINKING", "Drafting system architecture proposal..."
                    ));

                    ChatResponse response = chatModel.call(prompt);
                    String content = response.getResult().getOutput().getContent();

                    eventPublisher.publishEvent(new AgentMessageEvent(
                            projectId, AgentRole.SA, "SPEAKING", "Architecture proposal ready."
                    ));

                    // 4. Parse (Validation)
                    ArchitectureProposal proposal = converter.convert(content);

                    // 5. Serialize back to JSON string for storage
                    String proposalJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(proposal);
                    saveConversation(project, proposalJson);

                } catch (Exception e) {
                    logger.error("Failed to generate architecture proposal for project: {}", projectId, e);
                    // Fallback or retry logic could be added here. For now, we might save the error or retry manually.
                    saveConversation(project, "Error generating architecture proposal. Please try again.");
                }
            }
        }
    }

    @Override
    public AgentRole getResponsibleRole() {
        return AgentRole.SA;
    }

    private void saveConversation(Project project, String text) {
        Conversation conversation = new Conversation(
                snowflakeIdGenerator.nextId(),
                project,
                AgentRole.SA.name(),
                text,
                null
        );
        conversationRepository.save(conversation);
    }
}
