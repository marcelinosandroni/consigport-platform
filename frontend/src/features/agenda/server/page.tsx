import type { ReactNode } from 'react'

import { createApiClient, resolveApiBaseUrl } from '../../../shared/api/client.ts'
import { parseApiError, type ApiError } from '../../../shared/api/errors.ts'
import type { SupportedLocale } from '../../../shared/i18n/locales.ts'
import type { Translator } from '../../../shared/i18n/server.ts'
import { buttonClasses } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { PageHeader } from '../../../shared/ui/page-header.tsx'
import { StatusBadge, type StatusTone } from '../../../shared/ui/status-badge.tsx'
import { InstallmentTable } from '../ui/installment-table.tsx'

/**
 * The agenda of the beneficiary.
 *
 * A Server Component: the agenda is read on the server, during the render,
 * with the language of the cookie in the `Accept-Language`. The browser never
 * asks for the schedule — it only chooses the installments.
 */

export type AgendaPageProps = {
  beneficiaryId: string
  locale: SupportedLocale
  t: Translator
}

function AgendaShell({ t, children }: { t: Translator; children: ReactNode }) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <PageHeader
        eyebrow={t('agenda.eyebrow')}
        title={t('agenda.title')}
        description={t('agenda.description')}
      />
      {children}
    </section>
  )
}

type AgendaStateProps = {
  tone: StatusTone
  label: string
  message: string
  action?: { href: string; label: string }
}

/**
 * A state of the agenda: the empty one, the forbidden one, the failure.
 *
 * The screen always says something. An agenda that could not be read is not
 * a blank page: it names the state and offers the way out.
 */
function AgendaState({ tone, label, message, action }: AgendaStateProps) {
  return (
    <Card className='flex flex-col items-start gap-space-md p-space-lg'>
      <StatusBadge tone={tone}>{label}</StatusBadge>
      <p className='max-w-[64ch] text-body-md text-text-secondary'>{message}</p>
      {action ? (
        <a href={action.href} className={buttonClasses({ variant: 'primary' })}>
          {action.label}
        </a>
      ) : null}
    </Card>
  )
}

/**
 * The failure of the agenda.
 *
 * The message is keyed by the stable code of the contract — the catalog of
 * the interface wins — and the message of the server is the fallback, so the
 * screen still speaks when the catalog does not know the code.
 */
function AgendaErrorState({
  failure,
  beneficiaryId,
  t,
}: {
  failure: ApiError
  beneficiaryId: string
  t: Translator
}) {
  const message = t(`errors.${failure.code}`, failure.message)

  if (failure.code === 'AUTHORIZATION_REQUIRED') {
    return (
      <AgendaState
        tone='technical'
        label={t('agenda.stateForbidden')}
        message={message}
        action={{ href: '/autorizacao', label: t('agenda.authorize') }}
      />
    )
  }

  if (failure.code === 'BENEFICIARY_NOT_FOUND' || failure.code === 'AUTHORIZATION_NOT_FOUND') {
    return <AgendaState tone='neutral' label={t('agenda.stateUnknown')} message={message} />
  }

  return (
    <AgendaState
      tone='error'
      label={t('agenda.stateError')}
      message={message}
      action={{
        href: `/agenda?beneficiaryId=${encodeURIComponent(beneficiaryId)}`,
        label: t('common.retry'),
      }}
    />
  )
}

/**
 * The agenda itself, read during the render.
 *
 * The states are decided by the code of the error, not by the HTTP status:
 * the same status carries different codes depending on the route.
 */
export async function AgendaContent({ beneficiaryId, locale, t }: AgendaPageProps) {
  const client = createApiClient(resolveApiBaseUrl(), locale)
  const { data, error } = await client.GET('/api/v1/beneficiaries/{beneficiaryId}/agenda', {
    params: { path: { beneficiaryId } },
  })

  if (data === undefined) {
    return <AgendaErrorState failure={parseApiError(error)} beneficiaryId={beneficiaryId} t={t} />
  }

  if (data.installments.length === 0) {
    return <AgendaState tone='neutral' label={t('agenda.stateEmpty')} message={t('agenda.empty')} />
  }

  return <InstallmentTable installments={data.installments} beneficiaryId={data.beneficiaryId} />
}

/** The agenda of a beneficiary that the URL named. */
export function AgendaPage(props: AgendaPageProps) {
  return (
    <AgendaShell t={props.t}>
      <AgendaContent {...props} />
    </AgendaShell>
  )
}

/** The agenda without a beneficiary: the screen asks for one and points to the consent. */
export function AgendaWithoutBeneficiary({ t }: { t: Translator }) {
  return (
    <AgendaShell t={t}>
      <AgendaState
        tone='neutral'
        label={t('agenda.stateUnknown')}
        message={t('agenda.missingBeneficiary')}
        action={{ href: '/autorizacao', label: t('agenda.authorize') }}
      />
    </AgendaShell>
  )
}
