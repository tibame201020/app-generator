import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import { useTaskStore } from '../stores/useTaskStore';

export const useTaskWebSocket = (projectId: string) => {
  const { setConnectionStatus, handleEvent, fetchTasks } = useTaskStore();

  useEffect(() => {
    if (!projectId) return;

    // Fetch initial tasks
    fetchTasks(projectId);

    setConnectionStatus('connecting');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const brokerURL = `${protocol}//${window.location.host}/ws`;

    console.log(`Connecting to WebSocket at ${brokerURL}`);

    const client = new Client({
      brokerURL: brokerURL,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
        setConnectionStatus('connected');
        console.log('Connected to WebSocket');

        client.subscribe(`/topic/project/${projectId}/tasks`, (message) => {
          if (message.body) {
            try {
              const event = JSON.parse(message.body);
              console.log('Received event:', event);
              handleEvent(event);
            } catch (e) {
              console.error('Error parsing message:', e);
            }
          }
        });
    };

    client.onDisconnect = () => {
        setConnectionStatus('disconnected');
        console.log('Disconnected from WebSocket');
    };

    client.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };

    client.activate();

    return () => {
      client.deactivate();
      setConnectionStatus('disconnected');
    };
  }, [projectId, setConnectionStatus, handleEvent, fetchTasks]);
};
