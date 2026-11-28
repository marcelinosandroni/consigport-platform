'use client'

import { useTranslation } from 'react-i18next'

import { SUPPORTED_LOCALES, isSupportedLocale } from './locales.ts'
import { useLocale } from './provider.tsx'

/**
 * The language switch.
 *
 * A native `select` keeps the component honest: it works with the keyboard,
 * with a screen reader and without JavaScript.
 *
 * The switch has two halves: the client leaves change language at once, and
 * the cookie asks the server to render the page again — the Server Components
 * only speak the language the server resolved. The browser entry listens to
 * `replaceState`, so touching the history is what triggers the new render.
 */
export const LanguageSwitcher = () => {
  const { t } = useTranslation()
  const { locale, setLocale } = useLocale()

  return (
    <label className='flex items-center gap-space-sm font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
      <span>{t('language.label')}</span>
      <select
        aria-label={t('language.label')}
        className='min-h-11 rounded border border-border-subtle bg-surface-raised px-space-sm font-mono text-label-mono uppercase tracking-[0.06em] text-text-primary focus-visible:ring-2 focus-visible:ring-primary'
        value={locale}
        onChange={(event) => {
          const next = event.target.value
          if (!isSupportedLocale(next)) {
            return
          }
          setLocale(next)
          window.history.replaceState(null, '', window.location.href)
        }}
      >
        {SUPPORTED_LOCALES.map((option) => (
          <option key={option} value={option}>
            {t(`language.${option}`)}
          </option>
        ))}
      </select>
    </label>
  )
}
