import { describe, expect, it } from 'vitest'

import { formatDate } from './date.ts'

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
