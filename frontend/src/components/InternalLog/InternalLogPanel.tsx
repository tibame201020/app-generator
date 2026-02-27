import React, { useState, useRef, useEffect } from 'react';
import { type AgentMessageEvent, AgentRole } from '../../types/agent';

interface InternalLogPanelProps {
  messages: AgentMessageEvent[];
}

const InternalLogPanel: React.FC<InternalLogPanelProps> = ({ messages }) => {
  const [isOpen, setIsOpen] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isOpen]);

  const getRoleColor = (role: AgentRole) => {
    switch (role) {
      case AgentRole.PM: return 'text-warning';
      case AgentRole.UIUX: return 'text-accent';
      case AgentRole.SA: return 'text-info';
      case AgentRole.PG: return 'text-success';
      case AgentRole.SYSTEM: return 'text-error';
      default: return 'text-base-content';
    }
  };

  return (
    <div className={`fixed bottom-0 right-0 z-50 transition-all duration-300 ease-in-out ${isOpen ? 'w-96 h-96' : 'w-64 h-12'}`}>
      <div className="card bg-neutral text-neutral-content w-full h-full rounded-b-none rounded-tl-lg shadow-2xl border border-base-300">

        {/* Header / Toggle */}
        <div
          className="card-body p-3 cursor-pointer flex flex-row justify-between items-center bg-neutral-focus rounded-tl-lg hover:bg-neutral-800 transition-colors"
          onClick={() => setIsOpen(!isOpen)}
        >
          <div className="flex items-center gap-2">
            <h3 className="card-title text-sm font-mono">
              <span className="loading loading-ring loading-xs"></span>
              Internal Logs
            </h3>
            <span className="badge badge-sm badge-ghost">{messages.length}</span>
          </div>
          <button className="btn btn-ghost btn-xs">
            {isOpen ? '▼' : '▲'}
          </button>
        </div>

        {/* Log Content */}
        {isOpen && (
          <div className="overflow-y-auto p-3 font-mono text-xs space-y-2 h-full bg-black/30" ref={scrollRef}>
            {messages.length === 0 && (
                <div className="text-center opacity-50 py-4">No system logs yet...</div>
            )}
            {messages.map((msg, idx) => (
              <div key={idx} className="border-l-2 border-opacity-30 pl-2 hover:bg-white/5 p-1 rounded transition-colors" style={{borderColor: 'currentColor'}}>
                <div className="flex justify-between opacity-50 text-[10px] mb-1">
                   <span>{msg.timestamp}</span>
                   <span className="uppercase tracking-wider">{msg.action}</span>
                </div>
                <div>
                  <span className={`font-bold mr-2 ${getRoleColor(msg.agentRole)}`}>
                    [{msg.agentRole}]
                  </span>
                  <span className="break-words opacity-90">{msg.message}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default InternalLogPanel;
