import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProjects } from '../services/projectService';
import { Project } from '../types';

const HomePage: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Assuming a hardcoded user ID for now
    const userId = '00000000-0000-0000-0000-000000000000';
    getProjects(userId)
      .then(setProjects)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white p-8">
      <h1 className="text-3xl font-bold mb-6">Your Projects</h1>
      {loading && <div>Loading...</div>}
      {error && <div className="text-red-500">Error: {error}</div>}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {projects.map((project) => (
          <Link
            key={project.id}
            to={`/projects/${project.id}`}
            className="block p-4 bg-gray-800 rounded-lg hover:bg-gray-700 transition"
          >
            <h2 className="text-xl font-semibold">{project.name}</h2>
            <p className="text-gray-400 mt-2">{project.description || 'No description'}</p>
            <div className="mt-4 text-sm text-gray-500">
              ID: {project.id}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default HomePage;
