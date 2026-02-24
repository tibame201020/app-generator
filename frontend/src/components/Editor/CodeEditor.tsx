import React, { useEffect, useRef } from 'react';
import Editor, { OnMount } from '@monaco-editor/react';
import { useEditorStore } from '../../stores/useEditorStore';

interface CodeEditorProps {
  projectId: string;
}

export const CodeEditor: React.FC<CodeEditorProps> = ({ projectId }) => {
  const editorRef = useRef<any>(null);
  const { content, currentFile, isDirty, isLoading, updateContent, saveFile } = useEditorStore();

  const handleEditorDidMount: OnMount = (editor, monaco) => {
    editorRef.current = editor;

    // Add save command (Ctrl+S)
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
      saveFile(projectId);
    });
  };

  const handleEditorChange = (value: string | undefined) => {
    if (value !== undefined) {
      updateContent(value);
    }
  };

  if (isLoading) {
    return <div className="flex items-center justify-center h-full text-gray-500">Loading...</div>;
  }

  if (!currentFile) {
    return <div className="flex items-center justify-center h-full text-gray-500">Select a file to edit</div>;
  }

  return (
    <div className="h-full w-full">
      <div className="h-8 bg-gray-800 border-b border-gray-700 flex items-center px-4 text-sm text-gray-300">
        <span>{currentFile.path}</span>
        {isDirty && <span className="ml-2 text-yellow-500 text-xs">●</span>}
      </div>
      <Editor
        height="calc(100% - 2rem)"
        defaultLanguage="javascript"
        path={currentFile.path}
        value={content}
        theme="vs-dark"
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          scrollBeyondLastLine: false,
          automaticLayout: true,
        }}
      />
    </div>
  );
};
