import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useRuntimeStore } from '../useRuntimeStore';
import * as api from '../../services/projectService';

// Mock the API
vi.mock('../../services/projectService', () => ({
  runProject: vi.fn(),
  stopProject: vi.fn(),
  restartProject: vi.fn(),
  getProjectStatus: vi.fn(),
}));

describe('useRuntimeStore', () => {
  const projectId = 'test-project';

  beforeEach(() => {
    useRuntimeStore.setState({
      status: 'STOPPED',
      previewUrl: null,
      internalIp: null,
      isLoading: false,
      error: null,
      isPolling: false,
    });
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should fetch status and update state', async () => {
    (api.getProjectStatus as any).mockResolvedValue({
      status: 'RUNNING',
      previewUrl: 'http://test.com',
      internalIp: '127.0.0.1',
    });

    await useRuntimeStore.getState().fetchStatus(projectId);

    expect(useRuntimeStore.getState().status).toBe('RUNNING');
    expect(useRuntimeStore.getState().previewUrl).toBe('http://test.com');
  });

  it('should run project and start polling', async () => {
    (api.runProject as any).mockResolvedValue({});
    (api.getProjectStatus as any).mockResolvedValue({ status: 'STARTING' });

    await useRuntimeStore.getState().runProject(projectId);

    expect(api.runProject).toHaveBeenCalledWith(projectId);
    expect(useRuntimeStore.getState().isPolling).toBe(true);
    // Polling logic calls fetchStatus immediately
    expect(api.getProjectStatus).toHaveBeenCalledWith(projectId);
  });

  it('should handle errors', async () => {
    (api.runProject as any).mockRejectedValue(new Error('Failed'));

    await useRuntimeStore.getState().runProject(projectId);

    expect(useRuntimeStore.getState().error).toBe('Failed');
    expect(useRuntimeStore.getState().isLoading).toBe(false);
  });

  it('should stop polling', () => {
    useRuntimeStore.getState().startPolling(projectId);
    expect(useRuntimeStore.getState().isPolling).toBe(true);

    useRuntimeStore.getState().stopPolling();
    expect(useRuntimeStore.getState().isPolling).toBe(false);
  });
});
