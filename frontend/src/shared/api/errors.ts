/**
 * The stable error codes of the public API.
 *
 * The code is the contract: it never changes and it is never translated. The
 * message that comes with it is already localized by the backend, and the UI
 * keeps its own catalogs keyed by this same code.
 */
export const API_ERROR_CODES = [
  'VALIDATION_ERROR',
  'AUTHORIZATION_NOT_FOUND',
  'AUTHORIZATION_REQUIRED',
  'BENEFICIARY_NOT_FOUND',
  'OPERATION_NOT_FOUND',
  'IDEMPOTENCY_KEY_REQUIRED',
  'IDEMPOTENCY_CONFLICT',
  'INSTALLMENT_NOT_ELIGIBLE',
  'CENTRAL_UNAVAILABLE',
  'INTERNAL_ERROR',
] as const

export type ApiErrorCode = (typeof API_ERROR_CODES)[number]

export interface ApiErrorDetail {
  readonly field: string
  readonly message: string
}

const FALLBACK_MESSAGE = 'The API returned an error without a message.'

/**
 * A failure of the public API, already parsed into the shape the UI needs.
 *
 * The class exists so the UI can branch on `code` instead of on the HTTP
 * status: the same status carries different codes depending on the route.
 */
export class ApiError extends Error {
  readonly code: ApiErrorCode
  readonly locale: string
  readonly timestamp: string
  readonly path: string
  readonly details: readonly ApiErrorDetail[]

  constructor(init: {
    readonly code: ApiErrorCode
    readonly message: string
    readonly locale: string
    readonly timestamp: string
    readonly path: string
    readonly details: readonly ApiErrorDetail[]
  }) {
    super(init.message)
    this.name = 'ApiError'
    this.code = init.code
    this.locale = init.locale
    this.timestamp = init.timestamp
    this.path = init.path
    this.details = init.details
  }
}

export const isApiErrorCode = (value: unknown): value is ApiErrorCode =>
  typeof value === 'string' && (API_ERROR_CODES as readonly string[]).includes(value)

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

const readString = (source: Record<string, unknown>, key: string): string => {
  const value = source[key]
  return typeof value === 'string' ? value : ''
}

const readDetails = (value: unknown): readonly ApiErrorDetail[] => {
  if (!Array.isArray(value)) {
    return []
  }
  return value.flatMap((entry) => {
    if (!isRecord(entry)) {
      return []
    }
    const field = readString(entry, 'field')
    const message = readString(entry, 'message')
    return field.length > 0 || message.length > 0 ? [{ field, message }] : []
  })
}

/**
 * Parses the body of an error response into an {@link ApiError}.
 *
 * The payload comes from the network, so it is `unknown` until proven
 * otherwise: a malformed body degrades to `INTERNAL_ERROR` instead of
 * throwing inside the UI.
 */
export const parseApiError = (payload: unknown): ApiError => {
  if (!isRecord(payload)) {
    return new ApiError({
      code: 'INTERNAL_ERROR',
      message: FALLBACK_MESSAGE,
      locale: 'pt-BR',
      timestamp: '',
      path: '',
      details: [],
    })
  }

  const message = readString(payload, 'message')
  const locale = readString(payload, 'locale')

  return new ApiError({
    code: isApiErrorCode(payload.code) ? payload.code : 'INTERNAL_ERROR',
    message: message.length > 0 ? message : FALLBACK_MESSAGE,
    locale: locale.length > 0 ? locale : 'pt-BR',
    timestamp: readString(payload, 'timestamp'),
    path: readString(payload, 'path'),
    details: readDetails(payload.details),
  })
}

export const isApiError = (value: unknown): value is ApiError => value instanceof ApiError
