import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Activity, Server, Database, Box } from 'lucide-react';

interface Metrics {
  'import.total'?: number;
  'import.success'?: number;
  'import.failed'?: number;
  'analysis.total'?: number;
  'analysis.success'?: number;
  'analysis.failed'?: number;
  'analysis.avg_duration_ms'?: number;
}

interface HealthStatus {
  status: string;
  db: string;
  docker: string;
}

export const SystemStatusPanel: React.FC = () => {
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [isOpen, setIsOpen] = useState(false);

  const fetchStatus = async () => {
    try {
      const res = await api.get<HealthStatus>('/ops/health');
      setHealth(res.data);
    } catch (e) {
      setHealth({ status: 'DOWN', db: 'UNKNOWN', docker: 'UNKNOWN' });
    }

    try {
      const res = await api.get<Metrics>('/ops/metrics');
      setMetrics(res.data);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    fetchStatus();
    const interval = setInterval(fetchStatus, 30000); // Poll every 30s
    return () => clearInterval(interval);
  }, []);

  const isHealthy = health?.status === 'UP';

  return (
    <div className="relative inline-block text-left">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`flex items-center space-x-2 px-3 py-1 text-xs font-medium rounded-md transition-colors ${
          isHealthy ? 'text-green-400 hover:bg-green-900/20' : 'text-red-400 hover:bg-red-900/20'
        }`}
      >
        <Activity size={14} />
        <span className="hidden sm:inline">System: {health?.status || 'CONNECTING...'}</span>
      </button>

      {isOpen && (
        <div className="origin-bottom-right absolute right-0 bottom-8 mt-2 w-64 rounded-md shadow-lg bg-gray-800 ring-1 ring-black ring-opacity-5 divide-y divide-gray-700 z-50">
          <div className="px-4 py-3">
            <p className="text-sm font-medium text-white flex justify-between">
              <span>Backend Status</span>
              <span className={isHealthy ? 'text-green-400' : 'text-red-400'}>{health?.status}</span>
            </p>
            <div className="mt-2 text-xs text-gray-400 space-y-1">
              <div className="flex justify-between">
                <span className="flex items-center gap-1"><Database size={12}/> DB Connection</span>
                <span className={health?.db === 'UP' ? 'text-green-400' : 'text-red-400'}>{health?.db || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                 <span className="flex items-center gap-1"><Box size={12}/> Docker Daemon</span>
                 <span className={health?.docker === 'UP' ? 'text-green-400' : 'text-red-400'}>{health?.docker || 'N/A'}</span>
              </div>
            </div>
          </div>

          <div className="px-4 py-3">
            <p className="text-sm font-medium text-white mb-2 flex items-center gap-2"><Server size={14}/> Operational Metrics</p>
            <div className="space-y-2 text-xs text-gray-300">
               <div className="flex justify-between">
                  <span>Projects Imported</span>
                  <span>{metrics?.['import.total'] || 0} <span className="text-gray-500">({metrics?.['import.success']} ok)</span></span>
               </div>
               <div className="flex justify-between">
                  <span>Analysis Runs</span>
                  <span>{metrics?.['analysis.total'] || 0} <span className="text-gray-500">({metrics?.['analysis.success']} ok)</span></span>
               </div>
               <div className="flex justify-between pt-1 border-t border-gray-700">
                  <span>Avg Analysis Time</span>
                  <span>{metrics?.['analysis.avg_duration_ms'] || 0} ms</span>
               </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
