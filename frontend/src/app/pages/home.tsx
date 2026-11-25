import { Badge } from '../../shared/ui/badge.tsx'
import { Button } from '../../shared/ui/button.tsx'
import { Card } from '../../shared/ui/card.tsx'
import { StatCard } from '../../shared/ui/stat-card.tsx'

/**
 * The landing page of the platform: one message, one action and the numbers
 * that matter. Everything else is noise.
 */
export function HomePage() {
  return (
    <section className="flex flex-col gap-space-2xl md:gap-space-3xl">
      <header className="flex flex-col gap-space-md">
        <Badge tone="highlight">Fase 0 · fundação</Badge>
        <h1 className="text-display-hero-mobile md:text-display-hero">
          Portabilidade de convênios sem fricção
        </h1>
        <p className="max-w-[64ch] text-body-lg text-text-secondary">
          O ConsigPort captura a autorização do beneficiário, consulta a agenda de descontos
          futuros e acompanha a portabilidade até a confirmação — com trilha de auditoria
          imutável em cada mudança de estado.
        </p>
        <div className="flex flex-wrap items-center gap-space-md">
          <Button>Nova portabilidade</Button>
          <Button variant="secondary">Ver auditoria</Button>
        </div>
      </header>

      <div className="grid grid-cols-1 gap-space-md sm:grid-cols-2 lg:grid-cols-3">
        <StatCard
          label="Operações ativas"
          value="12"
          annotation="janela de 24 h, confirmação inclusa"
          status="live"
        />
        <StatCard
          label="Taxa de confirmação"
          value="94%"
          annotation="média das últimas 100 operações"
        />
        <StatCard
          label="Retorno da Central"
          value="1,8 s"
          annotation="p95 medido no portão assíncrono"
        />
      </div>

      <Card className="flex flex-col gap-space-sm p-space-lg">
        <p className="font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary">
          Estado do sistema
        </p>
        <p className="text-body-md text-text-secondary">
          A casca visual está pronta. Autorização, agenda, operações e auditoria entram nas
          fatias seguintes, cada uma com seu contrato e sua evidência.
        </p>
      </Card>
    </section>
  )
}
