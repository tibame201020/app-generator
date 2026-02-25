package com.tibame.app_generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "platform.llm")
public class LlmProperties {
    /**
     * LLM Provider: openai, azure-openai, etc.
     */
    private String provider = "openai";

    /**
     * API Key for the LLM provider.
     */
    private String apiKey;

    /**
     * Model name to use (e.g., gpt-4o, gpt-3.5-turbo).
     */
    private String modelName = "gpt-4o";

    /**
     * Base URL for the LLM provider (optional).
     */
    private String baseUrl;

    /**
     * Temperature for the model (0.0 to 1.0).
     */
    private Double temperature = 0.7;

    /**
     * Timeout in seconds.
     */
    private Integer timeout = 60;

    /**
     * Max retries for failed requests.
     */
    private Integer maxRetries = 3;
}
