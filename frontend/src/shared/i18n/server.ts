import { catalogs } from './i18n.ts'
import { DEFAULT_LOCALE, type SupportedLocale } from './locales.ts'

/**
 * The translator of the server components.
 *
 * The pages are Server Components: they cannot read the React context of the
 * browser, so they translate with the locale the server already resolved from
 * the cookie. The lookup is a plain walk of the catalog — no async, no
 * instance, no surprise.
 */

const lookup = (catalog: unknown, key: string): string | undefined => {
  let current: unknown = catalog

  for (const segment of key.split('.')) {
    if (typeof current !== 'object' || current === null || Array.isArray(current)) {
      return undefined
    }
    current = (current as Record<string, unknown>)[segment]
  }

  return typeof current === 'string' ? current : undefined
}

/**
 * Translates a key for a locale.
 *
 * The fallback is pt-BR — the default of the platform — and the key itself
 * when even the fallback does not know it, so a missing translation shows up
 * in the screen instead of rendering an empty hole. The caller may offer a
 * last resort: the message that came with the error, already localized by the
 * backend.
 */
export const translate = (locale: SupportedLocale, key: string, fallback?: string): string =>
  lookup(catalogs[locale], key) ?? lookup(catalogs[DEFAULT_LOCALE], key) ?? fallback ?? key

export type Translator = (key: string, fallback?: string) => string

/** Binds a locale to the translator, the shape the pages receive. */
export const createTranslator = (locale: SupportedLocale): Translator =>
  (key, fallback) => translate(locale, key, fallback)
