import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { createApiClient, parseErrorResponse } from './client.ts'
import { parseApiError } from './errors.ts'
import { server } from '../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('the API client', () => {
  it('sends Accept-Language with every request', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      acceptLanguage = request.headers.get('accept-language')
    })

    const client = createApiClient('http://localhost:8080', 'es')
    const { data } = await client.GET('/api/v1/authorizations/{beneficiaryId}', {
      params: { path: { beneficiaryId: 'BEN-001' } },
    })

    expect(data?.beneficiaryId).toBe('BEN-001')
    expect(acceptLanguage).toBe('es')
  })

  it('reads the body of an error response into an ApiError', async () => {
    const client = createApiClient('http://localhost:8080', 'pt-BR')
    const { error, response } = await client.GET('/api/v1/authorizations/{beneficiaryId}', {
      params: { path: { beneficiaryId: 'BEN-999' } },
    })

    expect(response?.status).toBe(404)

    const parsed = parseApiError(error)

    expect(parsed.code).toBe('AUTHORIZATION_NOT_FOUND')
    expect(parsed.path).toBe('/api/v1/authorizations/BEN-999')
    expect(parsed.details).toEqual([])
  })

  it('parses a raw response that no client has read yet', async () => {
    const raw = new Response(
      JSON.stringify({
        code: 'BENEFICIARY_NOT_FOUND',
        message: 'A Central não conhece este beneficiário.',
        locale: 'pt-BR',
        timestamp: '2026-11-28T10:00:00Z',
        path: '/api/v1/beneficiaries/BEN-999/agenda',
        details: [],
      }),
      { status: 404, headers: { 'content-type': 'application/json' } },
    )

    const error = await parseErrorResponse(raw)

    expect(error.code).toBe('BENEFICIARY_NOT_FOUND')
    expect(error.message).toBe('A Central não conhece este beneficiário.')
  })
})
