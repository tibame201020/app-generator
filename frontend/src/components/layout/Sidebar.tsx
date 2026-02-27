import React from 'react';
import { mockTracker } from '../../data/mockTracker';
import { type Task, type Phase } from '../../types/tracker';

const Sidebar: React.FC = () => {

  const getStatusClass = (status: Task['status']) => {
      switch(status) {
          case 'completed': return 'text-success';
          case 'in_progress': return 'text-info font-bold';
          default: return 'text-base-content/70';
      }
  }

  return (
    <div className="flex flex-col h-full bg-base-200">
        <h2 className="text-xl font-bold p-4">Task Tracker</h2>

        <div className="flex-1 overflow-y-auto px-2">
          {mockTracker.phases.map((phase: Phase) => (
            <div key={phase.phase_id} className="collapse collapse-arrow bg-base-100 mb-2 rounded-box border border-base-300">
              <input type="checkbox" defaultChecked={mockTracker.current_phase.startsWith(phase.name)} />
              <div className="collapse-title text-sm font-medium">
                {phase.name}
              </div>
              <div className="collapse-content">
                <ul className="steps steps-vertical w-full">
                  {phase.tasks.map((task: Task) => (
                    <li
                        key={task.id}
                        data-content={task.status === 'completed' ? '✓' : (task.status === 'in_progress' ? '●' : '○')}
                        className={`step ${task.status === 'completed' ? 'step-success' : (task.status === 'in_progress' ? 'step-info' : '')}`}
                    >
                        <div className="text-left w-full pl-1">
                             <span className={`text-xs ${getStatusClass(task.status)} block`}>{task.title}</span>
                        </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>

        <div className="text-xs text-center text-base-content/50 p-4 border-t border-base-300">
            Project: {mockTracker.project}
        </div>
    </div>
  );
};

export default Sidebar;
