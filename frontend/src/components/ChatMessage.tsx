import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { dracula } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { AgentRole } from '../types/agent';

interface ChatMessageProps {
  role: AgentRole;
  message: string;
  timestamp?: string;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ role, message, timestamp }) => {
  const isUser = role === AgentRole.USER;
  const chatClass = isUser ? 'chat chat-end' : 'chat chat-start';
  const bubbleClass = isUser ? 'chat-bubble chat-bubble-primary' : 'chat-bubble chat-bubble-secondary';

  // Role Badge Color Mapping
  const getRoleBadgeColor = (role: AgentRole) => {
    switch (role) {
      case AgentRole.PM: return 'badge-warning';
      case AgentRole.UIUX: return 'badge-accent';
      case AgentRole.SA: return 'badge-info';
      case AgentRole.PG: return 'badge-success';
      case AgentRole.SYSTEM: return 'badge-ghost';
      default: return 'badge-primary';
    }
  };

  return (
    <div className={chatClass}>
      <div className="chat-image avatar placeholder">
        <div className={`bg-neutral text-neutral-content rounded-full w-10`}>
          <span className="text-xs">{role.substring(0, 2)}</span>
        </div>
      </div>
      <div className="chat-header mb-1">
        <span className={`badge badge-sm mr-2 ${getRoleBadgeColor(role)}`}>{role}</span>
        <time className="text-xs opacity-50">{timestamp || new Date().toLocaleTimeString()}</time>
      </div>
      <div className={`${bubbleClass} max-w-[80%]`}>
        <ReactMarkdown
          components={{
            // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
            code({ node, inline, className, children, ...props }: any) {
              const match = /language-(\w+)/.exec(className || '');
              return !inline && match ? (
                <SyntaxHighlighter
                  style={dracula}
                  language={match[1]}
                  PreTag="div"
                  {...props}
                >
                  {String(children).replace(/\n$/, '')}
                </SyntaxHighlighter>
              ) : (
                <code className={className} {...props}>
                  {children}
                </code>
              );
            }
          }}
        >
          {message}
        </ReactMarkdown>
      </div>
    </div>
  );
};

export default ChatMessage;
