package com.jules.factory.core.statemachine;

import com.jules.factory.domain.entity.Conversation;
import java.util.List;

/**
 * StateContext
 * <p>
 * This record encapsulates the payload passed between state machine nodes.
 * It carries the Project ID and the current list of Conversation messages.
 * </p>
 *
 * @param projectId       The ID of the project being processed.
 * @param currentMessages The list of conversation messages associated with the project.
 */
public record StateContext(Long projectId, List<Conversation> currentMessages) {
}
