import type { SupportedLocale } from '../i18n/locales.ts'

/**
 * The money of the platform.
 *
 * The API carries a plain decimal amount — the consignado domain is Brazilian,
 * so the interface shows it in reais. The format follows the language of the
 * reader: the same number reads `R$ 450,00` in Portuguese, `R$ 450.00` in
 * English and `450,00 R$` in Spanish.
 */

/** The currency of the platform. */
export const PLATFORM_CURRENCY = 'BRL'

/**
 * Formats an amount for the reader.
 *
 * `Intl` does the work, so the rounding, the separators and the symbol
 * position belong to the locale — never to a hand-written mask.
 */
export const formatMoney = (amount: number, locale: SupportedLocale): string =>
  new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: PLATFORM_CURRENCY,
    currencyDisplay: 'narrowSymbol',
  }).format(amount)

/**
 * Sums amounts without the drift of the floating point.
 *
 * The total shown to the reader is the sum of the selected installments, and
 * it has to be the exact sum: each amount is turned into cents, the cents are
 * added, and only then the total goes back to a decimal.
 */
export const sumAmounts = (amounts: readonly number[]): number =>
  amounts.reduce((total, amount) => total + Math.round(amount * 100), 0) / 100
