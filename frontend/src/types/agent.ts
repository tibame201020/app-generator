export const AgentRole = {
    USER: 'USER',
    PM: 'PM',
    UIUX: 'UIUX',
    SA: 'SA',
    PG: 'PG',
    SYSTEM: 'SYSTEM',
} as const;

export type AgentRole = typeof AgentRole[keyof typeof AgentRole];

export type AgentMessageEvent = {
    projectId: number;
    agentRole: AgentRole;
    action: string;
    message: string;
    timestamp: string; // ISO 8601 string from backend Instant
};
