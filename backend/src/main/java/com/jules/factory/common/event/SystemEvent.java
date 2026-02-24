package com.jules.factory.common.event;

import java.time.Instant;
import java.util.Map;

/**
 * Standard system event for internal messaging.
 *
 * @param source    The source of the event (e.g., the object that triggered it).
 * @param type      The type of the event (e.g., "USER_CREATED").
 * @param data      Key-value data payload.
 * @param timestamp The time when the event occurred.
 */
public record SystemEvent(Object source, String type, Map<String, Object> data, Instant timestamp) {
    public SystemEvent(Object source, String type, Map<String, Object> data) {
        this(source, type, data, Instant.now());
    }
}
