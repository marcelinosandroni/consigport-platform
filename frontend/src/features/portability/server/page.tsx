import type { Translator } from '../../../shared/i18n/server.ts'
import { buttonClasses } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { PageHeader } from '../../../shared/ui/page-header.tsx'
import { StatusBadge } from '../../../shared/ui/status-badge.tsx'
import { ReceivingAccountForm } from '../ui/receiving-account-form.tsx'

/**
 * The portability request.
 *
 * A Server Component: it shows what is about to be ported — the installments
 * the visitor chose in the agenda — and hands the account form to the client
 * leaf. The page fetches nothing and holds no state.
 */

export type PortabilityPageProps = {
  beneficiaryId: string
  agreementId: string
  installmentIds: readonly string[]
  t: Translator
}

export function PortabilityPage({
  beneficiaryId,
  agreementId,
  installmentIds,
  t,
}: PortabilityPageProps) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <PageHeader
        eyebrow={t('portability.eyebrow')}
        title={t('portability.title')}
        description={t('portability.description')}
      />

      <Card className='flex flex-col gap-space-xs p-space-md'>
        <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
          {t('portability.selection')}
        </p>
        <ul className='flex flex-wrap gap-space-sm'>
          {installmentIds.map((installmentId) => (
            <li key={installmentId} className='font-mono text-body-sm text-text-primary'>
              {installmentId}
            </li>
          ))}
        </ul>
      </Card>

      <ReceivingAccountForm
        beneficiaryId={beneficiaryId}
        agreementId={agreementId}
        installmentIds={installmentIds}
      />
    </section>
  )
}

/** The request without a selection: the screen points back to the agenda. */
export function PortabilityWithoutSelection({ t }: { t: Translator }) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <PageHeader
        eyebrow={t('portability.eyebrow')}
        title={t('portability.title')}
        description={t('portability.description')}
      />

      <Card className='flex flex-col items-start gap-space-md p-space-lg'>
        <StatusBadge tone='neutral'>{t('portability.stateEmpty')}</StatusBadge>
        <p className='max-w-[64ch] text-body-md text-text-secondary'>{t('portability.noSelection')}</p>
        <a href='/agenda' className={buttonClasses({ variant: 'primary' })}>
          {t('portability.choose')}
        </a>
      </Card>
    </section>
  )
}
