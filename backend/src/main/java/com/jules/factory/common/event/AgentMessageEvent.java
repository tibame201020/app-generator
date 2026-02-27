package com.jules.factory.common.event;

import com.jules.factory.domain.enums.AgentRole;

import java.time.Instant;

/**
 * Event representing a message or action from an Agent.
 * Used for broadcasting agent status and thoughts to the frontend.
 *
 * @param projectId The ID of the project.
 * @param agentRole The role of the agent (PM, SA, etc.).
 * @param action    The type of action (e.g., "THINKING", "SPEAKING", "STATE_CHANGE").
 * @param message   The content of the message.
 * @param timestamp The time the event occurred.
 */
public record AgentMessageEvent(
        Long projectId,
        AgentRole agentRole,
        String action,
        String message,
        Instant timestamp
) {
    public AgentMessageEvent(Long projectId, AgentRole agentRole, String action, String message) {
        this(projectId, agentRole, action, message, Instant.now());
    }
}
