import type { ReactNode } from 'react'

import { HomePage } from './pages/home.tsx'
import { NotFoundPage } from './pages/not-found.tsx'
import { SectionPage } from './pages/section.tsx'
import { DEFAULT_LOCALE, type SupportedLocale } from '../shared/i18n/locales.ts'
import { createTranslator, type Translator } from '../shared/i18n/server.ts'

type Route = {
  path: string
  render: (t: Translator) => ReactNode
}

const ROUTES: Route[] = [
  { path: '/', render: (t) => <HomePage t={t} /> },
  {
    path: '/portabilidade',
    render: (t) => (
      <SectionPage
        eyebrow={t('portability.eyebrow')}
        title={t('portability.title')}
        description={t('portability.description')}
      />
    ),
  },
  {
    path: '/agenda',
    render: (t) => (
      <SectionPage
        eyebrow={t('agenda.eyebrow')}
        title={t('agenda.title')}
        description={t('agenda.description')}
      />
    ),
  },
  {
    path: '/auditoria',
    render: (t) => (
      <SectionPage
        eyebrow={t('audit.eyebrow')}
        title={t('audit.title')}
        description={t('audit.description')}
      />
    ),
  },
]

export const NOT_FOUND_PATH = '/404'

function normalize(pathname: string) {
  if (pathname.length > 1 && pathname.endsWith('/')) return pathname.slice(0, -1)
  return pathname
}

export function resolveRoute(pathname: string, locale: SupportedLocale = DEFAULT_LOCALE): ReactNode {
  const normalized = normalize(pathname)
  const route = ROUTES.find((candidate) => candidate.path === normalized)
  const t = createTranslator(locale)
  return route ? route.render(t) : <NotFoundPage t={t} />
}
