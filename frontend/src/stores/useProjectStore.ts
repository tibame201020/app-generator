import { create } from 'zustand';
import { Project, FileNode } from '../types';
import * as api from '../services/projectService';
import { ProjectRole } from './useAuthStore';
import axios from 'axios';

interface ProjectMember {
    id: string;
    userId: string;
    email: string; // Assuming we get email
    role: ProjectRole;
    joinedAt: string;
}

interface ProjectState {
  currentProject: Project | null;
  currentUserRole: ProjectRole | null;
  fileTree: FileNode[];
  selectedFile: string | null;
  isLoading: boolean;
  error: string | null;

  setProject: (project: Project) => void;
  loadFileTree: (projectId: string) => Promise<void>;
  selectFile: (path: string) => void;
  setError: (error: string | null) => void;
  fetchUserRole: (projectId: string) => Promise<void>;

  // Permissions helpers
  canEdit: () => boolean;
  canRun: () => boolean;
  canManageMembers: () => boolean;
  canDeleteProject: () => boolean;
}

export const useProjectStore = create<ProjectState>((set, get) => ({
  currentProject: null,
  currentUserRole: null,
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

  fetchUserRole: async (projectId) => {
      // In a real app we might fetch this from a specific endpoint or derive from project data if expanded
      // For now, let's assume we can get it from /api/projects/{id}/members/me or similar,
      // OR we can fetch all members and find current user.
      // A dedicated endpoint /api/projects/{id}/my-role would be better.
      // Let's implement a quick check by fetching members for now (if allowed) or assume
      // we need to add that endpoint.
      // Plan B: Try to fetch members, if 403, we are likely not admin/member? No, viewers can see members?
      // Actually, let's rely on the backend to tell us our role or we catch 403s.
      // But for UI state (disabling buttons), we need to know.
      // Let's add a helper to `useAuthStore` or `api` to check permissions?
      // Simplest: `GET /api/projects/{id}/members` returns list. Find self.
      try {
          // This requires we know our own user ID.
          // Let's skip precise role fetching for this step and rely on error handling?
          // No, requirement is "clear disabled states".
          // Let's assume for MVP we fetch all members and match email/id.
          // But getting own ID is in useAuthStore.
          const { user } = require('./useAuthStore').useAuthStore.getState();
          if (!user) return;

          const response = await axios.get(`/api/projects/${projectId}/members`);
          const members = response.data;
          const me = members.find((m: any) => m.user.id === user.id);
          if (me) {
              set({ currentUserRole: me.role });
          }
      } catch (e) {
          // If 403, maybe not even a viewer? Or maybe viewer can't see members?
          // Backend `getMembers` is `@PreAuthorize("@projectSecurityService.isViewer(#projectId)")`
          // So if we fail, we probably aren't even a viewer.
          console.error("Failed to fetch role", e);
          set({ currentUserRole: null });
      }
  },

  canEdit: () => {
      const role = get().currentUserRole;
      return role === ProjectRole.ADMIN || role === ProjectRole.MEMBER;
  },
  canRun: () => {
      const role = get().currentUserRole;
      return role === ProjectRole.ADMIN || role === ProjectRole.MEMBER;
  },
  canManageMembers: () => {
      const role = get().currentUserRole;
      return role === ProjectRole.ADMIN;
  },
  canDeleteProject: () => {
      const role = get().currentUserRole;
      return role === ProjectRole.ADMIN;
  }
}));
