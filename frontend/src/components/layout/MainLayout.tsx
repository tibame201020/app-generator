import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';

const MainLayout: React.FC = () => {
  return (
    <div className="drawer lg:drawer-open h-screen w-screen overflow-hidden">
      <input id="my-drawer-2" type="checkbox" className="drawer-toggle" />

      <div className="drawer-content flex flex-col h-full overflow-hidden">
        {/* Navbar */}
        <Navbar />

        {/* Page content */}
        <main className="flex-1 p-6 bg-base-100 overflow-y-auto">
           <Outlet />
        </main>

        <label htmlFor="my-drawer-2" className="btn btn-primary drawer-button lg:hidden fixed bottom-4 right-4 z-50 shadow-lg rounded-full">Menu</label>
      </div>

      <div className="drawer-side z-40">
        <label htmlFor="my-drawer-2" aria-label="close sidebar" className="drawer-overlay"></label>
        <div className="w-80 min-h-full bg-base-200 text-base-content h-full">
            <Sidebar />
        </div>
      </div>
    </div>
  );
};

export default MainLayout;
