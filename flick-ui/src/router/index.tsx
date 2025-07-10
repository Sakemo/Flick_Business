import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Layout
import AppLayout from '../components/layout/AppLayout'

// Pages
import DashboardPage from '../pages/DashboardPage';
import ProdutosPage from '../pages/ProdutosPage';
import ClientesPage from '../pages/ClientesPage';
import VendasPage from '../pages/VendasPage';
import DespesasPage from '../pages/DespesasPage';
import ConfiguracoesPage from '../pages/ConfiguracoesPage';
import NotFoundPage from '../pages/NotFoundPage';
import LoginPage from '../pages/LoginPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    errorElement: <NotFoundPage />,
    children: [
      {
        index: true,
        element: <DashboardPage />
      },
      {
        path: 'produtos',
        element: <ProdutosPage />
      },
      {
        path: 'clientes',
        element: <ClientesPage />
      },
      {
        path: 'vendas',
        element: <VendasPage />
      },
      {
        path: 'despesas',
        element: <DespesasPage />
      },
      {
        path: 'configuracoes',
        element: <ConfiguracoesPage />
      }
    ]
  },
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '*',
    element: <NotFoundPage />
  }
]);

export function AppRouter(){
  return <RouterProvider router={router} />
}