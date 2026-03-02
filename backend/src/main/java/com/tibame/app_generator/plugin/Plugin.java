package com.tibame.app_generator.plugin;

import java.util.List;

/**
 * Base interface for all App Generator extensions.
 */
public interface Plugin {
    /**
     * Gets the name of the plugin.
     */
    String getName();

    /**
     * Gets the version of the plugin.
     */
    String getVersion();

    /**
     * Initializes the plugin. Called once when the plugin is loaded.
     *
     * @param context Initialization context.
     * @throws PluginException if initialization fails.
     */
    void initialize(PluginContext context) throws PluginException;

    /**
     * Cleans up any resources used by the plugin. Called before application shutdown.
     *
     * @throws PluginException if teardown fails.
     */
    void teardown() throws PluginException;

    /**
     * Returns the list of generation capabilities provided by this plugin.
     */
    List<GenerationCapability> getCapabilities();
}
