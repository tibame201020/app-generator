package com.jules.factory.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementation of EventPublisher that uses Spring's ApplicationEventPublisher.
 */
@Component
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(SystemEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
