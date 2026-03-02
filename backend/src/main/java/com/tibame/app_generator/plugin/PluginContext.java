package com.tibame.app_generator.plugin;

import java.util.Map;

/**
 * Context provided to a plugin during initialization.
 */
public interface PluginContext {
    /**
     * Get global configuration properties available to plugins.
     */
    Map<String, String> getConfig();

    /**
     * Log an informational message.
     */
    void logInfo(String message);

    /**
     * Log an error message.
     */
    void logError(String message, Throwable t);
}
