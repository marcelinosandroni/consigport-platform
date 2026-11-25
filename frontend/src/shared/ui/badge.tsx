import type { HTMLAttributes } from 'react'

/**
 * A closed set of categories — state, type, tag.
 *
 * A badge never carries a value the reader compares against other values;
 * that is a number, not a badge.
 */

export type BadgeTone = 'neutral' | 'highlight'

const TONES: Record<BadgeTone, string> = {
  neutral: 'bg-surface-raised border border-border-subtle text-text-secondary',
  highlight: 'bg-primary/8 border-primary/40 text-primary',
}

export type BadgeProps = HTMLAttributes<HTMLSpanElement> & {
  tone?: BadgeTone
}

export function Badge({ tone = 'neutral', className, ...props }: BadgeProps) {
  return (
    <span
      className={[
        'inline-flex w-fit items-center rounded-sm border px-space-sm py-space-xs',
        'font-mono text-label-mono uppercase tracking-[0.06em]',
        TONES[tone],
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    />
  )
}
