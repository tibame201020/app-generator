import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ImportProjectModal } from './ImportProjectModal';

describe('ImportProjectModal', () => {
  it('renders correctly', () => {
    const onClose = vi.fn();
    const onImport = vi.fn();
    render(<ImportProjectModal onClose={onClose} onImport={onImport} />);
    expect(screen.getByText('Import Project')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('My Awesome Project')).toBeInTheDocument();
  });

  it('calls onImport when form submitted', async () => {
    const onClose = vi.fn();
    const onImport = vi.fn().mockResolvedValue(undefined);
    render(<ImportProjectModal onClose={onClose} onImport={onImport} />);

    fireEvent.change(screen.getByPlaceholderText('My Awesome Project'), {
      target: { value: 'Test Project' },
    });
    fireEvent.change(screen.getByPlaceholderText('https://github.com/username/repo.git'), {
      target: { value: 'http://example.com/repo.git' },
    });

    fireEvent.click(screen.getByText('Import Project'));

    await waitFor(() => {
        expect(onImport).toHaveBeenCalledWith('Test Project', 'http://example.com/repo.git');
        expect(onClose).toHaveBeenCalled();
    });
  });
});
