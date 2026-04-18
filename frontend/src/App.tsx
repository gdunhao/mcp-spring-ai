import React, { useState } from "react";
import ChatView from "./components/ChatView";
import DemosView from "./components/DemosView";
import SystemPromptView from "./components/SystemPromptView";
import { MessageSquare, PlayCircle, SlidersHorizontal } from "lucide-react";
import type { ActiveView } from "./types";

const NAV_ITEMS = [
  { id: "chat" as ActiveView, label: "Chat", icon: <MessageSquare size={18} /> },
  { id: "demos" as ActiveView, label: "Demos", icon: <PlayCircle size={18} /> },
  { id: "system" as ActiveView, label: "System", icon: <SlidersHorizontal size={18} /> },
];

const App: React.FC = () => {
  const [activeView, setActiveView] = useState<ActiveView>("chat");

  return (
    <div className="flex flex-col h-screen bg-gray-100 font-sans antialiased">
      <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-500 to-emerald-500 flex items-center justify-center text-white font-bold text-sm">
            M
          </div>
          <div>
            <h1 className="font-bold text-gray-900 text-sm leading-none">MCP Spring AI</h1>
            <p className="text-xs text-gray-400 leading-none mt-0.5">Demo Explorer</p>
          </div>
        </div>
        <nav className="flex gap-1 bg-gray-100 p-1 rounded-xl">
          {NAV_ITEMS.map((item) => (
            <button
              key={item.id}
              onClick={() => setActiveView(item.id)}
              className={
                "flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all " +
                (activeView === item.id
                  ? "bg-white text-indigo-600 shadow-sm"
                  : "text-gray-500 hover:text-gray-700")
              }
            >
              {item.icon}
              <span className="hidden sm:inline">{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-xs text-gray-500 hidden sm:inline">:8080</span>
        </div>
      </header>
      <main className="flex-1 overflow-hidden">
        <div className="h-full max-w-3xl mx-auto bg-white shadow-sm border-x border-gray-200">
          {activeView === "chat" && <ChatView />}
          {activeView === "demos" && <DemosView />}
          {activeView === "system" && <SystemPromptView />}
        </div>
      </main>
    </div>
  );
};

export default App;
