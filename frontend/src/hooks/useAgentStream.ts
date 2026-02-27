import { useEffect, useRef } from 'react';
import { useAgentContext } from '../context/AgentContext';
import type { AgentMessageEvent } from '../types/agent';

export const useAgentStream = (projectId: number) => {
  const { addMessage, setConnected } = useAgentContext();
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    if (!projectId) return;

    // Use a flag to track if the effect is active (mounted)
    let isMounted = true;

    const connect = () => {
      // Determine protocol and host
      // If we are on http://localhost:5173, protocol is http:, host is localhost:5173
      // We want ws://localhost:5173/ws/agents?projectId=...
      // Vite proxy will forward /ws to backend.

      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const host = window.location.host;
      const url = `${protocol}//${host}/ws/agents?projectId=${projectId}`;

      console.log(`Connecting to WebSocket: ${url}`);

      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        if (!isMounted) {
            ws.close();
            return;
        }
        console.log('WebSocket Connected');
        setConnected(true);
      };

      ws.onmessage = (event) => {
        if (!isMounted) return;
        try {
          const data: AgentMessageEvent = JSON.parse(event.data);
          // console.log('Received agent message:', data); // Verbose logging
          addMessage(data);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onclose = () => {
        if (!isMounted) return;
        console.log('WebSocket Disconnected');
        setConnected(false);

        // Simple reconnect logic with delay
        setTimeout(() => {
            if (isMounted) {
                console.log('Attempting to reconnect...');
                connect();
            }
        }, 3000);
      };

      ws.onerror = (error) => {
        if (!isMounted) return;
        console.error('WebSocket Error:', error);
        ws.close();
      };
    };

    connect();

    return () => {
      isMounted = false;
      if (wsRef.current) {
        // Prevent reconnect loop on unmount by clearing onclose handler
        wsRef.current.onclose = null;
        wsRef.current.close();
      }
    };
  }, [projectId]); // Remove addMessage and setConnected from dependency array to avoid re-renders if context changes (though context functions should be stable)
};
