import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { MainLayout } from '../components/Layout/MainLayout';
import { FileTree } from '../components/FileExplorer/FileTree';
import { CodeEditor } from '../components/Editor/CodeEditor';
import { StatusBar } from '../components/Status/StatusBar';
import { ProjectToolbar } from '../components/Runtime/ProjectToolbar';
import { PreviewPane } from '../components/Runtime/PreviewPane';
import { useProjectStore } from '../stores/useProjectStore';
import { useEditorStore } from '../stores/useEditorStore';
import { useRuntimeStore } from '../stores/useRuntimeStore';

const ProjectPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const loadFileTree = useProjectStore((state) => state.loadFileTree);
  const selectedFile = useProjectStore((state) => state.selectedFile);
  const loadFile = useEditorStore((state) => state.loadFile);

  const { previewUrl } = useRuntimeStore();

  const [isPreviewVisible, setIsPreviewVisible] = useState(false);

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
        <div className="flex-1 overflow-hidden relative">
          <CodeEditor projectId={projectId} />
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
