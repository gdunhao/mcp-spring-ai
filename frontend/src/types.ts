export type Role = 'user' | 'assistant' | 'system';

export interface Message {
  id: string;
  role: Role;
  content: string;
  timestamp: Date;
  isStreaming?: boolean;
}

export interface DemoScenario {
  id: string;
  label: string;
  icon: string;
  description: string;
  endpoint: string;
  badge: string;
}

export type ActiveView = 'chat' | 'demos' | 'system';

