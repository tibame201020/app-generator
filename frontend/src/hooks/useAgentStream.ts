import { useEffect, useRef, useState } from 'react';
import type { AgentMessageEvent } from '../types/agent';

type ConnectionStatus = 'CONNECTING' | 'OPEN' | 'CLOSED' | 'ERROR';

export const useAgentStream = (projectId: number | string) => {
    const [messages, setMessages] = useState<AgentMessageEvent[]>([]);
    const [lastMessage, setLastMessage] = useState<AgentMessageEvent | null>(null);
    const [status, setStatus] = useState<ConnectionStatus>('CONNECTING');
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        if (!projectId) return;

        // In development, Vite proxies /ws requests to backend.
        // We use window.location.host to construct the WebSocket URL relative to the current page.
        // This assumes the backend is proxied or served from the same origin.
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws/agents?projectId=${projectId}`;

        console.log(`Connecting to WebSocket: ${wsUrl}`);

        let socket: WebSocket;
        try {
            socket = new WebSocket(wsUrl);
            wsRef.current = socket;
        } catch (e) {
            console.error("Failed to create WebSocket:", e);
            setStatus('ERROR');
            return;
        }

        socket.onopen = () => {
            console.log('WebSocket Connected');
            setStatus('OPEN');
        };

        socket.onmessage = (event) => {
            try {
                const data: AgentMessageEvent = JSON.parse(event.data);
                console.log('Received message:', data);
                setLastMessage(data);
                setMessages((prev) => [...prev, data]);
            } catch (error) {
                console.error('Failed to parse WebSocket message:', error);
            }
        };

        socket.onclose = (event) => {
            console.log('WebSocket Disconnected', event.code, event.reason);
            setStatus('CLOSED');
        };

        socket.onerror = (error) => {
            console.error('WebSocket Error:', error);
            setStatus('ERROR');
        };

        return () => {
            if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
                socket.close();
            }
        };
    }, [projectId]);

    return {
        messages,
        lastMessage,
        status,
    };
};
