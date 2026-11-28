import i18next, { type i18n as I18nInstance } from 'i18next'
import { initReactI18next } from 'react-i18next'

import en from './en.json'
import es from './es.json'
import ptBR from './pt-BR.json'
import { DEFAULT_LOCALE, SUPPORTED_LOCALES, type SupportedLocale } from './locales.ts'

/**
 * The catalogs of the interface.
 *
 * The three files share the same key set — a test compares them — and the
 * fallback is pt-BR, the default of the platform.
 */
export const catalogs = {
  'pt-BR': ptBR,
  en,
  es,
} as const

export const createI18n = (locale: SupportedLocale = DEFAULT_LOCALE): I18nInstance => {
  const instance = i18next.createInstance()
  instance.use(initReactI18next)
  void instance.init({
    resources: {
      'pt-BR': { translation: ptBR },
      en: { translation: en },
      es: { translation: es },
    },
    lng: locale,
    fallbackLng: DEFAULT_LOCALE,
    supportedLngs: [...SUPPORTED_LOCALES],
    interpolation: { escapeValue: false },
    returnEmptyString: false,
  })
  return instance
}
