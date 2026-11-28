# 🎯 PLAN — a task em voo

> 🛑 **Regra fixa (agente de IA, leia antes de codar):**
> Existe **exatamente uma** task `[-]` neste arquivo. Se houver duas, o agente parou no
> lugar errado.
> Task maior que uma hora **é dividida antes de começar**. Nunca pule etapa: implemente,
> teste, cole a evidência, commite.
> Estados: `[ ]` a fazer, `[-]` em andamento, `[x]` feito.
>
> Catálogo completo: [`BACKLOG.md`](./BACKLOG.md). Visão macro:
> [`ROADMAP.md`](./ROADMAP.md). Regras: [`../AGENTS.md`](../AGENTS.md).

---

## Fase 0 — Fundação: concluída ✅

> Arquivada em [`history/phases/phase-0-finished.md`](./history/phases/phase-0-finished.md).
> Tag [`v0.1.0`](https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.1.0)
> publicada com as três imagens no GHCR e o staging validado.

### Critérios de saída da fase

- [x] Todas as tasks `[x]` com evidência colada
- [x] `docker compose config -q` verde
- [x] `./mvnw -N validate` e scaffolds compilando
- [x] `npm ci --prefix frontend && npm run --prefix frontend build` verde
- [x] Contratos válidos no Spectral
- [x] Job de documentação verde no PR
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.1.0`

---

## Fase 1 — Central mock: concluída ✅

> Arquivada em [`history/phases/phase-1-finished.md`](./history/phases/phase-1-finished.md).
> Tag `v0.2.0` publicada com a imagem do mock no GHCR.

### Tasks

- [x] **1.1 — Store em memória, fixtures e administração**
      Estado determinístico reiniciável com `ConcurrentHashMap`; fixtures do
      `ARCHITECTURE.md` §4; `POST /_admin/reset` → `204` e `GET /_admin/state` com o
      estado completo.
- [x] **1.2 — Consulta de agenda**
      `GET /beneficiarios/{id}/agenda` em PT com `parcelaId`, `convenioId`, `valor`,
      `dataDesconto` e `status`; `404` para beneficiário desconhecido e `403` sem
      autorização registrada (BR-01).
- [x] **1.3 — Opt-in de autorização**
      `POST /autorizacoes` → `201` com `autorizacaoId`; repetição devolve `200` com o
      mesmo id (BR-02); campos vazios → `400 REQUISICAO_INVALIDA`.
- [x] **1.4 — Submissão de portabilidade**
      `POST /operacoes-portabilidade` → `202` com protocolo `PRT-yyyyMMdd-NNNN`; estado
      inicial `PENDENTE`; parcelas inexistentes ou fechadas → `422 PARCELA_INVALIDA`.
- [x] **1.5 — Consulta de status com `Retry-After`**
      Antes do delay → `204` com `Retry-After: 2`; depois → `200` com `CONFIRMADA` ou
      `REJEITADA`; protocolo desconhecido → `404 OPERACAO_NAO_ENCONTRADA`.
- [x] **1.6 — HMAC e idempotência**
      Assinatura inválida ou timestamp fora de ±300 s → `401 ASSINATURA_INVALIDA`; mesma
      `Idempotency-Key` e payload igual → mesmo protocolo; payload diferente →
      `409 IDEMPOTENCIA_CONFLITO`.
- [x] **1.7 — Caos determinístico e contrato publicado**
      `-REJ` e soma acima de `5000.00` → `REJEITADA` com
      `MARGEM_CONSIGNADA_INSUFICIENTE`; `-TIMEOUT` atrasa os dois primeiros GETs; headers
      `X-Mock-Delay-Ms` e `X-Mock-Force-Error`; contrato exportado idêntico a
      `contracts/central-api.yaml`.

---

## Fase 2 — Backend core: concluída ✅

> Arquivada em [`history/phases/phase-2-finished.md`](./history/phases/phase-2-finished.md).
> Tag `v0.3.0` publicada com as três imagens no GHCR e o staging validado.

### Tasks

- [x] **2.1 — Esqueleto hexagonal e regras ArchUnit**
      `domain` sem Spring, JPA, Jackson ou HTTP; `application` só importa `domain`;
      `config` é o único composition root; regra verificada por `HexagonalArchitectureTest`.
- [x] **2.2 — Value objects compartilhados**
      `BeneficiaryId`, `AgreementId`, `InstallmentId`, `IdempotencyKey`, `Money` e
      `ReceivingAccount` com validação no construtor e igualdade por valor.
- [x] **2.3 — Agregado de autorização**
      `Authorization` com identidade, beneficiário, convênio e `createdAt`;
      `AuthorizationRepository` com `findByBeneficiaryAndAgreement` e `save`, sem anotação
      de framework.
- [x] **2.4 — Agregado de portabilidade e máquina de estados**
      `PortabilityOperation` com ao menos uma parcela, total igual à soma e snapshot
      imutável; transições apenas as da tabela de estados do `ARCHITECTURE.md` §7.
- [x] **2.5 — Evento de auditoria append-only**
      `OperationEvent` com os campos do `ARCHITECTURE.md` §2, sem mutadores públicos;
      `eventType` restrito ao enum; porta de repositório apenas com `append` e consultas.
- [x] **2.6 — Registro e consulta de autorização**
      `RegisterAuthorization` idempotente: repetir o opt-in devolve o registro existente e
      replica o consentimento na Central pela porta; `FindAuthorization` consulta o
      registro; `Clock` injetado.
- [x] **2.7 — Consulta de agenda com ACL**
      `AgendaGateway` e `Installment` no domínio; `CentralAcl` traduz o contrato PT da
      Central para o domínio EN; `403` sem autorização e `404` repassado.
- [x] **2.8 — Persistência e migração V1**
      DDL do `ARCHITECTURE.md` §6 com três tabelas, índices, constraints e trigger
      append-only; status restrito por `CHECK`; snapshot em JSONB não vazio.
- [x] **2.9 — Início da operação com idempotência**
      `POST /portability-operations` exige `Idempotency-Key`; o replay devolve `202` com
      `Idempotent-Replay: true`; payload divergente → `409 IDEMPOTENCY_CONFLICT`; parcela
      inelegível → `422`.
- [x] **2.10 — Assinatura HMAC e envio à Central**
      `HmacSigner` reproduz o vetor de `contracts/hmac-vector.json`; `CentralHttpClient`
      submete a operação assinada; `CREATED` → `PENDING` com `protocol`; falha após
      retries → `INTEGRATION_ERROR`.
- [x] **2.11 — Polling resiliente**
      `PollOperation` acompanha a Central até o estado terminal; intervalo de 2 s e lote
      de 20; honora o `Retry-After`; `204` gera `POLL_ATTEMPT`; o reenvio usa a mesma
      `Idempotency-Key`.
- [x] **2.12 — Auto-transição de integração e respostas tipadas da Central**
      Auto-transição `INTEGRATION_ERROR` → `INTEGRATION_ERROR` declarada na §7, com
      caminho único de falha; `404` da Central vira resposta tipada, fora do circuito e
      com o corpo bruto na trilha; rejeição com motivo fora do contrato fecha a
      operação como `REJECTED` com o código `UNMAPPED_REJECTION`.
- [x] **2.13 — Confirmação e atualização de conta**
      `CONFIRMADA` → `CONFIRMED` → `COMPLETED` com `account_updated_at`; `REJEITADA` →
      `REJECTED` com o motivo; os dois eventos gravados na mesma transação.
- [x] **2.14 — Consulta de operações e trilha**
      Leitura paginada com filtro por status; eventos em ordem cronológica;
      `404 OPERATION_NOT_FOUND`.
- [x] **2.15 — Recursos REST e tratamento de erros**
      Rotas do `ARCHITECTURE.md` §8 e erro com `code`, `message`, `locale`,
      `timestamp`, `path` e `details`.
- [x] **2.16 — i18n, Swagger e snapshot OpenAPI**
      `Accept-Language` com fallback `pt-BR`; `code` nunca traduzido; contrato gerado
      equivalente a `contracts/consigport-api.yaml`.
- [x] **2.17 — Gate de cobertura do núcleo**
      Domínio e aplicação com ≥ 80% de linhas e de branches; o build falha quando a
      cobertura cai; relatório anexado no CI.
- [x] **2.18 — Fluxo completo com o banco real**
      `CREATED` → `COMPLETED` e `CREATED` → `REJECTED` comprovados com o PostgreSQL real
      e a trilha completa de eventos.

---

## Fase 3 — Frontend RSC: concluída ✅

> Arquivada em [`history/phases/phase-3-finished.md`](./history/phases/phase-3-finished.md).
> Tag [`v0.4.0`](https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.4.0)
> publicada com as três imagens no GHCR e o staging validado.

### Tasks

- [x] **3.1 — Cliente OpenAPI tipado**
      Tipos gerados de `contracts/consigport-api.yaml`; `openapi-fetch` com base URL por
      ambiente; erro mapeado por `code`.
- [x] **3.2 — Catálogos i18n**
      Catálogos pt-BR/en/es com o mesmo conjunto de chaves; fallback pt-BR; troca de
      idioma persistida; `Accept-Language` enviado à API.
- [x] **3.3 — Primitivas de UI**
      Componentes base fiéis ao design Matrix: tokens de `theme.css`, foco visível,
      contraste AA e variantes sem valores mágicos.
- [x] **3.4 — Fluxo de autorização**
      Server Component renderiza; ilha cliente envia `POST /authorizations`; erro
      localizado; sucesso leva à agenda.
- [x] **3.5 — Agenda e seleção de parcelas**
      Apenas `OPEN` selecionável; total recalculado; estado vazio e `403` tratados.
- [x] **3.6 — Conta de recebimento e envio**
      Validação com Zod; `Idempotency-Key` gerada uma vez por tentativa; `409` e `422`
      exibidos com mensagem do servidor.
- [x] **3.7 — Painel de status com polling**
      TanStack Query com intervalo de 2 s; para em estado terminal; `nextPollAt`
      respeitado; sem ISR.
- [x] **3.8 — Timeline de auditoria**
      Trilha imutável em ordem cronológica: tipo, status anterior e novo, detalhe
      e horário; sem edição nem exclusão.
- [x] **3.9 — Responsividade, acessibilidade e testes finais**
      Breakpoints do `DESIGN.md` §4b; navegação por teclado;
      `prefers-reduced-motion` respeitado; testes de unidade verdes.
- [x] **3.10 — Promoção da release `v0.4.0` e arquivamento da Fase 3**
      `develop` promovido para `staging` e `main` por PR; tag `v0.4.0` criada pelo
      release-please; release no GitHub com o CHANGELOG; imagens no GHCR com
      `v0.4.0`; run de staging verde; fase arquivada em `specs/history/phases/`.

---

## Fase atual: 4 — Qualidade e vitrine

> Objetivo: provar o sistema no CI/CD — contratos, E2E no navegador, performance,
> segurança, entrega contínua e a vitrine do repositório.
> Task em voo: **4.2 — Testes de propriedade com Schemathesis**.

### Tasks

- [x] **4.1 — Gates de contrato**
      Spectral sem erro; `oasdiff` compara com a versão de `main` e falha em breaking
      change; snapshot do springdoc comparado ao contrato.
- [-] **4.2 — Testes de propriedade com Schemathesis**
      Schemathesis roda contra o compose; qualquer resposta fora do contrato falha o
      job; relatório anexado.

### Como usar este arquivo

1. Copie a próxima task do [`BACKLOG.md`](./BACKLOG.md) para cá, com o ID e o objetivo.
2. Marque `[-]` **antes** de começar e crie a branch da task.
3. Marque `[x]` **depois** de colar a saída verde dos comandos.
4. Ao fechar a fase, arquive em `specs/history/phases/` e crie a tag.

---

## 📋 Checklist de entrega (colar ao fim de cada task)

```markdown
**Evidência:**
- `node scripts/check-specs.mjs` → exit 0
- `node SDD/SKILLS/check-docs/check-docs.mjs` → exit 0
- (comando da task) → exit 0

**Arquivos tocados:** (lista, no máximo 5)
**Commit:** `type(scope): descrição. (Agent: OpenCode - MiniMax-M3.1-Flash)`
```

Gate completo e o que cada comando prova: [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).
