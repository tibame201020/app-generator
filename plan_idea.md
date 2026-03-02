Based on the milestone description:
"deliver a robust plugin architecture + extension SDK baseline as ONE cohesive PR. Scope:
(1) design and implement a typed plugin lifecycle (discover/register/init/execute/teardown) with error isolation so one plugin failure cannot crash generation flow;
(2) introduce a minimal extension SDK (interfaces, context helpers, validation utilities) and migrate at least 2 existing built-in generation capabilities to use the plugin path as reference implementations;
(3) add contract tests and integration tests for plugin loading, hook ordering, and failure handling, including fixture-based examples for third-party plugin authors;
(4) provide developer documentation with a quickstart template, versioning/compatibility policy, and security guardrails for untrusted plugin inputs."

Here's an analysis of the current system:
The application uses `WorkflowExecutor` to run a graph of nodes, where each node corresponds to an `AgentType` (PM, SA, PG, QA). Execution uses `LlmAgentExecutionService`.
Currently, these "built-in generation capabilities" are hardcoded in `enums/AgentType.java` and their execution is hardcoded inside `LlmAgentExecutionService.java` which fetches templates from `AgentPromptTemplate.java`.

To introduce a plugin architecture + extension SDK:

1.  **Define Extension SDK Interfaces & Classes (in a new package `com.tibame.app_generator.plugin` or `sdk`)**:
    *   `Plugin`: Interface with lifecycle hooks: `initialize(PluginContext)`, `teardown()`, `getName()`, `getVersion()`.
    *   `GenerationCapability`: Interface for actual execution, with `execute(TaskContext)` returning `Map<String, Object>`.
    *   `PluginContext`: Helper for initialization (logging, etc.).
    *   `TaskContext`: Context for execution (task info, inputs, validation utilities).
    *   `ValidationUtils`: Utility class for plugin inputs validation.
    *   `PluginException`: Custom exception for error isolation.

2.  **Plugin Manager (Lifecycle & Error Isolation)**:
    *   `PluginManager`: Service to discover, register, init, execute, and teardown plugins.
    *   Error isolation: Using `try-catch` blocks around plugin calls to prevent one plugin from crashing others or the main flow.

3.  **Migrate 2 Existing Capabilities**:
    *   Currently, we have PM, SA, PG, QA. We can migrate PM (Product Manager) and SA (System Architect) to be plugins.
    *   Create `PmPlugin` and `SaPlugin` implementing `Plugin` and providing `GenerationCapability`.
    *   Update `WorkflowExecutor` / `AgentTaskService` to route execution through the `PluginManager` instead of just `LlmAgentExecutionService` for these specific types, or maybe refactor `AgentType` to be dynamic (e.g., represent capabilities as strings rather than Enums, or have plugins register for specific string keys). Wait, the milestone says "migrate at least 2 existing built-in generation capabilities to use the plugin path". Since `AgentType` is an enum, we might need to change it or allow `AgentType.PLUGIN` or just use the agent type string directly in the workflow node if possible.
    Actually, let's keep `AgentType` but map `AgentType.PM` and `AgentType.SA` to the plugin registry. Or just make `AgentType` handle standard ones and check plugins for others. If we migrate 2 existing ones, we can just replace their `LlmAgentExecutionService` logic with the plugin logic.

4.  **Tests**:
    *   Contract/Integration tests for `PluginManager`: test loading, hook ordering (init before execute, teardown), failure handling (plugin throws exception during init or execute shouldn't crash the manager).
    *   Fixture-based examples: Create a dummy `TestPlugin` in the test folder to act as a 3rd party plugin.

5.  **Documentation**:
    *   Create `docs/PLUGIN_SDK.md` containing quickstart, versioning policy, and security guardrails.

Let's refine how `LlmAgentExecutionService` fits in. We probably need to provide an LLM client or service to the plugins so they can still do their work. Or we inject `ChatLanguageModel` into the plugins.

Let's look at `AgentPromptTemplate.java` to see what PM and SA do.
