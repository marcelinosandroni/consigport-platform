# Fase 2 — Backend core (concluída)

- **Data de Conclusão:** 2026-11-28
- **Tag:** `v0.3.0`
- **Release:** <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.3.0>
- **Objetivo:** core DDD/hexagonal na porta `8080`, com PostgreSQL, Flyway, ACL, i18n,
  polling resiliente e Swagger.

---

## 1. Entregas

| Módulo | Entrega | Estado |
|---|---|---|
| Arquitetura | Esqueleto hexagonal com ArchUnit: `domain` puro, `application` sem framework e `config` como único composition root | ✅ |
| Domínio | Value objects, agregados `Authorization` e `PortabilityOperation`, evento de auditoria append-only | ✅ |
| Persistência | Migração V1 do Flyway, adaptadores JPA com `@Version` e tradução das violações de unicidade | ✅ |
| Integração | ACL PT→EN, `CentralHttpClient` com HMAC canônico, idempotência e `Retry-After` | ✅ |
| Casos de uso | Opt-in, agenda, início idempotente, polling em lote de 20, fechamento transacional e consultas | ✅ |
| Borda HTTP | Recursos REST do `ARCHITECTURE.md` §8 e erro com `code`, `message`, `locale`, `timestamp`, `path` e `details` | ✅ |
| i18n e contrato | Catálogos pt-BR/en/es, `/v3/api-docs`, Swagger UI e comparação canônica com `contracts/consigport-api.yaml` | ✅ |
| Qualidade | Portão JaCoCo (≥ 80% de linhas e de branches no núcleo) e teste de integração do ciclo de vida no PostgreSQL 16 real | ✅ |

## 2. Tasks

