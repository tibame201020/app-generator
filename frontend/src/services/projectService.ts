import api from './api';
import { Project, FileNode, FileContent, AnalysisResultDTO, ProjectStatusDTO } from '../types';

export const getProjects = async (userId: string): Promise<Project[]> => {
  const response = await api.get<Project[]>('/projects', { params: { userId } });
  return response.data;
};

export const getFileTree = async (projectId: string): Promise<FileNode[]> => {
  const response = await api.get<FileNode[]>(`/projects/${projectId}/files`);
  return response.data;
};

export const getFileContent = async (projectId: string, path: string): Promise<FileContent> => {
  const response = await api.get<FileContent>(`/projects/${projectId}/files/content`, {
    params: { path },
  });
  return response.data;
};

export const saveFileContent = async (projectId: string, path: string, content: string): Promise<void> => {
  await api.put(`/projects/${projectId}/files/content`, { path, content });
};

export const runProject = async (projectId: string): Promise<void> => {
  await api.post(`/projects/${projectId}/run`);
};

export const stopProject = async (projectId: string): Promise<void> => {
  await api.post(`/projects/${projectId}/stop`);
};

export const restartProject = async (projectId: string): Promise<void> => {
  await api.post(`/projects/${projectId}/restart`);
};

export const getProjectStatus = async (projectId: string): Promise<any> => {
  const response = await api.get(`/projects/${projectId}/status`);
  return response.data;
};

export const importProject = async (userId: string, remoteRepoUrl: string, name: string): Promise<Project> => {
    const response = await api.post<Project>('/projects/import', { remoteRepoUrl, name }, { params: { userId } });
    return response.data;
};

export const getAnalysis = async (projectId: string): Promise<AnalysisResultDTO> => {
    const response = await api.get<AnalysisResultDTO>(`/projects/${projectId}/analysis`);
    return response.data;
};

export const triggerAnalysis = async (projectId: string): Promise<void> => {
    await api.post(`/projects/${projectId}/analysis`);
};

export const getProjectPipelineStatus = async (projectId: string): Promise<ProjectStatusDTO> => {
    const response = await api.get<ProjectStatusDTO>(`/projects/${projectId}/pipeline-status`);
    return response.data;
};

export const syncProject = async (projectId: string): Promise<void> => {
    await api.post(`/projects/${projectId}/sync`);
};
