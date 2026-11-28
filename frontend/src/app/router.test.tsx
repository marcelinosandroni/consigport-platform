import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { resolveRoute } from './router.tsx'

describe('resolveRoute', () => {
  it('renders the home page for the root path', () => {
    render(resolveRoute('/'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Portabilidade de convênios sem fricção',
    )
  })

  it('renders a section page for a known path', () => {
    render(resolveRoute('/portabilidade'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Solicitar portabilidade')
  })

  it('ignores a trailing slash', () => {
    render(resolveRoute('/agenda/'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Agenda de descontos')
  })

  it('renders the not found page for an unknown path', () => {
    render(resolveRoute('/rota-inexistente'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Página não encontrada')
  })

  it('renders the page in the requested language', () => {
    render(resolveRoute('/', 'en'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Consignment portability without friction',
    )
  })

  it('renders the page in Spanish', () => {
    render(resolveRoute('/portabilidade', 'es'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Solicitar portabilidad')
  })
})
