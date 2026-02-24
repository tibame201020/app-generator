import React from 'react';
import { useEditorStore } from '../../stores/useEditorStore';

export const StatusBar: React.FC = () => {
  const { currentFile, isDirty, isLoading, error } = useEditorStore();

  return (
    <div className="flex items-center w-full px-2">
      <div className="flex-1">
        {isLoading && <span>Processing...</span>}
        {error && <span className="text-red-400">Error: {error}</span>}
        {!isLoading && !error && <span>Ready</span>}
      </div>
      <div className="flex items-center space-x-4">
        {currentFile && (
          <>
            <span>{currentFile.path}</span>
            <span>{isDirty ? 'Unsaved' : 'Saved'}</span>
            <span>UTF-8</span>
          </>
        )}
      </div>
    </div>
  );
};
