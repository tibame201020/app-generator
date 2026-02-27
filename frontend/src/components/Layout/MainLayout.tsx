import React from 'react';
import Navbar from './Navbar';
import Sidebar from './Sidebar';

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="drawer lg:drawer-open h-screen">
      <input id="my-drawer-2" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-full">
        {/* Navbar */}
        <Navbar />

        {/* Main Content Area */}
        <main className="flex-1 p-6 overflow-y-auto bg-base-100">
          <label htmlFor="my-drawer-2" className="btn btn-primary drawer-button lg:hidden mb-4">
            Open Tracker
          </label>
          {children}
        </main>
      </div>

      {/* Sidebar */}
      <div className="drawer-side h-full z-20">
        <label htmlFor="my-drawer-2" aria-label="close sidebar" className="drawer-overlay"></label>
        <Sidebar />
      </div>
    </div>
  );
};

export default MainLayout;
