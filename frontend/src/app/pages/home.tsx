import { Badge } from '../../shared/ui/badge.tsx'
import { buttonClasses } from '../../shared/ui/button.tsx'
import { Card } from '../../shared/ui/card.tsx'
import { StatCard } from '../../shared/ui/stat-card.tsx'
import type { Translator } from '../../shared/i18n/server.ts'

/**
 * The landing page of the platform: one message, one action and the numbers
 * that matter. Everything else is noise.
 */
export function HomePage({ t }: { t: Translator }) {
  return (
    <section className="flex flex-col gap-space-2xl md:gap-space-3xl">
      {/* A `div`, not a `header`: the shell already owns the banner, and two
          banners on one page confuse the landmarks a screen reader lists. */}
      <div className="flex flex-col gap-space-md">
        <Badge tone="highlight">{t('home.eyebrow')}</Badge>
        <h1 className="text-display-hero-mobile md:text-display-hero">{t('home.title')}</h1>
        <p className="max-w-[64ch] text-body-lg text-text-secondary">{t('home.subtitle')}</p>
        <div className="flex flex-wrap items-center gap-space-md">
          {/* The calls to action are links, not buttons: they navigate. A
              button that changes the address lies to the keyboard and to the
              screen reader. */}
          <a href="/autorizacao" className={buttonClasses({ variant: 'primary' })}>
            {t('home.ctaNew')}
          </a>
          <a href="/auditoria" className={buttonClasses({ variant: 'secondary' })}>
            {t('home.ctaAudit')}
          </a>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-space-md md:grid-cols-2 lg:grid-cols-3">
        <StatCard
          label={t('home.statsActive')}
          value="12"
          annotation={t('home.statsActiveAnnotation')}
          status="live"
        />
        <StatCard
          label={t('home.statsConfirmation')}
          value="94%"
          annotation={t('home.statsConfirmationAnnotation')}
        />
        <StatCard
          label={t('home.statsCentral')}
          value="1,8 s"
          annotation={t('home.statsCentralAnnotation')}
        />
      </div>

      <Card className="flex flex-col gap-space-sm p-space-lg">
        <p className="font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
          {t('home.systemState')}
        </p>
        <p className="text-body-md text-text-secondary">{t('home.systemStateBody')}</p>
      </Card>
    </section>
  )
}
