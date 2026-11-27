# ConsigPort — Roadmap

> Visão macro. **Não trabalhe a partir deste arquivo**; a task ativa está em
> [`PLAN.md`](./PLAN.md) e o detalhe atômico em [`BACKLOG.md`](./BACKLOG.md).
> Regra: nenhuma task entra no PLAN sem existir aqui.

---

## 1. Estado atual

| Item | Estado |
|---|---|
| Bootstrap do repositório | ✅ `main` com commit inicial |
| Fase 0 — Fundação | ✅ concluída e arquivada, tag `v0.1.0` |
| Fase 1 — Central mock | ✅ concluída e arquivada, tag `v0.2.0` |
| Fase em andamento | Fase 2 — Backend core (task 2.17 em voo) |
| Próxima task | 2.17 — Fechamento do backend |
| Versão | `v0.2.0` (a Fase 2 fecha em `v0.3.0`) |

A Fase 0 entregou specs, ADRs, harness, gates, contratos, scaffolds, CI/CD e a
release `v0.1.0`. A Fase 1 entregou a Central de Convênios simulada, com HMAC,
idempotência, caos determinístico e contrato publicado. O código de aplicação do
backend começa na Fase 2, sempre em branch própria e via PR.

---

## 2. Fases

| Fase | Objetivo | Tasks | Branch | Tag |
|---|---|---|---|---|
| 0 — Fundação | Specs, contratos, CI/CD, Docker base, scaffolds | 12 | `docs/000-sdd-foundation`, `feat/00x-*` | `v0.1.0` ✅ |
| 1 — Central mock | Simular a Central com HMAC, idempotência, caos e admin | 7 | `feat/01x-central-mock` | `v0.2.0` ✅ |
| 2 — Backend core | DDD + hexagonal + ACL + Flyway + i18n + polling + Swagger | 17 | `feat/02x-backend-*` | `v0.3.0` |
| 3 — Frontend RSC | React 19 RSC, design Matrix, i18n, fluxo completo | 9 | `feat/03x-frontend-*` | `v0.4.0` |
| 4 — Qualidade | Contratos, E2E, k6, Docker full, CD, release, README | 7 | `feat/04x-quality-*` | `v1.0.0` |

Total: **52 tasks**, cada uma com 2–4 arquivos e um commit.

---

## 3. Fase 0 — Fundação (concluída)

**Objetivo:** deixar o projeto executável em Docker, com contratos antes do código,
CI/CD real e governança de branch.

**Tasks**

| ID | Entrega | Estado |
|---|---|---|
| 0.1 | Specs, ADRs, harness, gates, CI de docs e correção do `AGENTS.md` | `[x]` |
| 0.2 | Gitflow, PR template, CODEOWNERS e proteção de branch documentada | `[x]` |
| 0.3 | `.gitignore`, `.env.example`, `compose.yaml` com PostgreSQL 16 | `[x]` |
| 0.4 | Maven Wrapper + reactor raiz com `backend` e `central-convenios-mock` | `[x]` |
| 0.5 | `contracts/consigport-api.yaml`, `contracts/central-api.yaml`, vetor HMAC | `[x]` |
| 0.6 | Scaffold do backend (pom, aplicação, `application.yml`) | `[x]` |
| 0.7 | Scaffold do mock (pom, aplicação, `application.yml`) | `[x]` |
| 0.8 | Configuração do frontend RSC (package, Vite, TS, Tailwind) | `[x]` |
| 0.9 | Shell RSC + tema Matrix + fontes locais | `[x]` |
| 0.10 | `ci.yml` completo (docs, backend, frontend, contratos, E2E, k6 smoke, segurança) | `[x]` |
| 0.11 | `release-please`, `release.yml`, `cd-staging.yml`, imagens GHCR | `[x]` |
| 0.12 | Promoção da `v0.1.0` e arquivamento da fase | `[x]` |

