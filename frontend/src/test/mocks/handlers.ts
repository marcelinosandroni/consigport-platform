import { http, HttpResponse } from 'msw'

/** The root of the fake API: the tests never reach a real server. */
export const API_BASE_URL = 'http://localhost:8080'

const errorResponse = (
  status: number,
  code: string,
  path: string,
  message: string,
  details: { field: string; message: string }[] = [],
) =>
  HttpResponse.json(
    {
      code,
      message,
      locale: 'pt-BR',
      timestamp: '2026-11-28T10:00:00Z',
      path,
      details,
    },
    { status },
  )

const notFound = (code: string, path: string, message: string) =>
  errorResponse(404, code, path, message)

const badRequest = (message: string, details: { field: string; message: string }[] = []) =>
  errorResponse(400, 'VALIDATION_ERROR', '/api/v1/authorizations', message, details)

/**
 * The consent already registered by the fake, so the replay answers 200 the
 * way the contract describes.
 */
const registered = new Map<string, { authorizationId: string; createdAt: string }>()

/** The agenda of the beneficiaries the fake knows. */
const AGENDAS: Record<string, { installments: unknown[] }> = {
  'BEN-001': {
    installments: [
      {
        installmentId: 'PAR-001',
        agreementId: 'AGR-001',
        amount: 450,
        discountDate: '2026-12-05',
        status: 'OPEN',
      },
      {
        installmentId: 'PAR-002',
        agreementId: 'AGR-001',
        amount: 300,
        discountDate: '2027-01-05',
        status: 'OPEN',
      },
      {
        installmentId: 'PAR-003',
        agreementId: 'AGR-001',
        amount: 120.5,
        discountDate: '2026-11-20',
        status: 'CLOSED',
      },
    ],
  },
  'BEN-EMPTY': { installments: [] },
}

/** The operations the fake already accepted, by idempotency key. */
const operations = new Map<string, { payload: string; response: Record<string, unknown> }>()

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

  http.post(`${API_BASE_URL}/api/v1/authorizations`, async ({ request }) => {
    const body = (await request.json()) as { beneficiaryId?: string; agreementId?: string }
    const beneficiaryId = body.beneficiaryId ?? ''
    const agreementId = body.agreementId ?? ''

    if (beneficiaryId.length === 0 || agreementId.length === 0) {
      return badRequest('O corpo da requisição é inválido.')
    }

    if (beneficiaryId === 'BEN-000') {
      return badRequest('O beneficiário informado não é válido.', [
        { field: 'beneficiaryId', message: 'Beneficiário inexistente.' },
      ])
    }

    const key = `${beneficiaryId}:${agreementId}`
    const existing = registered.get(key)

    if (existing !== undefined) {
      return HttpResponse.json(
        {
          authorizationId: existing.authorizationId,
          beneficiaryId,
          agreementId,
          createdAt: existing.createdAt,
        },
        { status: 200 },
      )
    }

    const authorization = {
      authorizationId: '7f0f5d5e-2d6a-4f2e-9f4e-1b2c3d4e5f60',
      createdAt: '2026-11-28T10:00:00Z',
    }
    registered.set(key, authorization)

    return HttpResponse.json(
      { ...authorization, beneficiaryId, agreementId },
      { status: 201 },
    )
  }),

  http.get(`${API_BASE_URL}/api/v1/beneficiaries/:beneficiaryId/agenda`, ({ params }) => {
    const beneficiaryId = String(params.beneficiaryId)
    const path = `/api/v1/beneficiaries/${beneficiaryId}/agenda`

    if (beneficiaryId === 'BEN-FORBIDDEN') {
      return errorResponse(
        403,
        'AUTHORIZATION_REQUIRED',
        path,
        'O beneficiário informado não tem consentimento registrado.',
      )
    }

    const agenda = AGENDAS[beneficiaryId]

    if (agenda === undefined) {
      return notFound(
        'BENEFICIARY_NOT_FOUND',
        path,
        'A Central não conhece o beneficiário informado.',
      )
    }

    return HttpResponse.json({ beneficiaryId, installments: agenda.installments })
  }),

  http.post(`${API_BASE_URL}/api/v1/portability-operations`, async ({ request }) => {
    const key = request.headers.get('idempotency-key') ?? ''
    const body = (await request.json()) as {
      beneficiaryId?: string
      agreementId?: string
      installmentIds?: string[]
      assigneeCreditor?: string
      receivingAccount?: { bank?: string; branch?: string; account?: string }
    }

    if (key.length === 0) {
      return errorResponse(
        400,
        'IDEMPOTENCY_KEY_REQUIRED',
        '/api/v1/portability-operations',
        'A chave de idempotência é obrigatória.',
      )
    }

    const installmentIds = body.installmentIds ?? []

    if (installmentIds.length === 0) {
      return errorResponse(
        400,
        'VALIDATION_ERROR',
        '/api/v1/portability-operations',
        'Nenhuma parcela foi informada.',
      )
    }

    const closed = installmentIds.find((installmentId) => installmentId === 'PAR-003')
    if (closed !== undefined) {
      return errorResponse(
        422,
        'INSTALLMENT_NOT_ELIGIBLE',
        '/api/v1/portability-operations',
        `A parcela ${closed} não está aberta.`,
        [{ field: 'installmentIds', message: `${closed} está CLOSED` }],
      )
    }

    const payload = JSON.stringify(body)
    const existing = operations.get(key)

    if (existing !== undefined) {
      if (existing.payload !== payload) {
        return errorResponse(
          409,
          'IDEMPOTENCY_CONFLICT',
          '/api/v1/portability-operations',
          'A chave de idempotência já foi usada com outro conteúdo.',
        )
      }

      return HttpResponse.json(existing.response, {
        status: 202,
        headers: { 'Idempotent-Replay': 'true' },
      })
    }

    const response = {
      operationId: 'c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601',
      protocol: 'PRT-20261128-0001',
      status: 'PENDING',
      totalAmount: 750,
      createdAt: '2026-11-28T10:00:05Z',
    }
    operations.set(key, { payload, response })

    return HttpResponse.json(response, {
      status: 202,
      headers: { 'Idempotent-Replay': 'false' },
    })
  }),
]
