import type { DemoScenario } from './types';

export const DEMO_SCENARIOS: DemoScenario[] = [
  {
    id: 'file-search',
    label: 'File System',
    icon: '📁',
    description: 'LLM explores the workspace, reads files, and writes a summary using FileSystemTool.',
    endpoint: '/demo/file-search',
    badge: 'Tools',
  },
  {
    id: 'db-query',
    label: 'Database Query',
    icon: '🗄️',
    description: 'Natural language → SQL: employees, products, orders via DatabaseQueryTool.',
    endpoint: '/demo/db-query',
    badge: 'Tools',
  },
  {
    id: 'weather',
    label: 'Weather',
    icon: '🌤️',
    description: 'Multi-city weather comparison and travel recommendation via WeatherTool.',
    endpoint: '/demo/weather',
    badge: 'Tools',
  },
  {
    id: 'code-review',
    label: 'Code Review',
    icon: '🔍',
    description: 'AI-powered Java source analysis and code quality review via CodeAnalysisTool.',
    endpoint: '/demo/code-review',
    badge: 'Tools',
  },
  {
    id: 'knowledge-qa',
    label: 'Knowledge Q&A',
    icon: '📚',
    description: 'RAG-lite answers about MCP & Spring AI from knowledge base resources.',
    endpoint: '/demo/knowledge-qa',
    badge: 'Resources',
  },
];

export const API_BASE = '';

