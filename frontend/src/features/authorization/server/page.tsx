import type { Translator } from '../../../shared/i18n/server.ts'
import { AuthorizationForm } from '../ui/authorization-form.tsx'

/**
 * The consent page.
 *
 * A Server Component: it renders the shell of the journey and hands the form
 * to the client leaf. The page itself holds no state and fetches nothing.
 */
export function AuthorizationPage({ t }: { t: Translator }) {
  return (
    <section className='flex flex-col gap-space-lg'>
      <header className='flex flex-col gap-space-sm'>
        <p className='font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary'>
          {t('authorization.eyebrow')}
        </p>
        <h1 className='text-headline-lg'>{t('authorization.title')}</h1>
        <p className='max-w-[64ch] text-body-lg text-text-secondary'>{t('authorization.description')}</p>
      </header>

      <AuthorizationForm />
    </section>
  )
}
