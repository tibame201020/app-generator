import React, { useEffect, useState } from 'react';
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  Node,
  ReactFlowProvider,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { useWorkflowStore } from '../../stores/useWorkflowStore';
import AgentNode from './Nodes/AgentNode';
import { useParams } from 'react-router-dom';
import { Play, Save } from 'lucide-react';

const nodeTypes = {
  agent: AgentNode,
};

const WorkflowCanvasContent: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const {
    nodes,
    edges,
    onNodesChange,
    onEdgesChange,
    onConnect,
    loadWorkflow,
    saveWorkflow,
    runWorkflow,
    addNode,
    validateWorkflow
  } = useWorkflowStore();

  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    if (projectId) {
      loadWorkflow(projectId);
    }
  }, [projectId, loadWorkflow]);

  // Autosave
  useEffect(() => {
      const interval = setInterval(() => {
          if (projectId) {
              saveWorkflow(projectId);
          }
      }, 30000); // 30 seconds autosave
      return () => clearInterval(interval);
  }, [projectId, saveWorkflow]);

  const handleRun = async () => {
      if (!projectId) return;
      setIsRunning(true);

      // Implicit save before run
      await saveWorkflow(projectId);

      const errors = await validateWorkflow(projectId);
      if (errors && errors.length > 0) {
          setValidationErrors(errors);
          setIsRunning(false);
          return;
      }
      setValidationErrors([]);
      try {
          await runWorkflow(projectId);
      } catch (e) {
          console.error(e);
      } finally {
          setIsRunning(false);
      }
  };

  const handleAddNode = (type: string) => {
      const id = `${type}-${Date.now()}`;
      const newNode: Node = {
          id,
          type: 'agent',
          position: { x: Math.random() * 400 + 50, y: Math.random() * 400 + 50 },
          data: { label: `${type} Agent`, agentType: type },
      };
      addNode(newNode);
  };

  return (
    <div className="h-full w-full relative bg-gray-900">
      <div className="absolute top-4 left-4 z-10 flex space-x-2">
          <button onClick={() => handleAddNode('PM')} className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded shadow text-sm">Add PM</button>
          <button onClick={() => handleAddNode('SA')} className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded shadow text-sm">Add SA</button>
          <button onClick={() => handleAddNode('PG')} className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded shadow text-sm">Add PG</button>
          <button onClick={() => handleAddNode('QA')} className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded shadow text-sm">Add QA</button>
      </div>

      <div className="absolute top-4 right-4 z-10 flex space-x-2">
          <button onClick={() => projectId && saveWorkflow(projectId)} className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-1 rounded shadow flex items-center gap-1 text-sm">
              <Save size={14}/> Save
          </button>
          <button onClick={handleRun} disabled={isRunning} className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded shadow flex items-center gap-1 text-sm disabled:opacity-50">
              <Play size={14}/> {isRunning ? 'Running...' : 'Run Workflow'}
          </button>
      </div>

      {validationErrors.length > 0 && (
          <div className="absolute top-16 right-4 z-10 bg-red-900/90 text-white p-4 rounded shadow max-w-sm border border-red-500">
              <h4 className="font-bold mb-2">Validation Errors:</h4>
              <ul className="list-disc pl-4 text-sm">
                  {validationErrors.map((err, i) => <li key={i}>{err}</li>)}
              </ul>
              <button onClick={() => setValidationErrors([])} className="mt-2 text-xs text-gray-300 hover:text-white underline">Dismiss</button>
          </div>
      )}

      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        fitView
        className="bg-gray-900"
      >
        <Background color="#333" gap={16} />
        <Controls />
        <MiniMap nodeColor={(node) => {
            switch (node.data.agentType) {
                case 'PM': return '#1e40af';
                case 'SA': return '#6b21a8';
                case 'PG': return '#166534';
                case 'QA': return '#991b1b';
                default: return '#374151';
            }
        }} />
      </ReactFlow>
    </div>
  );
};

export const WorkflowCanvas = () => (
    <ReactFlowProvider>
        <WorkflowCanvasContent />
    </ReactFlowProvider>
);
