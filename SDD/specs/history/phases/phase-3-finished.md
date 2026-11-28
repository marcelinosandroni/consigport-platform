# Fase 3 — Frontend RSC (concluída)

- **Data de Conclusão:** 2026-11-28
- **Tag:** `v0.4.0`
- **Release:** <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.4.0>
- **Objetivo:** UI React 19 RSC na porta `5173`, design Matrix, i18n e fluxo completo.

---

## 1. Entregas

| Módulo | Entrega | Estado |
|---|---|---|
| Contrato | Cliente OpenAPI tipado gerado de `contracts/consigport-api.yaml`, com `openapi-fetch`, base URL por ambiente e erro mapeado pelo `code` | ✅ |
| Tipos | Esquemas exportados pelo contrato — nenhum schema escrito à mão | ✅ |
| i18n | Catálogos pt-BR/en/es com o mesmo conjunto de chaves, fallback pt-BR, cookie `consigport-locale` e `Accept-Language` em toda pergunta à API | ✅ |
| Primitivas | Botão, campo, tabela, badge, cartão e cabeçalho de página sobre os tokens do tema Matrix — nenhum hex nem `rgba` em classe | ✅ |
| Opt-in | Server Action com validação Zod, erro localizado pelo `code` e a fila `409`/`422` com a mensagem do servidor | ✅ |
| Agenda | Consulta da agenda no servidor, seleção apenas de parcelas `OPEN`, total recalculado a cada render e a seleção viajando pela URL | ✅ |
| Portabilidade | Conta de recebimento, envio idempotente com `Idempotency-Key` por tentativa e o protocolo no painel de sucesso | ✅ |
| Acompanhamento | Polling de 2 s na ilha cliente (TanStack Query), parando no estado terminal e respeitando o `nextPollAt` | ✅ |
| Auditoria | Trilha imutável lida no servidor, em ordem cronológica, sem edição nem exclusão | ✅ |
| Acessibilidade | Skip link, landmarks nomeados, ordem do tabulador, alvos de 44px, anel de foco e `prefers-reduced-motion` | ✅ |

## 2. Tasks

