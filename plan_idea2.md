So PM and SA are simple string manipulations followed by LLM call.

To make the plugin architecture robust:

1. **Create the Plugin SDK Package (`com.tibame.app_generator.plugin`)**:
    - `Plugin`: interface
      ```java
      public interface Plugin {
          String getName();
          String getVersion();
          void initialize(PluginContext context) throws PluginException;
          void teardown() throws PluginException;
          List<GenerationCapability> getCapabilities();
      }
      ```
    - `GenerationCapability`: interface
      ```java
      public interface GenerationCapability {
          String getCapabilityName(); // e.g., "PM" or "SA"
          Map<String, Object> execute(TaskContext context) throws PluginException;
      }
      ```
    - `PluginContext`: class holding initialization things (like logger, configuration).
    - `TaskContext`: class holding task context, inputs, and maybe `ChatLanguageModel` or a wrapper for LLM access.
    - `ValidationUtils`: class to validate plugin inputs.
    - `PluginException`: custom runtime exception.
    - `ExtensionPoint`: interface or annotation (optional, but let's stick to just standard interfaces).

2. **Create the `PluginManager`**:
    - Scans for implementations of `Plugin` (using `ServiceLoader` or Spring's component scanning). Since it's a Spring Boot app, we can make `PluginManager` a Spring Bean that discovers `Plugin` beans or uses Java `ServiceLoader`. Let's use standard Spring Bean discovery if plugins are part of the codebase, but since we want a "plugin architecture", `ServiceLoader` is standard for Java extensions without tying to Spring. However, for a baseline, registering plugins explicitly or via Spring might be easier. Let's provide a programmatic registration mechanism or ServiceLoader. Let's use `ServiceLoader` to simulate real 3rd party plugins!
    - It maintains a registry of `GenerationCapability` by name.
    - `discoverAndRegister()`: Loads plugins, calls `initialize()`. Handles exceptions gracefully (error isolation).
    - `executeCapability(String name, TaskContext context)`: Finds capability by name, executes it, wrapped in try-catch.
    - `teardownAll()`: Calls teardown on all plugins.

3. **Migrate 2 Existing Built-in Capabilities**:
    - Create `com.tibame.app_generator.plugin.builtin.PmPlugin` and `SaPlugin` (or a single `BuiltinPlugin` containing both PM and SA capabilities).
    - Refactor `LlmAgentExecutionService` or `WorkflowExecutor` to first check if `PluginManager` has a capability matching the `AgentType`'s name. If yes, use the plugin. If no, use the legacy hardcoded logic (or we can delete PM/SA from `LlmAgentExecutionService`).
    - The LLM logic for PM and SA involves `chatLanguageModel.generate()`. We should inject a `ChatLanguageModel` wrapper into `TaskContext`, or inject the model itself.
      ```java
      public interface LlmClient {
          String generate(String prompt);
      }
      ```
      Provide an `LlmClient` in `TaskContext`.

4. **Integration with `WorkflowExecutor`**:
    - Inside `executeNode()`:
      ```java
      String agentTypeStr = (String) data.get("agentType");
      ```
      If `pluginManager.hasCapability(agentTypeStr)`, it delegates to it. Otherwise, it falls back to `llmAgentExecutionService.executeTask(task, context)`.
      Wait, `LlmAgentExecutionService` currently also updates task progress (`agentTaskService.updateProgress`). The plugin should be able to do this. So `TaskContext` should have `updateProgress` method.

      Wait, in `WorkflowExecutor.java`:
      ```java
      AgentType agentType;
      try {
          agentType = AgentType.valueOf(agentTypeStr);
      } catch (IllegalArgumentException e) {
          ...
      }
      ```
      This means `agentTypeStr` *must* be an enum value right now. If we add new plugins with custom names, `AgentType.valueOf` will throw an exception! So we need to modify `WorkflowExecutor` to not fail if it's not an `AgentType`, or change `AgentTask.agentType` to be a `String` rather than an `AgentType` enum, OR add an `AgentType.PLUGIN` enum value, and store the actual capability name in the node data (e.g., `capabilityName`).
      Wait, `AgentTask` model has `AgentType agentType`. Changing `AgentTask.agentType` from Enum to String in the database would be a bigger migration.
      Alternatively, we can just say "plugins can override existing agent types" (PM, SA, PG, QA). The milestone says "migrate at least 2 existing built-in generation capabilities". So we will migrate PM and SA to use the plugin path. We don't necessarily have to change the `AgentType` enum right now, we can just map `AgentType.PM` to the plugin named "PM".

5. **Tests**:
    - Add `PluginManagerTest` to test loading, hook ordering, error handling.
    - Add a test fixture `TestPlugin` to simulate failures during init or execution.

6. **Documentation**:
    - Create `docs/PLUGIN_SDK.md`.

Let's refine `TaskContext`:
```java
public class TaskContext {
    private final UUID taskId;
    private final Map<String, Object> inputs;
    private final LlmClient llmClient;
    private final Consumer<String> progressLogger; // for updateProgress

    // Constructor, getters...
}
```

Let's refine `ValidationUtils`:
```java
public class ValidationUtils {
    public static void requireInput(Map<String, Object> inputs, String key) {
        if (!inputs.containsKey(key) || inputs.get(key) == null) {
            throw new PluginException("Missing required input: " + key);
        }
    }
}
```

Wait, `AgentPromptTemplate` has templates for PM and SA. I can move those into the plugin.
