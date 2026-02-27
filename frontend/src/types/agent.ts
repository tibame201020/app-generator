// Using 'as const' to simulate enum behavior without runtime code that TS disallows in erasableSyntaxOnly
export const AgentRole = {
  USER: 'USER',
  PM: 'PM',
  UIUX: 'UIUX',
  SA: 'SA',
  PG: 'PG',
  SYSTEM: 'SYSTEM',
} as const;

export type AgentRole = typeof AgentRole[keyof typeof AgentRole];

export interface AgentMessageEvent {
  projectId: number;
  agentRole: AgentRole;
  action: string;
  message: string;
  timestamp: string; // Instant is serialized as ISO string usually
}
