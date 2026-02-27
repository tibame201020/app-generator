package com.jules.factory.core.statemachine;

import com.jules.factory.common.event.AgentMessageEvent;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of StateMachineEngine.
 * <p>
 * This engine uses the Strategy Pattern to delegate processing to specific
 * {@link StateHandler} implementations based on the current {@link com.jules.factory.domain.enums.ProjectState}.
 * </p>
 */
@Service
public class StateMachineEngineImpl implements StateMachineEngine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineEngineImpl.class);

    private final List<StateHandler> stateHandlers;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StateMachineEngineImpl(List<StateHandler> stateHandlers, ProjectRepository projectRepository, ApplicationEventPublisher eventPublisher) {
        this.stateHandlers = stateHandlers;
        this.projectRepository = projectRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void processEvent(StateContext context) {
        Long projectId = context.projectId();
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            logger.error("Project not found for ID: {}", projectId);
            return; // Or throw exception
        }

        Project project = projectOpt.get();
        StateHandler handler = stateHandlers.stream()
                .filter(h -> h.supports(project.getStatus()))
                .findFirst()
                .orElse(null);

        if (handler == null) {
            logger.warn("No handler found for state: {} in project: {}", project.getStatus(), projectId);
            return;
        }

        // Guard Check: Block if waiting for user input
        AgentRole responsibleRole = handler.getResponsibleRole();
        List<Conversation> messages = context.currentMessages();

        if (messages != null && !messages.isEmpty()) {
            Conversation lastMsg = messages.get(messages.size() - 1);
            if (responsibleRole.name().equals(lastMsg.getSenderRole())) {
                logger.info("Project {} is waiting for user input (Last sender: {}). Halting execution.", projectId, lastMsg.getSenderRole());
                return;
            }
        }

        logger.info("Processing event for Project: {} in State: {} using Handler: {}",
                projectId, project.getStatus(), handler.getClass().getSimpleName());

        // Notify that the engine is engaging a specific role
        eventPublisher.publishEvent(new AgentMessageEvent(
                projectId,
                handler.getResponsibleRole(),
                "ACTIVATED",
                "Agent " + handler.getResponsibleRole() + " is now handling the project in state " + project.getStatus()
        ));

        handler.handle(context);
    }
}
