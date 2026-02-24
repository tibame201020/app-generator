package com.jules.factory.common.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class SpringEventPublisherTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private TestEventListener testEventListener;

    @Test
    public void testPublishEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        SystemEvent event = new SystemEvent(this, "TEST_EVENT", data);

        eventPublisher.publish(event);

        Assertions.assertEquals(1, testEventListener.getReceivedEvents().size());
        SystemEvent receivedEvent = testEventListener.getReceivedEvents().get(0);
        Assertions.assertEquals("TEST_EVENT", receivedEvent.type());
        Assertions.assertEquals("value", receivedEvent.data().get("key"));
    }

    @TestConfiguration
    static class Config {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    static class TestEventListener {
        private final List<SystemEvent> receivedEvents = new ArrayList<>();

        @EventListener
        public void handleEvent(SystemEvent event) {
            receivedEvents.add(event);
        }

        public List<SystemEvent> getReceivedEvents() {
            return receivedEvents;
        }
    }
}
