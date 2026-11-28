import { PageHeader } from '../../../shared/ui/page-header.tsx'
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
      <PageHeader
        eyebrow={t('authorization.eyebrow')}
        title={t('authorization.title')}
        description={t('authorization.description')}
      />

      <AuthorizationForm />
    </section>
  )
}
