import { createContext, useContext, useState, type ReactNode } from 'react';
import type { AgentMessageEvent } from '../types/agent';

interface AgentContextType {
  messages: AgentMessageEvent[];
  isConnected: boolean;
  addMessage: (msg: AgentMessageEvent) => void;
  setConnected: (status: boolean) => void;
}

const AgentContext = createContext<AgentContextType | undefined>(undefined);

interface AgentProviderProps {
  children: ReactNode;
}

export const AgentProvider: React.FC<AgentProviderProps> = ({ children }) => {
  const [messages, setMessages] = useState<AgentMessageEvent[]>([]);
  const [isConnected, setConnected] = useState(false);

  const addMessage = (msg: AgentMessageEvent) => {
    setMessages((prev) => [...prev, msg]);
  };

  return (
    <AgentContext.Provider value={{ messages, isConnected, addMessage, setConnected }}>
      {children}
    </AgentContext.Provider>
  );
};

export const useAgentContext = (): AgentContextType => {
  const context = useContext(AgentContext);
  if (!context) {
    throw new Error('useAgentContext must be used within an AgentProvider');
  }
  return context;
};