| ID | Entrega | Branch | PR | Estado |
|---|---|---|---|---|
| 2.1 | Esqueleto hexagonal e regras ArchUnit | `feat/021-backend-hexagonal-skeleton` | [#33](https://github.com/marcelinosandroni/consigport-platform/pull/33) | `[x]` |
| 2.2 | Value objects compartilhados | `feat/022-backend-value-objects` | [#34](https://github.com/marcelinosandroni/consigport-platform/pull/34) | `[x]` |
| 2.3 | Agregado de autorização | `feat/023-backend-authorization-aggregate` | [#35](https://github.com/marcelinosandroni/consigport-platform/pull/35) | `[x]` |
| 2.4 | Agregado de portabilidade e máquina de estados | `feat/024-backend-portability-aggregate` | [#36](https://github.com/marcelinosandroni/consigport-platform/pull/36) | `[x]` |
| 2.5 | Evento de auditoria append-only | `feat/025-backend-audit-event` | [#37](https://github.com/marcelinosandroni/consigport-platform/pull/37) | `[x]` |
| 2.6 | Registro e consulta de autorização | `feat/026-backend-register-authorization` | [#38](https://github.com/marcelinosandroni/consigport-platform/pull/38) | `[x]` |
| 2.7 | Consulta de agenda com ACL | `feat/027-backend-agenda-acl` | [#39](https://github.com/marcelinosandroni/consigport-platform/pull/39) | `[x]` |
| 2.8 | Persistência e migração V1 | `feat/028-backend-persistence-v1` | [#40](https://github.com/marcelinosandroni/consigport-platform/pull/40) | `[x]` |
| 2.9 | Início da operação com idempotência | `feat/029-backend-start-operation-idempotency` | [#41](https://github.com/marcelinosandroni/consigport-platform/pull/41) | `[x]` |
| 2.10 | Assinatura HMAC e envio à Central | `feat/030-backend-central-hmac-submission` | [#42](https://github.com/marcelinosandroni/consigport-platform/pull/42) | `[x]` |
| 2.11 | Polling resiliente | `feat/031-backend-resilient-polling` | [#43](https://github.com/marcelinosandroni/consigport-platform/pull/43) | `[x]` |
| 2.12 | Auto-transição de integração e respostas tipadas da Central | `feat/032-backend-typed-central-answers` | [#44](https://github.com/marcelinosandroni/consigport-platform/pull/44) | `[x]` |
| 2.13 | Confirmação e atualização de conta | `feat/033-backend-complete-operation` | [#45](https://github.com/marcelinosandroni/consigport-platform/pull/45) | `[x]` |
| 2.14 | Consulta de operações e trilha | `feat/034-backend-operation-queries` | [#46](https://github.com/marcelinosandroni/consigport-platform/pull/46) | `[x]` |
| 2.15 | Recursos REST e tratamento de erros | `feat/035-backend-rest-resources` | [#47](https://github.com/marcelinosandroni/consigport-platform/pull/47) | `[x]` |
| 2.16 | i18n, Swagger e snapshot OpenAPI | `feat/036-backend-i18n-openapi` | [#48](https://github.com/marcelinosandroni/consigport-platform/pull/48) | `[x]` |
| 2.17 | Gate de cobertura do núcleo | `feat/037-backend-coverage-gate` | [#49](https://github.com/marcelinosandroni/consigport-platform/pull/49) | `[x]` |
| 2.18 | Fluxo completo com o banco real | `feat/038-backend-lifecycle-integration` | [#50](https://github.com/marcelinosandroni/consigport-platform/pull/50) | `[x]` |

## 3. Critérios de Saída

- [x] Todas as tasks `[x]` com evidência colada
- [x] `./mvnw -B verify` verde no reactor completo (backend 481 testes, mock 39 testes)
- [x] Cobertura do núcleo (domínio + aplicação) com 99,71% de branches, 98,12% de linhas e
      100% de classes — acima do portão de 80%
- [x] `domain` sem Spring, JPA, Jackson ou HTTP, verificado por `HexagonalArchitectureTest`
- [x] Vetor HMAC de `contracts/hmac-vector.json` reproduzido byte a byte
- [x] Contrato gerado pelo springdoc equivalente a `contracts/consigport-api.yaml`
- [x] Ciclo de vida completo provado contra o PostgreSQL 16 real (`CREATED` →
      `COMPLETED`, `CREATED` → `REJECTED` e rollback da trilha)
- [x] Job de documentação e contratos verde nos dezoito PRs
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.3.0`

## 4. Evidência da Release

- PRs de promoção: [#51](https://github.com/marcelinosandroni/consigport-platform/pull/51)
  (`develop → staging`), [#52](https://github.com/marcelinosandroni/consigport-platform/pull/52)
  (`staging → main`) e [#53](https://github.com/marcelinosandroni/consigport-platform/pull/53)
  (release-please, tag `v0.3.0`).
- Release publicada: <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.3.0>.
- CI do `main` verde no push da release: run
  [`46094354043`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46094354043).
- Imagens publicadas no GHCR com `v0.3.0`, `0.3.0` e `latest`:
  - `backend` — `sha256:9cd162f26c72c90b78744f58fe5c4ab98e925b0da6865a3f7eafe53a0b0e1cf0`
  - `central-convenios-mock` — `sha256:f651491272db2cebd7c0b4ff435b2a28b1ea39f902e570ef299026e209f18b3a`
  - `frontend` — `sha256:fd11ce5fe79d970e977c9ff19928683643f98465d3ce0d4436e15ef4da6336ac`
- Run `CD Staging` [`46095428615`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/46095428615)
  (tag `v0.3.0`): postgres, backend, mock e frontend `healthy`;
  `/actuator/health` → `{"status":"UP","groups":["liveness","readiness"]}` nos dois serviços;
  teardown verde.
- As imagens foram publicadas por `workflow_dispatch` em `release.yml` com a tag
  `v0.3.0` (run `46095256699`): o `release-please` cria a tag pela API do GitHub, o que
  não dispara o evento `push` da tag. O caminho é o mesmo documentado em
  [`../../../../docs/process/RELEASE.md`](../../../../docs/process/RELEASE.md) §6.

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
- **Alinhar "Falhas até `INTEGRATION_ERROR`":** o `ARCHITECTURE.md` §11 e o ADR-0003
  dizem `5`, mas a implementação entra em `INTEGRATION_ERROR` assim que o cliente HTTP
  esgota as 3 tentativas.
- **Protocolo repetido pela Central:** quando a Central reemite um protocolo já
  guardado, a violação da restrição chega como `DataIntegrityViolationException` e a
  API responde `500` em vez de um erro mapeado.
- **Central indisponível no opt-in:** `POST /api/v1/authorizations` responde
  `503 CENTRAL_UNAVAILABLE`, código que o contrato público não lista para essa rota.
  Alinhar contrato e mapeamento exige autorização do responsável.
- O detalhamento de cada dívida, com a evidência que a originou, está em
  [`../../BACKLOG.md`](../../BACKLOG.md).

## 6. Próxima Fase

**Fase 3 — Frontend RSC** (9 tasks), começando pela task 3.1 (cliente OpenAPI tipado).
Catálogo em [`../../BACKLOG.md`](../../BACKLOG.md).
