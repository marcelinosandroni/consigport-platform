import { act, render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'

import { OperationStatus } from './operation-status.tsx'
import { QueryProvider } from '../../../shared/api/query-provider.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const URL_OPERATION = 'http://localhost:8080/api/v1/portability-operations/:operationId'

/** The real timer, captured before the fake one takes the place of it. */
const realSetTimeout = globalThis.setTimeout

const detail = (status: string, extra: Record<string, unknown> = {}) => ({
  operationId: 'OP-1',
  protocol: 'PRT-20261128-0001',
  beneficiaryId: 'BEN-001',
  agreementId: 'AGR-001',
  status,
  totalAmount: 750,
  installments: [
    { installmentId: 'PAR-001', agreementId: 'AGR-001', amount: 450, discountDate: '2026-12-05' },
    { installmentId: 'PAR-002', agreementId: 'AGR-001', amount: 300, discountDate: '2027-01-05' },
  ],
  assigneeCreditor: 'ConsigPort',
  receivingAccount: { bank: '001', branch: '1234', account: '56789-0' },
  rejectionReason: null,
  retryCount: 0,
  accountUpdatedAt: null,
  createdAt: '2026-11-28T10:00:05Z',
  updatedAt: '2026-11-28T10:00:05Z',
  ...extra,
})

const renderPanel = () =>
  render(
    <I18nProvider>
      <QueryProvider>
        <OperationStatus operationId='OP-1' />
      </QueryProvider>
    </I18nProvider>,
  )

/** Moves the clock of the test forward. */
const advance = async (milliseconds: number) => {
  await act(async () => {
    await vi.advanceTimersByTimeAsync(milliseconds)
  })
}

/**
 * Lets the answer of the API arrive and React render it.
 *
 * The fake timers own the schedule of the polling; the network still speaks in
 * real time. A real tick delivers the answer, and a tick of the fake clock
 * flushes the render React scheduled with it.
 */
const settle = async () => {
  for (let tick = 0; tick < 5; tick += 1) {
    await act(async () => {
      await new Promise((resolve) => realSetTimeout(resolve, 0))
    })
    await act(async () => {
      await vi.advanceTimersByTimeAsync(1)
    })
  }
}

describe('OperationStatus', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('follows the operation until it ends and stops there', async () => {
    let calls = 0

    server.use(
      http.get(URL_OPERATION, () => {
        calls += 1
        return HttpResponse.json(detail(calls === 1 ? 'PENDING' : 'COMPLETED'))
      }),
    )

    renderPanel()
    await advance(0)
    await settle()

    expect(screen.getByText('Em andamento')).toBeInTheDocument()
    expect(screen.getByText('PRT-20261128-0001')).toBeInTheDocument()
    expect(screen.getByText(/R\$\s*750,00/)).toBeInTheDocument()
    expect(screen.getByText('2 parcelas')).toBeInTheDocument()
    expect(calls).toBe(1)

    await advance(2000)
    await settle()

    expect(screen.getByText('Concluída')).toBeInTheDocument()
    expect(screen.getByText('Operação encerrada.')).toBeInTheDocument()
    expect(calls).toBe(2)

    // The operation is terminal: there is nothing left to ask.
    await advance(30000)
    await settle()

    expect(calls).toBe(2)
  })

  it('shows the reason of a rejection and stops polling', async () => {
    let calls = 0

    server.use(
      http.get(URL_OPERATION, () => {
        calls += 1
        return HttpResponse.json(detail('REJECTED', { rejectionReason: 'MARGIN_INSUFFICIENT' }))
      }),
    )

    renderPanel()
    await advance(0)
    await settle()

    expect(screen.getByText('Rejeitada')).toBeInTheDocument()
    expect(screen.getByText('MARGIN_INSUFFICIENT')).toBeInTheDocument()

    await advance(30000)
    await settle()

    expect(calls).toBe(1)
  })

  it('waits for the instant the server declares before asking again', async () => {
    let calls = 0

    server.use(
      http.get(URL_OPERATION, () => {
        calls += 1
        return HttpResponse.json(
          detail('PENDING', { nextPollAt: new Date(Date.now() + 10000).toISOString() }),
        )
      }),
    )

    renderPanel()
    await advance(0)
    await settle()

    expect(calls).toBe(1)

    // Two seconds are not enough: the server asked for ten.
    await advance(2000)
    await settle()

    expect(calls).toBe(1)
    expect(screen.getByText('Em andamento')).toBeInTheDocument()

    await advance(8000)
    await settle()

    expect(calls).toBe(2)
  })

  it('asks the API in the language of the reader', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    renderPanel()
    await advance(0)
    await settle()

    expect(acceptLanguage).toBe('pt-BR')
  })

  it('says when the API cannot answer', async () => {
    server.use(
      http.get(URL_OPERATION, () =>
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

    renderPanel()
    await advance(0)
    await settle()

    // The first failure is retried once; the second closes the question.
    await advance(2000)
    await settle()

    expect(screen.getByText('A operação não pôde ser consultada.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeInTheDocument()
  })
})
