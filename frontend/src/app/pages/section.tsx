import type { ReactNode } from 'react'

/**
 * A section that exists in the navigation but whose feature arrives in a later
 * task. The shell still has to render it honestly, with the same primitives.
 */

export type SectionPageProps = {
  title: string
  description: string
  eyebrow: string
  children?: ReactNode
}

export function SectionPage({ title, description, eyebrow, children }: SectionPageProps) {
  return (
    <section className="flex flex-col gap-space-lg">
      <header className="flex flex-col gap-space-sm">
        <p className="font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
          {eyebrow}
        </p>
        <h1 className="text-headline-lg">{title}</h1>
        <p className="max-w-[64ch] text-body-lg text-text-secondary">{description}</p>
      </header>
      {children}
    </section>
  )
}
