import React from 'react';
import { trackerData } from '../../mocks/trackerData';
import type { TaskStatus } from '../../types/tracker';

const StatusBadge: React.FC<{ status: TaskStatus }> = ({ status }) => {
  let badgeClass = 'badge-ghost';
  const label = status;

  switch (status) {
    case 'completed':
      badgeClass = 'badge-success';
      break;
    case 'in_progress':
      badgeClass = 'badge-warning';
      break;
    case 'pending':
      badgeClass = 'badge-ghost opacity-50';
      break;
  }

  return <span className={`badge badge-xs ${badgeClass} ml-2`}>{label}</span>;
};

const Sidebar: React.FC = () => {
  return (
    <ul className="menu bg-base-200 text-base-content min-h-full w-80 p-4">
      <li className="menu-title text-lg font-bold mb-2">Project Tracker</li>
      {trackerData.phases.map((phase) => (
        <li key={phase.phase_id}>
          <details open={phase.phase_id === trackerData.current_phase.split(':')[0].toLowerCase().replace(' ', '_')}>
            <summary className="font-semibold">{phase.name}</summary>
            <ul>
              {phase.tasks.map((task) => (
                <li key={task.id}>
                  <a className="flex justify-between items-center text-sm py-2">
                    <span className="truncate w-40" title={task.title}>{task.title}</span>
                    <StatusBadge status={task.status} />
                  </a>
                </li>
              ))}
            </ul>
          </details>
        </li>
      ))}
    </ul>
  );
};

export default Sidebar;
