import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'

import { registerAuthorization, type AuthorizationState } from './actions.ts'
import { server } from '../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const idle: AuthorizationState = { status: 'idle' }

const formData = (entries: Record<string, string>): FormData => {
  const data = new FormData()
  for (const [key, value] of Object.entries(entries)) {
    data.set(key, value)
  }
  return data
}

describe('registerAuthorization', () => {
  it('registers the consent and answers with the beneficiary', async () => {
    const state = await registerAuthorization(
      idle,
      formData({ beneficiaryId: 'BEN-100', agreementId: 'AGR-100', locale: 'pt-BR' }),
    )

    expect(state.status).toBe('success')
    expect(state.beneficiaryId).toBe('BEN-100')
    expect(state.agreementId).toBe('AGR-100')
    expect(state.created).toBe(true)
  })

  it('answers 200 as a replay when the consent already exists', async () => {
    const entries = { beneficiaryId: 'BEN-101', agreementId: 'AGR-101', locale: 'pt-BR' }

    const first = await registerAuthorization(idle, formData(entries))
    const second = await registerAuthorization(idle, formData(entries))

    expect(first.created).toBe(true)
    expect(second.created).toBe(false)
    expect(second.status).toBe('success')
  })

  it('rejects an empty form before reaching the API', async () => {
    const state = await registerAuthorization(idle, formData({}))

    expect(state.status).toBe('error')
    expect(state.code).toBe('VALIDATION_ERROR')
    expect(state.fieldErrors?.map((error) => error.field)).toEqual(['beneficiaryId', 'agreementId'])
  })

  it('maps the error of the API by its stable code', async () => {
    const state = await registerAuthorization(
      idle,
      formData({ beneficiaryId: 'BEN-000', agreementId: 'AGR-000', locale: 'pt-BR' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('VALIDATION_ERROR')
    expect(state.fieldErrors?.[0]?.field).toBe('beneficiaryId')
  })

  it('answers an error when the API cannot be reached', async () => {
    server.use(
      http.post('http://localhost:8080/api/v1/authorizations', () => HttpResponse.error()),
    )

    const state = await registerAuthorization(
      idle,
      formData({ beneficiaryId: 'BEN-104', agreementId: 'AGR-104', locale: 'pt-BR' }),
    )

    expect(state.status).toBe('error')
    expect(state.code).toBe('INTERNAL_ERROR')
  })

  it('sends the language the interface asked for', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await registerAuthorization(
      idle,
      formData({ beneficiaryId: 'BEN-102', agreementId: 'AGR-102', locale: 'es' }),
    )

    expect(acceptLanguage).toBe('es')
  })

  it('falls back to pt-BR when the locale is unknown', async () => {
    let acceptLanguage: string | null = null

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        acceptLanguage = request.headers.get('accept-language')
      }
    })

    await registerAuthorization(
      idle,
      formData({ beneficiaryId: 'BEN-103', agreementId: 'AGR-103', locale: 'xx' }),
    )

    expect(acceptLanguage).toBe('pt-BR')
  })
})
