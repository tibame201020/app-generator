import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { MainLayout } from '../components/Layout/MainLayout';
import { FileTree } from '../components/FileExplorer/FileTree';
import { CodeEditor } from '../components/Editor/CodeEditor';
import { StatusBar } from '../components/Status/StatusBar';
import { useProjectStore } from '../stores/useProjectStore';
import { useEditorStore } from '../stores/useEditorStore';

const ProjectPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const loadFileTree = useProjectStore((state) => state.loadFileTree);
  const selectedFile = useProjectStore((state) => state.selectedFile);
  const loadFile = useEditorStore((state) => state.loadFile);

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
    >
      <CodeEditor projectId={projectId} />
    </MainLayout>
  );
};

export default ProjectPage;
