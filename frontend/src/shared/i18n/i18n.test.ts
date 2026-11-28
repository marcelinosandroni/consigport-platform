import { describe, expect, it } from 'vitest'

import { catalogs } from './i18n.ts'
import { DEFAULT_LOCALE, LOCALE_COOKIE, readLocaleCookie, resolveLocale } from './locales.ts'

const flattenKeys = (value: unknown, prefix = ''): string[] => {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    return [prefix]
  }

  return Object.entries(value).flatMap(([key, child]) =>
    flattenKeys(child, prefix.length > 0 ? `${prefix}.${key}` : key),
  )
}

describe('catalogs', () => {
  it('has the same key set in the three languages', () => {
    const [ptBR, en, es] = ['pt-BR', 'en', 'es'].map((locale) => flattenKeys(catalogs[locale as keyof typeof catalogs]).sort())

    expect(en).toEqual(ptBR)
    expect(es).toEqual(ptBR)
  })

  it('keeps every error code of the public contract', () => {
    const codes = [
      'VALIDATION_ERROR',
      'AUTHORIZATION_NOT_FOUND',
      'AUTHORIZATION_REQUIRED',
      'BENEFICIARY_NOT_FOUND',
      'OPERATION_NOT_FOUND',
      'IDEMPOTENCY_KEY_REQUIRED',
      'IDEMPOTENCY_CONFLICT',
      'INSTALLMENT_NOT_ELIGIBLE',
      'CENTRAL_UNAVAILABLE',
      'INTERNAL_ERROR',
    ]

    for (const locale of ['pt-BR', 'en', 'es'] as const) {
      for (const code of codes) {
        expect(catalogs[locale].errors).toHaveProperty(code)
      }
    }
  })
})

describe('resolveLocale', () => {
  it('accepts the supported locales', () => {
    expect(resolveLocale('pt-BR')).toBe('pt-BR')
    expect(resolveLocale('en')).toBe('en')
    expect(resolveLocale('es')).toBe('es')
  })

  it('falls back to pt-BR for an unknown language', () => {
    expect(resolveLocale('fr-FR')).toBe(DEFAULT_LOCALE)
    expect(resolveLocale('')).toBe(DEFAULT_LOCALE)
    expect(resolveLocale(undefined)).toBe(DEFAULT_LOCALE)
  })

  it('matches the language of a regional tag', () => {
    expect(resolveLocale('es-AR')).toBe('es')
    expect(resolveLocale('en-US')).toBe('en')
  })
})

describe('readLocaleCookie', () => {
  it('reads the locale from the cookie header', () => {
    expect(readLocaleCookie(`${LOCALE_COOKIE}=es`)).toBe('es')
    expect(readLocaleCookie(`other=1; ${LOCALE_COOKIE}=en; another=2`)).toBe('en')
  })

  it('falls back to pt-BR without the cookie', () => {
    expect(readLocaleCookie(undefined)).toBe(DEFAULT_LOCALE)
    expect(readLocaleCookie('other=1')).toBe(DEFAULT_LOCALE)
  })
})
