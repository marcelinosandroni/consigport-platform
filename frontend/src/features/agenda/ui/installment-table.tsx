'use client'

import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import type { components } from '../../../shared/api/schema.ts'
import { formatDate } from '../../../shared/format/date.ts'
import { formatMoney, sumAmounts } from '../../../shared/format/money.ts'
import { statusTone } from '../../../shared/format/status.ts'
import { useLocale } from '../../../shared/i18n/provider.tsx'
import { StatusBadge } from '../../../shared/ui/status-badge.tsx'
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHead,
  TableRow,
} from '../../../shared/ui/table.tsx'
import { SelectionSummary } from './selection-summary.tsx'

/**
 * The agenda of the beneficiary, with the selection of the installments.
 *
 * The island is the leaf: it owns the selection and the total, and nothing
 * else. The data arrives from the server, already fetched — the browser never
 * asks the API for the agenda.
 *
 * Only an `OPEN` installment can be chosen: a closed one is a debt already
 * discounted, and offering it would be offering an operation the API refuses.
 */

export type AgendaInstallment = components['schemas']['Installment']

export type InstallmentTableProps = {
  installments: readonly AgendaInstallment[]
  beneficiaryId: string
}

export function InstallmentTable({ installments, beneficiaryId }: InstallmentTableProps) {
  const { t } = useTranslation()
  const { locale } = useLocale()
  const [selectedIds, setSelectedIds] = useState<ReadonlySet<string>>(() => new Set())

  const eligible = installments.filter((installment) => installment.status === 'OPEN')
  const selected = eligible.filter((installment) => selectedIds.has(installment.installmentId))
  const total = sumAmounts(selected.map((installment) => installment.amount))

  const toggle = (installmentId: string) => {
    setSelectedIds((current) => {
      const next = new Set(current)
      if (next.has(installmentId)) {
        next.delete(installmentId)
      } else {
        next.add(installmentId)
      }
      return next
    })
  }

  const clear = () => setSelectedIds(new Set())

  // The selection travels to the next step in the URL: the portability screen
  // reads the same installments the user chose here.
  const continueToPortability = () => {
    const query = new URLSearchParams({
      beneficiaryId,
      installments: selected.map((installment) => installment.installmentId).join(','),
    })
    window.history.pushState(null, '', `/portabilidade?${query.toString()}`)
  }

  return (
    <div className='flex flex-col gap-space-md'>
      <Table label={t('agenda.tableLabel')}>
        <TableHead>
          <TableRow>
            <TableHeader className='w-16'>{t('agenda.selection')}</TableHeader>
            <TableHeader>{t('agenda.installment')}</TableHeader>
            <TableHeader>{t('agenda.agreement')}</TableHeader>
            <TableHeader>{t('agenda.amount')}</TableHeader>
            <TableHeader>{t('agenda.discountDate')}</TableHeader>
            <TableHeader>{t('agenda.status')}</TableHeader>
          </TableRow>
        </TableHead>

        <TableBody>
          {installments.map((installment) => {
            const open = installment.status === 'OPEN'
            const checked = selectedIds.has(installment.installmentId)

            return (
              <TableRow key={installment.installmentId} className={checked ? 'bg-primary/5' : undefined}>
                <TableCell>
                  <div className='flex items-center gap-space-sm'>
                    {/* The label wraps the control: the touch area is 44px, the
                        box is 20px, and the name of the control is its own. */}
                    <label className='flex min-h-11 min-w-11 items-center justify-center'>
                      <input
                        type='checkbox'
                        className='size-5 accent-primary'
                        checked={checked}
                        disabled={!open}
                        onChange={() => toggle(installment.installmentId)}
                        aria-label={t('agenda.selectLabel', {
                          installmentId: installment.installmentId,
                        })}
                      />
                    </label>
                    {checked ? <StatusBadge tone='progress'>{t('agenda.selected')}</StatusBadge> : null}
                  </div>
                </TableCell>

                <TableCell className='font-mono text-body-sm'>{installment.installmentId}</TableCell>
                <TableCell className='font-mono text-body-sm'>{installment.agreementId}</TableCell>
                <TableCell>{formatMoney(installment.amount, locale)}</TableCell>
                <TableCell>{formatDate(installment.discountDate, locale)}</TableCell>
                <TableCell>
                  <StatusBadge tone={statusTone(installment.status)}>
                    {t(open ? 'agenda.statusOpen' : 'agenda.statusClosed')}
                  </StatusBadge>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>

      <SelectionSummary
        selectedCount={selected.length}
        totalLabel={formatMoney(total, locale)}
        onClear={clear}
        onContinue={continueToPortability}
      />
    </div>
  )
}
