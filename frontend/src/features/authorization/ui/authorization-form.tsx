'use client'

import { useActionState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'

import { registerAuthorization, type AuthorizationState } from '../actions.ts'
import { navigate } from '../../../framework/navigation.ts'
import { useLocale } from '../../../shared/i18n/provider.tsx'
import { Button } from '../../../shared/ui/button.tsx'
import { TextField } from '../../../shared/ui/field.tsx'

/**
 * The opt-in form.
 *
 * The island is the leaf: it owns the form, the pending state and the error,
 * and nothing else. The mutation itself belongs to the server action — the
 * browser never speaks to the API directly — and the form works without
 * JavaScript, because the action is the `action` of the form.
 */

const INITIAL_STATE: AuthorizationState = { status: 'idle' }

export function AuthorizationForm() {
  const { t } = useTranslation()
  const { locale } = useLocale()
  const [state, formAction, pending] = useActionState(registerAuthorization, INITIAL_STATE)

  // The consent is registered: the agenda is the next step of the journey.
  useEffect(() => {
    if (state.status === 'success' && state.beneficiaryId !== undefined) {
      const target = `/agenda?beneficiaryId=${encodeURIComponent(state.beneficiaryId)}`
      navigate(target)
    }
  }, [state])

  const failed = state.status === 'error'
  const fieldError = (field: string): string | undefined => {
    if (state.code === 'VALIDATION_ERROR') {
      return undefined
    }
    return state.fieldErrors?.find((error) => error.field === field)?.message
  }

  const formError =
    failed && state.code !== undefined
      ? t(`errors.${state.code}`, { defaultValue: state.message ?? '' })
      : undefined

  return (
    <form action={formAction} className='flex flex-col gap-space-md'>
      <input type='hidden' name='locale' value={locale} />

      <TextField
        id='beneficiaryId'
        name='beneficiaryId'
        label={t('authorization.beneficiaryId')}
        placeholder={t('authorization.beneficiaryIdPlaceholder')}
        required
        error={fieldError('beneficiaryId')}
      />

      <TextField
        id='agreementId'
        name='agreementId'
        label={t('authorization.agreementId')}
        placeholder={t('authorization.agreementIdPlaceholder')}
        required
        error={fieldError('agreementId')}
      />

      {formError ? (
        <p role='alert' className='text-body-sm text-error'>
          {formError}
        </p>
      ) : null}

      {state.status === 'success' ? (
        <p role='status' className='text-body-sm text-secondary'>
          {state.created ? t('authorization.success') : t('authorization.replay')}
        </p>
      ) : null}

      <div className='flex flex-wrap items-center gap-space-md'>
        <Button type='submit' loading={pending}>
          {pending ? t('authorization.submitting') : t('authorization.submit')}
        </Button>
      </div>
    </form>
  )
}
