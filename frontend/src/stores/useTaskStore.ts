import { create } from 'zustand';
import axios from 'axios';

export type TaskStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAIL';

export interface Task {
  id: string;
  projectId: string;
  workflowRunId?: string;
  agentType: string;
  taskName: string;
  status: TaskStatus;
  progressPct: number;
  logContent: string;
  inputContext?: any;
  contextData?: any;
  retryCount?: number;
  errorDetails?: string;
  createdAt: string;
}

export interface TaskEvent {
  type: 'QUEUED' | 'RUNNING' | 'STEP_START' | 'STEP_COMPLETE' | 'PROGRESS' | 'COMPLETED' | 'FAILED';
  projectId: string;
  taskId: string;
  taskName: string;
  progress: number;
  message: string;
  payload: any;
  timestamp: string;
}

interface TaskState {
  tasks: Record<string, Task>;
  connectionStatus: 'connected' | 'disconnected' | 'connecting';

  setConnectionStatus: (status: 'connected' | 'disconnected' | 'connecting') => void;
  fetchTasks: (projectId: string) => Promise<void>;
  fetchTasksByRun: (runId: string) => Promise<void>;
  handleEvent: (event: TaskEvent) => void;
}

export const useTaskStore = create<TaskState>((set, get) => ({
  tasks: {},
  connectionStatus: 'disconnected',

  setConnectionStatus: (status) => set({ connectionStatus: status }),

  fetchTasks: async (projectId) => {
    try {
        const response = await axios.get(`/api/projects/${projectId}/tasks`);
        const taskMap = response.data.reduce((acc: Record<string, Task>, task: Task) => {
            acc[task.id] = task;
            return acc;
        }, {});
        set({ tasks: taskMap });
    } catch (error) {
        console.error('Failed to fetch tasks', error);
    }
  },

  fetchTasksByRun: async (runId) => {
      try {
          const response = await axios.get(`/api/runs/${runId}/tasks`);
          const newTasks = response.data.reduce((acc: Record<string, Task>, task: Task) => {
              acc[task.id] = task;
              return acc;
          }, {});

          set((state) => ({
              tasks: {
                  ...state.tasks,
                  ...newTasks
              }
          }));
      } catch (error) {
          console.error('Failed to fetch run tasks', error);
      }
  },

  handleEvent: (event) => {
    const { tasks } = get();
    const existingTask = tasks[event.taskId];

    let newTask: Task;

    if (!existingTask) {
        // Create shell task from event
        newTask = {
            id: event.taskId,
            projectId: event.projectId,
            agentType: 'Unknown', // Will be updated on next fetch or we should add to DTO
            taskName: event.taskName,
            status: 'PENDING',
            progressPct: event.progress || 0,
            logContent: event.message || '',
            createdAt: event.timestamp
        };
    } else {
        newTask = { ...existingTask };
    }

    // Update progress
    if (event.progress !== null && event.progress !== undefined) {
        newTask.progressPct = event.progress;
    }

    // Update context
    if (event.payload) {
        newTask.contextData = event.payload;
    }

    // Append log
    if (event.message) {
         newTask.logContent = newTask.logContent ? newTask.logContent + '\n' + event.message : event.message;
    }

    // Update status
    switch (event.type) {
        case 'QUEUED':
            newTask.status = 'PENDING';
            break;
        case 'RUNNING':
            newTask.status = 'RUNNING';
            break;
        case 'COMPLETED':
            newTask.status = 'SUCCESS';
            newTask.progressPct = 100;
            break;
        case 'FAILED':
            newTask.status = 'FAIL';
            newTask.errorDetails = event.message;
            break;
    }

    set({
        tasks: {
            ...tasks,
            [event.taskId]: newTask
        }
    });
  }
}));
