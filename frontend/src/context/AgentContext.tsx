import React, { createContext, useContext, useState } from 'react';
import { useAgentStream } from '../hooks/useAgentStream';
import type { AgentMessageEvent } from '../types/agent';

interface AgentContextType {
    messages: AgentMessageEvent[];
    lastMessage: AgentMessageEvent | null;
    status: string;
    projectId: number | string | null;
    setProjectId: (id: number | string) => void;
}

const AgentContext = createContext<AgentContextType | undefined>(undefined);

export const AgentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    // In a real app, this might come from URL params or global state.
    // For now, we default to 1 as per plan.
    const [projectId, setProjectId] = useState<number | string | null>(1);

    const { messages, lastMessage, status } = useAgentStream(projectId || '');

    return (
        <AgentContext.Provider value={{ messages, lastMessage, status: status as string, projectId, setProjectId }}>
            {children}
        </AgentContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAgentContext = () => {
    const context = useContext(AgentContext);
    if (!context) {
        throw new Error('useAgentContext must be used within an AgentProvider');
    }
    return context;
};
