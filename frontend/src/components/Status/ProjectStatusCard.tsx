import React, { useEffect, useState } from 'react';
import { ProjectStatusDTO } from '../../types/status';
import { syncProject, getProjectPipelineStatus } from '../../services/projectService';
import { RefreshCw, GitBranch, Search, AlertCircle, CheckCircle, Clock, Loader2 } from 'lucide-react';

interface ProjectStatusCardProps {
  projectId: string;
}

export const ProjectStatusCard: React.FC<ProjectStatusCardProps> = ({ projectId }) => {
  const [status, setStatus] = useState<ProjectStatusDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);

  const fetchStatus = async () => {
    // Only set loading on initial fetch
    if (!status) setLoading(true);
    try {
      const data = await getProjectPipelineStatus(projectId);
      setStatus(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
    // Poll every 5s
    const interval = setInterval(fetchStatus, 5000);
    return () => clearInterval(interval);
  }, [projectId]);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await syncProject(projectId);
      // Immediately fetch status to show loading/cloning state
      await fetchStatus();
    } catch (e) {
      console.error(e);
    } finally {
      setSyncing(false);
    }
  };

  const getStatusIcon = (s?: string) => {
    if (s === 'SUCCESS') return <CheckCircle className="text-green-400" size={18} />;
    if (s === 'FAILED') return <AlertCircle className="text-red-400" size={18} />;
    if (s === 'RUNNING' || s === 'CLONING') return <Loader2 className="text-blue-400 animate-spin" size={18} />;
    return <Clock className="text-gray-400" size={18} />;
  };

  if (!status && loading) return <div className="p-4 text-center text-gray-500 text-sm">Loading pipeline status...</div>;
  if (!status) return null;

  return (
    <div className="bg-gray-800 border border-gray-700 rounded-lg p-4 mb-4 shadow-sm">
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-semibold text-gray-200 flex items-center gap-2">
            Status Dashboard
        </h3>
        <button
          onClick={handleSync}
          disabled={syncing || status.importStatus === 'CLONING' || status.analysisStatus === 'RUNNING'}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded text-xs font-medium text-white transition-colors"
          title="Re-import from Git and run analysis"
        >
          <RefreshCw size={14} className={syncing || status.importStatus === 'CLONING' ? 'animate-spin' : ''} />
          {status.importStatus === 'CLONING' ? 'Syncing...' : 'Sync & Analyze'}
        </button>
      </div>

      <div className="grid grid-cols-2 gap-4">
        {/* Import Status */}
        <div className="bg-gray-900 rounded p-3 border border-gray-700">
          <div className="flex items-center gap-2 mb-2">
            <GitBranch size={16} className="text-purple-400" />
            <span className="text-sm font-medium text-gray-300">Import</span>
            <span className="ml-auto">{getStatusIcon(status.importStatus)}</span>
          </div>
          <div className="text-xs text-gray-400 space-y-1">
             <div className="flex justify-between">
                <span>Status:</span>
                <span className={`font-mono ${status.importStatus === 'SUCCESS' ? 'text-green-400' : status.importStatus === 'FAILED' ? 'text-red-400' : 'text-blue-400'}`}>
                    {status.importStatus}
                </span>
             </div>
             {status.importEndTime && (
                 <div className="flex justify-between" title={new Date(status.importEndTime).toLocaleString()}>
                    <span>Last Sync:</span>
                    <span>{new Date(status.importEndTime).toLocaleTimeString()}</span>
                 </div>
             )}
             {status.importFailureReason && (
                 <div className="mt-2 p-2 bg-red-900/20 border border-red-900/50 rounded text-red-300 break-words">
                    {status.importFailureReason}
                 </div>
             )}
          </div>
        </div>

        {/* Analysis Status */}
        <div className="bg-gray-900 rounded p-3 border border-gray-700">
          <div className="flex items-center gap-2 mb-2">
            <Search size={16} className="text-teal-400" />
            <span className="text-sm font-medium text-gray-300">Analysis</span>
            <span className="ml-auto">{getStatusIcon(status.analysisStatus)}</span>
          </div>
          <div className="text-xs text-gray-400 space-y-1">
             <div className="flex justify-between">
                <span>Status:</span>
                <span className={`font-mono ${status.analysisStatus === 'SUCCESS' ? 'text-green-400' : status.analysisStatus === 'FAILED' ? 'text-red-400' : 'text-blue-400'}`}>
                    {status.analysisStatus}
                </span>
             </div>
             {status.metadata && (
                 <>
                    <div className="flex justify-between">
                        <span>Files:</span>
                        <span className="text-gray-200">{status.metadata.fileCount}</span>
                    </div>
                    <div className="flex justify-between">
                        <span>Classes:</span>
                        <span className="text-gray-200">{status.metadata.classCount}</span>
                    </div>
                    <div className="flex justify-between">
                        <span>Methods:</span>
                        <span className="text-gray-200">{status.metadata.methodCount}</span>
                    </div>
                    <div className="flex justify-between">
                        <span>Duration:</span>
                        <span className="text-gray-200">{status.metadata.durationMs}ms</span>
                    </div>
                 </>
             )}
             {status.analysisFailureReason && (
                 <div className="mt-2 p-2 bg-red-900/20 border border-red-900/50 rounded text-red-300 break-words">
                    {status.analysisFailureReason}
                 </div>
             )}
          </div>
        </div>
      </div>

      {/* Recent Logs */}
      <div className="mt-4">
        <h4 className="text-xs font-semibold text-gray-400 mb-2 uppercase tracking-wider">Recent Activity</h4>
        <div className="bg-gray-950 rounded border border-gray-800 max-h-32 overflow-y-auto text-xs font-mono p-2 space-y-1">
            {status.recentLogs.map(log => (
                <div key={log.id} className="flex gap-2 text-gray-400">
                    <span className="text-gray-600 whitespace-nowrap">{new Date(log.timestamp).toLocaleTimeString()}</span>
                    <span className={`font-bold w-16 ${log.status === 'SUCCESS' ? 'text-green-500' : log.status === 'FAILED' ? 'text-red-500' : 'text-blue-500'}`}>
                        [{log.operationType}]
                    </span>
                    <span className="text-gray-300">{log.message}</span>
                </div>
            ))}
            {status.recentLogs.length === 0 && <div className="text-gray-600 italic">No logs yet.</div>}
        </div>
      </div>
    </div>
  );
};
