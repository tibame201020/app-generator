package com.tibame.app_generator.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.service.AgentTaskService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmAgentExecutionServiceTest {

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private AgentTaskService agentTaskService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LlmAgentExecutionService service;

    private AgentTask task;
    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        task = AgentTask.builder()
                .id(UUID.randomUUID())
                .agentType(AgentType.PM)
                .taskName("Test Task")
                .contextData(new HashMap<>())
                .build();
        context = new HashMap<>();
        context.put("description", "A simple web app");
    }

    @Test
    void executeTask_Success() {
        // Mock LLM response
        String jsonResponse = """
                {
                    "summary": "Requirements analyzed.",
                    "requirements": []
                }
                """;
        when(chatLanguageModel.generate(anyString())).thenReturn(jsonResponse);

        // Execute
        Map<String, Object> result = service.executeTask(task, context);

        // Verify
        assertNotNull(result);
        assertEquals("Requirements analyzed.", result.get("summary"));

        verify(agentTaskService).startTask(task.getId());
        verify(agentTaskService).updateContext(eq(task.getId()), anyMap());
        verify(agentTaskService).completeTask(task.getId(), "Requirements analyzed.");
    }

    @Test
    void executeTask_Failure() {
        // Mock LLM failure
        when(chatLanguageModel.generate(anyString())).thenThrow(new RuntimeException("API Error"));

        // Execute
        assertThrows(RuntimeException.class, () -> service.executeTask(task, context));

        // Verify
        verify(agentTaskService).failTask(eq(task.getId()), contains("API Error"));
    }
}
