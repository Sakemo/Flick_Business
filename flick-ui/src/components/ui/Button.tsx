import React from 'react';
import clsx from 'clsx';
import { LuLoader } from 'react-icons/lu';

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'link';
type ButtonSize = 'sm' | 'md' | 'lg' | 'icon';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  iconLeft?: React.ReactNode;
  iconRight?: React.ReactNode;
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      iconLeft,
      iconRight,
      className,
      disabled,
      asChild = false,
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'font-semibold rounded-btn focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-bg-dark transition-all duration-200 ease-in-out inline-flex items-center disabled:opacity-60 disabled:cursor-not-allowed whitespace-nowrap';

    const variantStyles = {
      primary:
        'bg-brand-primary text-white hover:bg-brand-primary/90 focus:ring-brand-primary focus:ring-opacity-50 shadow-soft hover:shadow-md',
      secondary:
        'bg-transparent border border-brand-muted dark:border-gray-600 text-text-primary dark:text-gray-200 hover:bg-brand-muted/30 dark:hover:bg-gray-700 focus:ring-brand-primary focus:ring-opacity-50',
      danger:
        'br-red-600 text-text-secondary hover:bg-red-700 hover:text-white focus:ring-red-500 focus:ring-opacity-50 shadow-soft hover:shadow-md',
      check:
        'br-green-600 text-text-secondary hover:bg-green-700 hover:text-white focus:ring-red-500 focus:ring-opacity-50 shadow-soft hover:shadow-md',
      ghost:
        'bg-transparent text-text-secondary dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700/50 focus:ring-brand-primary focus:ring-opacity-50',
      link: 'bg-transparent text-brand-primary dark:text-brand-accent hover:underline focus:ring-0 focus:outline-none focus:underline p-0 h-auto',
    };

    const darkVariantStyles = {
      primary:
        'dark:bg-brand-accent dark:text-bg-dark dark:hover:bg-brand-accent/90 dark:focus:ring-brand-accent',
    };

    const sizeStyles = {
      sm: 'px-3 py-1.5 text-xs h-8',
      md: 'px-5, py-2 text-sm h-10',
      lg: 'px-6 py-2.5 text-base h-12',
      icon: 'h-10 w-10 p-0',
    };

    const Comp = asChild ? 'span' : 'button';

    return (
      <Comp
        className={clsx(
          baseStyles,
          variantStyles[variant],
          darkVariantStyles[variant as keyof typeof darkVariantStyles],
          sizeStyles[size],
          { 'px-3': size === 'icon' },
          className
        )}
        disabled={disabled || isLoading}
        ref={ref}
        {...props}
      >
        {isLoading && (
          <LuLoader
            className={clsx(
              'animate-spin',
              children ? 'mr-2' : '',
              size === 'sm' ? 'h-3 w-3' : 'h-4 w-4'
            )}
          />
        )}
        {!isLoading && iconLeft && <span className={clsx(children ? 'ml-2' : '')}>{iconLeft}</span>}
        {!asChild ? children : null}
        {!isLoading && iconRight && (
          <span className={clsx(children ? 'ml-2' : '')}>{iconRight}</span>
        )}
      </Comp>
    );
  }
);
Button.displayName = 'Button';
export default Button;
