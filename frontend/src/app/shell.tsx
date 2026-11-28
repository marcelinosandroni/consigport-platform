import type { ReactNode } from 'react'

import { Navigation } from './navigation.tsx'
import { LanguageSwitcher } from '../shared/i18n/language-switcher.tsx'
import { I18nProvider } from '../shared/i18n/provider.tsx'
import { QueryProvider } from '../shared/api/query-provider.tsx'
import type { SupportedLocale } from '../shared/i18n/locales.ts'

/**
 * The application shell: a glass navigation rail over the Tier 0 canvas, a
 * centred content column and a quiet footer.
 *
 * Dark only by design (Matrix theme). Mobile first: the navigation wraps, the
 * content reflows, and no target is smaller than 44px.
 */

export type ShellProps = {
  pathname: string
  locale: SupportedLocale
  children: ReactNode
}

export function Shell({ pathname, locale, children }: ShellProps) {
  return (
    <I18nProvider initialLocale={locale}>
      <QueryProvider>
        <div className='flex min-h-screen flex-col bg-surface-base text-text-primary'>
          <header className='sticky top-0 z-50 border-b border-border-subtle bg-surface-base/82 backdrop-blur-[12px]'>
            <div className='mx-auto flex w-full max-w-[1320px] flex-wrap items-center gap-x-space-lg gap-y-space-sm px-gutter py-space-sm md:px-gutter-desktop'>
              <a
                href='/'
                className='flex min-h-11 items-center gap-space-sm font-mono text-label-mono uppercase tracking-[0.06em] text-text-primary'
              >
                <span aria-hidden='true' className='size-2 rounded-full bg-primary' />
                ConsigPort
              </a>

              <Navigation pathname={pathname} />

              <div className='ml-auto'>
                <LanguageSwitcher />
              </div>
            </div>
          </header>

          <main className='mx-auto w-full max-w-[1320px] flex-1 px-gutter py-margin md:px-gutter-desktop md:py-margin-desktop'>
            {children}
          </main>

          <footer className='border-t border-border-subtle'>
            <div className='mx-auto flex w-full max-w-[1320px] flex-wrap items-center justify-between gap-space-sm px-gutter py-space-md md:px-gutter-desktop'>
              <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-muted'>
                ConsigPort · plataforma de portabilidade
              </p>
              <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-muted'>
                Dark only · Matrix
              </p>
            </div>
          </footer>
        </div>
      </QueryProvider>
    </I18nProvider>
  )
}
