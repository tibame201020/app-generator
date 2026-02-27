import MainLayout from './components/Layout/MainLayout';
import { useAgentContext } from './context/AgentContext';

function App() {
  const { status, lastMessage, projectId } = useAgentContext();

  return (
    <MainLayout>
      <div className="flex flex-col items-center justify-center min-h-[50vh]">
        <h1 className="text-4xl font-bold mb-4">Jules Software Factory</h1>
        <p className="mb-4">Welcome to the autonomous software factory dashboard.</p>

        <div className="card w-96 bg-base-100 shadow-xl mt-4">
          <div className="card-body">
            <h2 className="card-title">Agent Connection</h2>
            <p>Project ID: {projectId}</p>
            <p>Status: <span className={`badge ${status === 'OPEN' ? 'badge-success' : 'badge-warning'}`}>{status}</span></p>
            {lastMessage && (
              <div className="mt-2">
                <p className="font-bold">Last Message:</p>
                <div className="mockup-code bg-base-300 text-xs p-2 mt-2">
                  <pre data-prefix=">"><code>{JSON.stringify(lastMessage, null, 2)}</code></pre>
                </div>
              </div>
            )}
          </div>
        </div>

        <button className="btn btn-primary mt-4">Test Connection</button>
      </div>
    </MainLayout>
  )
}

export default App
