import React from 'react';
import { SystemStatusPanel } from '../Status/SystemStatusPanel';

interface MainLayoutProps {
  sidebar?: React.ReactNode;
  children: React.ReactNode;
  statusBar?: React.ReactNode;
  topBar?: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ sidebar, children, statusBar, topBar }) => {
  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white overflow-hidden">
      {/* Top Bar (Optional, can be just a header or menu) */}
      <div className="h-10 bg-gray-800 border-b border-gray-700 flex items-center px-4 select-none justify-between">
         <div className="flex items-center w-full">
            <span className="font-semibold text-sm mr-6">App Generator</span>
            {topBar}
         </div>
      </div>

      {/* Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <div className="w-64 bg-gray-800 border-r border-gray-700 flex flex-col overflow-hidden">
          {sidebar}
        </div>

        {/* Editor Area */}
        <div className="flex-1 flex flex-col bg-gray-900 overflow-hidden relative">
          {children}
        </div>
      </div>

      {/* Status Bar */}
      <div className="h-6 bg-blue-600 flex items-center px-2 text-xs select-none text-white justify-between relative">
        <div className="flex-1 flex items-center overflow-hidden mr-4">
           {statusBar}
        </div>
        <div className="flex-shrink-0">
           <SystemStatusPanel />
        </div>
      </div>
    </div>
  );
};
