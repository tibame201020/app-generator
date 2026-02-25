import { act, renderHook } from '@testing-library/react';
import { useWorkflowStore } from '../useWorkflowStore';
import axios from 'axios';
import { vi, describe, it, expect, beforeEach } from 'vitest';

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('useWorkflowStore', () => {
    beforeEach(() => {
        const { result } = renderHook(() => useWorkflowStore());
        act(() => {
            result.current.setNodes([]);
            result.current.setEdges([]);
        });
        vi.clearAllMocks();
    });

    it('should load workflow', async () => {
        const projectId = 'test-project';
        const mockData = {
            nodes: [{ id: '1', data: { label: 'Node 1' }, position: { x: 0, y: 0 } }],
            edges: []
        };
        (axios.get as any).mockResolvedValue({ data: { graphData: mockData } });

        const { result } = renderHook(() => useWorkflowStore());

        await act(async () => {
            await result.current.loadWorkflow(projectId);
        });

        expect(axios.get).toHaveBeenCalledWith(`/api/projects/${projectId}/workflow`);
        expect(result.current.nodes).toHaveLength(1);
        expect(result.current.nodes[0].id).toBe('1');
    });

    it('should save workflow', async () => {
        const projectId = 'test-project';
        const node = { id: '1', data: { label: 'Node 1' }, position: { x: 0, y: 0 } };

        const { result } = renderHook(() => useWorkflowStore());

        act(() => {
            result.current.setNodes([node]);
        });

        await act(async () => {
            await result.current.saveWorkflow(projectId);
        });

        expect(axios.post).toHaveBeenCalledWith(`/api/projects/${projectId}/workflow`, expect.objectContaining({
            nodes: [node],
            edges: []
        }));
    });

    it('should validate workflow', async () => {
        const projectId = 'test-project';
        (axios.post as any).mockResolvedValue({ data: { valid: false, errors: ['Error 1'] } });

        const { result } = renderHook(() => useWorkflowStore());

        let errors;
        await act(async () => {
            errors = await result.current.validateWorkflow(projectId);
        });

        expect(axios.post).toHaveBeenCalledWith(`/api/projects/${projectId}/workflow/validate`, expect.any(Object));
        expect(errors).toEqual(['Error 1']);
    });

    it('should run workflow', async () => {
        const projectId = 'test-project';
        (axios.post as any).mockResolvedValue({});

        const { result } = renderHook(() => useWorkflowStore());

        await act(async () => {
            await result.current.runWorkflow(projectId);
        });

        // Should save first
        expect(axios.post).toHaveBeenCalledWith(`/api/projects/${projectId}/workflow`, expect.any(Object));
        // Then run
        expect(axios.post).toHaveBeenCalledWith(`/api/projects/${projectId}/workflow/run`);
    });
});
