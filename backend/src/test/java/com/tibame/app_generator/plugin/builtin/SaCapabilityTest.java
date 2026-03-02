package com.tibame.app_generator.plugin.builtin;

import com.tibame.app_generator.plugin.LlmClient;
import com.tibame.app_generator.plugin.TaskContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaCapabilityTest {

    @Test
    public void testExecute_Success() {
        SaCapability sa = new SaCapability();

        TaskContext context = new TaskContext() {
            @Override public UUID getTaskId() { return UUID.randomUUID(); }
            @Override public Map<String, Object> getInputs() {
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("requirements", "Some requirements");
                return inputs;
            }
            @Override public LlmClient getLlmClient() {
                return prompt -> "{\n" +
                        "  \"summary\": \"Architecture summary\",\n" +
                        "  \"databaseSchema\": \"Users table\"\n" +
                        "}";
            }
            @Override public void updateProgress(int progress, String message) {}
        };

        Map<String, Object> result = sa.execute(context);

        assertTrue(result.containsKey("summary"));
        assertEquals("Architecture summary", result.get("summary"));
    }
}
