import { create } from 'zustand';
import { Project, FileNode } from '../types';
import * as api from '../services/projectService';

interface ProjectState {
  currentProject: Project | null;
  fileTree: FileNode[];
  selectedFile: string | null;
  isLoading: boolean;
  error: string | null;

  setProject: (project: Project) => void;
  loadFileTree: (projectId: string) => Promise<void>;
  selectFile: (path: string) => void;
  setError: (error: string | null) => void;
}

export const useProjectStore = create<ProjectState>((set) => ({
  currentProject: null,
  fileTree: [],
  selectedFile: null,
  isLoading: false,
  error: null,

  setProject: (project) => set({ currentProject: project }),

  loadFileTree: async (projectId) => {
    set({ isLoading: true, error: null });
    try {
      const tree = await api.getFileTree(projectId);
      set({ fileTree: tree, isLoading: false });
    } catch (err: any) {
      set({ error: err.message, isLoading: false });
    }
  },

  selectFile: (path) => set({ selectedFile: path }),

  setError: (error) => set({ error }),
}));
