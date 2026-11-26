# Fase 0 — Fundação (concluída)

- **Data de Conclusão:** 2026-11-26
- **Tag:** `v0.1.0`
- **Release:** <https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.1.0>
- **Objetivo:** projeto executável em Docker, contratos antes do código, CI/CD real e
  governança de branch.

---

## 1. Entregas

| Módulo | Entrega | Estado |
|---|---|---|
| Documentação | Specs em pt-BR, ADRs, harness de IA e gates de qualidade | ✅ |
| Contratos | `contracts/consigport-api.yaml`, `contracts/central-api.yaml`, `contracts/hmac-vector.json` | ✅ |
| Backend | Scaffold Spring Boot 3.5 / Java 21 com health, actuator e springdoc | ✅ |
| Mock | Scaffold Spring MVC na porta 8081, sem banco e sem Flyway | ✅ |
| Frontend | React 19.2 + Vite RSC + Tailwind 4, shell Matrix e fontes locais | ✅ |
| Infra | `.env.example`, `compose.yaml` (PostgreSQL 16), `compose.staging.yaml` | ✅ |
| CI | `ci.yml` (docs, backend, mock, frontend, contratos, E2E, k6, segurança) e `nightly.yml` | ✅ |
| CD | `release.yml`, `cd-staging.yml`, imagens no GHCR e tag SemVer | ✅ |

## 2. Tasks

| ID | Entrega | Branch | Estado |
|---|---|---|---|
| 0.1 | Refinamento SDD completo | `docs/000-sdd-foundation` | `[x]` |
| 0.2 | Gitflow, PR template, CODEOWNERS e proteção documentada | `feat/002-gitflow` | `[x]` |
| 0.3 | `.gitignore`, `.env.example` e `compose.yaml` com PostgreSQL 16 | `feat/003-local-env` | `[x]` |
| 0.4 | Maven Wrapper e reactor com `backend` e `central-convenios-mock` | `feat/004-maven-reactor` | `[x]` |
| 0.5 | Contratos OpenAPI e vetor HMAC | `feat/005-contracts` | `[x]` |
| 0.6 | Scaffold do backend | `feat/006-backend-scaffold` | `[x]` |
| 0.7 | Scaffold do mock | `feat/007-mock-scaffold` | `[x]` |
| 0.8 | Base do frontend RSC | `feat/008-frontend-base` | `[x]` |
| 0.9 | Shell RSC e design Matrix | `feat/009-shell-matrix` | `[x]` |
| 0.10 | Pipeline de integração contínua | `feat/010-ci-pipeline` | `[x]` |
| 0.11 | Release e entrega contínua | `feat/011-release-cd` + `fix/011-release-closure` | `[x]` |
| 0.12 | Promoção da `v0.1.0` e arquivamento da fase | `chore/012-phase-0-archive` | `[x]` |

## 3. Critérios de Saída

- [x] Todas as tasks `[x]` com evidência colada
- [x] `docker compose config -q` verde
- [x] `./mvnw -N validate` e scaffolds compilando
- [x] `npm ci --prefix frontend && npm run --prefix frontend build` verde
- [x] Contratos válidos no Spectral
- [x] Job de documentação verde no PR
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.1.0`

## 4. Evidência da Release

- PRs de promoção: [#18](https://github.com/marcelinosandroni/consigport-platform/pull/18)
  (`develop → staging`), [#19](https://github.com/marcelinosandroni/consigport-platform/pull/19)
  (`staging → main`) e [#20](https://github.com/marcelinosandroni/consigport-platform/pull/20)
  (release-please).
- Imagens publicadas no GHCR com `v0.1.0`, `0.1.0` e `latest`:
  - `backend` — `sha256:bc42cbb391575e1da940536168c60fb25040b0745c5fa1e7c21a0ff40fc452a1`
  - `central-convenios-mock` — `sha256:caadf344bacfd9a64e85e9218913d4aecbe04bb665713f76b7dc971c17a37cb5`
  - `frontend` — `sha256:278a1113b55827c1667828f8243797b88002e9dcb785a81450e3bf23ca2bc2f3`
- Run `CD Staging` `45661533860` (tag `v0.1.0`): postgres, backend, mock e frontend
  `healthy`; `/actuator/health` → `{"status":"UP"}`; teardown verde.

## 5. Dívidas Conhecidas

- A proteção de branches (`main`, `staging`, `develop`) permanece **desativada por
  decisão do responsável**: os merges são integrados via comando (`gh pr merge`).
  A ativação fica registrada em [`../../../../docs/process/GITFLOW.md`](../../../../docs/process/GITFLOW.md).
- O staging da Fase 0 é efêmero no runner do GitHub; o host persistente e a aprovação
  manual chegam na task 4.6.
- Backend e mock são apenas scaffolds; as regras de negócio começam na Fase 1.

## 6. Próxima Fase

**Fase 1 — Central mock** (7 tasks), começando pela task 1.1 (store em memória,
fixtures e administração). Catálogo em [`../../BACKLOG.md`](../../BACKLOG.md).
