import type { SupportedLocale } from '../i18n/locales.ts'

/**
 * The dates of the platform.
 *
 * The API sends a calendar date — `2026-12-05` — with no time and no zone.
 * The interface shows it in the language of the reader, on the same day the
 * Central meant: the date is built from its parts in the local time, never
 * from `new Date('2026-12-05')`, which would be midnight UTC and could land
 * on the previous day west of Greenwich.
 */

const ISO_DATE = /^(\d{4})-(\d{2})-(\d{2})/

/**
 * Formats a calendar date for the reader.
 *
 * A value that is not a calendar date is returned untouched: the screen shows
 * what the API sent instead of inventing a date.
 */
export const formatDate = (value: string, locale: SupportedLocale): string => {
  const match = ISO_DATE.exec(value)

  if (match === null) {
    return value
  }

  const year = Number(match[1])
  const month = Number(match[2])
  const day = Number(match[3])

  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(
    new Date(year, month - 1, day),
  )
}
