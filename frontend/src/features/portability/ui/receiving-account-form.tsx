'use client'

import { useActionState, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { submitPortabilityOperation, type PortabilityState } from '../actions.ts'
import { createIdempotencyKey } from '../idempotency.ts'
import { useLocale } from '../../../shared/i18n/provider.tsx'
import { Button } from '../../../shared/ui/button.tsx'
import { TextField } from '../../../shared/ui/field.tsx'
import { SubmitOperation } from './submit-operation.tsx'

/**
 * The receiving account of the operation.
 *
 * The island is the leaf: it owns the fields, the pending state and the key of
 * the attempt. The mutation belongs to the server action — the browser never
 * speaks to the API — and the form works without JavaScript, because the action
 * is the `action` of the form.
 */

export type ReceivingAccountFormProps = {
  beneficiaryId: string
  agreementId: string
  installmentIds: readonly string[]
}

const INITIAL_STATE: PortabilityState = { status: 'idle' }

type Account = {
  bank: string
  branch: string
  account: string
  assigneeCreditor: string
}

const EMPTY_ACCOUNT: Account = {
  bank: '',
  branch: '',
  account: '',
  assigneeCreditor: 'ConsigPort',
}

export function ReceivingAccountForm({
  beneficiaryId,
  agreementId,
  installmentIds,
}: ReceivingAccountFormProps) {
  const { t } = useTranslation()
  const { locale } = useLocale()
  const [state, formAction, pending] = useActionState(submitPortabilityOperation, INITIAL_STATE)
  const [account, setAccount] = useState<Account>(EMPTY_ACCOUNT)

  // The attempt: the fingerprint of the payload and the key that names it.
  const attempt = useRef<{ fingerprint: string; key: string } | null>(null)
  const keyInput = useRef<HTMLInputElement>(null)

  const fingerprint = JSON.stringify({
    beneficiaryId,
    agreementId,
    installmentIds,
    ...account,
  })

  /**
   * Names the attempt before the form leaves the browser.
   *
   * The same payload keeps the same key — a retry of a failed submission must
   * not create a second operation — and another payload gets a new key, because
   * the API refuses a key that arrives with another content.
   */
  const onSubmit = () => {
    const current = attempt.current
    const key =
      current !== null && current.fingerprint === fingerprint ? current.key : createIdempotencyKey()

    attempt.current = { fingerprint, key }

    if (keyInput.current !== null) {
      keyInput.current.value = key
    }
  }

  const fieldError = (field: string): string | undefined =>
    state.fieldErrors?.find((error) => error.field === field)?.message

  const update = (field: keyof Account, value: string) =>
    setAccount((current) => ({ ...current, [field]: value }))

  return (
    <form action={formAction} onSubmit={onSubmit} className='flex flex-col gap-space-md'>
      <input type='hidden' name='beneficiaryId' value={beneficiaryId} />
      <input type='hidden' name='agreementId' value={agreementId} />
      <input type='hidden' name='installmentIds' value={installmentIds.join(',')} />
      <input type='hidden' name='locale' value={locale} />
      {/* The key of the attempt: written by the submit handler, read by the action. */}
      <input type='hidden' name='idempotencyKey' ref={keyInput} defaultValue='' />

      <TextField
        id='bank'
        name='bank'
        label={t('portability.bank')}
        placeholder={t('portability.bankPlaceholder')}
        value={account.bank}
        onChange={(event) => update('bank', event.target.value)}
        error={fieldError('bank')}
        required
      />

      <TextField
        id='branch'
        name='branch'
        label={t('portability.branch')}
        placeholder={t('portability.branchPlaceholder')}
        value={account.branch}
        onChange={(event) => update('branch', event.target.value)}
        error={fieldError('branch')}
        required
      />

      <TextField
        id='account'
        name='account'
        label={t('portability.account')}
        placeholder={t('portability.accountPlaceholder')}
        value={account.account}
        onChange={(event) => update('account', event.target.value)}
        error={fieldError('account')}
        required
      />

      <TextField
        id='assigneeCreditor'
        name='assigneeCreditor'
        label={t('portability.assigneeCreditor')}
        value={account.assigneeCreditor}
        onChange={(event) => update('assigneeCreditor', event.target.value)}
        error={fieldError('assigneeCreditor')}
        required
      />

      <div className='flex flex-wrap items-center gap-space-md'>
        <Button type='submit' loading={pending}>
          {pending ? t('portability.submitting') : t('portability.submit')}
        </Button>
      </div>

      <SubmitOperation state={state} />
    </form>
  )
}
