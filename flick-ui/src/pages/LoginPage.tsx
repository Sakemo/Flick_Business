import React from 'react';

const LoginPage: React.FC = () => {
  return (
    <div>
      <h1 className='text-2xl font-bold text-text-primary dark:text-white mb-6'>
      Login
      </h1>

      <p className='text-text-secondary dark:text-gray-400'>
      Faça login para acessar o sistema
      </p>
    </div>
  );
};

export default LoginPage;