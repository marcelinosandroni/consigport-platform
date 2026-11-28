'use client'

import { useTranslation } from 'react-i18next'

import { Button } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'

/**
 * The summary of the selection.
 *
 * The total is not stored: it is recalculated from the selection every time
 * the selection changes, so the number on the screen and the number in the
 * request can never drift apart. The amount is announced politely, because a
 * screen reader user needs to hear that the total moved.
 */

export type SelectionSummaryProps = {
  /** How many installments are selected. */
  selectedCount: number
  /** The total, already formatted for the reader. */
  totalLabel: string
  onClear: () => void
  onContinue: () => void
}

export function SelectionSummary({
  selectedCount,
  totalLabel,
  onClear,
  onContinue,
}: SelectionSummaryProps) {
  const { t } = useTranslation()
  const empty = selectedCount === 0

  return (
    <Card className='flex flex-col gap-space-md p-space-md'>
      <div className='flex flex-wrap items-baseline justify-between gap-space-sm'>
        <h2 className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
          {t('agenda.summary')}
        </h2>
        <p className='text-body-sm text-text-secondary'>
          {empty ? t('agenda.selectionEmpty') : t('agenda.selectedCount', { count: selectedCount })}
        </p>
      </div>

      <div className='flex flex-wrap items-end justify-between gap-space-md'>
        <div className='flex flex-col gap-space-xs'>
          <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
            {t('agenda.total')}
          </p>
          <p aria-live='polite' className='text-headline-md'>
            {totalLabel}
          </p>
        </div>

        <div className='flex flex-wrap items-center gap-space-sm'>
          <Button variant='ghost' onClick={onClear} disabled={empty}>
            {t('agenda.clear')}
          </Button>
          <Button onClick={onContinue} disabled={empty}>
            {t('agenda.continue')}
          </Button>
        </div>
      </div>
    </Card>
  )
}
