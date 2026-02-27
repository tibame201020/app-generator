import MainLayout from './components/Layout/MainLayout';

function App() {
  return (
    <MainLayout>
      <div className="flex flex-col items-center justify-center min-h-[50vh]">
        <h1 className="text-4xl font-bold mb-4">Jules Software Factory</h1>
        <p className="mb-4">Welcome to the autonomous software factory dashboard.</p>
        <button className="btn btn-primary">Test Connection</button>
      </div>
    </MainLayout>
  )
}

export default App
