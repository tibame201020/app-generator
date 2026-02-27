import { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { User, Server, Database, CheckSquare } from 'lucide-react';

const AgentNode = ({ data, selected }: NodeProps) => {
  const getIcon = () => {
    switch (data.agentType) {
      case 'PM': return <User size={20} />;
      case 'SA': return <Server size={20} />;
      case 'PG': return <Database size={20} />;
      case 'QA': return <CheckSquare size={20} />;
      default: return <User size={20} />;
    }
  };

  const getColor = () => {
      switch (data.agentType) {
          case 'PM': return 'border-blue-500 bg-blue-900/50';
          case 'SA': return 'border-purple-500 bg-purple-900/50';
          case 'PG': return 'border-green-500 bg-green-900/50';
          case 'QA': return 'border-red-500 bg-red-900/50';
          default: return 'border-gray-500 bg-gray-900/50';
      }
  };

  return (
    <div className={`px-4 py-2 shadow-md rounded-md border-2 min-w-[150px] ${getColor()} ${selected ? 'ring-2 ring-white' : ''} transition-all`}>
      <div className="flex items-center">
        <div className="rounded-full w-8 h-8 flex justify-center items-center bg-gray-800 text-white mr-2">
          {getIcon()}
        </div>
        <div className="ml-2">
          <div className="text-lg font-bold text-white">{data.label}</div>
          <div className="text-gray-400 text-xs">{data.agentType} Agent</div>
        </div>
      </div>

      <Handle type="target" position={Position.Top} className="w-3 h-3 bg-gray-400" />
      <Handle type="source" position={Position.Bottom} className="w-3 h-3 bg-gray-400" />
    </div>
  );
};

export default memo(AgentNode);
