package com.jules.factory.config.websocket;

import com.jules.factory.common.event.AgentMessageEvent;
import com.jules.factory.domain.enums.AgentRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgentWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldReceiveBroadcastMessage() throws Exception {
        // Arrange
        Long projectId = 1001L;
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        StandardWebSocketClient client = new StandardWebSocketClient();

        String url = "ws://localhost:" + port + "/ws/agents?projectId=" + projectId;

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        }, url).get(5, TimeUnit.SECONDS);

        assertThat(session.isOpen()).isTrue();

        // Act
        // Simulate an event being published by the system
        AgentMessageEvent event = new AgentMessageEvent(projectId, AgentRole.PM, "THINKING", "Hello from Test");
        eventPublisher.publishEvent(event);

        // Assert
        String receivedPayload = messages.poll(5, TimeUnit.SECONDS);
        assertThat(receivedPayload).isNotNull();
        assertThat(receivedPayload).contains("Hello from Test");
        assertThat(receivedPayload).contains("THINKING");
        assertThat(receivedPayload).contains("PM");

        session.close();
    }
}
