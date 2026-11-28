import { render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { OperationContent } from './page.tsx'
import { QueryProvider } from '../../../shared/api/query-provider.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'
import { createTranslator } from '../../../shared/i18n/server.ts'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const t = createTranslator('pt-BR')

const renderOperation = async (operationId: string) => {
  const content = await OperationContent({ operationId, locale: 'pt-BR', t })

  return render(
    <I18nProvider>
      <QueryProvider>{content}</QueryProvider>
    </I18nProvider>,
  )
}

describe('OperationContent', () => {
  it('shows the operation read during the render', async () => {
    await renderOperation('OP-1')

    expect(screen.getByText('Em andamento')).toBeInTheDocument()
    expect(screen.getByText('PRT-20261128-0001')).toBeInTheDocument()
    expect(screen.getByText('ConsigPort')).toBeInTheDocument()
    expect(screen.getByText('2 parcelas')).toBeInTheDocument()
  })

  it('says when the operation does not exist', async () => {
    await renderOperation('OPERATION-UNKNOWN')

    expect(screen.getByText('A operação não existe.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solicitar portabilidade' })).toHaveAttribute(
      'href',
      '/portabilidade',
    )
  })

  it('offers the retry when the query fails', async () => {
    server.use(
      http.get('http://localhost:8080/api/v1/portability-operations/:operationId', () =>
        HttpResponse.json(
          {
            code: 'INTERNAL_ERROR',
            message: 'Falha interna.',
            locale: 'pt-BR',
            timestamp: '2026-11-28T10:00:05Z',
            path: '/api/v1/portability-operations/OP-1',
            details: [],
          },
          { status: 500 },
        ),
      ),
    )

    await renderOperation('OP-1')

    expect(screen.getByText('Algo quebrou do nosso lado. Tente de novo.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Tentar de novo' })).toHaveAttribute(
      'href',
      '/operacoes?operationId=OP-1',
    )
  })

  it('asks the API in the language of the reader', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await OperationContent({ operationId: 'OP-1', locale: 'es', t })

    expect(acceptLanguage).toBe('es')
  })
})
