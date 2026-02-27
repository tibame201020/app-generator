import MainLayout from './components/Layout/MainLayout';
import { AgentProvider, useAgentContext } from './context/AgentContext';
import { useAgentStream } from './hooks/useAgentStream';

// Inner component to use the hook
const AgentConnectionManager = () => {
    // Hardcoded project ID for Phase 4 as per context (e.g., project 1)
    // In a real app, this would come from a URL param or user selection.
    useAgentStream(1);

    const { isConnected, messages } = useAgentContext();

    return (
        <div className="fixed bottom-4 right-4 z-50">
             <div className={`badge ${isConnected ? 'badge-success' : 'badge-error'} badge-lg shadow-lg`}>
                {isConnected ? 'Agent Stream Connected' : 'Disconnected'}
            </div>
            {isConnected && (
                <div className="text-xs text-right mt-1 text-base-content/50">
                    {messages.length} messages received
                </div>
            )}
        </div>
    );
};

function App() {
  return (
    <AgentProvider>
        <AgentConnectionManager />
        <MainLayout>
          <div className="flex flex-col items-center justify-center min-h-[50vh]">
            <h1 className="text-4xl font-bold mb-4">Jules Software Factory</h1>
            <p className="mb-4">Welcome to the autonomous software factory dashboard.</p>
            <button className="btn btn-primary">Test Connection</button>
          </div>
        </MainLayout>
    </AgentProvider>
  )
}

export default App
