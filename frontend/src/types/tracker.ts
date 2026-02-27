export type TaskStatus = 'completed' | 'in_progress' | 'pending';

export interface Task {
  id: string;
  phase: string;
  title: string;
  status: TaskStatus;
  depends_on: string[];
  spec_ref: string;
}

export interface Phase {
  phase_id: string;
  name: string;
  tasks: Task[];
}

export interface Tracker {
  project: string;
  current_phase: string;
  phases: Phase[];
}
