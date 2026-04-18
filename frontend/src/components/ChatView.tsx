import React, { useState, useRef, useEffect, useCallback } from 'react';
import type { Message } from '../types';
import MessageBubble from './MessageBubble';
import { SendHorizonal, Loader2, Trash2, Zap } from 'lucide-react';

const generateId = () => Math.random().toString(36).slice(2);

const ChatView: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      role: 'assistant',
      content:
        "👋 Hi! I'm your MCP-powered AI assistant. I have access to **file system**, **database**, **weather**, and **code analysis** tools. Ask me anything, or use the **Demos** tab to see pre-built scenarios in action.",
      timestamp: new Date(),
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [streaming, setStreaming] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const abortRef = useRef<AbortController | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = useCallback(
    async (text: string, useStream: boolean) => {
      if (!text.trim() || loading) return;
      const userMsg: Message = {
        id: generateId(),
        role: 'user',
        content: text.trim(),
        timestamp: new Date(),
      };
      setMessages((m) => [...m, userMsg]);
      setInput('');
      setLoading(true);

      if (useStream) {
        setStreaming(true);
        const assistantId = generateId();
        const assistantMsg: Message = {
          id: assistantId,
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          isStreaming: true,
        };
        setMessages((m) => [...m, assistantMsg]);

        abortRef.current = new AbortController();
        try {
          const res = await fetch('/chat/stream', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text.trim() }),
            signal: abortRef.current.signal,
          });
          const reader = res.body!.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            const chunk = decoder.decode(value, { stream: true });
            // SSE lines: "data: token\n\n"
            const lines = chunk.split('\n');
            for (const line of lines) {
              if (line.startsWith('data:')) {
                buffer += line.slice(5);
              }
            }
            setMessages((m) =>
              m.map((msg) =>
                msg.id === assistantId ? { ...msg, content: buffer } : msg
              )
            );
          }
          setMessages((m) =>
            m.map((msg) =>
              msg.id === assistantId ? { ...msg, isStreaming: false } : msg
            )
          );
        } catch (e: unknown) {
          if (e instanceof Error && e.name !== 'AbortError') {
            setMessages((m) =>
              m.map((msg) =>
                msg.id === assistantId
                  ? { ...msg, content: '⚠️ Stream error. Is the server running?', isStreaming: false }
                  : msg
              )
            );
          }
        } finally {
          setLoading(false);
          setStreaming(false);
        }
      } else {
        try {
          const res = await fetch('/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text.trim() }),
          });
          const data = await res.json();
          setMessages((m) => [
            ...m,
            {
              id: generateId(),
              role: 'assistant',
              content: data.response ?? data.error ?? 'No response',
              timestamp: new Date(),
            },
          ]);
        } catch {
          setMessages((m) => [
            ...m,
            {
              id: generateId(),
              role: 'assistant',
              content: '⚠️ Request failed. Is the MCP client running on port 8080?',
              timestamp: new Date(),
            },
          ]);
        } finally {
          setLoading(false);
        }
      }
    },
    [loading]
  );

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage(input, false);
    }
  };

  const clearChat = () => {
    if (streaming && abortRef.current) abortRef.current.abort();
    setMessages([
      {
        id: 'welcome',
        role: 'assistant',
        content:
          "👋 Hi! I'm your MCP-powered AI assistant. Ask me anything!",
        timestamp: new Date(),
      },
    ]);
    setLoading(false);
    setStreaming(false);
  };

  const QUICK_PROMPTS = [
    'What files are in the workspace?',
    'Who are the top 5 highest-paid employees?',
    "What's the weather in Tokyo?",
    'Explain the MCP protocol briefly.',
  ];

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-white">
        <div>
          <h2 className="font-semibold text-gray-800">Chat</h2>
          <p className="text-xs text-gray-500">Direct conversation with MCP-connected AI</p>
        </div>
        <button
          onClick={clearChat}
          className="text-gray-400 hover:text-red-500 transition-colors p-1 rounded"
          title="Clear chat"
        >
          <Trash2 size={16} />
        </button>
      </div>

      {/* Quick prompts */}
      <div className="px-4 py-2 flex gap-2 overflow-x-auto border-b border-gray-100 bg-gray-50">
        {QUICK_PROMPTS.map((p) => (
          <button
            key={p}
            onClick={() => sendMessage(p, false)}
            disabled={loading}
            className="flex-shrink-0 text-xs bg-white border border-gray-200 text-gray-600 rounded-full px-3 py-1 hover:border-indigo-400 hover:text-indigo-600 transition-colors disabled:opacity-50"
          >
            {p}
          </button>
        ))}
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4 bg-gray-50">
        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}
        {loading && !streaming && (
          <div className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-emerald-600 flex items-center justify-center text-white">
              <Loader2 size={14} className="animate-spin" />
            </div>
            <div className="bg-white border border-gray-200 rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm">
              <div className="flex gap-1 items-center">
                <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:0ms]" />
                <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:150ms]" />
                <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:300ms]" />
              </div>
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input area */}
      <div className="px-4 py-3 bg-white border-t border-gray-200">
        <div className="flex gap-2 items-end">
          <textarea
            ref={inputRef}
            rows={1}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask anything… (Enter to send, Shift+Enter for newline)"
            disabled={loading}
            className="flex-1 resize-none rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 disabled:opacity-50 max-h-32 overflow-y-auto"
            style={{ minHeight: '42px' }}
          />
          <button
            onClick={() => sendMessage(input, true)}
            disabled={loading || !input.trim()}
            title="Stream response"
            className="p-2.5 rounded-xl bg-emerald-500 text-white hover:bg-emerald-600 disabled:opacity-40 transition-colors"
          >
            <Zap size={16} />
          </button>
          <button
            onClick={() => sendMessage(input, false)}
            disabled={loading || !input.trim()}
            title="Send (full response)"
            className="p-2.5 rounded-xl bg-indigo-500 text-white hover:bg-indigo-600 disabled:opacity-40 transition-colors"
          >
            <SendHorizonal size={16} />
          </button>
        </div>
        <p className="text-xs text-gray-400 mt-1">
          <Zap size={10} className="inline mr-0.5" />Stream &nbsp;·&nbsp;
          <SendHorizonal size={10} className="inline mr-0.5" />Full response
        </p>
      </div>
    </div>
  );
};

export default ChatView;

