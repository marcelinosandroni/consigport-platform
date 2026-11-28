'use client'

import { useTranslation } from 'react-i18next'

/**
 * The navigation rail.
 *
 * It is a client leaf so the language switch is visible at once: the labels
 * come from the catalog of the chosen language, without a new request.
 */

const NAV_ITEMS = [
  { href: '/', key: 'nav.home' },
  { href: '/autorizacao', key: 'nav.authorization' },
  { href: '/portabilidade', key: 'nav.portability' },
  { href: '/operacoes', key: 'nav.operations' },
  { href: '/agenda', key: 'nav.agenda' },
  { href: '/auditoria', key: 'nav.audit' },
] as const

export function Navigation({ pathname }: { pathname: string }) {
  const { t } = useTranslation()

  return (
    <nav aria-label={t('nav.label')} className='flex flex-wrap items-center gap-space-sm'>
      {NAV_ITEMS.map((item) => {
        const active = pathname === item.href
        return (
          <a
            key={item.href}
            href={item.href}
            aria-current={active ? 'page' : undefined}
            className={[
              'inline-flex min-h-11 items-center rounded px-space-sm',
              'font-mono text-label-mono uppercase tracking-[0.06em]',
              'transition-colors duration-150',
              // The hairline migrates to the accent colour on focus: the keyboard
              // sees where it is, the pointer does not pay for it.
              'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
              active ? 'text-primary' : 'text-text-secondary hover:text-text-primary',
            ].join(' ')}
          >
            {t(item.key)}
          </a>
        )
      })}
    </nav>
  )
}
