import { render, screen, waitFor } from '@testing-library/react';
import { ProjectStatusCard } from '../ProjectStatusCard';
import * as projectService from '../../../services/projectService';
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../../services/projectService', async (importOriginal) => {
    const actual = await importOriginal<typeof projectService>();
    return {
        ...actual,
        getProjectPipelineStatus: vi.fn(),
        syncProject: vi.fn()
    };
});

describe('ProjectStatusCard', () => {
  const mockStatus = {
    projectId: '123',
    importStatus: 'SUCCESS',
    analysisStatus: 'SUCCESS',
    metadata: {
      fileCount: 10,
      classCount: 5,
      methodCount: 20,
      durationMs: 1000,
      lastAnalysisTime: '2023-01-01T12:00:00Z'
    },
    recentLogs: []
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (projectService.getProjectPipelineStatus as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(mockStatus);
  });

  it('renders status information', async () => {
    render(<ProjectStatusCard projectId="123" />);

    await waitFor(() => {
        expect(screen.getByText('Status Dashboard')).toBeInTheDocument();
        expect(screen.getByText('Import')).toBeInTheDocument();
        expect(screen.getByText('Analysis')).toBeInTheDocument();
    });
  });
});
