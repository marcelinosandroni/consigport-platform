import type { ReactNode } from 'react'

import { PageHeader } from '../../shared/ui/page-header.tsx'

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
    <section className='flex flex-col gap-space-lg'>
      <PageHeader eyebrow={eyebrow} title={title} description={description} />
      {children}
    </section>
  )
}
