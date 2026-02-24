export interface Project {
  id: string;
  name: string;
  description?: string;
  gitRepoPath?: string;
  techStack?: string;
  createdAt?: string;
}

export interface FileNode {
  name: string;
  path: string;
  type: 'file' | 'directory';
  children?: FileNode[];
}

export interface FileContent {
  path: string;
  content: string;
}

export interface ContainerStatus {
  status: 'STOPPED' | 'STARTING' | 'RUNNING';
  previewUrl?: string;
  internalIp?: string;
}
