import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { SendHorizonal, Loader2, Info } from 'lucide-react';

const SYSTEM_EXAMPLES = [
  {
    label: 'SQL Expert',
    system: 'You are a SQL expert. Always respond with SQL queries and explain them.',
    message: 'Show me all employees earning more than 130000',
  },
  {
    label: 'Weather Reporter',
    system: 'You are a friendly weather reporter. Give vivid, enthusiastic weather updates.',
    message: 'What is the weather in Paris?',
  },
  {
    label: 'Code Reviewer',
    system: 'You are a senior Java engineer. Review code critically and focus on best practices.',
    message: 'Review the FileSystemTool in the MCP server',
  },
];

const SystemPromptView: React.FC = () => {
  const [system, setSystem] = useState('');
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [elapsed, setElapsed] = useState<number | null>(null);

  const send = async () => {
    if (!message.trim()) return;
    setLoading(true);
    setResponse('');
    setElapsed(null);
    const start = Date.now();
    try {
      const res = await fetch('/chat/system', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ system, message }),
      });
      const data = await res.json();
      setResponse(data.response ?? data.error ?? 'No response');
      setElapsed(Date.now() - start);
    } catch {
      setResponse('⚠️ Request failed. Is the MCP client running on port 8080?');
      setElapsed(Date.now() - start);
    } finally {
      setLoading(false);
    }
  };

  const applyExample = (ex: typeof SYSTEM_EXAMPLES[0]) => {
    setSystem(ex.system);
    setMessage(ex.message);
    setResponse('');
  };

  return (
    <div className="flex flex-col h-full overflow-y-auto">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-200 bg-white sticky top-0 z-10">
        <h2 className="font-semibold text-gray-800">Custom System Prompt</h2>
        <p className="text-xs text-gray-500">Override the AI's persona or behavior for specialized use cases.</p>
      </div>

      <div className="p-4 space-y-4 bg-gray-50 min-h-full">
        {/* Quick examples */}
        <div>
          <p className="text-xs font-medium text-gray-500 mb-2 uppercase tracking-wide">Quick examples</p>
          <div className="flex flex-wrap gap-2">
            {SYSTEM_EXAMPLES.map((ex) => (
              <button
                key={ex.label}
                onClick={() => applyExample(ex)}
                className="text-xs bg-white border border-gray-200 text-gray-600 rounded-full px-3 py-1 hover:border-indigo-400 hover:text-indigo-600 transition-colors"
              >
                {ex.label}
              </button>
            ))}
          </div>
        </div>

        {/* System prompt textarea */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            System instruction{' '}
            <span className="font-normal text-gray-400">(optional — leave blank for default)</span>
          </label>
          <textarea
            rows={4}
            value={system}
            onChange={(e) => setSystem(e.target.value)}
            placeholder="e.g. You are a helpful assistant that always responds in Spanish."
            className="w-full rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
          />
        </div>

        {/* Message */}
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">User message</label>
          <textarea
            rows={3}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Your question or request…"
            className="w-full rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
          />
        </div>

        <button
          onClick={send}
          disabled={loading || !message.trim()}
          className="flex items-center gap-2 bg-indigo-500 hover:bg-indigo-600 disabled:opacity-50 text-white text-sm font-medium px-4 py-2.5 rounded-xl transition-colors"
        >
          {loading ? <Loader2 size={14} className="animate-spin" /> : <SendHorizonal size={14} />}
          {loading ? 'Thinking…' : 'Send'}
        </button>

        {/* Info callout */}
        <div className="flex gap-2 bg-blue-50 border border-blue-100 rounded-xl p-3 text-xs text-blue-700">
          <Info size={14} className="flex-shrink-0 mt-0.5" />
          <span>
            This calls <code className="bg-blue-100 rounded px-1">POST /chat/system</code>. The system
            instruction overrides the default AI persona for this single request.
          </span>
        </div>

        {/* Response */}
        {response && (
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div className="px-4 py-2 border-b border-gray-100 flex items-center justify-between">
              <span className="text-xs font-medium text-gray-600">Response</span>
              {elapsed && (
                <span className="text-xs text-gray-400">{(elapsed / 1000).toFixed(1)}s</span>
              )}
            </div>
            <div className="px-4 py-4 text-sm text-gray-700 max-h-96 overflow-y-auto">
              <div className="prose prose-sm max-w-none prose-pre:bg-gray-100 prose-code:text-indigo-600 prose-code:bg-indigo-50 prose-code:px-1 prose-code:rounded">
                <ReactMarkdown>{response}</ReactMarkdown>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SystemPromptView;

