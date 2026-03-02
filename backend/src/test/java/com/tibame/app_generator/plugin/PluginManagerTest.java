package com.tibame.app_generator.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PluginManagerTest {

    private PluginManager pluginManager;
    private List<String> executionLog;

    @BeforeEach
    public void setup() {
        pluginManager = new PluginManager();
        executionLog = new ArrayList<>();
    }

    private void injectPlugins(Plugin... plugins) throws Exception {
        Field loadedPluginsField = PluginManager.class.getDeclaredField("loadedPlugins");
        loadedPluginsField.setAccessible(true);
        List<Plugin> loadedPlugins = (List<Plugin>) loadedPluginsField.get(pluginManager);

        Field registryField = PluginManager.class.getDeclaredField("capabilitiesRegistry");
        registryField.setAccessible(true);
        Map<String, GenerationCapability> registry = (Map<String, GenerationCapability>) registryField.get(pluginManager);

        PluginContext context = new PluginContext() {
            @Override public Map<String, String> getConfig() { return Collections.emptyMap(); }
            @Override public void logInfo(String msg) { executionLog.add("INFO: " + msg); }
            @Override public void logError(String msg, Throwable t) { executionLog.add("ERROR: " + msg); }
        };

        for (Plugin p : plugins) {
            try {
                p.initialize(context);
                loadedPlugins.add(p);
                if (p.getCapabilities() != null) {
                    for (GenerationCapability c : p.getCapabilities()) {
                        registry.put(c.getCapabilityName(), c);
                    }
                }
            } catch (Exception e) {
                executionLog.add("INIT_FAILED: " + p.getName());
            }
        }
    }

    private TaskContext createTestContext() {
        return new TaskContext() {
            @Override public UUID getTaskId() { return UUID.randomUUID(); }
            @Override public Map<String, Object> getInputs() { return new HashMap<>(); }
            @Override public LlmClient getLlmClient() { return prompt -> "response"; }
            @Override public void updateProgress(int progress, String message) {}
        };
    }

    @Test
    public void testHookOrdering() throws Exception {
        Plugin testPlugin = new Plugin() {
            @Override public String getName() { return "TestPlugin"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public void initialize(PluginContext ctx) throws PluginException {
                executionLog.add("INIT_CALLED");
            }
            @Override public void teardown() throws PluginException {
                executionLog.add("TEARDOWN_CALLED");
            }
            @Override public List<GenerationCapability> getCapabilities() {
                return List.of(new GenerationCapability() {
                    @Override public String getCapabilityName() { return "TEST_CAP"; }
                    @Override public Map<String, Object> execute(TaskContext ctx) {
                        executionLog.add("EXECUTE_CALLED");
                        return Map.of("status", "success");
                    }
                });
            }
        };

        injectPlugins(testPlugin);
        assertTrue(pluginManager.hasCapability("TEST_CAP"));

        Map<String, Object> result = pluginManager.executeCapability("TEST_CAP", createTestContext());
        assertEquals("success", result.get("status"));

        pluginManager.teardownAll();

        List<String> expectedLog = List.of("INIT_CALLED", "EXECUTE_CALLED", "TEARDOWN_CALLED");
        assertEquals(expectedLog, executionLog);
    }

    @Test
    public void testErrorIsolation_InitFails() throws Exception {
        Plugin crashInitPlugin = new Plugin() {
            @Override public String getName() { return "CrashInit"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public void initialize(PluginContext ctx) throws PluginException {
                throw new RuntimeException("Init crashed!");
            }
            @Override public void teardown() {}
            @Override public List<GenerationCapability> getCapabilities() {
                return List.of(new GenerationCapability() {
                    @Override public String getCapabilityName() { return "CRASH_INIT_CAP"; }
                    @Override public Map<String, Object> execute(TaskContext ctx) { return Collections.emptyMap(); }
                });
            }
        };

        Plugin successPlugin = new Plugin() {
            @Override public String getName() { return "Success"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public void initialize(PluginContext ctx) { executionLog.add("SUCCESS_INIT"); }
            @Override public void teardown() {}
            @Override public List<GenerationCapability> getCapabilities() {
                return List.of(new GenerationCapability() {
                    @Override public String getCapabilityName() { return "SUCCESS_CAP"; }
                    @Override public Map<String, Object> execute(TaskContext ctx) { return Map.of("ok", true); }
                });
            }
        };

        injectPlugins(crashInitPlugin, successPlugin);

        // Ensure success plugin is available
        assertTrue(pluginManager.hasCapability("SUCCESS_CAP"));

        // Ensure crashed plugin capabilities are not registered
        assertFalse(pluginManager.hasCapability("CRASH_INIT_CAP"));

        assertTrue(executionLog.contains("INIT_FAILED: CrashInit"));
        assertTrue(executionLog.contains("SUCCESS_INIT"));
    }

    @Test
    public void testErrorIsolation_ExecuteFails() throws Exception {
        Plugin crashExecPlugin = new Plugin() {
            @Override public String getName() { return "CrashExec"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public void initialize(PluginContext ctx) {}
            @Override public void teardown() {}
            @Override public List<GenerationCapability> getCapabilities() {
                return List.of(new GenerationCapability() {
                    @Override public String getCapabilityName() { return "CRASH_EXEC"; }
                    @Override public Map<String, Object> execute(TaskContext ctx) {
                        throw new RuntimeException("Execute crashed!");
                    }
                });
            }
        };

        injectPlugins(crashExecPlugin);

        PluginException ex = assertThrows(PluginException.class, () -> {
            pluginManager.executeCapability("CRASH_EXEC", createTestContext());
        });

        assertTrue(ex.getMessage().contains("Execute crashed!"));
    }
}
