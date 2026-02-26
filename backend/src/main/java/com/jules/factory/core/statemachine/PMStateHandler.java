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
public class PMStateHandler implements StateHandler {

    private static final Logger logger = LoggerFactory.getLogger(PMStateHandler.class);

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public PMStateHandler(ProjectRepository projectRepository,
                          ConversationRepository conversationRepository,
                          SnowflakeIdGenerator snowflakeIdGenerator) {
        this.projectRepository = projectRepository;
        this.conversationRepository = conversationRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
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
                // However, in this simple logic, we just wait for user input.
                logger.debug("Waiting for user input for project: {}", projectId);
            }
        } else {
            // User responded. PM acknowledges and moves to next state.
            logger.info("User input received. Transitioning to ARCHITECTURE_DESIGN for project: {}", projectId);

            saveConversation(project, "Understood. I have gathered your requirements. Proceeding to Architecture Design phase.");

            project.setStatus(ProjectState.ARCHITECTURE_DESIGN);
            projectRepository.save(project);
        }
    }

    @Override
    public AgentRole getResponsibleRole() {
        return AgentRole.PM;
    }

    private void saveConversation(Project project, String text) {
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
