import React, { useState } from 'react';
import { useTaskStore, Task } from '../../stores/useTaskStore';
import { Play, CheckCircle, XCircle, Clock, Loader2, ChevronDown, ChevronRight, Terminal } from 'lucide-react';
import axios from 'axios';

interface TaskPanelProps {
  projectId: string;
}

export const TaskPanel: React.FC<TaskPanelProps> = ({ projectId }) => {
  const tasks = useTaskStore((state) => state.tasks);
  const connectionStatus = useTaskStore((state) => state.connectionStatus);
  const [expandedTaskId, setExpandedTaskId] = useState<string | null>(null);

  const handleSimulate = async () => {
    try {
      await axios.post(`/api/projects/${projectId}/tasks/simulate`);
    } catch (error) {
      console.error('Failed to start simulation', error);
    }
  };

  const sortedTasks = Object.values(tasks).sort((a, b) =>
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  return (
    <div className="flex flex-col h-full bg-gray-900 border-t border-gray-700">
      <div className="flex items-center justify-between px-4 py-2 bg-gray-800 border-b border-gray-700">
        <div className="flex items-center gap-2">
            <h3 className="text-sm font-semibold text-gray-200">Tasks</h3>
            <span className={`w-2 h-2 rounded-full ${
                connectionStatus === 'connected' ? 'bg-green-500' :
                connectionStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
            }`} title={`Status: ${connectionStatus}`}></span>
        </div>
        <button
            onClick={handleSimulate}
            className="flex items-center gap-1 px-2 py-1 text-xs bg-blue-600 hover:bg-blue-700 rounded text-white transition-colors"
        >
            <Play size={12} />
            Simulate Task
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-2 space-y-2">
        {sortedTasks.length === 0 && (
            <div className="text-center text-gray-500 text-sm py-4">No tasks found. Start one!</div>
        )}
        {sortedTasks.map((task) => (
          <TaskItem
            key={task.id}
            task={task}
            isExpanded={expandedTaskId === task.id}
            onToggle={() => setExpandedTaskId(expandedTaskId === task.id ? null : task.id)}
          />
        ))}
      </div>
    </div>
  );
};

const TaskItem: React.FC<{ task: Task; isExpanded: boolean; onToggle: () => void }> = ({ task, isExpanded, onToggle }) => {
    const getStatusIcon = () => {
        switch (task.status) {
            case 'PENDING': return <Clock size={16} className="text-gray-400" />;
            case 'RUNNING': return <Loader2 size={16} className="text-blue-400 animate-spin" />;
            case 'SUCCESS': return <CheckCircle size={16} className="text-green-400" />;
            case 'FAIL': return <XCircle size={16} className="text-red-400" />;
            default: return <Clock size={16} className="text-gray-400" />;
        }
    };

    return (
        <div className="bg-gray-800 rounded border border-gray-700 overflow-hidden">
            <div className="flex items-center gap-2 p-2 cursor-pointer hover:bg-gray-750 transition-colors" onClick={onToggle}>
                <button className="text-gray-400 hover:text-white">
                    {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                </button>
                <div className="min-w-[20px] flex justify-center">{getStatusIcon()}</div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                        <span className="text-sm font-medium text-gray-200 truncate">{task.taskName}</span>
                        <span className="text-xs text-gray-500 ml-2 whitespace-nowrap">{new Date(task.createdAt).toLocaleTimeString()}</span>
                    </div>
                    {task.status === 'RUNNING' && (
                        <div className="w-full bg-gray-700 h-1.5 mt-1.5 rounded-full overflow-hidden">
                            <div
                                className="bg-blue-500 h-full transition-all duration-300"
                                style={{ width: `${task.progressPct}%` }}
                            />
                        </div>
                    )}
                </div>
            </div>

            {isExpanded && (
                <div className="border-t border-gray-700 p-2 bg-gray-900 font-mono text-xs text-gray-300">
                    <div className="flex items-center gap-1 mb-1 text-gray-500">
                        <Terminal size={12} />
                        <span>Logs</span>
                    </div>
                    <pre className="whitespace-pre-wrap max-h-40 overflow-y-auto p-1 scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-transparent">
                        {task.logContent || 'No logs available.'}
                    </pre>
                </div>
            )}
        </div>
    );
};
