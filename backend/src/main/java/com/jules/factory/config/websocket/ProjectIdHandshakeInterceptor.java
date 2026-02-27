package com.jules.factory.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Interceptor to extract projectId from WebSocket handshake query parameters.
 * e.g., ws://.../ws/agents?projectId=123
 */
public class ProjectIdHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ProjectIdHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // Extract query parameters
            String query = request.getURI().getQuery();
            if (query == null) {
                logger.warn("WebSocket handshake rejected: No query parameters found.");
                return false;
            }

            // Simple parsing for projectId
            // In a production environment, use a proper query parser or UriComponentsBuilder
            String projectIdStr = UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("projectId");

            if (projectIdStr != null) {
                try {
                    Long projectId = Long.parseLong(projectIdStr);
                    attributes.put("projectId", projectId);
                    return true;
                } catch (NumberFormatException e) {
                    logger.warn("WebSocket handshake rejected: Invalid projectId format: {}", projectIdStr);
                    return false;
                }
            } else {
                logger.warn("WebSocket handshake rejected: projectId parameter missing.");
                return false;
            }
        } catch (Exception e) {
            logger.error("WebSocket handshake error", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
