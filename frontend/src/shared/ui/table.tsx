import type { HTMLAttributes, TdHTMLAttributes, ThHTMLAttributes } from 'react'

/**
 * A data table.
 *
 * The wrapper is a scroll region: on a narrow screen the table scrolls inside
 * its own box instead of pushing the page sideways. The region is focusable
 * and named, so a keyboard user can reach the scroll and a screen reader
 * announces what the region holds.
 */

export type TableProps = HTMLAttributes<HTMLDivElement> & {
  /** The name of the region, read by the assistive technology. */
  label: string
}

export function Table({ label, className, children, ...props }: TableProps) {
  return (
    <div
      role="region"
      aria-label={label}
      tabIndex={0}
      className={['w-full overflow-x-auto rounded-lg border border-border-subtle', className]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      <table className="w-full border-collapse text-left">{children}</table>
    </div>
  )
}

export function TableHead({ className, ...props }: HTMLAttributes<HTMLTableSectionElement>) {
  return <thead className={className} {...props} />
}

export function TableBody({ className, ...props }: HTMLAttributes<HTMLTableSectionElement>) {
  return <tbody className={className} {...props} />
}

export function TableRow({ className, ...props }: HTMLAttributes<HTMLTableRowElement>) {
  return <tr className={['border-t border-border-subtle', className].filter(Boolean).join(' ')} {...props} />
}

/** A column header: mono, uppercase, the chrome of the table. */
export function TableHeader({ className, scope = 'col', ...props }: ThHTMLAttributes<HTMLTableCellElement>) {
  return (
    <th
      scope={scope}
      className={[
        'px-space-md py-space-sm font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    />
  )
}

/** A cell: body copy, never below the 13px floor. */
export function TableCell({ className, ...props }: TdHTMLAttributes<HTMLTableCellElement>) {
  return (
    <td className={['px-space-md py-space-sm text-body-md text-text-primary', className].filter(Boolean).join(' ')} {...props} />
  )
}
