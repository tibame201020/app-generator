package com.jules.factory.core.statemachine;

import com.jules.factory.domain.enums.ProjectState;

/**
 * StateHandler
 * <p>
 * Strategy interface for handling logic specific to a project state.
 * Implementations of this interface are responsible for executing the
 * business logic (e.g., PM, SA, PG tasks) associated with a particular state.
 * </p>
 */
public interface StateHandler {

    /**
     * Checks if this handler supports the given project state.
     *
     * @param state The current state of the project.
     * @return true if this handler can process the state, false otherwise.
     */
    boolean supports(ProjectState state);

    /**
     * Handles the event processing for the given context.
     *
     * @param context The context containing the project ID and current messages.
     */
    void handle(StateContext context);
}
