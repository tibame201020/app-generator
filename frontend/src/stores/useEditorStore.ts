import { create } from 'zustand';
import { FileContent } from '../types';
import { getFileContent, saveFileContent } from '../services/projectService';

interface EditorState {
  currentFile: FileContent | null;
  content: string;
  isDirty: boolean;
  isLoading: boolean;
  error: string | null;

  loadFile: (projectId: string, path: string) => Promise<void>;
  updateContent: (content: string) => void;
  saveFile: (projectId: string) => Promise<void>;
}

export const useEditorStore = create<EditorState>((set, get) => ({
  currentFile: null,
  content: '',
  isDirty: false,
  isLoading: false,
  error: null,

  loadFile: async (projectId, path) => {
    set({ isLoading: true, error: null, isDirty: false });
    try {
      const file = await getFileContent(projectId, path);
      set({ currentFile: file, content: file.content, isLoading: false });
    } catch (err: any) {
      set({ error: err.message, isLoading: false });
    }
  },

  updateContent: (content) => set({ content, isDirty: true }),

  saveFile: async (projectId) => {
    const { currentFile, content } = get();
    if (!currentFile) return;

    set({ isLoading: true, error: null });
    try {
      await saveFileContent(projectId, currentFile.path, content);
      set({ isDirty: false, isLoading: false });
    } catch (err: any) {
      set({ error: err.message, isLoading: false });
    }
  },
}));
