import type { SupportedLocale } from '../../../shared/i18n/locales.ts'
import type { Translator } from '../../../shared/i18n/server.ts'
import { EventItem, type OperationEvent } from './event-item.tsx'

/**
 * The trail of the operation.
 *
 * The API already answers the events in chronological order; the timeline
 * sorts them anyway, by the instant and then by the sequential identifier, so
 * the order of the screen never depends on the order of the array. The trail
 * is append-only: there is no action of editing or deleting — the past is
 * read, not rewritten.
 */

export type EventTimelineProps = {
  readonly events: readonly OperationEvent[]
  readonly locale: SupportedLocale
  readonly t: Translator
}

export function EventTimeline({ events, locale, t }: EventTimelineProps) {
  const ordered = [...events].sort((left, right) => {
    const byInstant = Date.parse(left.createdAt) - Date.parse(right.createdAt)
    return byInstant !== 0 ? byInstant : left.eventId - right.eventId
  })

  return (
    <ol className='flex flex-col gap-space-md'>
      {ordered.map((event) => (
        <EventItem key={event.eventId} event={event} locale={locale} t={t} />
      ))}
    </ol>
  )
}
