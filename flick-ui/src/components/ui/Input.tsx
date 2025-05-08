import React, { InputHTMLAttributes, useId } from 'react';
import clsx from 'clsx';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  iconLeft?: React.ReactElement<{ className?: string }>;
  iconRight?: React.ReactElement<{ className?: string }>;
  wrapperClassName?: string;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      name,
      error,
      className,
      wrapperClassName,
      iconLeft,
      iconRight,
      type = 'text',
      ...props
    },
    ref
  ) => {
    const id = useId();
    const inputId = props.id || name || id;

    return (
      <div className={clsx('w-full', wrapperClassName)}>
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-text-secondary dark:text-gray-300 mb-1"
          >
            {label}
          </label>
        )}
        <div className="relative">
          {iconLeft && (
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-text-secondary/70 dark:text-gray-400">
              {React.cloneElement(iconLeft, {
                className: clsx(iconLeft.props.className, 'h-4 w-4'),
              })}
            </div>
          )}
          <input
            ref={ref}
            id={inputId}
            name={name}
            type={type}
            className={clsx(
              'block w-full rounded-btn border-gray-300 dark:border-gray-600 shadow-sm dark:bg-gray-700 dark:text-white sm:text-sm h-10',
              'focus:border-brand-primary focus:ring-brand-primary focus:ring-opacity-50 dark:focus:border-brand-accent dark:focus:ring-brand-accent',
              error
                ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
                : 'border-gray-300 dark:border-gray-600',
              iconLeft ? 'pl-10' : 'px-3',
              iconRight ? 'pr-10' : 'px-3',
              className
            )}
            {...props}
          />
          {iconRight && (
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-text-secondary/70 dark:text-gray-400">
              {React.cloneElement(iconRight, {
                className: clsx(iconRight.props.className, 'h-4 w-4'),
              })}
            </div>
          )}
        </div>
        {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
