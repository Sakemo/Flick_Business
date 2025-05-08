import React, { SelectHTMLAttributes, useId } from 'react';
import clsx from 'clsx';
import { LuChevronDown } from 'react-icons/lu';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  wrapperClassName?: string;
}

const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, name, error, className, wrapperClassName, children, ...props }, ref) => {
    const id = useId();
    const selectId = props.id || name || id;

    return (
      <div className={clsx('w-full', wrapperClassName)}>
        {label && (
          <label
            htmlFor={selectId}
            className="block text-sm font-medium text-text-secondary dark:text-gray-300 mb-1"
          >
            {label}
          </label>
        )}
        <div className="relative">
          <select
            ref={ref}
            name={name}
            id={selectId}
            className={clsx(
              'block w-full appearance-none rounded-btn border-gray-300 dark:border-gray-600 shadow-sm dark:bg-gray-700 dark:text-white sm:text-sm h-10 pl-3 pr-10',
              'focus:border-brand-primary focus:ring-brand-primary focus:ring-opacity-50 dark:focus:border-brand-accent dark:focus:ring-brand-accent',
              error
                ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
                : 'border-gray-300 dark:border-gray-600',
              className
            )}
            {...props}
          >
            {children}
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700 dark:text-gray-400">
            <LuChevronDown className="h-4 w-4" />
          </div>
        </div>
        {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
      </div>
    );
  }
);

Select.displayName = 'Select';
export default Select;
