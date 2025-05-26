import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LuLayoutDashboard,
  LuShoppingCart,
  LuPackage,
  LuUsers,
  LuDollarSign,
  LuSettings,
  LuSun,
  LuMoon,
} from 'react-icons/lu';
import clsx from 'clsx';
import { useDarkMode } from '../../hooks/useDarkMode';

interface SidebarItem {
  path: string;
  name: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  end?: boolean;
}

const Sidebar: React.FC = () => {
  const [theme, toggleTheme] = useDarkMode();

  const navItems: SidebarItem[] = [
    { path: '/', name: 'Dashboard', icon: LuLayoutDashboard, end: true },
    { path: '/vendas', name: 'Vendas', icon: LuShoppingCart },
    { path: '/produtos', name: 'Produtos', icon: LuPackage },
    { path: '/clientes', name: 'Clientes', icon: LuUsers },
    { path: '/despesas', name: 'Despesas', icon: LuDollarSign },
  ];

  const settingsItem: SidebarItem = {
    path: '/configuracoes',
    name: 'Configurações',
    icon: LuSettings,
  };

  const linkClasses =
    'flex items-center px-4 py-3 text-sm font-medium text-text-secondary dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-btn transition-colors duration-200 group';
  const activeLinkClasses =
    'bg-brand-muted/30 dark:bg-gray-700/50 text-brand-primary dark:text-white font-semibold border-l-2 border-brand-primary';

  return (
    <aside>
      {/* Logo/Titulo */}
      <div className="w-64 flex-shrink-0 bg-card-light dark:bg-card-dark border-r border-gray-200 dark:border-gray-700/50 flex flex-col p-4 space-y-4">
        <h1 className="text-xl font-bold text-text-primary dark:text-white">
          Flick<span className="text-brand-primary">.</span>Business
        </h1>
      </div>

      {/* Links */}
      <nav className="flex-1 space-y-2">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => clsx(linkClasses, isActive && activeLinkClasses)}
            end={item.end}
          >
            <item.icon
              className="mr-3 h-5 w-5 flex-shrink-0 text-gray-400 dark:text-gray-500 group-hover:text-text-primary dark:group-hover:text-gray-300 transition-colors"
              aria-hidden="true"
            />
            {item.name}
          </NavLink>
        ))}
      </nav>

      {/** Rodapé da Sidebar */}
      <div className="mt-auto space-y-2 border-t border-gray-200 dark:border-gray-700/50 pt-4">
        <NavLink
          to={settingsItem.path}
          className={({ isActive }) => clsx(linkClasses, isActive && activeLinkClasses)}
        >
          <settingsItem.icon
            className="mr-3 h-5 w-5 flex-shrink-0 text-gray-400 dark:text-gray-500 group-hover:text-text-primary dark:group-hover:text-gray-300 transition-colors"
            aria-hidden="true"
          />
          {settingsItem.name}
        </NavLink>
      </div>
      <button onClick={toggleTheme} className={clsx(linkClasses, 'w-full justify-start')}>
        {theme === 'light' ? (
          <LuMoon className="mr-3 h-5 w-5 flex-shrink-0 text-gray-400 dark:text-gray-500 group-hover:text-text-primary dark:group-hover:text-gray-300 transition-colors" />
        ) : (
          <LuSun className="mr-3 h-5 flex-shrink-0 text-gray-400 dark:text-gray-500 group-hover:text-text-primary dark:group-hover:text-gray-300 transition-colors" />
        )}
        {theme === 'light' ? 'Modo Escuro' : 'Modo Claro'}
      </button>
      {/* TODO: Adicionar link/botão de Logout aqui no futuro */}
    </aside>
  );
};

export default Sidebar;
