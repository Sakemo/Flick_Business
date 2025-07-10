import React from 'react';

const DashboardPage: React.FC = () => {
  return (
    <div>
      <h1 className='text-2xl font-bold text-text-primary dark:text-white mb-6'>
        Dashboard
      </h1>

      <p className='text-text-secondary dark:text-gray-400'>
        Visão geral do seu negócio
      </p>
    </div>
  );
};

export default DashboardPage