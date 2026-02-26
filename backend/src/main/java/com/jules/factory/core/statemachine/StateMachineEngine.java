package com.jules.factory.core.statemachine;

/**
 * StateMachineEngine
 * <p>
 * This interface defines the contract for the core state machine engine.
 * It is responsible for processing events and driving the state transitions
 * of the Jules Software Factory agents (PM, SA, PG, etc.).
 * </p>
 */
public interface StateMachineEngine {

    /**
     * Processes an event for a given state context.
     * <p>
     * <b>IMPORTANT:</b> Do NOT annotate this method or its implementation with @Transactional.
     * Long-running operations, especially those involving LLM calls (Spring AI), must not hold
     * database transactions open. Database persistence operations should be short, independent,
     * and executed within their own transactional boundaries.
     * </p>
     *
     * @param context The context containing the project ID and current messages.
     */
    void processEvent(StateContext context);
}
