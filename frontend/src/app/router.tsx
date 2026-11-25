import type { ReactNode } from 'react'

import { HomePage } from './pages/home.tsx'
import { NotFoundPage } from './pages/not-found.tsx'
import { SectionPage } from './pages/section.tsx'

/**
 * The route table.
 *
 * The RSC request handler owns the URL; this module only decides which page
 * renders it. Unknown paths render the 404 inside the same shell.
 */

type Route = {
  path: string
  render: () => ReactNode
}

const ROUTES: Route[] = [
  { path: '/', render: () => <HomePage /> },
  {
    path: '/portabilidade',
    render: () => (
      <SectionPage
        eyebrow="Operações"
        title="Portabilidade"
        description="A criação da operação, o acompanhamento do polling e o estado final entram na fatia de operações, com o contrato OpenAPI já publicado."
      />
    ),
  },
  {
    path: '/agenda',
    render: () => (
      <SectionPage
        eyebrow="Consultas"
        title="Agenda de descontos"
        description="A agenda de parcelas futuras do beneficiário, com elegibilidade visível por parcela, entra na fatia de agenda."
      />
    ),
  },
  {
    path: '/auditoria',
    render: () => (
      <SectionPage
        eyebrow="Auditoria"
        title="Trilha de eventos"
        description="A trilha append-only de cada operação, com status anterior e novo, entra na fatia de auditoria."
      />
    ),
  },
]

export const NOT_FOUND_PATH = '/404'

function normalize(pathname: string) {
  if (pathname.length > 1 && pathname.endsWith('/')) {
    return pathname.slice(0, -1)
  }
  return pathname
}

/** The page for a pathname, or the 404 page when the route does not exist. */
export function resolveRoute(pathname: string): ReactNode {
  const normalized = normalize(pathname)
  const route = ROUTES.find((candidate) => candidate.path === normalized)
  return route ? route.render() : <NotFoundPage />
}
