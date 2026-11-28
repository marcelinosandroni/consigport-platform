import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { resolveRoute } from './router.tsx'

const route = (path: string) => new URL(path, 'http://localhost')

describe('resolveRoute', () => {
  it('renders the home page for the root path', () => {
    render(resolveRoute(route('/')))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Portabilidade de convênios sem fricção',
    )
  })

  it('renders a section page for a known path', () => {
    render(resolveRoute(route('/portabilidade')))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Solicitar portabilidade')
  })

  it('ignores a trailing slash', () => {
    render(resolveRoute(route('/agenda/')))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Agenda de descontos')
  })

  it('asks for the beneficiary when the agenda has none', () => {
    render(resolveRoute(route('/agenda')))

    expect(screen.getByText('Informe o beneficiário para ver a agenda.')).toBeInTheDocument()
  })

  it('asks for the operation when the panel has none', () => {
    render(resolveRoute(route('/operacoes')))

    expect(screen.getByText('Informe a operação para acompanhar o status.')).toBeInTheDocument()
  })

  it('asks for the operation when the audit trail has none', () => {
    render(resolveRoute(route('/auditoria')))

    expect(screen.getByText('Informe a operação para ver a trilha de auditoria.')).toBeInTheDocument()
  })

  it('renders the not found page for an unknown path', () => {
    render(resolveRoute(route('/rota-inexistente')))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Página não encontrada')
  })

  it('renders the page in the requested language', () => {
    render(resolveRoute(route('/'), 'en'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Consignment portability without friction',
    )
  })

  it('renders the page in Spanish', () => {
    render(resolveRoute(route('/portabilidade'), 'es'))

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Solicitar portabilidad')
  })
})
