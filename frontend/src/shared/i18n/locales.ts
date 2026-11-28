/**
 * The locales of the interface.
 *
 * The interface speaks pt-BR by default and negotiates the same three
 * languages with the API through the `Accept-Language` header. The preference
 * lives in a cookie: the server reads it on the next request, and the browser
 * reads it without a round trip.
 */
export const SUPPORTED_LOCALES = ['pt-BR', 'en', 'es'] as const

export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]

export const DEFAULT_LOCALE: SupportedLocale = 'pt-BR'

export const LOCALE_COOKIE = 'consigport-locale'

export const isSupportedLocale = (value: string): value is SupportedLocale =>
  (SUPPORTED_LOCALES as readonly string[]).includes(value)

/**
 * Resolves any language tag to a supported locale.
 *
 * `pt`, `pt-BR` and `PT-br` all mean pt-BR; anything unknown falls back to
 * pt-BR, which is the default of the whole platform.
 */
export const resolveLocale = (value: string | null | undefined): SupportedLocale => {
  if (value === null || value === undefined) {
    return DEFAULT_LOCALE
  }

  const candidate = value.trim().toLowerCase()
  if (candidate.length === 0) {
    return DEFAULT_LOCALE
  }

  const exact = SUPPORTED_LOCALES.find((locale) => locale.toLowerCase() === candidate)
  if (exact !== undefined) {
    return exact
  }

  const language = candidate.split('-')[0]
  const byLanguage = SUPPORTED_LOCALES.find(
    (locale) => locale.toLowerCase().split('-')[0] === language,
  )
  return byLanguage ?? DEFAULT_LOCALE
}

/** Reads the locale from the `Cookie` header of a request. */
export const readLocaleCookie = (cookieHeader: string | null | undefined): SupportedLocale => {
  if (cookieHeader === null || cookieHeader === undefined) {
    return DEFAULT_LOCALE
  }

  for (const part of cookieHeader.split(';')) {
    const [name, ...rest] = part.trim().split('=')
    if (name === LOCALE_COOKIE) {
      return resolveLocale(decodeURIComponent(rest.join('=')))
    }
  }

  return DEFAULT_LOCALE
}

/** The cookie that persists the language choice for a year. */
export const localeCookie = (locale: SupportedLocale): string =>
  `${LOCALE_COOKIE}=${locale}; Path=/; Max-Age=31536000; SameSite=Lax`
