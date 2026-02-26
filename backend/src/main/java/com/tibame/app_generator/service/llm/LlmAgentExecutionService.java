package com.tibame.app_generator.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.service.AgentTaskService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmAgentExecutionService {

    private final ChatLanguageModel chatLanguageModel;
    private final AgentTaskService agentTaskService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> executeTask(AgentTask task, Map<String, Object> inputContext) {
        log.info("Executing LLM Task: {} (Type: {})", task.getTaskName(), task.getAgentType());

        try {
            agentTaskService.startTask(task.getId());

            // 1. Prepare Prompt
            String templateString = AgentPromptTemplate.getTemplate(task.getAgentType());

            // Convert inputContext to JSON string for the prompt
            String inputJson = objectMapper.writeValueAsString(inputContext);

            Map<String, Object> variables = new HashMap<>();
            variables.put("input", inputJson);

            String prompt = PromptTemplate.from(templateString).apply(variables).text();

            log.debug("Prompt for task {}: {}", task.getId(), prompt);

            // 2. Call LLM
            agentTaskService.updateProgress(task.getId(), 20, "Analyzing requirements with AI...");
            String response = chatLanguageModel.generate(prompt);

            agentTaskService.updateProgress(task.getId(), 80, "AI processing complete. Parsing results...");

            // 3. Parse Response (Handle Markdown code blocks)
            String cleanJson = cleanJson(response);
            Map<String, Object> result = objectMapper.readValue(cleanJson, Map.class);

            // 4. Update Task with Results
            // Specifically look for a summary
            String summary = (String) result.getOrDefault("summary", "Task completed successfully.");

            agentTaskService.updateContext(task.getId(), result);
            agentTaskService.completeTask(task.getId(), summary);

            return result;

        } catch (Exception e) {
            log.error("Task execution failed", e);
            agentTaskService.failTask(task.getId(), "AI Execution Failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String cleanJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) { // sometimes it's just ```
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
}
