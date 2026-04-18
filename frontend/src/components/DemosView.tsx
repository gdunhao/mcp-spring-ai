import React, { useState } from 'react';
import { DEMO_SCENARIOS } from '../constants';
import ReactMarkdown from 'react-markdown';
import { Play, Loader2, ChevronDown, ChevronRight } from 'lucide-react';

interface DemoResult {
  scenarioId: string;
  description: string;
  response: string;
  elapsed: number;
}

const DemosView: React.FC = () => {
  const [running, setRunning] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, DemoResult>>({});
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});

  const runDemo = async (id: string, endpoint: string) => {
    setRunning(id);
    const start = Date.now();
    try {
      const res = await fetch(endpoint);
      const data = await res.json();
      setResults((r) => ({
        ...r,
        [id]: {
          scenarioId: id,
          description: data.description ?? '',
          response: data.response ?? JSON.stringify(data, null, 2),
          elapsed: Date.now() - start,
        },
      }));
      setExpanded((e) => ({ ...e, [id]: true }));
    } catch {
      setResults((r) => ({
        ...r,
        [id]: {
          scenarioId: id,
          description: '',
          response: '⚠️ Request failed. Make sure the MCP client is running on port 8080.',
          elapsed: Date.now() - start,
        },
      }));
      setExpanded((e) => ({ ...e, [id]: true }));
    } finally {
      setRunning(null);
    }
  };

  const toggleExpand = (id: string) => {
    setExpanded((e) => ({ ...e, [id]: !e[id] }));
  };

  return (
    <div className="flex flex-col h-full overflow-y-auto">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-200 bg-white sticky top-0 z-10">
        <h2 className="font-semibold text-gray-800">Demo Scenarios</h2>
        <p className="text-xs text-gray-500">
          Pre-built prompts that showcase each MCP tool. Click ▶ Run to execute.
        </p>
      </div>

      <div className="p-4 space-y-4 bg-gray-50 min-h-full">
        {DEMO_SCENARIOS.map((scenario) => {
          const isRunning = running === scenario.id;
          const result = results[scenario.id];
          const isExpanded = expanded[scenario.id];

          return (
            <div
              key={scenario.id}
              className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden"
            >
              {/* Card header */}
              <div className="px-4 py-3 flex items-start justify-between gap-3">
                <div className="flex items-start gap-3">
                  <span className="text-2xl mt-0.5">{scenario.icon}</span>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-gray-800 text-sm">{scenario.label}</h3>
                      <span className="text-[10px] font-medium bg-indigo-50 text-indigo-600 border border-indigo-100 rounded-full px-2 py-0.5">
                        {scenario.badge}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 mt-0.5">{scenario.description}</p>
                    <p className="text-xs font-mono text-gray-400 mt-1">GET {scenario.endpoint}</p>
                  </div>
                </div>

                <button
                  onClick={() => runDemo(scenario.id, scenario.endpoint)}
                  disabled={isRunning || running !== null}
                  className="flex-shrink-0 flex items-center gap-1.5 bg-indigo-500 hover:bg-indigo-600 disabled:opacity-50 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition-colors"
                >
                  {isRunning ? (
                    <Loader2 size={12} className="animate-spin" />
                  ) : (
                    <Play size={12} />
                  )}
                  {isRunning ? 'Running…' : 'Run'}
                </button>
              </div>

              {/* Result */}
              {result && (
                <div className="border-t border-gray-100">
                  <button
                    onClick={() => toggleExpand(scenario.id)}
                    className="w-full flex items-center justify-between px-4 py-2 text-xs text-gray-500 hover:bg-gray-50 transition-colors"
                  >
                    <span>
                      ✅ Done in {(result.elapsed / 1000).toFixed(1)}s
                    </span>
                    {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                  </button>

                  {isExpanded && (
                    <div className="px-4 pb-4">
                      <div className="rounded-xl bg-gray-50 border border-gray-200 px-4 py-3 text-sm text-gray-700 max-h-96 overflow-y-auto">
                        <div className="prose prose-sm max-w-none prose-pre:bg-gray-100 prose-code:text-indigo-600 prose-code:bg-indigo-50 prose-code:px-1 prose-code:rounded">
                          <ReactMarkdown>{result.response}</ReactMarkdown>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default DemosView;

