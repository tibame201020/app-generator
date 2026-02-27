package com.jules.factory.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.factory.common.event.AgentMessageEvent;
import com.jules.factory.config.WebSocketConfig;
import com.jules.factory.config.websocket.AgentWebSocketHandler;
import com.jules.factory.domain.enums.AgentRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAgentMessageBroadcast() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        };

        // Connect to WebSocket with projectId=1
        String url = "ws://localhost:" + port + "/ws/agents?projectId=1";
        WebSocketSession session = client.doHandshake(handler, url).get(5, TimeUnit.SECONDS);

        assertThat(session.isOpen()).isTrue();

        // Trigger an event
        AgentMessageEvent event = new AgentMessageEvent(1L, AgentRole.PM, "TEST", "Hello WebSocket");
        eventPublisher.publishEvent(event);

        // Verify message received
        String receivedPayload = messages.poll(5, TimeUnit.SECONDS);
        assertThat(receivedPayload).isNotNull();

        AgentMessageEvent receivedEvent = objectMapper.readValue(receivedPayload, AgentMessageEvent.class);
        assertThat(receivedEvent.projectId()).isEqualTo(1L);
        assertThat(receivedEvent.message()).isEqualTo("Hello WebSocket");

        session.close();
    }
}
