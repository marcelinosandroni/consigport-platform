'use server'

import { z } from 'zod'

import { createApiClient, resolveApiBaseUrl } from '../../shared/api/client.ts'
import { parseApiError } from '../../shared/api/errors.ts'
import { DEFAULT_LOCALE, isSupportedLocale, type SupportedLocale } from '../../shared/i18n/locales.ts'

/**
 * The opt-in of the beneficiary.
 *
 * The action is the only door to the mutation: the browser never speaks to the
 * API directly. It validates the input, asks the API to register the consent
 * and hands the screen a state it can render — no exception, no object that
 * cannot cross the server boundary.
 */

const authorizationSchema = z.object({
  beneficiaryId: z.string().trim().min(1).max(64),
  agreementId: z.string().trim().min(1).max(64),
})

export type AuthorizationFieldError = {
  readonly field: string
  readonly message: string
}

export type AuthorizationState = {
  readonly status: 'idle' | 'success' | 'error'
  /** The stable code of the contract; the UI keys its catalog by it. */
  readonly code?: string
  /** The localized message of the server; the UI falls back to it. */
  readonly message?: string
  readonly beneficiaryId?: string
  readonly agreementId?: string
  /** True when the consent was registered by this call, false when it already existed. */
  readonly created?: boolean
  readonly fieldErrors?: readonly AuthorizationFieldError[]
}

const readLocale = (formData: FormData): SupportedLocale => {
  const raw = formData.get('locale')
  return typeof raw === 'string' && isSupportedLocale(raw) ? raw : DEFAULT_LOCALE
}

export async function registerAuthorization(
  _previous: AuthorizationState,
  formData: FormData,
): Promise<AuthorizationState> {
  const parsed = authorizationSchema.safeParse({
    beneficiaryId: formData.get('beneficiaryId'),
    agreementId: formData.get('agreementId'),
  })

  if (!parsed.success) {
    return {
      status: 'error',
      code: 'VALIDATION_ERROR',
      message: 'The submitted data did not pass validation.',
      fieldErrors: parsed.error.issues.map((issue) => ({
        field: issue.path.join('.') || 'form',
        message: issue.message,
      })),
    }
  }

  const client = createApiClient(resolveApiBaseUrl(), readLocale(formData))

  try {
    const { data, error, response } = await client.POST('/api/v1/authorizations', { body: parsed.data })

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
      beneficiaryId: data.beneficiaryId,
      agreementId: data.agreementId,
      created: response?.status === 201,
    }
  } catch {
    // The API could not be reached at all. The screen still gets an answer:
    // a broken connection is a failure of the request, not a crash of the page.
    return {
      status: 'error',
      code: 'INTERNAL_ERROR',
      message: 'The API could not be reached.',
    }
  }
}
