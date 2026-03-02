package com.tibame.app_generator.plugin.builtin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.plugin.GenerationCapability;
import com.tibame.app_generator.plugin.PluginException;
import com.tibame.app_generator.plugin.TaskContext;

import java.util.Map;

public class SaCapability implements GenerationCapability {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getCapabilityName() {
        return "SA";
    }

    @Override
    public Map<String, Object> execute(TaskContext context) throws PluginException {
        context.updateProgress(10, "SA Plugin: Analyzing requirements...");

        Map<String, Object> inputs = context.getInputs();
        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(inputs);
        } catch (Exception e) {
            throw new PluginException("Failed to serialize inputs", e);
        }

        context.updateProgress(20, "SA Plugin: Designing system architecture with AI...");

        String prompt = "You are a skilled Software Architect.\n" +
                "Based on the following requirements, design the system architecture and API.\n\n" +
                "Requirements:\n" +
                inputJson + "\n\n" +
                "Output must be in strict JSON format with the following structure:\n" +
                "{\n" +
                "  \"summary\": \"Overview of the architecture\",\n" +
                "  \"databaseSchema\": \"Description or SQL of the schema\",\n" +
                "  \"apiEndpoints\": [\n" +
                "    { \"method\": \"GET\", \"path\": \"/api/...\", \"description\": \"...\" }\n" +
                "  ],\n" +
                "  \"componentDesign\": \"Description of components\"\n" +
                "}";

        try {
            String response = context.getLlmClient().generate(prompt);
            context.updateProgress(80, "SA Plugin: Parsing architecture design...");

            String cleanJson = cleanJson(response);
            return objectMapper.readValue(cleanJson, Map.class);
        } catch (Exception e) {
            throw new PluginException("Failed to generate or parse system architecture", e);
        }
    }

    private String cleanJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
}
