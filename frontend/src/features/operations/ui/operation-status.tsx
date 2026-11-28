'use client'

import { useTranslation } from 'react-i18next'

import { useOperationPolling, type OperationDetail } from './use-operation-polling.ts'
import { formatDateTime } from '../../../shared/format/date.ts'
import { formatMoney } from '../../../shared/format/money.ts'
import { statusTone } from '../../../shared/format/status.ts'
import { useLocale } from '../../../shared/i18n/provider.tsx'
import { Button } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { StatusBadge } from '../../../shared/ui/status-badge.tsx'

/**
 * The panel of the operation.
 *
 * The island is the leaf: it owns the polling and nothing else. The page
 * around it is a Server Component, and the state it delivers is the first
 * answer the reader sees — the island takes over from there and asks again
 * every two seconds until the operation ends.
 */

/** The catalog key of each status of the contract. */
const STATUS_LABEL: Record<string, string> = {
  CREATED: 'operations.created',
  PENDING: 'operations.pending',
  CONFIRMED: 'operations.confirmed',
  COMPLETED: 'operations.completed',
  REJECTED: 'operations.rejected',
  INTEGRATION_ERROR: 'operations.integrationError',
}

export type OperationStatusProps = {
  readonly operationId: string
  readonly initialOperation?: OperationDetail
}

/** A fact of the operation: the label above, the value below. */
function Detail({ label, value }: { readonly label: string; readonly value: string }) {
  return (
    <div className='flex flex-col gap-space-xs'>
      <dt className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
        {label}
      </dt>
      <dd className='text-body-md text-text-primary'>{value}</dd>
    </div>
  )
}

export function OperationStatus({ operationId, initialOperation }: OperationStatusProps) {
  const { t } = useTranslation()
  const { locale } = useLocale()
  const { operation, failure, isFetching, refresh } = useOperationPolling({
    operationId,
    locale,
    initialOperation,
  })

  if (operation === undefined) {
    return (
      <Card className='flex flex-col gap-space-sm p-space-lg'>
        <p aria-live='polite' className='text-body-md text-text-secondary'>
          {failure === undefined ? t('operations.loading') : t('operations.unavailable')}
        </p>
        {failure !== undefined ? (
          <Button variant='secondary' onClick={refresh}>
            {t('common.retry')}
          </Button>
        ) : null}
      </Card>
    )
  }

  const terminal = operation.status === 'COMPLETED' || operation.status === 'REJECTED'
  const labelKey = STATUS_LABEL[operation.status]

  return (
    <Card className='flex flex-col gap-space-md p-space-lg'>
      <div className='flex flex-wrap items-center justify-between gap-space-sm'>
        <StatusBadge tone={statusTone(operation.status)}>
          {labelKey === undefined ? operation.status : t(labelKey)}
        </StatusBadge>
        <span className='font-mono text-body-sm text-text-secondary'>
          {operation.protocol ?? operation.operationId}
        </span>
      </div>

      <dl className='grid gap-space-md sm:grid-cols-2'>
        <Detail label={t('operations.totalAmount')} value={formatMoney(operation.totalAmount, locale)} />
        <Detail
          label={t('operations.installments')}
          value={t('operations.installmentCount', { count: operation.installments.length })}
        />
        <Detail label={t('operations.creditor')} value={operation.assigneeCreditor} />
        <Detail
          label={t('operations.updatedAt')}
          value={formatDateTime(operation.updatedAt, locale)}
        />
      </dl>

      {operation.status === 'REJECTED' && operation.rejectionReason !== null ? (
        <div className='flex flex-col gap-space-xs'>
          <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
            {t('operations.rejectionReason')}
          </p>
          <p className='font-mono text-body-md text-error'>{operation.rejectionReason}</p>
        </div>
      ) : null}

      {failure !== undefined ? (
        <p role='alert' className='text-body-sm text-error'>
          {t(`errors.${failure.code}`, failure.message)}
        </p>
      ) : null}

      <p aria-live='polite' className='text-body-sm text-text-secondary'>
        {terminal ? t('operations.terminal') : isFetching ? t('operations.polling') : t('operations.waiting')}
      </p>

      <div className='flex flex-wrap items-center gap-space-md'>
        <Button variant='ghost' onClick={refresh} loading={isFetching}>
          {t('operations.refresh')}
        </Button>
      </div>
    </Card>
  )
}
