'use client'

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { I18nextProvider } from 'react-i18next'

import { setApiLocale } from '../api/client.ts'
import { createI18n } from './i18n.ts'
import { DEFAULT_LOCALE, localeCookie, type SupportedLocale } from './locales.ts'

type LocaleContextValue = {
  readonly locale: SupportedLocale
  readonly setLocale: (locale: SupportedLocale) => void
}

const LocaleContext = createContext<LocaleContextValue | null>(null)

/**
 * The provider of the interface language.
 *
 * It is a client leaf: the server keeps rendering the pages, and only the
 * language switch — plus the `Accept-Language` header of the API — lives on
 * the browser. The choice is persisted in a cookie, so the next request
 * arrives already in the chosen language.
 */
export const I18nProvider = ({
  initialLocale = DEFAULT_LOCALE,
  children,
}: {
  readonly initialLocale?: SupportedLocale
  readonly children: ReactNode
}) => {
  const instance = useMemo(() => createI18n(initialLocale), [initialLocale])
  const [locale, setLocaleState] = useState<SupportedLocale>(initialLocale)
  const [renderedLocale, setRenderedLocale] = useState<SupportedLocale>(initialLocale)

  // The server may deliver another locale — the cookie changed and the page
  // was re-rendered. The leaf follows the server instead of fighting it: the
  // new instance already speaks the new language.
  if (renderedLocale !== initialLocale) {
    setRenderedLocale(initialLocale)
    setLocaleState(initialLocale)
  }

  // The API is asked to answer in the same language the user chose.
  useEffect(() => {
    setApiLocale(initialLocale)
  }, [initialLocale])

  const setLocale = useCallback(
    (next: SupportedLocale) => {
      setLocaleState(next)
      void instance.changeLanguage(next)
      setApiLocale(next)
      document.cookie = localeCookie(next)
    },
    [instance],
  )

  const value = useMemo(() => ({ locale, setLocale }), [locale, setLocale])

  return (
    <LocaleContext.Provider value={value}>
      <I18nextProvider i18n={instance}>{children}</I18nextProvider>
    </LocaleContext.Provider>
  )
}

export const useLocale = (): LocaleContextValue => {
  const value = useContext(LocaleContext)
  if (value === null) {
    throw new Error('useLocale must be used inside the I18nProvider')
  }
  return value
}
