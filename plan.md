1.  **Define Extension SDK Interfaces & Classes**
    - Package `com.tibame.app_generator.plugin`
    - Create `PluginException` (RuntimeException) for error isolation.
    - Create `PluginContext` (holds initialization context, perhaps a logger or generic map).
    - Create `TaskContext` (holds `AgentTask` ID, inputs, a progress updater lambda, and `LlmClient` interface).
    - Create `LlmClient` interface: `String generate(String prompt)`.
    - Create `ValidationUtils`: `static void requireInput(Map<String, Object> inputs, String key)`.
    - Create `GenerationCapability`: `String getCapabilityName()`, `Map<String, Object> execute(TaskContext context)`.
    - Create `Plugin` interface: `String getName()`, `String getVersion()`, `void initialize(PluginContext context)`, `void teardown()`, `List<GenerationCapability> getCapabilities()`.

2.  **Implement Plugin Manager**
    - `com.tibame.app_generator.plugin.PluginManager`
    - It uses `ServiceLoader<Plugin>` or explicit registration. Let's use standard `@Service` and `ServiceLoader` to load external plugins dynamically. We'll also register built-in plugins explicitly or via Spring DI if they are `@Component`. To keep it truly standard Java plugins, we can create a `META-INF/services/com.tibame.app_generator.plugin.Plugin` file, but standard Spring Boot handles plugins fine if we just `@Component` them. The prompt says "robust plugin architecture... third-party plugin authors", so `ServiceLoader` is standard. We will implement `PluginManager` which loads using `ServiceLoader` and stores registered capabilities.
    - Methods: `loadPlugins()`, `getCapability(String name)`, `teardown()`.
    - Ensure error isolation: if a plugin throws an exception during `initialize()`, catch it, log it, and do not register its capabilities. If it throws during `execute()`, catch and wrap in `PluginException` so it doesn't crash the JVM but properly fails the step in `WorkflowExecutor`.

3.  **Migrate PM and SA to Plugins**
    - Create `com.tibame.app_generator.plugin.builtin.PmPlugin` and `SaPlugin` (or `BuiltinPlugin` providing both).
    - Implement `GenerationCapability` for both. Move the prompt templates from `AgentPromptTemplate` to the plugin code.
    - They will use `ValidationUtils` to check inputs.
    - Register them so `PluginManager` discovers them.

4.  **Integrate with Application**
    - Update `LlmAgentExecutionService` or create a wrapper. Actually, `WorkflowExecutor` calls `llmAgentExecutionService.executeTask`. We can modify `WorkflowExecutor` to call `PluginManager` if a plugin capability matches the `AgentType` name. If not, it falls back to `llmAgentExecutionService.executeTask`.
    - For this, `WorkflowExecutor` needs an `LlmClient` implementation to pass into the `TaskContext`. We can adapt `ChatLanguageModel` into `LlmClient`.

5.  **Write Tests**
    - `PluginManagerTest`: Tests loading plugins, failure isolation during init/execute, hook ordering.
    - Contract test with a fixture plugin (a fake plugin throwing errors, returning success, etc.).

6.  **Add Documentation**
    - Create `docs/PLUGIN_SDK.md` outlining the quickstart, versioning policy, and security guardrails.
    - Add pre-commit check instructions as required by the system.
