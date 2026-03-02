package com.tibame.app_generator.plugin;

import java.util.Map;

/**
 * Utility class for validating untrusted plugin inputs.
 */
public class ValidationUtils {

    /**
     * Ensures that the specified key exists and is not null in the inputs map.
     *
     * @param inputs The input map to check.
     * @param key The required key.
     * @throws PluginException if the key is missing or null.
     */
    public static void requireInput(Map<String, Object> inputs, String key) {
        if (inputs == null || !inputs.containsKey(key) || inputs.get(key) == null) {
            throw new PluginException("Missing required input: " + key);
        }
    }

    /**
     * Ensures that the specified key exists and is of the expected type.
     */
    public static <T> T requireInputOfType(Map<String, Object> inputs, String key, Class<T> type) {
        requireInput(inputs, key);
        Object value = inputs.get(key);
        if (!type.isInstance(value)) {
            throw new PluginException("Input '" + key + "' is not of expected type: " + type.getName());
        }
        return type.cast(value);
    }
}
