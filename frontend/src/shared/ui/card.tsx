import type { HTMLAttributes } from 'react'

/**
 * A Tier 1 surface: the base of cards, panels and data modules.
 */

export type CardProps = HTMLAttributes<HTMLDivElement>

export function Card({ className, ...props }: CardProps) {
  return (
    <div
      className={['rounded-lg border border-border-subtle bg-surface-raised', className]
        .filter(Boolean)
        .join(' ')}
      {...props}
    />
  )
}
