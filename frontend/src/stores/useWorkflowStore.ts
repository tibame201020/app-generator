import { create } from 'zustand';
import {
  Connection,
  Edge,
  EdgeChange,
  Node,
  NodeChange,
  addEdge,
  OnNodesChange,
  OnEdgesChange,
  OnConnect,
  applyNodeChanges,
  applyEdgeChanges,
} from 'reactflow';
import axios from 'axios';

type WorkflowState = {
  nodes: Node[];
  edges: Edge[];
  onNodesChange: OnNodesChange;
  onEdgesChange: OnEdgesChange;
  onConnect: OnConnect;
  setNodes: (nodes: Node[]) => void;
  setEdges: (edges: Edge[]) => void;
  loadWorkflow: (projectId: string) => Promise<void>;
  saveWorkflow: (projectId: string) => Promise<void>;
  validateWorkflow: (projectId: string) => Promise<string[]>;
  runWorkflow: (projectId: string) => Promise<void>;
  addNode: (node: Node) => void;
  isRunning: boolean;
};

export const useWorkflowStore = create<WorkflowState>((set, get) => ({
  nodes: [],
  edges: [],
  isRunning: false,

  onNodesChange: (changes: NodeChange[]) => {
    set({
      nodes: applyNodeChanges(changes, get().nodes),
    });
  },

  onEdgesChange: (changes: EdgeChange[]) => {
    set({
      edges: applyEdgeChanges(changes, get().edges),
    });
  },

  onConnect: (connection: Connection) => {
    set({
      edges: addEdge(connection, get().edges),
    });
  },

  setNodes: (nodes) => set({ nodes }),
  setEdges: (edges) => set({ edges }),

  addNode: (node) => set({ nodes: [...get().nodes, node] }),

  loadWorkflow: async (projectId) => {
    try {
      const response = await axios.get(`/api/projects/${projectId}/workflow`);
      const { graphData } = response.data;
      if (graphData) {
        set({
          nodes: graphData.nodes || [],
          edges: graphData.edges || [],
        });
      }
    } catch (error) {
      console.warn('Workflow not found or error loading', error);
      set({ nodes: [], edges: [] });
    }
  },

  saveWorkflow: async (projectId) => {
    const { nodes, edges } = get();
    const graphData = { nodes, edges };
    await axios.post(`/api/projects/${projectId}/workflow`, graphData);
  },

  validateWorkflow: async (projectId) => {
    const { nodes, edges } = get();
    const graphData = { nodes, edges };
    try {
        const response = await axios.post(`/api/projects/${projectId}/workflow/validate`, graphData);
        if (response.data.valid) {
            return [];
        } else {
            return response.data.errors || ['Unknown validation error'];
        }
    } catch (error: any) {
        return [error.message || 'Validation failed'];
    }
  },

  runWorkflow: async (projectId) => {
    set({ isRunning: true });
    try {
        await get().saveWorkflow(projectId);
        await axios.post(`/api/projects/${projectId}/workflow/run`);
    } finally {
        set({ isRunning: false });
    }
  },
}));
