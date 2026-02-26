package com.jules.factory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArchitectureProposal(
    @JsonProperty("backend") String backend,
    @JsonProperty("frontend") String frontend,
    @JsonProperty("database") String database,
    @JsonProperty("message_queue") String messageQueue,
    @JsonProperty("ai_integration") String aiIntegration
) {
}
