import type { ReactNode } from 'react'

/**
 * The header of a page: the eyebrow, the title and the promise of the page.
 *
 * Every feature opens the same way — a mono eyebrow, the headline and one
 * sentence that says what the page is for — so the header is a primitive and
 * not a copy pasted from feature to feature.
 */

export type PageHeaderProps = {
  /** The mono line above the title: the area of the journey. */
  eyebrow: string
  title: string
  /** One sentence, at most a paragraph: what the page is for. */
  description?: string
  children?: ReactNode
}

export function PageHeader({ eyebrow, title, description, children }: PageHeaderProps) {
  return (
    <header className='flex flex-col gap-space-sm'>
      <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
        {eyebrow}
      </p>
      <h1 className='text-headline-lg'>{title}</h1>
      {description ? (
        <p className='max-w-[64ch] text-body-lg text-text-secondary'>{description}</p>
      ) : null}
      {children}
    </header>
  )
}
