package com.tibame.app_generator.plugin;

/**
 * Interface allowing plugins to access LLM capabilities securely.
 */
public interface LlmClient {
    /**
     * Generates a text response for the given prompt.
     * @param prompt The prompt to send to the LLM.
     * @return The generated text.
     */
    String generate(String prompt);
}
