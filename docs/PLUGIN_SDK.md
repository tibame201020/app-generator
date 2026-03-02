# Extension SDK & Plugin Architecture

The App Generator supports an extensible architecture allowing you to plug in custom AI generation capabilities seamlessly.

## Quickstart

To create a new capability plugin:

1. **Include the App Generator SDK in your project** (or use standard Java interfaces if integrated directly).
2. **Implement `GenerationCapability`** to define your feature:
   ```java
   public class MyCustomCapability implements GenerationCapability {
       @Override
       public String getCapabilityName() {
           return "MY_CAP";
       }

       @Override
       public Map<String, Object> execute(TaskContext context) throws PluginException {
           context.updateProgress(10, "Starting custom processing...");

           // Ensure required inputs exist
           ValidationUtils.requireInput(context.getInputs(), "myRequiredKey");

           String prompt = "Do something specific: " + context.getInputs().get("myRequiredKey");

           // Access the LLM safely via context
           String response = context.getLlmClient().generate(prompt);

           return Map.of("summary", "Done!", "result", response);
       }
   }
   ```
3. **Implement the `Plugin` interface** to register your capability:
   ```java
   public class MyPlugin implements Plugin {
       @Override
       public String getName() { return "My Custom Plugin"; }
       @Override
       public String getVersion() { return "1.0.0"; }
       @Override
       public void initialize(PluginContext context) {
           context.logInfo("Initializing My Plugin!");
       }
       @Override
       public void teardown() {}
       @Override
       public List<GenerationCapability> getCapabilities() {
           return List.of(new MyCustomCapability());
       }
   }
   ```
4. **Register the Plugin** using standard Java `ServiceLoader`. Create the file:
   `src/main/resources/META-INF/services/com.tibame.app_generator.plugin.Plugin`
   and add the fully qualified class name of your Plugin:
   ```
   com.mycompany.MyPlugin
   ```

## Versioning & Compatibility Policy

*   **SDK Interfaces** (`Plugin`, `TaskContext`, `GenerationCapability`, etc.) are currently considered `1.x.x` baseline.
*   **Minor Version Updates** will add methods with `default` implementations to ensure backward compatibility.
*   **Breaking Changes** will only occur in Major version bumps (e.g., `2.0.0`), giving developers advance notice.
*   Plugins should declare their compatible version in `getVersion()` to aid in future dependency resolution features.

## Security Guardrails

Since plugins often deal with untrusted generation inputs:

1.  **Do not use direct Network/API calls to LLMs** inside your plugin. Always use `TaskContext.getLlmClient()` to ensure API keys and rate limits are managed by the App Generator core securely.
2.  **Validate all inputs**. Use `ValidationUtils.requireInput` to ensure untrusted `inputs` maps contain the correct fields before attempting to process or format them into prompts. This guards against injection and null pointer exceptions.
3.  **Error Isolation**. The `PluginManager` wraps plugin initialization and execution in protective `try-catch` blocks. If your plugin throws an exception (e.g., a `PluginException`), it will fail the current step gracefully without crashing the main application flow.
