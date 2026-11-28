import { buttonClasses } from '../../shared/ui/button.tsx'
import type { Translator } from '../../shared/i18n/server.ts'

/**
 * The 404: the shell stays, the content explains, one action brings the
 * visitor back.
 */
export function NotFoundPage({ t }: { t: Translator }) {
  return (
    <section className="flex flex-col items-start gap-space-md">
      <p className="font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
        {t('common.appName')} · 404
      </p>
      <h1 className="text-headline-lg">{t('notFound.title')}</h1>
      <p className="max-w-[64ch] text-body-lg text-text-secondary">{t('notFound.description')}</p>
      <a href="/" className={buttonClasses({ variant: 'secondary' })}>
        {t('notFound.back')}
      </a>
    </section>
  )
}
