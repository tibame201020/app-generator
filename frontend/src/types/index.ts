export * from './status';
export interface Project {
  id: string;
  name: string;
  description?: string;
  gitRepoPath?: string;
  techStack?: string;
  createdAt?: string;
  remoteRepoUrl?: string;
  defaultBranch?: string;
  importStatus?: 'PENDING' | 'CLONING' | 'SUCCESS' | 'FAILED';
  importFailureReason?: string;
  lastSyncTime?: string;
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
  status: 'STOPPED' | 'STARTING' | 'RUNNING' | 'EXPIRED';
  previewUrl?: string;
  internalIp?: string;
}

export interface PackageDTO {
  name: string;
  classes: ClassDTO[];
}

export interface ClassDTO {
  name: string;
  type: string;
  modifiers: string[];
  fields: FieldDTO[];
  methods: MethodDTO[];
  dependencies: string[];
}

export interface FieldDTO {
  name: string;
  type: string;
  modifiers: string[];
}

export interface MethodDTO {
  name: string;
  returnType: string;
  parameters: string[];
  modifiers: string[];
}

export interface AnalysisResultDTO {
  packages: PackageDTO[];
}
