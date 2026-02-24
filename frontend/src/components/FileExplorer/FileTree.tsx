import React, { useState } from 'react';
import { FileNode } from '../../types';
import { ChevronRight, ChevronDown, File } from 'lucide-react';
import { useProjectStore } from '../../stores/useProjectStore';

interface FileTreeItemProps {
  node: FileNode;
  level: number;
}

const FileTreeItem: React.FC<FileTreeItemProps> = ({ node, level }) => {
  const [isOpen, setIsOpen] = useState(false);
  const selectFile = useProjectStore((state) => state.selectFile);
  const selectedFile = useProjectStore((state) => state.selectedFile);

  const handleClick = () => {
    if (node.type === 'directory') {
      setIsOpen(!isOpen);
    } else {
      selectFile(node.path);
    }
  };

  const isSelected = selectedFile === node.path;

  return (
    <div>
      <div
        className={`flex items-center py-1 px-2 cursor-pointer hover:bg-gray-700 text-sm ${isSelected ? 'bg-gray-700 text-blue-300' : 'text-gray-300'}`}
        style={{ paddingLeft: `${level * 12 + 4}px` }}
        onClick={handleClick}
      >
        <span className="mr-1">
          {node.type === 'directory' ? (
            isOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />
          ) : (
            <File size={14} className="text-gray-400" />
          )}
        </span>
        <span className="truncate">{node.name}</span>
      </div>
      {isOpen && node.children && (
        <div>
          {node.children.map((child) => (
            <FileTreeItem key={child.path} node={child} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  );
};

export const FileTree: React.FC = () => {
  const fileTree = useProjectStore((state) => state.fileTree);
  const isLoading = useProjectStore((state) => state.isLoading);

  if (isLoading) return <div className="p-4 text-gray-400 text-sm">Loading files...</div>;
  if (!fileTree || fileTree.length === 0) return <div className="p-4 text-gray-500 text-sm">No files found.</div>;

  return (
    <div className="flex-1 overflow-y-auto py-2">
      {fileTree.map((node) => (
        <FileTreeItem key={node.path} node={node} level={0} />
      ))}
    </div>
  );
};
