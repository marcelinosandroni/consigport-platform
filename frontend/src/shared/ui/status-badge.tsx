import type { HTMLAttributes } from 'react'

/**
 * A closed set of states, read at a glance.
 *
 * The status is a badge plus a dot: the dot carries the state for the reader
 * who scans, the word carries it for the reader who reads. The tone is a
 * property of the primitive — what a state *means* belongs to the feature
 * that owns it.
 */

export type StatusTone = 'neutral' | 'progress' | 'success' | 'technical' | 'error'

const TONES: Record<StatusTone, { readonly badge: string; readonly dot: string }> = {
  neutral: {
    badge: 'bg-surface-raised border border-border-subtle text-text-secondary',
    dot: 'bg-text-secondary',
  },
  progress: {
    badge: 'bg-primary/8 border-primary/40 text-primary',
    dot: 'bg-primary',
  },
  success: {
    badge: 'bg-secondary/8 border-secondary/40 text-secondary',
    dot: 'bg-secondary',
  },
  technical: {
    badge: 'bg-tertiary/8 border-tertiary/40 text-tertiary',
    dot: 'bg-tertiary',
  },
  error: {
    badge: 'bg-error/8 border-error/40 text-error',
    dot: 'bg-error',
  },
}

export type StatusBadgeProps = HTMLAttributes<HTMLSpanElement> & {
  tone?: StatusTone
  /** The dot is decoration: the word next to it already names the state. */
  withDot?: boolean
}

export function StatusBadge({
  tone = 'neutral',
  withDot = true,
  className,
  children,
  ...props
}: StatusBadgeProps) {
  const palette = TONES[tone]

  return (
    <span
      className={[
        'inline-flex w-fit items-center gap-space-xs rounded-sm border px-space-sm py-space-xs',
        'font-mono text-label-mono uppercase tracking-[0.06em]',
        palette.badge,
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      {withDot ? <span aria-hidden="true" className={['size-2 rounded-full', palette.dot].join(' ')} /> : null}
      {children}
    </span>
  )
}
