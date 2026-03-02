package com.tibame.app_generator.plugin.builtin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.plugin.GenerationCapability;
import com.tibame.app_generator.plugin.PluginException;
import com.tibame.app_generator.plugin.TaskContext;
import com.tibame.app_generator.plugin.ValidationUtils;

import java.util.Map;

public class PmCapability implements GenerationCapability {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getCapabilityName() {
        return "PM";
    }

    @Override
    public Map<String, Object> execute(TaskContext context) throws PluginException {
        context.updateProgress(10, "PM Plugin: Validating inputs...");

        Map<String, Object> inputs = context.getInputs();
        // Since input might be nested or direct, we'll try to extract the description
        // In the original system, the inputContext JSON was dumped into the prompt.
        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(inputs);
        } catch (Exception e) {
            throw new PluginException("Failed to serialize inputs", e);
        }

        context.updateProgress(20, "PM Plugin: Generating requirements with AI...");

        String prompt = "You are an experienced Project Manager.\n" +
                "Analyze the following project description and generate detailed requirements.\n\n" +
                "Project Description:\n" +
                inputJson + "\n\n" +
                "Output must be in strict JSON format with the following structure:\n" +
                "{\n" +
                "  \"summary\": \"A brief summary of the requirements\",\n" +
                "  \"requirements\": [\n" +
                "    { \"id\": \"REQ-001\", \"title\": \"...\", \"description\": \"...\" }\n" +
                "  ],\n" +
                "  \"userStories\": [\n" +
                "    { \"id\": \"US-001\", \"role\": \"...\", \"action\": \"...\", \"benefit\": \"...\" }\n" +
                "  ]\n" +
                "}";

        try {
            String response = context.getLlmClient().generate(prompt);
            context.updateProgress(80, "PM Plugin: Parsing generated requirements...");

            String cleanJson = cleanJson(response);
            return objectMapper.readValue(cleanJson, Map.class);
        } catch (Exception e) {
            throw new PluginException("Failed to generate or parse requirements", e);
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
