'use server'

import { z } from 'zod'

import { createApiClient, resolveApiBaseUrl } from '../../shared/api/client.ts'
import { parseApiError } from '../../shared/api/errors.ts'
import { DEFAULT_LOCALE, isSupportedLocale, type SupportedLocale } from '../../shared/i18n/locales.ts'
import { createIdempotencyKey, isUuid } from './idempotency.ts'

/**
 * The submission of the portability operation.
 *
 * The action is the only door to the mutation: the browser never speaks to the
 * API. It validates the account with **Zod** — the same rules the contract
 * declares — sends the `Idempotency-Key` of the attempt and hands the screen a
 * state it can render: never an exception, never an object that cannot cross
 * the server boundary.
 */

const receivingAccountSchema = z.object({
  bank: z.string().trim().min(1).max(64),
  branch: z.string().trim().min(1).max(32),
  account: z.string().trim().min(1).max(64),
})

const submissionSchema = z.object({
  beneficiaryId: z.string().trim().min(1).max(64),
  agreementId: z.string().trim().min(1).max(64),
  installmentIds: z.array(z.string().trim().min(1)).min(1),
  assigneeCreditor: z.string().trim().min(1).max(255),
  receivingAccount: receivingAccountSchema,
})

export type PortabilityFieldError = {
  readonly field: string
  readonly message: string
}

export type PortabilityState = {
  readonly status: 'idle' | 'success' | 'error'
  /** The stable code of the contract; the UI keys its catalog by it. */
  readonly code?: string
  /** The localized message of the server; the UI shows it as the detail. */
  readonly message?: string
  readonly operationId?: string
  readonly protocol?: string
  readonly operationStatus?: string
  readonly totalAmount?: number
  readonly fieldErrors?: readonly PortabilityFieldError[]
}

/** The field a validation issue belongs to. */
const fieldOf = (path: readonly PropertyKey[]): string => {
  const last = path.at(-1)
  const owner = path.at(-2)

  if (typeof last === 'number') {
    return typeof owner === 'string' ? owner : 'form'
  }
  return typeof last === 'string' ? last : 'form'
}

const readInstallmentIds = (formData: FormData): string[] => {
  const raw = formData.get('installmentIds')
  if (typeof raw !== 'string') {
    return []
  }
  return raw
    .split(',')
    .map((installmentId) => installmentId.trim())
    .filter((installmentId) => installmentId.length > 0)
}

const readLocale = (formData: FormData): SupportedLocale => {
  const raw = formData.get('locale')
  return typeof raw === 'string' && isSupportedLocale(raw) ? raw : DEFAULT_LOCALE
}

/**
 * The key of the attempt.
 *
 * The browser sends the key it generated for this attempt. Without JavaScript
 * there is no key to send, and the action creates one — the operation is still
 * created exactly once.
 */
const readIdempotencyKey = (formData: FormData): string => {
  const raw = formData.get('idempotencyKey')
  return typeof raw === 'string' && isUuid(raw) ? raw : createIdempotencyKey()
}

export async function submitPortabilityOperation(
  _previous: PortabilityState,
  formData: FormData,
): Promise<PortabilityState> {
  const parsed = submissionSchema.safeParse({
    beneficiaryId: formData.get('beneficiaryId'),
    agreementId: formData.get('agreementId'),
    installmentIds: readInstallmentIds(formData),
    assigneeCreditor: formData.get('assigneeCreditor'),
    receivingAccount: {
      bank: formData.get('bank'),
      branch: formData.get('branch'),
      account: formData.get('account'),
    },
  })

  if (!parsed.success) {
    return {
      status: 'error',
      code: 'VALIDATION_ERROR',
      message: 'The submitted data did not pass validation.',
      fieldErrors: parsed.error.issues.map((issue) => ({
        field: fieldOf(issue.path),
        message: issue.message,
      })),
    }
  }

  const client = createApiClient(resolveApiBaseUrl(), readLocale(formData))

  try {
    const { data, error } = await client.POST('/api/v1/portability-operations', {
      params: { header: { 'Idempotency-Key': readIdempotencyKey(formData) } },
      body: parsed.data,
    })

    if (data === undefined) {
      const failure = parseApiError(error)
      return {
        status: 'error',
        code: failure.code,
        message: failure.message,
        fieldErrors: failure.details.map((detail) => ({
          field: detail.field,
          message: detail.message,
        })),
      }
    }

    return {
      status: 'success',
      operationId: data.operationId,
      protocol: data.protocol ?? undefined,
      operationStatus: data.status,
      totalAmount: data.totalAmount,
    }
  } catch {
    // The API could not be reached at all. The screen still gets an answer: a
    // broken connection is a failure of the request, not a crash of the page.
    return {
      status: 'error',
      code: 'INTERNAL_ERROR',
      message: 'The API could not be reached.',
    }
  }
}
