package com.tibame.app_generator.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LlmConfig {

    private final LlmProperties llmProperties;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("Initializing ChatLanguageModel with provider: {}", llmProperties.getProvider());

        // Currently defaulting to OpenAI.
        // In the future, we can switch based on llmProperties.getProvider()
        // e.g. if ("azure".equalsIgnoreCase(llmProperties.getProvider())) ...

        String apiKey = llmProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No API key provided for LLM. Using 'demo' key which might not work.");
            apiKey = "demo";
        }

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(llmProperties.getModelName())
                .temperature(llmProperties.getTemperature())
                .timeout(Duration.ofSeconds(llmProperties.getTimeout()))
                .maxRetries(llmProperties.getMaxRetries());

        if (llmProperties.getBaseUrl() != null && !llmProperties.getBaseUrl().isBlank()) {
            builder.baseUrl(llmProperties.getBaseUrl());
        }

        return builder.build();
    }
}
