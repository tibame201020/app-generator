import { render, screen, fireEvent } from '@testing-library/react';
import { ProjectToolbar } from '../ProjectToolbar';
import { useRuntimeStore } from '../../../stores/useRuntimeStore';
import { vi, describe, it, expect, beforeEach } from 'vitest';

// Mock Zustand store
vi.mock('../../../stores/useRuntimeStore');

describe('ProjectToolbar', () => {
  const mockStore = {
    status: 'STOPPED',
    previewUrl: null,
    isLoading: false,
    startPolling: vi.fn(),
    stopPolling: vi.fn(),
    runProject: vi.fn(),
    stopProject: vi.fn(),
    restartProject: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useRuntimeStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue(mockStore);
  });

  it('renders correctly', () => {
    render(
      <ProjectToolbar
        projectId="123"
        onTogglePreview={vi.fn()}
        isPreviewVisible={false}
      />
    );

    expect(screen.getByTitle('Run Project')).toBeInTheDocument();
    expect(screen.getByTitle('Stop Project')).toBeInTheDocument();
    expect(screen.getByTitle('Restart Project')).toBeInTheDocument();
    expect(screen.getByText('Stopped')).toBeInTheDocument();
  });

  it('calls startPolling on mount and stopPolling on unmount', () => {
    const { unmount } = render(
      <ProjectToolbar
        projectId="123"
        onTogglePreview={vi.fn()}
        isPreviewVisible={false}
      />
    );

    expect(mockStore.startPolling).toHaveBeenCalledWith('123');

    unmount();

    expect(mockStore.stopPolling).toHaveBeenCalled();
  });

  it('handles run click', () => {
    render(
      <ProjectToolbar
        projectId="123"
        onTogglePreview={vi.fn()}
        isPreviewVisible={false}
      />
    );

    fireEvent.click(screen.getByTitle('Run Project'));
    expect(mockStore.runProject).toHaveBeenCalledWith('123');
  });

  it('shows preview button when running', () => {
    (useRuntimeStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
      ...mockStore,
      status: 'RUNNING',
      previewUrl: 'http://test.com',
    });

    render(
      <ProjectToolbar
        projectId="123"
        onTogglePreview={vi.fn()}
        isPreviewVisible={false}
      />
    );

    expect(screen.getByText('Preview')).toBeInTheDocument();
  });
});
