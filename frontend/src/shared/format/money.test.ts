import { describe, expect, it } from 'vitest'

import { formatMoney, sumAmounts } from './money.ts'

/** The currency of pt-BR is separated by a no-break space. */
const plain = (value: string): string => value.replace(/\u00a0/g, ' ')

describe('formatMoney', () => {
  it('shows the amount in reais, in the format of the reader', () => {
    expect(plain(formatMoney(450, 'pt-BR'))).toBe('R$ 450,00')
    expect(plain(formatMoney(450, 'en'))).toBe('R$450.00')
  })

  it('keeps the cents', () => {
    expect(plain(formatMoney(120.5, 'pt-BR'))).toBe('R$ 120,50')
  })
})

describe('sumAmounts', () => {
  it('adds without the drift of the floating point', () => {
    expect(sumAmounts([450.1, 300.2])).toBe(750.3)
    expect(sumAmounts([0.1, 0.2])).toBe(0.3)
  })

  it('sums nothing into zero', () => {
    expect(sumAmounts([])).toBe(0)
  })
})
