import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/useAuthStore';
import HomePage from './pages/HomePage';
import ProjectPage from './pages/ProjectPage';
import LoginPage from './pages/LoginPage';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { accessToken } = useAuthStore();

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

function App() {
  const { fetchMe, accessToken } = useAuthStore();
  const [init, setInit] = useState(false);

  useEffect(() => {
    if (accessToken) {
        fetchMe().finally(() => setInit(true));
    } else {
        setInit(true);
    }
  }, [accessToken, fetchMe]);

  if (!init) return <div className="flex h-screen items-center justify-center bg-gray-900 text-white">Loading...</div>;

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/projects/:projectId"
          element={
            <ProtectedRoute>
              <ProjectPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
