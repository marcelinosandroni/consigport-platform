import { render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { AuditContent } from './page.tsx'
import { createTranslator } from '../../../shared/i18n/server.ts'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const t = createTranslator('pt-BR')

const renderAudit = async (operationId: string) => {
  const content = await AuditContent({ operationId, locale: 'pt-BR', t })
  return render(content)
}

describe('AuditContent', () => {
  it('shows the trail of the operation', async () => {
    await renderAudit('OP-1')

    expect(screen.getByText('Operação criada')).toBeInTheDocument()
    expect(screen.getByText('Operação enviada')).toBeInTheDocument()
    expect(screen.getByText('Tentativa de consulta')).toBeInTheDocument()
    expect(screen.getByText('Operação confirmada')).toBeInTheDocument()
    expect(screen.getByText('Operação concluída')).toBeInTheDocument()
  })

  it('says when the operation has no events yet', async () => {
    await renderAudit('OPERATION-WITHOUT-EVENTS')

    expect(screen.getByText('A operação ainda não tem eventos.')).toBeInTheDocument()
  })

  it('says when the operation does not exist', async () => {
    await renderAudit('OPERATION-UNKNOWN')

    expect(screen.getByText('A operação não existe.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solicitar portabilidade' })).toHaveAttribute(
      'href',
      '/portabilidade',
    )
  })

  it('offers the retry when the query fails', async () => {
    server.use(
      http.get('http://localhost:8080/api/v1/portability-operations/:operationId/events', () =>
        HttpResponse.json(
          {
            code: 'INTERNAL_ERROR',
            message: 'Falha interna.',
            locale: 'pt-BR',
            timestamp: '2026-11-28T10:00:05Z',
            path: '/api/v1/portability-operations/OP-1/events',
            details: [],
          },
          { status: 500 },
        ),
      ),
    )

    await renderAudit('OP-1')

    expect(screen.getByText('Algo quebrou do nosso lado. Tente de novo.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Tentar de novo' })).toHaveAttribute(
      'href',
      '/auditoria?operationId=OP-1',
    )
  })

  it('asks the API in the language of the reader', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await AuditContent({ operationId: 'OP-1', locale: 'es', t })

    expect(acceptLanguage).toBe('es')
  })
})
