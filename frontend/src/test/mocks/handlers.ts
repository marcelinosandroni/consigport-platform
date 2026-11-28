import { http, HttpResponse } from 'msw'

/** The root of the fake API: the tests never reach a real server. */
export const API_BASE_URL = 'http://localhost:8080'

const notFound = (code: string, path: string, message: string) =>
  HttpResponse.json(
    {
      code,
      message,
      locale: 'pt-BR',
      timestamp: '2026-11-28T10:00:00Z',
      path,
      details: [],
    },
    { status: 404 },
  )

/**
 * The fake of the public API.
 *
 * The Central is fake and the PostgreSQL is the only real dependency of the
 * integration tests; here even the API is fake, so the unit tests stay fast
 * and deterministic. Every handler answers the shape of the public contract.
 */
export const handlers = [
  http.get(`${API_BASE_URL}/api/v1/authorizations/:beneficiaryId`, ({ params }) => {
    const beneficiaryId = String(params.beneficiaryId)

    if (beneficiaryId !== 'BEN-001') {
      return notFound(
        'AUTHORIZATION_NOT_FOUND',
        `/api/v1/authorizations/${beneficiaryId}`,
        'Não há autorização registrada para o beneficiário informado.',
      )
    }

    return HttpResponse.json({
      authorizationId: '0f5a6a3e-6f1a-4d4e-9d2f-6f0b2c8a1d33',
      beneficiaryId,
      agreementId: 'AGR-001',
      createdAt: '2026-11-28T10:00:00Z',
    })
  }),
]
