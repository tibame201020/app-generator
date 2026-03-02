package com.tibame.app_generator.plugin.builtin;

import com.tibame.app_generator.plugin.LlmClient;
import com.tibame.app_generator.plugin.TaskContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PmCapabilityTest {

    @Test
    public void testExecute_Success() {
        PmCapability pm = new PmCapability();

        TaskContext context = new TaskContext() {
            @Override public UUID getTaskId() { return UUID.randomUUID(); }
            @Override public Map<String, Object> getInputs() {
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("description", "A simple e-commerce app");
                return inputs;
            }
            @Override public LlmClient getLlmClient() {
                return prompt -> "```json\n" +
                        "{\n" +
                        "  \"summary\": \"Test summary\",\n" +
                        "  \"requirements\": []\n" +
                        "}\n" +
                        "```";
            }
            @Override public void updateProgress(int progress, String message) {}
        };

        Map<String, Object> result = pm.execute(context);

        assertTrue(result.containsKey("summary"));
        assertEquals("Test summary", result.get("summary"));
    }
}
