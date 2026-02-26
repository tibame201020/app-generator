import React, { useEffect } from 'react';
import { Play, Square, RefreshCw, LayoutTemplate, Code, Layers } from 'lucide-react';
import { useRuntimeStore } from '../../stores/useRuntimeStore';

interface ProjectToolbarProps {
  projectId: string;
  onTogglePreview: () => void;
  isPreviewVisible: boolean;
  viewMode: 'code' | 'workflow' | 'analysis';
  onViewModeChange: (mode: 'code' | 'workflow' | 'analysis') => void;
}

export const ProjectToolbar: React.FC<ProjectToolbarProps> = ({
  projectId,
  onTogglePreview,
  isPreviewVisible,
  viewMode,
  onViewModeChange
}) => {
  const {
    status,
    runProject,
    stopProject,
    restartProject,
    isLoading,
    startPolling,
    stopPolling,
    previewUrl
  } = useRuntimeStore();

  useEffect(() => {
    startPolling(projectId);
    return () => stopPolling();
  }, [projectId, startPolling, stopPolling]);

  const handleRun = () => runProject(projectId);
  const handleStop = () => stopProject(projectId);
  const handleRestart = () => restartProject(projectId);

  const getStatusColor = () => {
    switch (status) {
      case 'RUNNING': return 'bg-green-500';
      case 'STARTING': return 'bg-yellow-500';
      case 'STOPPED': return 'bg-red-500';
      case 'EXPIRED': return 'bg-gray-500';
      default: return 'bg-gray-500';
    }
  };

  const getStatusText = () => {
     switch (status) {
      case 'RUNNING': return 'Running';
      case 'STARTING': return 'Starting...';
      case 'STOPPED': return 'Stopped';
      case 'EXPIRED': return 'Expired';
      default: return 'Unknown';
    }
  };

  return (
    <div className="flex items-center space-x-4">
      {/* Status Indicator */}
      <div className="flex items-center space-x-2 text-sm px-2">
        <div className={`w-2 h-2 rounded-full ${getStatusColor()}`} />
        <span className="text-gray-300 font-medium">{getStatusText()}</span>
      </div>

      <div className="h-4 w-px bg-gray-700" />

      {/* View Switcher */}
      <div className="flex bg-gray-800 rounded p-0.5 border border-gray-700">
          <button
            onClick={() => onViewModeChange('code')}
            className={`px-2 py-1 rounded flex items-center gap-1 text-xs font-medium transition-colors ${
              viewMode === 'code' ? 'bg-gray-700 text-white shadow-sm' : 'text-gray-400 hover:text-white'
            }`}
            title="Code View"
          >
            <Code size={14} />
            <span>Code</span>
          </button>
          <button
            onClick={() => onViewModeChange('workflow')}
            className={`px-2 py-1 rounded flex items-center gap-1 text-xs font-medium transition-colors ${
              viewMode === 'workflow' ? 'bg-gray-700 text-white shadow-sm' : 'text-gray-400 hover:text-white'
            }`}
            title="Workflow Canvas"
          >
            <LayoutTemplate size={14} />
            <span>Workflow</span>
          </button>
          <button
            onClick={() => onViewModeChange('analysis')}
            className={`px-2 py-1 rounded flex items-center gap-1 text-xs font-medium transition-colors ${
              viewMode === 'analysis' ? 'bg-gray-700 text-white shadow-sm' : 'text-gray-400 hover:text-white'
            }`}
            title="Analysis View"
          >
            <Layers size={14} />
            <span>Analysis</span>
          </button>
      </div>

      <div className="h-4 w-px bg-gray-700" />

      {/* Controls */}
      <div className="flex items-center space-x-1">
        <button
          onClick={handleRun}
          disabled={status === 'RUNNING' || status === 'STARTING' || isLoading}
          className="p-1.5 hover:bg-gray-700 rounded text-green-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          title="Run Project"
        >
          <Play size={16} />
        </button>

        <button
          onClick={handleStop}
          disabled={status === 'STOPPED' || status === 'EXPIRED' || isLoading}
          className="p-1.5 hover:bg-gray-700 rounded text-red-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          title="Stop Project"
        >
          <Square size={16} />
        </button>

        <button
          onClick={handleRestart}
          disabled={status === 'STOPPED' || status === 'EXPIRED' || isLoading}
          className="p-1.5 hover:bg-gray-700 rounded text-blue-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          title="Restart Project"
        >
          <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''} />
        </button>
      </div>

      {status === 'RUNNING' && previewUrl && (
        <>
          <div className="h-4 w-px bg-gray-700" />
          <button
            onClick={onTogglePreview}
            className={`flex items-center space-x-1 px-2 py-1 rounded text-xs font-medium transition-colors ${
              isPreviewVisible
                ? 'bg-blue-600 text-white'
                : 'text-gray-300 hover:bg-gray-700'
            }`}
          >
            <span>Preview</span>
          </button>
        </>
      )}
    </div>
  );
};