**Saída**
- `docker compose config -q` verde.
- `./mvnw -N validate` e scaffolds compilando.
- `npm ci --prefix frontend && npm run --prefix frontend build` verde.
- Contratos válidos no Spectral.
- CI de documentação verde no PR.
- Tag `v0.1.0`.

**Riscos**
- RSC em Vite ainda é uma fronteira móvel; o shell estático é o primeiro entregável.
- Branch protection depende de permissão no GitHub; a task 0.2 documenta e aplica via `gh`.

---

## 4. Fase 1 — Central mock (concluída)

**Objetivo:** Central determinística na porta `8081`, com contrato PT e Swagger.

**Tasks**

| ID | Entrega | Estado |
|---|---|---|
| 1.1 | Store em memória, fixtures e administração | `[x]` |
| 1.2 | Consulta de agenda | `[x]` |
| 1.3 | Opt-in de autorização | `[x]` |
| 1.4 | Submissão de portabilidade | `[x]` |
| 1.5 | Consulta de status com `Retry-After` | `[x]` |
| 1.6 | HMAC e idempotência | `[x]` |
| 1.7 | Caos determinístico e contrato publicado | `[x]` |

**Saída**
- Opt-in, agenda, portabilidade `202`, status `204/200`, HMAC, idempotência, caos e admin.
- Testes de contrato e MockMvc verdes (39 testes no mock).
- Tag `v0.2.0` com as três imagens no GHCR e staging validado.

**Riscos**
- Assinatura canônica divergente entre mock e backend; o vetor compartilhado é a defesa.
- Contadores de caos mal definidos; a especificação exige sequência determinística.

---

## 5. Fase 2 — Backend core

**Objetivo:** core DDD/hexagonal na porta `8080`, com PostgreSQL, Flyway, ACL, i18n,
polling resiliente e Swagger.

**Saída**
- Domínio puro + ArchUnit + JaCoCo verdes.
- Migração V1 aplicada em Testcontainers.
- `POST /portability-operations` idempotente; polling até terminal; conta atualizada.
- Erros localizados em pt-BR/en/es; OpenAPI sem drift.
- Tag `v0.3.0`.

**Riscos**
- Drift entre `contracts/consigport-api.yaml` e springdoc; o CI compara os dois.
- Concorrência do worker; `@Version` + transação curta.
- Duplicação de submissão; a mesma `Idempotency-Key` em todos os retries.

---

## 6. Fase 3 — Frontend RSC

**Objetivo:** UI React 19 RSC na porta `5173`, design Matrix, i18n e fluxo completo.

**Saída**
- Shell estático + streaming SSR + ilhas interativas.
- Opt-in, agenda, seleção, conta, status e timeline.
- Catálogos pt-BR/en/es e troca de idioma.
- Sem Next.js, sem Redux, sem Axios.
- Tag `v0.4.0`.

**Riscos**
- Complexidade do RSC manual; congelar o template oficial na task 0.8.
- Cache/streaming mal configurado; E2E contra o build de produção.

---

## 7. Fase 4 — Qualidade e vitrine

**Objetivo:** provar o sistema no CI/CD, com contratos, E2E, performance e release.

**Saída**
- Spectral, oasdiff e Schemathesis no CI.
- Playwright E2E no compose; k6 smoke no PR e load no nightly/release.
- Imagens GHCR, staging e produção; release-please com tag e changelog.
- README em pt-BR com execução, decisões, bibliotecas, trade-offs e próximos passos.
- Tag `v1.0.0`.

**Riscos**
- Tempo de CI; jobs pesados ficam em nightly/release.
- Relatórios de performance; k6 gera HTML e thresholds.

---

## 8. Regras de avanço

1. Uma task por vez no `PLAN.md`, sempre `[-]`.
2. Nenhum commit direto em `main`, `staging` ou `develop`.
3. PR obrigatório com CI verde; merge via squash.
4. Task só fecha com evidência colada e commit.
5. Fase só fecha com todas as tasks `[x]`, tag e histórico em `specs/history/phases/`.
