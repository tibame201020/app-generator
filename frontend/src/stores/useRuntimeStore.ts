import { create } from 'zustand';
import { ContainerStatus } from '../types';
import * as api from '../services/projectService';

interface RuntimeState {
  status: ContainerStatus['status'];
  previewUrl: string | null;
  internalIp: string | null;
  isLoading: boolean;
  error: string | null;
  isPolling: boolean;

  // Actions
  fetchStatus: (projectId: string) => Promise<void>;
  runProject: (projectId: string) => Promise<void>;
  stopProject: (projectId: string) => Promise<void>;
  restartProject: (projectId: string) => Promise<void>;
  startPolling: (projectId: string) => void;
  stopPolling: () => void;
}

const POLL_INTERVAL_FAST = 1000;
const POLL_INTERVAL_SLOW = 5000;

export const useRuntimeStore = create<RuntimeState>((set, get) => {
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  const clearTimer = () => {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = null;
    }
  };

  return {
    status: 'STOPPED',
    previewUrl: null,
    internalIp: null,
    isLoading: false,
    error: null,
    isPolling: false,

    fetchStatus: async (projectId: string) => {
      try {
        const data = await api.getProjectStatus(projectId);
        set({
          status: data.status,
          previewUrl: data.previewUrl || null,
          internalIp: data.internalIp || null,
          error: null,
        });
      } catch (err: any) {
        set({ error: err.message });
      }
    },

    runProject: async (projectId: string) => {
      set({ isLoading: true, error: null });
      try {
        await api.runProject(projectId);
        // Force immediate status check
        get().startPolling(projectId);
      } catch (err: any) {
        set({ error: err.message });
      } finally {
        set({ isLoading: false });
      }
    },

    stopProject: async (projectId: string) => {
      set({ isLoading: true, error: null });
      try {
        await api.stopProject(projectId);
        get().startPolling(projectId);
      } catch (err: any) {
        set({ error: err.message });
      } finally {
        set({ isLoading: false });
      }
    },

    restartProject: async (projectId: string) => {
      set({ isLoading: true, error: null });
      try {
        await api.restartProject(projectId);
        get().startPolling(projectId);
      } catch (err: any) {
        set({ error: err.message });
      } finally {
        set({ isLoading: false });
      }
    },

    startPolling: (projectId: string) => {
      // Reset if already polling to ensure fresh interval logic
      clearTimer();
      set({ isPolling: true });

      const step = async () => {
        // If polling was stopped in the meantime
        if (!get().isPolling) return;

        await get().fetchStatus(projectId);

        // Check if still polling after await
        if (!get().isPolling) return;

        const currentStatus = get().status;
        // Fast poll if starting or if we just triggered an action (isLoading might be false by now but status not yet updated)
        // But simply: if 'STARTING', fast poll.
        // If 'STOPPED' or 'RUNNING', slow poll.
        // If 'EXPIRED', slow poll.
        const isTransitioning = currentStatus === 'STARTING';
        const interval = isTransitioning ? POLL_INTERVAL_FAST : POLL_INTERVAL_SLOW;

        pollTimer = setTimeout(step, interval);
      };

      step();
    },

    stopPolling: () => {
      clearTimer();
      set({ isPolling: false });
    },
  };
});
