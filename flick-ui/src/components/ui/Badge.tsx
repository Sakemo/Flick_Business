import React from 'react';
import clsx from 'clsx';

type BadgeColorScheme =
  | 'green'
  | 'red'
  | 'yellow'
  | 'blue'
  | 'purple'
  | 'gray'
  | 'primary'
  | 'accent';
type BadgeVariant = 'subtle' | 'solid' | 'outline';

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  colorScheme?: BadgeColorScheme;
  variant?: BadgeVariant;
  children: React.ReactNode;
}

const Badge: React.FC<BadgeProps> = ({
  children,
  colorScheme = 'gray',
  variant = 'subtle',
  className,
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold whitespace-nowrap';

  const variantStyles = {
    subtle: {
      gray: 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-200',
      green: 'bg-green-100 text-green-800',
      red: 'bg-red-100 text-red-800',
      yellow: 'bg-yellow-100 text-yellow-800',
      blue: 'bg-blue-100 text-blue-800',
      purple: 'bg-purple-100 text-purple-800',
      primary: 'bg-brand-muted text-brand-primary',
      accent: 'bg-yellow-100 text-yellow-800',
    },
    solid: {
      gray: 'bg-gray-500 text-white dark:bg-gray-600 dark:text-gray-100',
      green: 'bg-green-500 text-white',
      red: 'bg-red-600 text-white',
      yellow: 'bg-yellow-500 text-gray-900',
      blue: 'bg-blue-500 text-white',
      purple: 'bg-purple-500 text-white',
      primary: 'bg-brand-primary text-white',
      accent: 'bg-brand-accent text-gray-900',
    },
    outline: {
      gray: 'text-gray-700 dark:text-gray-200 ring-1 ring-inset ring-gray-500/40 dark:ring-gray-600',
      green:
        'text-green-800 dark:text-green-200 ring-1 ring-inset ring-green-600/40 dark:ring-green-700',
      red: 'text-red-800 dark:text-red-200 ring-1 ring-inset ring-red-600/40 dark:ring-red-700',
      yellow:
        'text-yellow-800 dark:text-yellow-200 ring-1 ring-inset ring-yellow-600/40 dark:ring-yellow-700',
      blue: 'text-blue-800 dark:text-yellow-200 ring-1 ring-inset ring-yellow-600/40 dark:ring-blue-700',
      purple:
        'text-purple-800 dark:text-purple-300 ring-1 ring-inset ring-brand-primary/60 dark:ring-purple-700',
      primary:
        'text-brand-primary dark:text-purple-300 ring-1 ring-inset ring-bran-primary/50 dark:ring-purple-700',
      accent:
        'text-yellow-800 dark:text-yello2-300 ring-1 ring-inset ring-brand-accent/50 dark:ring-yellow-700',
    },
  };

  return (
    <span
      className={clsx(
        baseStyles,
        variantStyles[variant]?.[colorScheme] ?? variantStyles.subtle.gray,
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
};

export default Badge;