| ID | Entrega | Branch | PR | Estado |
|---|---|---|---|---|
| 3.1 | Cliente OpenAPI tipado | `feat/031-frontend-openapi-client` | [#56](https://github.com/marcelinosandroni/consigport-platform/pull/56) | `[x]` |
| 3.2 | Catálogos i18n | `feat/032-frontend-i18n-catalogs` | [#57](https://github.com/marcelinosandroni/consigport-platform/pull/57) | `[x]` |
| 3.3 | Primitivas de UI | `feat/033-frontend-ui-primitives` | [#58](https://github.com/marcelinosandroni/consigport-platform/pull/58) | `[x]` |
| 3.4 | Fluxo de autorização | `feat/034-frontend-authorization-flow` | [#59](https://github.com/marcelinosandroni/consigport-platform/pull/59) | `[x]` |
| 3.5 | Agenda e seleção de parcelas | `feat/035-frontend-agenda-selection` | [#60](https://github.com/marcelinosandroni/consigport-platform/pull/60) | `[x]` |
| 3.6 | Conta de recebimento e envio | `feat/036-frontend-portability-submit` | [#61](https://github.com/marcelinosandroni/consigport-platform/pull/61) | `[x]` |
| 3.7 | Painel de status com polling | `feat/037-frontend-operations-polling` | [#62](https://github.com/marcelinosandroni/consigport-platform/pull/62) | `[x]` |
| 3.8 | Timeline de auditoria | `feat/038-frontend-audit-timeline` | [#63](https://github.com/marcelinosandroni/consigport-platform/pull/63) | `[x]` |
| 3.9 | Responsividade, acessibilidade e testes finais | `feat/039-frontend-responsive-a11y` | [#64](https://github.com/marcelinosandroni/consigport-platform/pull/64) | `[x]` |
| 3.10 | Promoção da `v0.4.0` e arquivamento da fase | `chore/0310-phase-3-release-archive` | [#65](https://github.com/marcelinosandroni/consigport-platform/pull/65), [#66](https://github.com/marcelinosandroni/consigport-platform/pull/66) | `[x]` |

## 3. Critérios de Saída

- [x] Todas as tasks `[x]` com evidência colada
- [x] `npm run --prefix frontend lint`, `typecheck`, `test:unit` e `build` verdes
- [x] 29 arquivos de teste e 159 testes do frontend verdes
- [x] Tipos gerados a partir de `contracts/consigport-api.yaml` — o contrato público não foi alterado
- [x] Catálogos pt-BR/en/es com o mesmo conjunto de chaves
- [x] Contraste AA comprovado por teste que lê os tokens do tema
- [x] `prefers-reduced-motion` respeitado e breakpoints do `DESIGN.md` §4b auditados
- [x] Job de documentação e contratos verde nos nove PRs da fase
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.4.0`

## 4. Evidência da Release

- PRs de promoção: [#65](https://github.com/marcelinosandroni/consigport-platform/pull/65)
  (`develop → staging`), [#66](https://github.com/marcelinosandroni/consigport-platform/pull/66)
  (`staging → main`) e [#67](https://github.com/marcelinosandroni/consigport-platform/pull/67)
  (release-please, tag `v0.4.0`). As três promoções passaram por onze checks verdes.
- Release publicada: <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.4.0>
  (2026-11-28T09:56:18Z), com o CHANGELOG gerado pelo `release-please` a partir dos
  commits promovidos — ninguém escreve a tag à mão.
- CI do `main` verde no push da release: run
  [`46132773593`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46132773593)
  — onze jobs, todos verdes (`E2E — Playwright` e `Performance Smoke — k6` sem suíte
  neste commit, como esperado até as tasks 4.3 e 4.4).
- Imagens publicadas no GHCR com `v0.4.0`, `0.4.0` e `latest` (run
  [`46132827648`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46132827648),
  `workflow_dispatch` em `release.yml` com a tag `v0.4.0`):
  - `backend` — `sha256:6ea6cac724c9746bcc160c07236609df0bedced9bb2d05cb3dcc7083b5c1ee6c`
  - `central-convenios-mock` — `sha256:40d50903aa7b8460c03b8f7dfa081f40ea6f8f7e9380d7fa462a50482702709d`
  - `frontend` — `sha256:228d5e761c950643e8a0f8665bf6126d06c471fafa06f3ee9d1f5bd55f32ed95`
- Run `CD Staging` [`46132952326`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46132952326)
  (tag `v0.4.0`): postgres, backend, mock e frontend `Healthy`;
  `/actuator/health` → `{"status":"UP","groups":["liveness","readiness"]}` nos dois serviços;
  o frontend respondeu `200` em `/`; teardown verde.
- As imagens foram publicadas por `workflow_dispatch` em `release.yml` com a tag
  `v0.4.0`: o `release-please` cria a tag pela API do GitHub, o que não dispara o
  evento `push` da tag — o job `publish-images` fica `skipped` no push da branch. O
  caminho é o mesmo documentado em
  [`../../../../docs/process/RELEASE.md`](../../../../docs/process/RELEASE.md) §6.
- **Observação:** o run de CI do PR de release
  [`46131523964`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46131523964)
  foi registrado como falha com **zero jobs** ("This run likely failed because of a
  workflow file issue"), sem qualquer passo executado. O código promovido é o mesmo
  que o CI do `main` provou verde no run `46132773593`; o mesmo comportamento ocorreu
  com o PR de release da `v0.3.0`, que também ficou sem checks. Nenhum job de produto
  rodou no run falho, então nada foi comprovado contra ele.

## 5. Dívidas Conhecidas

- A proteção de branches (`main`, `staging`, `develop`) permanece **desativada por
  decisão do responsável**: os merges são integrados via comando (`gh pr merge`).
  A ativação fica registrada em [`../../../../docs/process/GITFLOW.md`](../../../../docs/process/GITFLOW.md).
- **Portão de formatação sem implementação:** o portão 3.2 do `docs/quality/GATES.md`
  exige `./mvnw -q spotless:check`, mas nenhum `pom.xml` declara o plugin Spotless.
  Ativar o Spotless exige formatar o código existente, o que produz um diff largo que
  deve ficar em task própria.
- **Teto de tentativas para `404` da Central:** a operação que a Central não conhece
  continua em polling a cada 60 s, para sempre (`retry_count` cresce sem limite).
- **Protocolo repetido pela Central:** quando a Central reemite um protocolo já
  guardado, a violação da restrição chega como `DataIntegrityViolationException` e a
  API responde `500` em vez de um erro mapeado.
- **Central indisponível no opt-in:** `POST /api/v1/authorizations` responde
  `503 CENTRAL_UNAVAILABLE`, código que o contrato público não lista para essa rota.
  Alinhar contrato e mapeamento exige autorização do responsável.
- **`nextPollAt` fora do contrato publicado:** o domínio e o `ARCHITECTURE.md` §11
  definem o campo, mas o `OperationDetail` do contrato público ainda não o publica. O
  frontend o lê de forma defensiva; alinhar o contrato exige autorização do responsável.
- **Prova geométrica da responsividade:** as quatro regras do `DESIGN.md` §4b.2 têm
  prova estrutural (breakpoints, nome acessível, alvo de 44px, anel de foco) e a
  prova geométrica — `scrollWidth` real de 390px, caixa real de cada alvo — chega com
  o Playwright da task 4.3.
- O detalhamento de cada dívida, com a evidência que a originou, está em
  [`../../BACKLOG.md`](../../BACKLOG.md).

## 6. Próxima Fase

**Fase 4 — Qualidade e vitrine** (7 tasks), começando pela task 4.1 (gates de contrato).
Catálogo em [`../../BACKLOG.md`](../../BACKLOG.md).
