import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { submitPortabilityOperation, type PortabilityState } from './actions.ts'
import { server } from '../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const idle: PortabilityState = { status: 'idle' }

const formData = (entries: Record<string, string>): FormData => {
  const data = new FormData()
  for (const [key, value] of Object.entries(entries)) {
    data.set(key, value)
  }
  return data
}

const KEY = '11111111-1111-4111-8111-111111111111'

const submission = {
  beneficiaryId: 'BEN-001',
  agreementId: 'AGR-001',
  installmentIds: 'PAR-001,PAR-002',
  assigneeCreditor: 'ConsigPort',
  bank: '001',
  branch: '1234',
  account: '56789-0',
  locale: 'pt-BR',
  idempotencyKey: KEY,
}

describe('submitPortabilityOperation', () => {
  it('creates the operation and answers with the protocol', async () => {
    const state = await submitPortabilityOperation(idle, formData(submission))

    expect(state.status).toBe('success')
    expect(state.protocol).toBe('PRT-20261128-0001')
    expect(state.operationStatus).toBe('PENDING')
    expect(state.totalAmount).toBe(750)
  })

  it('the same key with the same content returns the same operation', async () => {
    const first = await submitPortabilityOperation(idle, formData(submission))
    const second = await submitPortabilityOperation(idle, formData(submission))

    expect(second.status).toBe('success')
    expect(second.operationId).toBe(first.operationId)
  })

  it('the same key with another content is a conflict', async () => {
    await submitPortabilityOperation(idle, formData(submission))

    const state = await submitPortabilityOperation(
      idle,
      formData({ ...submission, account: '99999-9' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('IDEMPOTENCY_CONFLICT')
    expect(state.message).toBe('A chave de idempotência já foi usada com outro conteúdo.')
  })

  it('refuses a closed installment', async () => {
    const state = await submitPortabilityOperation(
      idle,
      formData({ ...submission, installmentIds: 'PAR-003', idempotencyKey: '22222222-2222-4222-8222-222222222222' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('INSTALLMENT_NOT_ELIGIBLE')
    expect(state.message).toBe('A parcela PAR-003 não está aberta.')
    expect(state.fieldErrors?.[0]?.field).toBe('installmentIds')
  })

  it('validates the account before reaching the API', async () => {
    const state = await submitPortabilityOperation(
      idle,
      formData({ ...submission, bank: '', branch: '', account: '' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('VALIDATION_ERROR')
    expect(state.fieldErrors?.map((error) => error.field).sort()).toEqual([
      'account',
      'bank',
      'branch',
    ])
  })

  it('creates a key when the browser sends none', async () => {
    let idempotencyKey: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        idempotencyKey = request.headers.get('idempotency-key')
      }
    })

    const entries = { ...submission }
    delete (entries as Partial<typeof submission>).idempotencyKey

    const state = await submitPortabilityOperation(idle, formData(entries))

    expect(state.status).toBe('success')
    expect(idempotencyKey).toMatch(
      /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i,
    )
  })

  it('sends the language the interface asked for', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await submitPortabilityOperation(
      idle,
      formData({ ...submission, locale: 'es', idempotencyKey: '33333333-3333-4333-8333-333333333333' }),
    )

    expect(acceptLanguage).toBe('es')
  })

  it('answers an error when the API cannot be reached', async () => {
    const { http, HttpResponse } = await import('msw')

    server.use(
      http.post('http://localhost:8080/api/v1/portability-operations', () => HttpResponse.error()),
    )

    const state = await submitPortabilityOperation(
      idle,
      formData({ ...submission, idempotencyKey: '44444444-4444-4444-8444-444444444444' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('INTERNAL_ERROR')
  })
})
