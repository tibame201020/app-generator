package com.tibame.app_generator.plugin;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Context provided to a capability during execution.
 */
public interface TaskContext {
    /**
     * Gets the ID of the current task.
     */
    UUID getTaskId();

    /**
     * Gets the input data for the task.
     */
    Map<String, Object> getInputs();

    /**
     * Gets the LLM client for this task.
     */
    LlmClient getLlmClient();

    /**
     * Updates the progress of the task.
     * @param progress A number between 0 and 100.
     * @param message A description of the current progress.
     */
    void updateProgress(int progress, String message);
}
