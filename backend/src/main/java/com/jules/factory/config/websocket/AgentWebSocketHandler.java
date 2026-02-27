package com.jules.factory.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.factory.common.event.AgentMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for broadcasting Agent events to frontend clients.
 * Maintains a mapping of projectId -> Set<WebSocketSession>.
 */
@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentWebSocketHandler.class);

    // Map<ProjectId, Set<Session>>
    private final Map<Long, Set<WebSocketSession>> projectSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public AgentWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long projectId = (Long) session.getAttributes().get("projectId");
        if (projectId != null) {
            projectSessions.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(session);
            logger.info("WebSocket connected: Session {} for Project {}", session.getId(), projectId);
        } else {
            logger.warn("WebSocket connected without projectId in attributes. Session: {}", session.getId());
            try {
                session.close(CloseStatus.BAD_DATA);
            } catch (IOException e) {
                logger.error("Error closing invalid session", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long projectId = (Long) session.getAttributes().get("projectId");
        if (projectId != null) {
            Set<WebSocketSession> sessions = projectSessions.get(projectId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    projectSessions.remove(projectId);
                }
            }
            logger.info("WebSocket disconnected: Session {} for Project {}", session.getId(), projectId);
        }
    }

    /**
     * Listen for AgentMessageEvent and broadcast to relevant sessions.
     */
    @EventListener
    public void handleAgentMessageEvent(AgentMessageEvent event) {
        Long projectId = event.projectId();
        Set<WebSocketSession> sessions = projectSessions.get(projectId);

        if (sessions != null && !sessions.isEmpty()) {
            try {
                String payload = objectMapper.writeValueAsString(event);
                TextMessage message = new TextMessage(payload);

                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            synchronized (session) {
                                session.sendMessage(message);
                            }
                        } catch (IOException e) {
                            logger.error("Error sending WebSocket message to session {}", session.getId(), e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error serializing AgentMessageEvent", e);
            }
        }
    }
}
