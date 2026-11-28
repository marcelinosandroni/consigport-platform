import type { ReactNode } from 'react'

import { createApiClient, resolveApiBaseUrl } from '../../../shared/api/client.ts'
import { parseApiError, type ApiError } from '../../../shared/api/errors.ts'
import type { SupportedLocale } from '../../../shared/i18n/locales.ts'
import type { Translator } from '../../../shared/i18n/server.ts'
import { buttonClasses } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { PageHeader } from '../../../shared/ui/page-header.tsx'
import { StatusBadge, type StatusTone } from '../../../shared/ui/status-badge.tsx'
import { EventTimeline } from '../ui/event-timeline.tsx'

/**
 * The trail of the operation.
 *
 * A Server Component: the trail is read on the server, during the render,
 * with the language of the cookie in the `Accept-Language`. The browser never
 * asks for the events — it only reads them. There is no editing and no
 * deletion: the trail is append-only, and the screen proves it by showing
 * every fact as it was written.
 */

export type AuditPageProps = {
  operationId: string
  locale: SupportedLocale
  t: Translator
}

function AuditShell({ t, children }: { readonly t: Translator; children: ReactNode }) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <PageHeader
        eyebrow={t('audit.eyebrow')}
        title={t('audit.title')}
        description={t('audit.description')}
      />
      {children}
    </section>
  )
}

type AuditStateProps = {
  readonly tone: StatusTone
  readonly label: string
  readonly message: string
  readonly action?: { readonly href: string; readonly label: string }
}

function AuditState({ tone, label, message, action }: AuditStateProps) {
  return (
    <Card className='flex flex-col items-start gap-space-md p-space-lg'>
      <StatusBadge tone={tone}>{label}</StatusBadge>
      <p className='max-w-[64ch] text-body-md text-text-secondary'>{message}</p>
      {action !== undefined ? (
        <a href={action.href} className={buttonClasses({ variant: 'primary' })}>
          {action.label}
        </a>
      ) : null}
    </Card>
  )
}

/**
 * The failure of the query.
 *
 * The message is keyed by the stable code of the contract — the catalog of
 * the interface wins — and the message of the server is the fallback.
 */
function AuditErrorState({
  failure,
  operationId,
  t,
}: {
  readonly failure: ApiError
  readonly operationId: string
  readonly t: Translator
}) {
  const message = t(`errors.${failure.code}`, failure.message)

  if (failure.code === 'OPERATION_NOT_FOUND') {
    return (
      <AuditState
        tone='neutral'
        label={t('audit.stateUnknown')}
        message={message}
        action={{ href: '/portabilidade', label: t('audit.create') }}
      />
    )
  }

  return (
    <AuditState
      tone='error'
      label={t('audit.stateError')}
      message={message}
      action={{
        href: `/auditoria?operationId=${encodeURIComponent(operationId)}`,
        label: t('common.retry'),
      }}
    />
  )
}

/** The trail, read during the render. */
export async function AuditContent({ operationId, locale, t }: AuditPageProps) {
  const client = createApiClient(resolveApiBaseUrl(), locale)
  const { data, error } = await client.GET(
    '/api/v1/portability-operations/{operationId}/events',
    { params: { path: { operationId } } },
  )

  if (data === undefined) {
    return <AuditErrorState failure={parseApiError(error)} operationId={operationId} t={t} />
  }

  if (data.events.length === 0) {
    return (
      <AuditState tone='neutral' label={t('audit.stateEmpty')} message={t('audit.empty')} />
    )
  }

  return <EventTimeline events={data.events} locale={locale} t={t} />
}

/** The trail of the operation the URL named. */
export function AuditPage(props: AuditPageProps) {
  return (
    <AuditShell t={props.t}>
      <AuditContent {...props} />
    </AuditShell>
  )
}

/** The trail without an operation: the screen asks for one. */
export function AuditWithoutOperation({ t }: { readonly t: Translator }) {
  return (
    <AuditShell t={t}>
      <AuditState
        tone='neutral'
        label={t('audit.stateUnknown')}
        message={t('audit.missingOperation')}
        action={{ href: '/portabilidade', label: t('audit.create') }}
      />
    </AuditShell>
  )
}
