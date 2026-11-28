import { describe, expect, it } from 'vitest'

import { formatDate, formatDateTime } from './date.ts'

describe('formatDate', () => {
  it('shows the calendar date in the language of the reader', () => {
    expect(formatDate('2026-12-05', 'en')).toBe('Dec 5, 2026')
    expect(formatDate('2026-12-05', 'pt-BR')).toMatch(/^5 de dez\.? de 2026$/)
  })

  it('keeps the day the Central meant', () => {
    // The date has no time and no zone: the day does not move with the clock.
    expect(formatDate('2026-01-01', 'en')).toBe('Jan 1, 2026')
  })

  it('returns an unknown date untouched', () => {
    expect(formatDate('not-a-date', 'pt-BR')).toBe('not-a-date')
  })
})

describe('formatDateTime', () => {
  it('shows the instant in the language and the zone of the reader', () => {
    // The instant is built in the local zone, so the expectation holds
    // wherever the test runs.
    const instant = new Date(2026, 10, 28, 15, 30, 0)

    expect(formatDateTime(instant.toISOString(), 'pt-BR')).toBe('28 de nov. de 2026, 15:30')
    expect(formatDateTime(instant.toISOString(), 'en')).toBe('Nov 28, 2026, 3:30 PM')
  })

  it('returns an unknown instant untouched', () => {
    expect(formatDateTime('not-an-instant', 'pt-BR')).toBe('not-an-instant')
  })
})
