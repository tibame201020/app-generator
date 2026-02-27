import MainLayout from './components/Layout/MainLayout';
import { useAgentContext } from './context/AgentContext';
import ChatMessage from './components/ChatMessage';
import { AgentRole } from './types/agent';

function App() {
  const { status, lastMessage, projectId } = useAgentContext();

  const demoMessages = [
    {
      role: AgentRole.USER,
      message: "Please design a login page for our app.",
      timestamp: "10:00:00"
    },
    {
      role: AgentRole.PM,
      message: "**Requirement Analysis**:\nWe need a secure login page with:\n- Email/Password\n- Google OAuth\n- Forgot Password link",
      timestamp: "10:00:05"
    },
    {
      role: AgentRole.UIUX,
      message: "Here is the layout plan:\n\n1. Centered Card\n2. Hero Image on left\n3. Form on right",
      timestamp: "10:00:15"
    },
    {
      role: AgentRole.PG,
      message: "I will implement the form using React Hook Form.\n\n```tsx\nconst LoginForm = () => {\n  const { register, handleSubmit } = useForm();\n  return (\n    <form onSubmit={handleSubmit(onSubmit)}>\n      <input {...register('email')} />\n    </form>\n  );\n}\n```",
      timestamp: "10:00:30"
    }
  ];

  return (
    <MainLayout>
      <div className="flex flex-col h-full">
        {/* Connection Status Header */}
        <div className="p-4 bg-base-200 shadow-sm flex justify-between items-center">
            <div>
                <h1 className="text-2xl font-bold">Jules Software Factory</h1>
                <p className="text-sm opacity-70">Project ID: {projectId}</p>
            </div>
            <div className="flex items-center gap-2">
                <span className="text-sm font-semibold">Status:</span>
                <span className={`badge ${status === 'OPEN' ? 'badge-success' : 'badge-warning'}`}>{status}</span>
            </div>
        </div>

        {/* Chat Area - Scrollable */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-base-100">
            {/* Real-time Last Message (if any) */}
            {lastMessage && (
               <div className="alert alert-info shadow-lg mb-4">
                  <div>
                    <h3 className="font-bold">New Incoming Message!</h3>
                    <div className="text-xs">{JSON.stringify(lastMessage)}</div>
                  </div>
               </div>
            )}

            {/* Demo Chat Flow */}
            <div className="divider">Demo Conversation</div>

            {demoMessages.map((msg, idx) => (
                <ChatMessage
                    key={idx}
                    role={msg.role}
                    message={msg.message}
                    timestamp={msg.timestamp}
                />
            ))}
        </div>

        {/* Input Area (Placeholder) */}
        <div className="p-4 bg-base-200">
            <div className="join w-full">
                <input className="input input-bordered join-item w-full" placeholder="Type your message..." />
                <button className="btn btn-primary join-item">Send</button>
            </div>
        </div>
      </div>
    </MainLayout>
  )
}

export default App
