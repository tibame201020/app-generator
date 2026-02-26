import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProjects, importProject } from '../services/projectService';
import { Project } from '../types';
import { ImportProjectModal } from '../components/Project/ImportProjectModal';
import { Plus } from 'lucide-react';

const HomePage: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);

  useEffect(() => {
    // Assuming a hardcoded user ID for now
    const userId = '00000000-0000-0000-0000-000000000000';
    getProjects(userId)
      .then(setProjects)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleImport = async (name: string, url: string) => {
    // hardcoded user ID
    const userId = '00000000-0000-0000-0000-000000000000';
    try {
        const project = await importProject(userId, url, name);
        setProjects([...projects, project]);
    } catch (e) {
        throw e; // Modal handles error display
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white p-8">
      <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Your Projects</h1>
          <button
             onClick={() => setIsImportModalOpen(true)}
             className="px-4 py-2 bg-blue-600 rounded text-white hover:bg-blue-500 transition flex items-center gap-2"
           >
             <Plus size={20} />
             Import Project
           </button>
      </div>

      {loading && <div>Loading...</div>}
      {error && <div className="text-red-500">Error: {error}</div>}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {projects.map((project) => (
          <Link
            key={project.id}
            to={`/projects/${project.id}`}
            className="block p-4 bg-gray-800 rounded-lg hover:bg-gray-700 transition border border-gray-700 hover:border-blue-500 relative"
          >
            <h2 className="text-xl font-semibold">{project.name}</h2>
            <p className="text-gray-400 mt-2 text-sm">{project.description || 'No description'}</p>
            {project.importStatus && (
               <div className={`mt-2 text-xs font-mono px-2 py-0.5 rounded w-fit ${
                   project.importStatus === 'SUCCESS' ? 'bg-green-900 text-green-300' :
                   project.importStatus === 'FAILED' ? 'bg-red-900 text-red-300' :
                   'bg-yellow-900 text-yellow-300'
               }`}>
                  Import: {project.importStatus}
               </div>
            )}
            <div className="mt-4 text-xs text-gray-600 font-mono truncate">
              ID: {project.id}
            </div>
          </Link>
        ))}
      </div>

      {isImportModalOpen && (
        <ImportProjectModal
            onClose={() => setIsImportModalOpen(false)}
            onImport={handleImport}
        />
      )}
    </div>
  );
};

export default HomePage;
