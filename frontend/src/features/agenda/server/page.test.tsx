import { render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { AgendaContent } from './page.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'
import { createTranslator } from '../../../shared/i18n/server.ts'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const t = createTranslator('pt-BR')

const renderAgenda = async (beneficiaryId: string) => {
  const content = await AgendaContent({ beneficiaryId, locale: 'pt-BR', t })
  return render(<I18nProvider>{content}</I18nProvider>)
}

describe('AgendaContent', () => {
  it('shows the installments of the beneficiary', async () => {
    await renderAgenda('BEN-001')

    expect(screen.getByText('PAR-001')).toBeInTheDocument()
    expect(screen.getByText('PAR-002')).toBeInTheDocument()
  })

  it('shows the empty state when the beneficiary has no installment', async () => {
    await renderAgenda('BEN-EMPTY')

    expect(screen.getByText('Nenhuma parcela elegível na agenda.')).toBeInTheDocument()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
  })

  it('asks for the consent when the API refuses the agenda', async () => {
    await renderAgenda('BEN-FORBIDDEN')

    expect(screen.getByText('Autorize o convênio para continuar.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Autorizar convênio' })).toHaveAttribute(
      'href',
      '/autorizacao',
    )
  })

  it('says when the Central does not know the beneficiary', async () => {
    await renderAgenda('BEN-UNKNOWN')

    expect(screen.getByText('A Central não conhece este beneficiário.')).toBeInTheDocument()
  })

  it('offers the retry when the query fails', async () => {
    server.use(
      http.get('http://localhost:8080/api/v1/beneficiaries/:beneficiaryId/agenda', () =>
        HttpResponse.json(
          {
            code: 'INTERNAL_ERROR',
            message: 'Falha interna.',
            locale: 'pt-BR',
            timestamp: '2026-11-28T10:00:00Z',
            path: '/api/v1/beneficiaries/BEN-001/agenda',
            details: [],
          },
          { status: 500 },
        ),
      ),
    )

    await renderAgenda('BEN-001')

    expect(screen.getByText('Algo quebrou do nosso lado. Tente de novo.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Tentar de novo' })).toHaveAttribute(
      'href',
      '/agenda?beneficiaryId=BEN-001',
    )
  })

  it('asks the API in the language of the reader', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await AgendaContent({ beneficiaryId: 'BEN-001', locale: 'es', t })

    expect(acceptLanguage).toBe('es')
  })
})
