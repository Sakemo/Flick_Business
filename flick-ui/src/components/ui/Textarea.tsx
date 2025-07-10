import React, { TextareaHTMLAttributes, useId } from 'react';
import clsx from 'clsx';

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  wrapperClassName?: string;
}

const Textarea = React.forwardRef<HTMLTextAreaElement, TextAreaProps>(
  ({ label, name, error, className, wrapperClassName, ...props }, ref) => {
    const id = useId();
    const textareaId = props.id || name || id;

    return (
      <div className={clsx('w-full', wrapperClassName)}>
        {label && (
          <label
            htmlFor={textareaId}
            className="bloc text-sm font-medium text-text-secondary dark:text-gray-300 mb-1"
          >
            {label}
          </label>
        )}
        <textarea
          ref={ref}
          id={textareaId}
          name={name}
          className={clsx(
            'block w-full rounded-btn border-gray-300 dark:border-gray-600 shadow-sm dark:bg-gray-700 dark:text-white sm:text-sm p-3',
            'focus:border-brand-primary focus:ring-brand-primary focus:ring-opacity-50 dark:focus:border-brand-accent dark:focus:ring-brand-accent',
            error
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
              : 'border-gray-300 dark:border-gray-600 shadow-sm dark:bg-gray-700 dark:text-white sm:text-sm p-3',
            'focus:border-brand-primary focus:ring-brand-primary focus:ring-opacity-50 dark:focus:border-brand-accent dark:focus:ring-brand-accent',
            error
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
              : 'border-gray-300 dark:border-gray-600',
            className
          )}
          rows={4}
          {...props}
        />
        {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
      </div>
    );
  }
);
Textarea.displayName = 'Textarea';
export default Textarea;
