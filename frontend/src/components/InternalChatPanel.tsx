import React, { useEffect, useRef } from 'react';
import { AgentRole, type AgentMessageEvent } from '../types/agent';

interface InternalChatPanelProps {
  messages: AgentMessageEvent[];
  isOpen: boolean;
  onClose: () => void;
}

const InternalChatPanel: React.FC<InternalChatPanelProps> = ({ messages, isOpen, onClose }) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-y-0 right-0 z-50 w-96 bg-neutral text-neutral-content shadow-xl flex flex-col transition-transform transform translate-x-0">
      {/* Header */}
      <div className="p-4 bg-neutral-focus border-b border-neutral-content/20 flex justify-between items-center">
        <h2 className="font-mono font-bold text-lg flex items-center gap-2">
          <span className="loading loading-spinner loading-xs"></span>
          Internal Logs
        </h2>
        <button onClick={onClose} className="btn btn-sm btn-ghost btn-circle">
          ✕
        </button>
      </div>

      {/* Terminal Output */}
      <div className="flex-1 overflow-y-auto p-4 font-mono text-xs space-y-2" ref={scrollRef}>
        {messages.length === 0 ? (
          <div className="text-neutral-content/50 italic">Waiting for system events...</div>
        ) : (
          messages.map((msg, idx) => (
            <div key={idx} className={`p-2 rounded ${msg.agentRole === AgentRole.SYSTEM ? 'bg-error/10 text-error-content' : 'hover:bg-neutral-focus/50'}`}>
              <div className="flex justify-between opacity-50 mb-1">
                <span className={`font-bold ${getRoleColorClass(msg.agentRole)}`}>[{msg.agentRole}]</span>
                <span>{msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : ''}</span>
              </div>
              <div className="whitespace-pre-wrap break-words">
                {msg.message}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Footer / Status */}
      <div className="p-2 bg-neutral-focus text-center text-xs opacity-50">
        Connected to Agent Bus
      </div>
    </div>
  );
};

const getRoleColorClass = (role: string) => {
    switch (role) {
        case AgentRole.PM: return 'text-warning';
        case AgentRole.UIUX: return 'text-accent';
        case AgentRole.SA: return 'text-info';
        case AgentRole.PG: return 'text-success';
        case AgentRole.SYSTEM: return 'text-error'; // Highlight system logs
        default: return 'text-base-content';
    }
};

export default InternalChatPanel;
