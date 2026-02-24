import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { MainLayout } from '../components/Layout/MainLayout';
import { FileTree } from '../components/FileExplorer/FileTree';
import { CodeEditor } from '../components/Editor/CodeEditor';
import { StatusBar } from '../components/Status/StatusBar';
import { ProjectToolbar } from '../components/Runtime/ProjectToolbar';
import { PreviewPane } from '../components/Runtime/PreviewPane';
import { TaskPanel } from '../components/Runtime/TaskPanel';
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
        />
      }
    >
      <div className="flex flex-1 overflow-hidden h-full">
        <div className="flex-1 flex flex-col overflow-hidden relative">
          <div className="flex-1 overflow-hidden">
            <CodeEditor projectId={projectId} />
          </div>

          {isTaskPanelVisible && (
            <div className="h-64 min-h-[160px] max-h-[50%] border-t border-gray-700 bg-gray-900 overflow-hidden shrink-0">
               <TaskPanel projectId={projectId} />
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
