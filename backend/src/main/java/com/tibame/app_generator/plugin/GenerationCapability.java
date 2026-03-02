package com.tibame.app_generator.plugin;

import java.util.Map;

/**
 * Represents a specific generation capability provided by a plugin (e.g., "PM", "SA").
 */
public interface GenerationCapability {
    /**
     * Returns the unique name of this capability.
     */
    String getCapabilityName();

    /**
     * Executes the capability with the given context.
     *
     * @param context The task context including inputs and utilities.
     * @return A map representing the result of the execution.
     * @throws PluginException if an error occurs during execution.
     */
    Map<String, Object> execute(TaskContext context) throws PluginException;
}
