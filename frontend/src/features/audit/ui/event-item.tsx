import type { components } from '../../../shared/api/schema.ts'
import { formatDateTime } from '../../../shared/format/date.ts'
import { statusLabelKey, statusTone } from '../../../shared/format/status.ts'
import type { SupportedLocale } from '../../../shared/i18n/locales.ts'
import type { Translator } from '../../../shared/i18n/server.ts'
import { StatusBadge } from '../../../shared/ui/status-badge.tsx'

/**
 * One event of the trail.
 *
 * The trail is append-only: there is nothing to edit and nothing to delete, so
 * the component is read-only and needs no browser at all. It is rendered by
 * the server, with the translator of the language the reader chose.
 */

/** The event as the public contract publishes it. */
export type OperationEvent = components['schemas']['OperationEvent']

export type EventItemProps = {
  readonly event: OperationEvent
  readonly locale: SupportedLocale
  readonly t: Translator
}

/** The label of a status: the catalog of the feature, or the raw state. */
const statusLabel = (status: string, t: Translator): string => {
  const key = statusLabelKey(status)
  return key === undefined ? status : t(key, status)
}

export function EventItem({ event, locale, t }: EventItemProps) {
  const label = t(`audit.eventTypes.${event.eventType}`, event.eventType)

  return (
    <li className='flex flex-col gap-space-sm border-l border-border-subtle pl-space-md'>
      <div className='flex flex-wrap items-center gap-space-sm'>
        <StatusBadge tone={statusTone(event.newStatus)}>{label}</StatusBadge>
        <span className='font-mono text-body-sm text-text-secondary'>
          {formatDateTime(event.createdAt, locale)}
        </span>
      </div>

      <p className='text-body-md text-text-primary'>{event.details}</p>

      <p className='font-mono text-body-sm text-text-secondary'>
        {t('audit.previousStatus')}:{' '}
        {event.previousStatus === null
          ? t('audit.noPreviousStatus')
          : statusLabel(event.previousStatus, t)}
        {' · '}
        {t('audit.newStatus')}: {statusLabel(event.newStatus, t)}
      </p>

      {/* The answer of the Central, when there is one to show. */}
      {event.rawPayload !== null ? (
        <details className='flex flex-col gap-space-xs'>
          <summary className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
            {t('audit.rawPayload')}
          </summary>
          <pre className='overflow-x-auto rounded border border-border-subtle p-space-sm font-mono text-body-sm text-text-secondary'>
            {JSON.stringify(event.rawPayload, null, 2)}
          </pre>
        </details>
      ) : null}
    </li>
  )
}
