import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

const AppLayout: React.FC = () => {
  return (
    <div className="flex h-screen bg-bg-light dark:bg-bg-dark overflow-hidden">
      <Sidebar />

      <div className="flex=1 flex-col overflow-y-auto w-full">
        <main className="flex-1 p-6 md:p-8 lg:p-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
