import type { ButtonHTMLAttributes } from 'react'

/**
 * The page's one main action, plus the two quieter variants.
 *
 * Every colour comes from the Matrix tokens; no hex literal lives in a component.
 */

export type ButtonVariant = 'primary' | 'secondary' | 'ghost'

const VARIANTS: Record<ButtonVariant, string> = {
  primary: [
    'bg-primary text-on-primary font-semibold',
    'hover:brightness-110 hover:shadow-[0_0_16px_var(--kpi-accent-glow)]',
    'active:scale-[0.98]',
  ].join(' '),
  secondary: [
    'border border-border-subtle bg-transparent text-text-primary',
    'hover:border-text-secondary hover:bg-on-surface/5',
  ].join(' '),
  ghost: [
    'font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary',
    'hover:text-primary',
  ].join(' '),
}

/**
 * The classes of a button, so an anchor can wear the same surface as a button
 * without nesting one interactive element inside another.
 */
export function buttonClasses({
  variant = 'primary',
  className,
}: {
  variant?: ButtonVariant
  className?: string
} = {}) {
  return [
    'inline-flex min-h-11 items-center justify-center gap-space-sm rounded px-space-md',
    'transition-[background-color,border-color,box-shadow,transform] duration-150',
    VARIANTS[variant],
    className,
  ]
    .filter(Boolean)
    .join(' ')
}

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant
}

export function Button({ variant = 'primary', className, type = 'button', ...props }: ButtonProps) {
  return <button type={type} className={buttonClasses({ variant, className })} {...props} />
}
