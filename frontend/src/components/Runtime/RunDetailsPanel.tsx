import React, { useEffect, useState } from 'react';
import { useTaskStore, Task } from '../../stores/useTaskStore';
import { useWorkflowRunStore } from '../../stores/useWorkflowRunStore';
import { Play, RotateCcw, CheckCircle, XCircle, Clock, Loader2, ChevronDown, ChevronRight, Terminal, FileText, AlertTriangle } from 'lucide-react';
import axios from 'axios';

interface RunDetailsPanelProps {
  projectId: string;
}

export const RunDetailsPanel: React.FC<RunDetailsPanelProps> = ({ projectId }) => {
  const { runs, currentRun, fetchRuns, fetchRun, startRun, retryRun, setCurrentRun } = useWorkflowRunStore();
  const { tasks, fetchTasksByRun, connectionStatus } = useTaskStore();

  const [expandedTaskId, setExpandedTaskId] = useState<string | null>(null);

  useEffect(() => {
    fetchRuns(projectId);
  }, [projectId]);

  useEffect(() => {
    if (runs.length > 0 && !currentRun) {
        setCurrentRun(runs[0]);
    }
  }, [runs]);

  useEffect(() => {
    if (currentRun) {
        fetchTasksByRun(currentRun.id);
    }
  }, [currentRun?.id]);

  // Poll for run status if running
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (currentRun && currentRun.status === 'RUNNING') {
        interval = setInterval(async () => {
             await fetchRuns(projectId);
             const updatedRun = await fetchRun(currentRun.id);
             if (updatedRun && updatedRun.status !== 'RUNNING') {
                 fetchTasksByRun(updatedRun.id);
             }
        }, 2000);
    }
    return () => clearInterval(interval);
  }, [currentRun?.status, currentRun?.id, projectId, fetchRuns, fetchRun, fetchTasksByRun]);

  const handleStartRun = async () => {
    try {
        await startRun(projectId);
    } catch (e) {
        alert("Failed to start run");
    }
  };

  const handleRetryRun = async () => {
      if (!currentRun) return;
      try {
          await retryRun(currentRun.id);
      } catch (e) {
          alert("Failed to retry run");
      }
  };

  const runTasks = Object.values(tasks)
    .filter(t => t.workflowRunId === currentRun?.id)
    .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());

  return (
    <div className="flex flex-col h-full bg-gray-900 border-t border-gray-700">
      {/* Header: Run Selection & Actions */}
      <div className="flex items-center justify-between px-4 py-2 bg-gray-800 border-b border-gray-700">
        <div className="flex items-center gap-2">
            <h3 className="text-sm font-semibold text-gray-200">Run:</h3>
            <select
                className="bg-gray-700 text-xs text-white rounded border border-gray-600 px-2 py-1 outline-none"
                value={currentRun?.id || ''}
                onChange={(e) => {
                    const run = runs.find(r => r.id === e.target.value);
                    setCurrentRun(run || null);
                }}
            >
                {runs.map(run => (
                    <option key={run.id} value={run.id}>
                        {new Date(run.createdAt).toLocaleString()} ({run.status})
                    </option>
                ))}
                {runs.length === 0 && <option value="">No runs</option>}
            </select>

            <span className={`w-2 h-2 rounded-full ${
                connectionStatus === 'connected' ? 'bg-green-500' :
                connectionStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
            }`} title={`Status: ${connectionStatus}`}></span>
        </div>

        <div className="flex gap-2">
            {currentRun?.status === 'FAIL' && (
                <button
                    onClick={handleRetryRun}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-orange-600 hover:bg-orange-700 rounded text-white transition-colors"
                >
                    <RotateCcw size={12} />
                    Retry Run
                </button>
            )}
            <button
                onClick={handleStartRun}
                className="flex items-center gap-1 px-2 py-1 text-xs bg-blue-600 hover:bg-blue-700 rounded text-white transition-colors"
            >
                <Play size={12} />
                New Run
            </button>
        </div>
      </div>

      {/* Task List */}
      <div className="flex-1 overflow-y-auto p-2 space-y-2">
        {!currentRun && (
            <div className="text-center text-gray-500 text-sm py-4">Select or start a run.</div>
        )}
        {currentRun && runTasks.length === 0 && (
             <div className="text-center text-gray-500 text-sm py-4">No tasks in this run.</div>
        )}
        {runTasks.map((task) => (
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

interface TaskItemProps {
    task: Task;
    isExpanded: boolean;
    onToggle: () => void;
}

const TaskItem: React.FC<TaskItemProps> = ({ task, isExpanded, onToggle }) => {
    const [selectedTab, setSelectedTab] = useState<'log' | 'summary' | 'error'>('log');

    const handleRetryTask = async (e: React.MouseEvent) => {
        e.stopPropagation();
        try {
            await axios.post(`/api/tasks/${task.id}/retry`);
        } catch (error) {
            console.error("Retry task failed", error);
        }
    };

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
        <div className={`bg-gray-800 rounded border ${task.status === 'FAIL' ? 'border-red-900' : 'border-gray-700'} overflow-hidden`}>
            <div className="flex items-center gap-2 p-2 cursor-pointer hover:bg-gray-750 transition-colors" onClick={onToggle}>
                <button className="text-gray-400 hover:text-white">
                    {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                </button>
                <div className="min-w-[20px] flex justify-center">{getStatusIcon()}</div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                        <span className="text-sm font-medium text-gray-200 truncate flex items-center gap-2">
                            {task.taskName}
                            <span className="text-xs text-gray-500">({task.agentType})</span>
                        </span>
                        <div className="flex items-center gap-2">
                            {task.status === 'FAIL' && (
                                <button
                                    onClick={handleRetryTask}
                                    className="p-1 hover:bg-gray-600 rounded text-orange-400"
                                    title="Retry Task"
                                >
                                    <RotateCcw size={12} />
                                </button>
                            )}
                            <span className="text-xs text-gray-500 whitespace-nowrap">{new Date(task.createdAt).toLocaleTimeString()}</span>
                        </div>
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
                <div className="border-t border-gray-700 bg-gray-900">
                    <div className="flex border-b border-gray-700">
                        <button
                            className={`flex-1 py-1 text-xs ${selectedTab === 'log' ? 'bg-gray-800 text-white' : 'text-gray-400 hover:text-gray-200'}`}
                            onClick={() => setSelectedTab('log')}
                        >
                            <div className="flex items-center justify-center gap-1">
                                <Terminal size={12} /> Logs
                            </div>
                        </button>
                        <button
                            className={`flex-1 py-1 text-xs ${selectedTab === 'summary' ? 'bg-gray-800 text-white' : 'text-gray-400 hover:text-gray-200'}`}
                            onClick={() => setSelectedTab('summary')}
                        >
                            <div className="flex items-center justify-center gap-1">
                                <FileText size={12} /> Summary
                            </div>
                        </button>
                        {task.errorDetails && (
                            <button
                                className={`flex-1 py-1 text-xs ${selectedTab === 'error' ? 'bg-gray-800 text-red-400' : 'text-red-500 hover:text-red-300'}`}
                                onClick={() => setSelectedTab('error')}
                            >
                                <div className="flex items-center justify-center gap-1">
                                    <AlertTriangle size={12} /> Error
                                </div>
                            </button>
                        )}
                    </div>

                    <div className="p-2 font-mono text-xs text-gray-300 max-h-60 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-transparent">
                        {selectedTab === 'log' && (
                            <pre className="whitespace-pre-wrap">{task.logContent || 'No logs available.'}</pre>
                        )}
                        {selectedTab === 'summary' && (
                            <div className="whitespace-pre-wrap">
                                {task.inputContext && (
                                    <div className="mb-2">
                                        <div className="text-gray-500 mb-1">Input Context:</div>
                                        <pre className="bg-gray-950 p-1 rounded text-gray-400 overflow-x-auto">
                                            {JSON.stringify(task.inputContext, null, 2)}
                                        </pre>
                                    </div>
                                )}
                                {/* Need to find summary in contextData or assume it's merged into it?
                                    Wait, AgentTask doesn't expose output directly, only contextData (accumulated).
                                    Ideally we'd want just the output of *this* task.
                                    The `contextData` on `Task` entity is just `contextData`.
                                    Which gets updated.
                                    Actually `createTask` sets initial `contextData`.
                                    `updateContext` updates it.
                                    So `contextData` on the task entity represents the configuration + result?
                                    Actually `contextData` on `Task` entity is NOT updated by `AgentTaskService.updateContext`.
                                    Wait, `AgentTaskService.updateContext` loads task, updates map, saves task.
                                    So yes, `contextData` accumulates the result.

                                    If we want to show just the summary, we can look for "summary" key in contextData?
                                    Usually we put summary in `summary` field of the result.
                                */}
                                <div className="text-gray-500 mb-1">Task Data (Config + Output):</div>
                                <pre className="bg-gray-950 p-1 rounded overflow-x-auto">
                                    {/* Usually contextData is a map. If it has 'summary', show it prominently? */}
                                    {JSON.stringify(task.contextData || {}, null, 2)}
                                </pre>
                            </div>
                        )}
                        {selectedTab === 'error' && (
                            <pre className="whitespace-pre-wrap text-red-400">{task.errorDetails || 'No error details.'}</pre>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};
