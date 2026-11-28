import type { ReactNode } from 'react'

import { HomePage } from './pages/home.tsx'
import { NotFoundPage } from './pages/not-found.tsx'
import { SectionPage } from './pages/section.tsx'
import { AgendaPage, AgendaWithoutBeneficiary } from '../features/agenda/server/page.tsx'
import { AuthorizationPage } from '../features/authorization/server/page.tsx'
import { DEFAULT_LOCALE, type SupportedLocale } from '../shared/i18n/locales.ts'
import { createTranslator, type Translator } from '../shared/i18n/server.ts'

/**
 * The route table.
 *
 * The router is the only place that knows which screen answers which address.
 * The features own their pages; the router only hands them the language of
 * the reader and what the URL carries.
 */

type RouteContext = {
  t: Translator
  locale: SupportedLocale
  searchParams: URLSearchParams
}

type Route = {
  path: string
  render: (context: RouteContext) => ReactNode
}

const ROUTES: Route[] = [
  { path: '/', render: ({ t }) => <HomePage t={t} /> },
  {
    path: '/autorizacao',
    render: ({ t }) => <AuthorizationPage t={t} />,
  },
  {
    path: '/portabilidade',
    render: ({ t }) => (
      <SectionPage
        eyebrow={t('portability.eyebrow')}
        title={t('portability.title')}
        description={t('portability.description')}
      />
    ),
  },
  {
    path: '/agenda',
    render: ({ t, locale, searchParams }) => {
      const beneficiaryId = searchParams.get('beneficiaryId')?.trim() ?? ''

      return beneficiaryId.length > 0 ? (
        <AgendaPage beneficiaryId={beneficiaryId} locale={locale} t={t} />
      ) : (
        <AgendaWithoutBeneficiary t={t} />
      )
    },
  },
  {
    path: '/auditoria',
    render: ({ t }) => (
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

/**
 * Resolves the screen of a request.
 *
 * The whole URL is the input: the path chooses the screen, the query carries
 * what the screen needs — the beneficiary of the agenda, for instance.
 */
export function resolveRoute(url: URL, locale: SupportedLocale = DEFAULT_LOCALE): ReactNode {
  const normalized = normalize(url.pathname)
  const route = ROUTES.find((candidate) => candidate.path === normalized)
  const t = createTranslator(locale)

  if (route === undefined) {
    return <NotFoundPage t={t} />
  }

  return route.render({ t, locale, searchParams: url.searchParams })
}
