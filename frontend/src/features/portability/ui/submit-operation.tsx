'use client'

import { useTranslation } from 'react-i18next'

import type { PortabilityState } from '../actions.ts'
import { formatMoney } from '../../../shared/format/money.ts'
import { statusTone } from '../../../shared/format/status.ts'
import { useLocale } from '../../../shared/i18n/provider.tsx'
import { buttonClasses } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { StatusBadge } from '../../../shared/ui/status-badge.tsx'

/**
 * The outcome of the submission.
 *
 * The failure speaks with two voices: the catalog of the interface, keyed by
 * the stable code of the contract, and the message of the server — the reason
 * the API actually gave ("a parcela PAR-003 não está aberta"). The second
 * voice only appears when it says something the first does not.
 */

export type SubmitOperationProps = {
  state: PortabilityState
}

export function SubmitOperation({ state }: SubmitOperationProps) {
  const { t } = useTranslation()
  const { locale } = useLocale()

  if (state.status === 'idle') {
    return null
  }

  if (state.status === 'success') {
    return (
      <Card className='flex flex-col gap-space-sm p-space-md'>
        <StatusBadge tone='success'>{t('portability.created')}</StatusBadge>
        <p className='text-body-md text-text-secondary'>
          {t('portability.protocol')}:{' '}
          <span className='font-mono text-text-primary'>{state.protocol ?? state.operationId}</span>
        </p>
        <p className='text-body-md text-text-secondary'>
          {t('portability.total')}:{' '}
          <span className='font-mono text-text-primary'>
            {formatMoney(state.totalAmount ?? 0, locale)}
          </span>
        </p>
        {/* The panel of the operation follows the status of the operation. */}
        {state.operationId !== undefined ? (
          <a
            href={`/operacoes?operationId=${encodeURIComponent(state.operationId)}`}
            className={buttonClasses({ variant: 'primary' })}
          >
            {t('portability.track')}
          </a>
        ) : null}
      </Card>
    )
  }

  const headline =
    state.code !== undefined
      ? t(`errors.${state.code}`, { defaultValue: state.message ?? '' })
      : (state.message ?? '')
  const detail = state.message !== undefined && state.message !== headline ? state.message : undefined

  return (
    <Card className='flex flex-col gap-space-sm p-space-md'>
      <StatusBadge tone={statusTone('REJECTED')}>{t('portability.failed')}</StatusBadge>
      <p role='alert' className='text-body-md text-error'>
        {headline}
      </p>
      {detail ? <p className='text-body-sm text-text-secondary'>{detail}</p> : null}
    </Card>
  )
}
