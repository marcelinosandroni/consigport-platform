import type { HTMLAttributes } from 'react'

/**
 * One dominant number plus its context: a mono header, the number, and the
 * annotation that makes the number honest — unit, window and caveat.
 *
 * A bare number is not a stat card; render it as the metric alone.
 */

export type StatCardProps = HTMLAttributes<HTMLDivElement> & {
  label: string
  value: string
  annotation?: string
  status?: 'live' | 'idle'
}

export function StatCard({
  label,
  value,
  annotation,
  status = 'idle',
  className,
  ...props
}: StatCardProps) {
  return (
    <div
      className={[
        'rounded-lg border border-border-subtle bg-surface-raised p-space-md',
        'transition-shadow duration-150 hover:shadow-[0_0_16px_var(--kpi-accent-glow)]',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      <p className="flex items-center gap-space-sm font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
        {status === 'live' ? (
          <span aria-hidden="true" className="size-2 rounded-full bg-secondary" />
        ) : null}
        {label}
      </p>
      <p className="mt-space-sm text-metric-stat-mobile md:text-metric-stat">{value}</p>
      {annotation ? <p className="mt-space-xs text-body-sm text-text-muted">{annotation}</p> : null}
    </div>
  )
}
