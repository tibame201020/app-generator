import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { MainLayout } from '../components/Layout/MainLayout';
import { FileTree } from '../components/FileExplorer/FileTree';
import { CodeEditor } from '../components/Editor/CodeEditor';
import { WorkflowCanvas } from '../components/Workflow/WorkflowCanvas';
import { AnalysisPanel } from '../components/Analysis/AnalysisPanel';
import { StatusBar } from '../components/Status/StatusBar';
import { ProjectToolbar } from '../components/Runtime/ProjectToolbar';
import { PreviewPane } from '../components/Runtime/PreviewPane';
import { RunDetailsPanel } from '../components/Runtime/RunDetailsPanel';
import { useProjectStore } from '../stores/useProjectStore';
import { useEditorStore } from '../stores/useEditorStore';
import { useRuntimeStore } from '../stores/useRuntimeStore';
import { useTaskWebSocket } from '../hooks/useTaskWebSocket';

const ProjectPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();

  // Connect to WebSocket for tasks
  useTaskWebSocket(projectId || '');

  const loadFileTree = useProjectStore((state) => state.loadFileTree);
  const selectedFile = useProjectStore((state) => state.selectedFile);
  const loadFile = useEditorStore((state) => state.loadFile);

  const { previewUrl } = useRuntimeStore();

  const [isPreviewVisible, setIsPreviewVisible] = useState(false);
  const [isTaskPanelVisible] = useState(true);
  const [viewMode, setViewMode] = useState<'code' | 'workflow' | 'analysis'>('code');

  useEffect(() => {
    if (projectId) {
      loadFileTree(projectId);
    }
  }, [projectId, loadFileTree]);

  useEffect(() => {
    if (projectId && selectedFile) {
      loadFile(projectId, selectedFile);
    }
  }, [projectId, selectedFile, loadFile]);

  if (!projectId) return <div>Invalid Project ID</div>;

  return (
    <MainLayout
      sidebar={<FileTree />}
      statusBar={<StatusBar />}
      topBar={
        <ProjectToolbar
          projectId={projectId}
          onTogglePreview={() => setIsPreviewVisible(!isPreviewVisible)}
          isPreviewVisible={isPreviewVisible}
          viewMode={viewMode}
          onViewModeChange={setViewMode}
        />
      }
    >
      <div className="flex flex-1 overflow-hidden h-full">
        <div className="flex-1 flex flex-col overflow-hidden relative">
          <div className="flex-1 overflow-hidden h-full relative">
            {viewMode === 'code' ? (
                <CodeEditor projectId={projectId} />
            ) : viewMode === 'workflow' ? (
                <WorkflowCanvas />
            ) : (
                <AnalysisPanel projectId={projectId} />
            )}
          </div>

          {isTaskPanelVisible && (
            <div className="h-64 min-h-[160px] max-h-[50%] border-t border-gray-700 bg-gray-900 overflow-hidden shrink-0">
               <RunDetailsPanel projectId={projectId} />
            </div>
          )}
        </div>

        {isPreviewVisible && previewUrl && (
          <div className="w-[45%] min-w-[320px] max-w-[80%] border-l border-gray-700 bg-white shadow-xl z-10 transition-all duration-300">
            <PreviewPane
              url={previewUrl}
              onClose={() => setIsPreviewVisible(false)}
            />
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default ProjectPage;
