import type { ReactNode } from 'react'

import { createApiClient, resolveApiBaseUrl } from '../../../shared/api/client.ts'
import { parseApiError, type ApiError } from '../../../shared/api/errors.ts'
import type { SupportedLocale } from '../../../shared/i18n/locales.ts'
import type { Translator } from '../../../shared/i18n/server.ts'
import { buttonClasses } from '../../../shared/ui/button.tsx'
import { Card } from '../../../shared/ui/card.tsx'
import { PageHeader } from '../../../shared/ui/page-header.tsx'
import { StatusBadge, type StatusTone } from '../../../shared/ui/status-badge.tsx'
import { OperationStatus } from '../ui/operation-status.tsx'

/**
 * The panel of the operation.
 *
 * A Server Component: the first state of the operation is read during the
 * render, with the language of the cookie in the `Accept-Language`, and the
 * browser takes over from there. There is no ISR anywhere: the data of a
 * transaction is read when the page is rendered and refreshed by the island
 * that owns it.
 */

export type OperationPageProps = {
  operationId: string
  locale: SupportedLocale
  t: Translator
}

function OperationShell({ t, children }: { readonly t: Translator; children: ReactNode }) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <PageHeader
        eyebrow={t('operations.eyebrow')}
        title={t('operations.title')}
        description={t('operations.description')}
      />
      {children}
    </section>
  )
}

type OperationStateProps = {
  readonly tone: StatusTone
  readonly label: string
  readonly message: string
  readonly action?: { readonly href: string; readonly label: string }
}

function OperationState({ tone, label, message, action }: OperationStateProps) {
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
function OperationErrorState({
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
      <OperationState
        tone='neutral'
        label={t('operations.stateUnknown')}
        message={message}
        action={{ href: '/portabilidade', label: t('operations.create') }}
      />
    )
  }

  return (
    <OperationState
      tone='error'
      label={t('operations.stateError')}
      message={message}
      action={{
        href: `/operacoes?operationId=${encodeURIComponent(operationId)}`,
        label: t('common.retry'),
      }}
    />
  )
}

/** The operation, read during the render and followed by the island. */
export async function OperationContent({ operationId, locale, t }: OperationPageProps) {
  const client = createApiClient(resolveApiBaseUrl(), locale)
  const { data, error } = await client.GET('/api/v1/portability-operations/{operationId}', {
    params: { path: { operationId } },
  })

  if (data === undefined) {
    return <OperationErrorState failure={parseApiError(error)} operationId={operationId} t={t} />
  }

  return <OperationStatus operationId={operationId} initialOperation={data} />
}

/** The panel of the operation the URL named. */
export function OperationPage(props: OperationPageProps) {
  return (
    <OperationShell t={props.t}>
      <OperationContent {...props} />
    </OperationShell>
  )
}

/** The panel without an operation: the screen asks for one. */
export function OperationWithoutId({ t }: { readonly t: Translator }) {
  return (
    <OperationShell t={t}>
      <OperationState
        tone='neutral'
        label={t('operations.stateUnknown')}
        message={t('operations.missingOperation')}
        action={{ href: '/portabilidade', label: t('operations.create') }}
      />
    </OperationShell>
  )
}
