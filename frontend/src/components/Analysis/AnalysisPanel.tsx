import React, { useEffect, useState } from 'react';
import { ClassDTO, AnalysisResultDTO } from '../../types';
import { getAnalysis, triggerAnalysis } from '../../services/projectService';
import { RefreshCw, Package, FileCode, Box, Layers } from 'lucide-react';
import { ProjectStatusCard } from '../Status/ProjectStatusCard';

interface AnalysisPanelProps {
  projectId: string;
}

export const AnalysisPanel: React.FC<AnalysisPanelProps> = ({ projectId }) => {
  const [analysis, setAnalysis] = useState<AnalysisResultDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedClass, setSelectedClass] = useState<ClassDTO | null>(null);

  const fetchAnalysis = async () => {
    setLoading(true);
    try {
      const data = await getAnalysis(projectId);
      setAnalysis(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalysis();
  }, [projectId]);

  const handleReAnalyze = async () => {
    setLoading(true);
    try {
      await triggerAnalysis(projectId);
      // Poll or wait? For now just fetch after a delay or optimistically wait.
      // Analysis is async in backend.
      // We should probably poll. But for MVP, let's just wait 2s then fetch.
      setTimeout(fetchAnalysis, 2000);
    } catch (e) {
      console.error(e);
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full bg-gray-900 text-white">
      <div className="px-4 pt-4">
        <ProjectStatusCard projectId={projectId} />
      </div>
      <div className="flex flex-1 overflow-hidden">
      {/* Sidebar: Packages & Classes */}
      <div className="w-1/3 border-r border-gray-700 flex flex-col">
        <div className="p-4 border-b border-gray-700 flex justify-between items-center bg-gray-800">
          <h2 className="font-semibold flex items-center gap-2">
            <Layers size={18} />
            Structure
          </h2>
          <button
            onClick={handleReAnalyze}
            className="p-1.5 hover:bg-gray-700 rounded text-gray-400 hover:text-white transition"
            title="Re-analyze"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
        <div className="flex-1 overflow-auto p-2">
          {analysis?.packages.map((pkg) => (
            <div key={pkg.name} className="mb-4">
              <div className="flex items-center gap-2 text-sm font-medium text-blue-400 mb-1 px-2">
                <Package size={14} />
                {pkg.name}
              </div>
              <div className="pl-4 space-y-0.5">
                {pkg.classes.map((cls) => (
                  <div
                    key={cls.name}
                    onClick={() => setSelectedClass(cls)}
                    className={`flex items-center gap-2 px-2 py-1.5 rounded cursor-pointer text-sm transition ${
                      selectedClass === cls
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-300 hover:bg-gray-800'
                    }`}
                  >
                    <FileCode size={14} />
                    {cls.name}
                  </div>
                ))}
              </div>
            </div>
          ))}
          {!loading && !analysis && (
             <div className="text-center text-gray-500 mt-10">No analysis data found.</div>
          )}
        </div>
      </div>

      {/* Main Content: Class Details */}
      <div className="flex-1 overflow-auto bg-gray-900 p-6">
        {selectedClass ? (
          <div className="max-w-3xl mx-auto">
            <div className="mb-6 border-b border-gray-700 pb-4">
              <h1 className="text-2xl font-bold flex items-center gap-3">
                <FileCode className="text-blue-500" />
                {selectedClass.name}
              </h1>
              <div className="flex gap-2 mt-2">
                 <span className="px-2 py-0.5 rounded bg-gray-800 text-xs text-gray-400 border border-gray-700">
                    {selectedClass.type}
                 </span>
                 {selectedClass.modifiers.map(m => (
                    <span key={m} className="px-2 py-0.5 rounded bg-gray-800 text-xs text-blue-300 border border-gray-700">
                        {m}
                    </span>
                 ))}
              </div>
            </div>

            {/* Fields */}
            <div className="mb-8">
                <h3 className="text-lg font-semibold mb-3 flex items-center gap-2 text-gray-200">
                    <Box size={18} /> Fields
                </h3>
                <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
                    {selectedClass.fields.length > 0 ? (
                        <table className="w-full text-sm text-left">
                            <thead className="bg-gray-900/50 text-gray-400 border-b border-gray-700">
                                <tr>
                                    <th className="px-4 py-2 font-medium">Name</th>
                                    <th className="px-4 py-2 font-medium">Type</th>
                                    <th className="px-4 py-2 font-medium">Modifiers</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-700">
                                {selectedClass.fields.map((field) => (
                                    <tr key={field.name} className="hover:bg-gray-700/50">
                                        <td className="px-4 py-2 font-mono text-blue-300">{field.name}</td>
                                        <td className="px-4 py-2 font-mono text-yellow-300">{field.type}</td>
                                        <td className="px-4 py-2 text-gray-400">{field.modifiers.join(', ')}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="p-4 text-gray-500 italic">No fields</div>
                    )}
                </div>
            </div>

            {/* Methods */}
            <div>
                <h3 className="text-lg font-semibold mb-3 flex items-center gap-2 text-gray-200">
                    <Layers size={18} /> Methods
                </h3>
                <div className="space-y-3">
                    {selectedClass.methods.map((method) => (
                        <div key={method.name + method.parameters.join('')} className="bg-gray-800 rounded-lg border border-gray-700 p-3 hover:border-gray-600 transition">
                            <div className="flex items-baseline gap-2 mb-1">
                                <span className="font-mono text-green-400 font-bold">{method.name}</span>
                                <span className="text-gray-500 text-sm">({method.parameters.join(', ')})</span>
                                <span className="text-gray-400 text-sm ml-auto font-mono">
                                    : <span className="text-yellow-300">{method.returnType}</span>
                                </span>
                            </div>
                            <div className="flex gap-2 mt-1">
                                {method.modifiers.map(m => (
                                    <span key={m} className="text-xs text-gray-500 uppercase tracking-wider">{m}</span>
                                ))}
                            </div>
                        </div>
                    ))}
                    {selectedClass.methods.length === 0 && (
                        <div className="p-4 bg-gray-800 rounded border border-gray-700 text-gray-500 italic">No methods</div>
                    )}
                </div>
            </div>

          </div>
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-gray-500">
            <Layers size={48} className="mb-4 opacity-50" />
            <p>Select a class to view details</p>
          </div>
        )}
      </div>
      </div>
    </div>
  );
};
