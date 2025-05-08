import React from 'react';
import clsx from 'clsx';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
  children: React.ReactNode;
  padding?: 'sm' | 'md' | 'lg' | 'none';
  noBorder?: boolean;
}

const Card: React.FC<CardProps> = ({
  title,
  description,
  actions,
  children,
  className,
  padding = 'md',
  noBorder = false,
  ...props
}) => {
  const paddingClasses = {
    sm: 'p-3 md:p-4',
    md: 'p-card-padding',
    lg: 'p-6 md:p-8',
    none: 'p-0',
  };

  return (
    <div
      className={clsx(
        'bg-card-light dark:bg-card-dark rounded-card shadow-card',
        !noBorder && 'border border-gray-200 dark:border-gray-700/50',
        paddingClasses[padding],
        className
      )}
      {...props}
    >
      {(title || description || actions) && (
        <div
          className={clsx(
            'flex flex-col sm:flex-row sm:items-center sm:justify-between',
            padding !== 'none' && 'mb-4 pb-4 border-b border-gray-200 dark:border-gray-700/50'
          )}
        >
          <div>
            {title && (
              <h2 className="text-lg font-semibold text-text-primary dark:text-white">{title}</h2>
            )}
            {description && (
              <p className="mt-1 text-sm text-text-secondary dark:text-gray-400">{description}</p>
            )}
          </div>
          {actions && (
            <div className="mt-3 sm:mt-0 flex item-center space-x-2 flex-shrink-0">{actions}</div>
          )}
        </div>
      )}
      <div>{children}</div>
    </div>
  );
};
export default Card;
