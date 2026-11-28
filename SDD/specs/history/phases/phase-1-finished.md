# Fase 1 — Central mock (concluída)

- **Data de Conclusão:** 2026-11-26
- **Tag:** `v0.2.0`
- **Release:** <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.2.0>
- **Objetivo:** simular a Central de Convênios com resposta assíncrona, HMAC,
  idempotência, caos determinístico e endpoints administrativos.

---

## 1. Entregas

| Módulo | Entrega | Estado |
|---|---|---|
| Estado | `InMemoryCentralStore` com fixtures determinísticas, reset e snapshot | ✅ |
| Agenda | `GET /beneficiarios/{id}/agenda` com gate de autorização (BR-01) | ✅ |
| Opt-in | `POST /autorizacoes` com `201`/`200` e unicidade por beneficiário e convênio (BR-02) | ✅ |
| Portabilidade | `POST /operacoes-portabilidade` → `202` com protocolo `PRT-yyyyMMdd-NNNN` (BR-03/BR-04) | ✅ |
| Status | `GET /operacoes-portabilidade/{protocolo}` → `204` com `Retry-After` e `200` no estado terminal | ✅ |
| Integridade | HMAC-SHA256 canônico sobre o vetor compartilhado e janela de ±300 s (BR-06) | ✅ |
| Idempotência | `Idempotency-Key` com retorno do mesmo protocolo e `409` em payload divergente (BR-07) | ✅ |
| Caos | `-REJ`, soma acima de `5000.00`, `-TIMEOUT`, `X-Mock-Delay-Ms` e `X-Mock-Force-Error` (BR-14) | ✅ |
| Contrato | Cópia publicada em `resources/openapi/central-api.yaml`, idêntica a `contracts/central-api.yaml` | ✅ |
| Admin | `POST /_admin/reset` → `204` e `GET /_admin/state` com o estado completo | ✅ |

## 2. Tasks

| ID | Entrega | Branch | PR | Estado |
|---|---|---|---|---|
| 1.1 | Store em memória, fixtures e administração | `feat/101-mock-store` | [#22](https://github.com/marcelinosandroni/consigport-platform/pull/22) | `[x]` |
| 1.2 | Consulta de agenda | `feat/102-mock-agenda` | [#23](https://github.com/marcelinosandroni/consigport-platform/pull/23) | `[x]` |
| 1.3 | Opt-in de autorização | `feat/103-mock-authorization` | [#24](https://github.com/marcelinosandroni/consigport-platform/pull/24) | `[x]` |
| 1.4 | Submissão de portabilidade | `feat/104-mock-portability` | [#25](https://github.com/marcelinosandroni/consigport-platform/pull/25) | `[x]` |
| 1.5 | Consulta de status com `Retry-After` | `feat/105-mock-status` | [#26](https://github.com/marcelinosandroni/consigport-platform/pull/26) | `[x]` |
| 1.6 | HMAC e idempotência | `feat/106-mock-hmac` | [#27](https://github.com/marcelinosandroni/consigport-platform/pull/27) | `[x]` |
| 1.7 | Caos determinístico e contrato publicado | `feat/107-mock-chaos` | [#28](https://github.com/marcelinosandroni/consigport-platform/pull/28) | `[x]` |

## 3. Critérios de Saída

- [x] Todas as tasks `[x]` com evidência colada
- [x] `./mvnw -B -pl central-convenios-mock -am verify` verde (39 testes)
- [x] Vetor HMAC de `contracts/hmac-vector.json` reproduzido byte a byte
- [x] Contrato exportado sem diferença em relação a `contracts/central-api.yaml`
- [x] Job de documentação e contratos verde nos sete PRs
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.2.0`

## 4. Evidência da Release

- PRs de promoção: [#29](https://github.com/marcelinosandroni/consigport-platform/pull/29)
  (`develop → staging`), [#30](https://github.com/marcelinosandroni/consigport-platform/pull/30)
  (`staging → main`) e [#31](https://github.com/marcelinosandroni/consigport-platform/pull/31)
  (release-please, tag `v0.2.0`).
- Release publicada: <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.2.0>.
- Imagens publicadas no GHCR com `v0.2.0`, `0.2.0` e `latest`:
  - `backend` — `sha256:b036e79c7e9cae9396154b74bfe263c5b8b7fd9cc2471d1449f920b2d049c75a`
  - `central-convenios-mock` — `sha256:8097fae4e6b83cf52e058c5c99b063fad368baf697016e227ed551c58fe43417`
  - `frontend` — `sha256:788b083540757b20b8b788782e8853af31e490fa151ba27c93b728c6b6d4196d`
- Run `CD Staging` [`45818653277`](https://github.com/marcelinosandroni/consigport-platform/actions/runs/45818653277)
  (tag `v0.2.0`): postgres, backend, mock e frontend `healthy`;
  `/actuator/health` → `{"status":"UP"}` nos dois serviços; teardown verde.
- As imagens foram publicadas por `workflow_dispatch` em `release.yml` com a tag
  `v0.2.0` (run `45817926979`): o `release-please` cria a tag pela API do GitHub, o que
  não dispara o evento `push` da tag. O caminho é o mesmo documentado em
  [`../../../../docs/process/RELEASE.md`](../../../../docs/process/RELEASE.md) §6.

## 5. Dívidas Conhecidas

- A proteção de branches (`main`, `staging`, `develop`) permanece **desativada por
  decisão do responsável**: os merges são integrados via comando (`gh pr merge`).
  A ativação fica registrada em [`../../../../docs/process/GITFLOW.md`](../../../../docs/process/GITFLOW.md).
- A rejeição por margem soma apenas as parcelas enviadas na operação; o limite
  conservador de `5000.00` é o do `ARCHITECTURE.md` §5 e não considera outros convênios.
- O atraso de `-TIMEOUT` dorme no thread da requisição; com o delay padrão de 6 s e o
  timeout de leitura do backend em 3 s, o cliente expira antes da resposta, como o E2E exige.

## 6. Próxima Fase

**Fase 2 — Backend core** (16 tasks), começando pela task 2.1 (esqueleto hexagonal e
regras ArchUnit). Catálogo em [`../../BACKLOG.md`](../../BACKLOG.md).
