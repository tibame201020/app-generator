package com.jules.factory.common.event;

/**
 * Interface for publishing system events.
 */
public interface EventPublisher {
    /**
     * Publishes a system event.
     *
     * @param event The event to publish.
     */
    void publish(SystemEvent event);
}
