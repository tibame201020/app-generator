package com.jules.factory.core.statemachine;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SAStateHandler implements StateHandler {

    private static final Logger logger = LoggerFactory.getLogger(SAStateHandler.class);

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public SAStateHandler(ProjectRepository projectRepository,
                          ConversationRepository conversationRepository,
                          SnowflakeIdGenerator snowflakeIdGenerator) {
        this.projectRepository = projectRepository;
        this.conversationRepository = conversationRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
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
                String dummyJson = """
                        {
                          "proposed_architecture": {
                            "backend": "Spring Boot 3",
                            "frontend": "React + TypeScript",
                            "database": "PostgreSQL",
                            "message_queue": "RabbitMQ",
                            "ai_integration": "Spring AI"
                          }
                        }
                        """;
                saveConversation(project, dummyJson);
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
