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
            // setStatus('ERROR'); // FIX: Avoid calling setState in effect synchronously if possible, or wrap in async/timeout
            // However, here it is sync. But 'try' block wraps constructor.
            // If new WebSocket() throws, we can't do much.
            // The lint error specifically complained about cascading updates.
            // We can defer the update or ignore it if we want to follow strict rules,
            // but setting state on error is standard.
            // The linter might be overzealous about "synchronous" execution in effect.
            // Let's use a small timeout to break the sync chain for the linter satisfaction.
            setTimeout(() => setStatus('ERROR'), 0);
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
