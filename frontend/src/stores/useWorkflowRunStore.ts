import { create } from 'zustand';
import axios from 'axios';

export type RunStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAIL';

export interface WorkflowRun {
  id: string;
  projectId: string;
  status: RunStatus;
  startedAt: string;
  endedAt?: string;
  createdAt: string;
  updatedAt: string;
}

interface WorkflowRunState {
  runs: WorkflowRun[];
  currentRun: WorkflowRun | null;
  isLoading: boolean;

  fetchRuns: (projectId: string) => Promise<void>;
  fetchRun: (runId: string) => Promise<WorkflowRun | null>;
  startRun: (projectId: string) => Promise<void>;
  retryRun: (runId: string) => Promise<void>;
  setCurrentRun: (run: WorkflowRun | null) => void;
}

export const useWorkflowRunStore = create<WorkflowRunState>((set, get) => ({
  runs: [],
  currentRun: null,
  isLoading: false,

  fetchRuns: async (projectId) => {
    set({ isLoading: true });
    try {
      const response = await axios.get(`/api/projects/${projectId}/runs`);
      set({ runs: response.data });
    } catch (error) {
      console.error('Failed to fetch runs', error);
    } finally {
      set({ isLoading: false });
    }
  },

  fetchRun: async (runId) => {
    try {
        const response = await axios.get(`/api/runs/${runId}`);
        const run = response.data;
        // If current run is this one, update it
        if (get().currentRun?.id === runId) {
            set({ currentRun: run });
        }
        return run;
    } catch (error) {
        console.error('Failed to fetch run', error);
        return null;
    }
  },

  startRun: async (projectId) => {
    try {
      const response = await axios.post(`/api/projects/${projectId}/runs`);
      const newRun = response.data;
      set((state) => ({
          runs: [newRun, ...state.runs],
          currentRun: newRun
      }));
    } catch (error) {
      console.error('Failed to start run', error);
      throw error;
    }
  },

  retryRun: async (runId) => {
    try {
        await axios.post(`/api/runs/${runId}/retry`);
        // Optimistically update status
        const { runs, currentRun } = get();
        const updatedRuns = runs.map(r => r.id === runId ? { ...r, status: 'RUNNING' as RunStatus } : r);
        set({ runs: updatedRuns });
        if (currentRun?.id === runId) {
            set({ currentRun: { ...currentRun, status: 'RUNNING' } });
        }
    } catch (error) {
        console.error('Failed to retry run', error);
        throw error;
    }
  },

  setCurrentRun: (run) => set({ currentRun: run })
}));
