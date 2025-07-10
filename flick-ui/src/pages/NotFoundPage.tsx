import React from 'react';

const NotFoundPage: React.FC = () => {
  return (
    <div>
      <h1 className='text-2xl font-bold text-text-primary dark:text-white mb-6'>
        Erro 404 - Página não encontrada
      </h1>

      <p className='text-text-secondary dark:text-gray-400'>
        Ops! A página que você está procurando não existe.
      </p>

      <a href='/' className='text-primary hover:underline'>
        Voltar ao Dashboard.
      </a>
    </div>
  );
};

export default NotFoundPage;