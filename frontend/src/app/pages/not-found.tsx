import { buttonClasses } from '../../shared/ui/button.tsx'

/**
 * The 404: the shell stays, the content explains, one action brings the
 * visitor back.
 */
export function NotFoundPage() {
  return (
    <section className="flex flex-col items-start gap-space-md">
      <p className="font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
        Erro 404
      </p>
      <h1 className="text-headline-lg">Página não encontrada</h1>
      <p className="max-w-[64ch] text-body-lg text-text-secondary">
        O endereço acessado não corresponde a nenhuma rota do ConsigPort.
      </p>
      <a href="/" className={buttonClasses({ variant: 'secondary' })}>
        Voltar ao início
      </a>
    </section>
  )
}
