package com.tibame.app_generator.plugin;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class PluginManager {

    private final List<Plugin> loadedPlugins = new ArrayList<>();
    private final Map<String, GenerationCapability> capabilitiesRegistry = new HashMap<>();

    @PostConstruct
    public void loadPlugins() {
        log.info("Starting Plugin Manager and discovering plugins...");

        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class);

        // Simple PluginContext implementation for initialization
        PluginContext pluginContext = new PluginContext() {
            @Override
            public Map<String, String> getConfig() {
                // Return an unmodifiable empty map for now or inject application properties
                return Collections.emptyMap();
            }

            @Override
            public void logInfo(String message) {
                log.info("[Plugin] {}", message);
            }

            @Override
            public void logError(String message, Throwable t) {
                log.error("[Plugin] " + message, t);
            }
        };

        for (Plugin plugin : serviceLoader) {
            try {
                log.info("Discovered plugin: {} (version {})", plugin.getName(), plugin.getVersion());

                // Error isolation during initialization
                plugin.initialize(pluginContext);

                List<GenerationCapability> capabilities = plugin.getCapabilities();
                if (capabilities != null) {
                    for (GenerationCapability capability : capabilities) {
                        String name = capability.getCapabilityName();
                        if (capabilitiesRegistry.containsKey(name)) {
                            log.warn("Capability '{}' is already registered. Overwriting with capability from plugin '{}'",
                                     name, plugin.getName());
                        }
                        capabilitiesRegistry.put(name, capability);
                        log.info("Registered capability '{}' from plugin '{}'", name, plugin.getName());
                    }
                }

                loadedPlugins.add(plugin);
            } catch (Exception e) {
                // Isolate failure so it doesn't crash other plugins or the system
                log.error("Failed to initialize plugin: " + plugin.getClass().getName(), e);
            }
        }

        log.info("Plugin Manager started successfully. Loaded {} plugins with {} capabilities.",
                 loadedPlugins.size(), capabilitiesRegistry.size());
    }

    public boolean hasCapability(String name) {
        return capabilitiesRegistry.containsKey(name);
    }

    public Map<String, Object> executeCapability(String name, TaskContext context) throws PluginException {
        GenerationCapability capability = capabilitiesRegistry.get(name);
        if (capability == null) {
            throw new PluginException("No capability registered with name: " + name);
        }

        try {
            return capability.execute(context);
        } catch (Exception e) {
            // Error isolation during execution
            log.error("Execution of capability '{}' failed", name, e);
            throw new PluginException("Capability execution failed: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void teardownAll() {
        log.info("Tearing down all plugins...");
        for (Plugin plugin : loadedPlugins) {
            try {
                plugin.teardown();
                log.info("Successfully tore down plugin: {}", plugin.getName());
            } catch (Exception e) {
                log.error("Failed to tear down plugin: " + plugin.getName(), e);
            }
        }
        loadedPlugins.clear();
        capabilitiesRegistry.clear();
    }
}
