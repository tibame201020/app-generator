package com.jules.factory.core.statemachine;

import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public StateMachineEngineImpl(List<StateHandler> stateHandlers, ProjectRepository projectRepository) {
        this.stateHandlers = stateHandlers;
        this.projectRepository = projectRepository;
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

        logger.info("Processing event for Project: {} in State: {} using Handler: {}",
                projectId, project.getStatus(), handler.getClass().getSimpleName());

        handler.handle(context);
    }
}
