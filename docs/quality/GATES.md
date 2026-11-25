# Portões de Qualidade (Quality Gates) — ConsigPort

> Critérios objetivos e automatizados que governam a aceitação de código, documentação e releases no ConsigPort.
> Referências: [`../../SDD/APP-STACK.md`](../../SDD/APP-STACK.md), [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md), [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md) e [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md).

---

## 1. Visão Geral e Filosofia

O ConsigPort adota uma política rigorosa de **Portões de Qualidade** para assegurar que nenhuma regressão de contrato, quebra arquitetural, degradação de performance ou violação de integridade chegue aos ambientes de homologação (`staging`) ou produção (`main`).

### Princípios Norteadores:
1. **Verificação Automatizada Antecipada:** Falhas devem ser capturadas no estágio mais barato (localmente via lint/testes unitários ou no PR via CI).
2. **Separação Local vs. CI:** Testes unitários e linters rodam na máquina local. Testes ponta a ponta (E2E com Playwright), testes de carga (k6) e varreduras de segurança rodam **exclusivamente no CI/CD** sobre o ambiente instanciado em Docker Compose.
3. **Contrato Primeiro (Schema-First):** Mudanças em endpoints exigem alteração prévia do contrato OpenAPI; quebras incompatíveis (*breaking changes*) bloqueiam o pipeline.
4. **Arquitetura Imutável:** Regras de arquitetura hexagonal e isolamento de domínio são fiscalizadas em tempo de compilação/teste via ArchUnit.

---

## 2. Matriz de Execução dos Gates

| Portão | Escopo | Execução Local | CI no PR (`develop`/`staging`/`main`) | Push `develop` (CD Staging) | Push `main` (Release) | Nightly (Cron) |
|---|---|:---:|:---:|:---:|:---:|:---:|
| **Docs & Links** | Markdown, links relativos, integridade SDD | ✅ | ✅ | — | — | — |
| **AI Usage Log** | Estrutura das sessões e atualização no PR | ✅ | ✅ | — | — | — |
| **Backend Lint** | Spotless (formatação Java) | ✅ | ✅ | — | — | — |
| **Backend Unit & Arch** | JUnit 5 + AssertJ + ArchUnit | ✅ | ✅ | — | — | — |
| **Backend Integration** | Testcontainers (PostgreSQL 16) + JaCoCo | ✅ | ✅ | — | — | — |
| **Contratos OpenAPI** | Spectral + oasdiff (breaking change) | ✅ | ✅ | — | — | — |
| **Frontend Checks** | ESLint + TypeScript (`tsc`) + Vitest + Build | ✅ | ✅ | — | — | — |
| **Docker Compose** | Validação sintática e compose config | ✅ | ✅ | ✅ | ✅ | — |
| **E2E Smoke** | Playwright (fluxo completo em Docker) | ❌ | ✅ | ✅ | ✅ | ✅ |
| **Performance Smoke** | k6 smoke test (thresholds estritos) | ❌ | ✅ | ✅ | — | — |
| **Performance Load** | k6 load test sustentado + relatório HTML | ❌ | ❌ | — | — | ✅ |
| **Contrato Dinâmico** | Schemathesis (property-based) | ❌ | ✅ (smoke) | — | — | ✅ (full) |
| **Segurança** | Trivy container vulnerability scan | ❌ | ✅ | ✅ | ✅ | ✅ |

---

## 3. Detalhamento de Cada Portão

### 3.1. Portão de Documentação, Especificação (SDD) e Arquitetura
- **Comandos:**
  ```bash
  node SDD/SKILLS/check-docs/check-docs.mjs .
  node scripts/check-specs.mjs
  node scripts/check-architecture.mjs
  node scripts/check-ai-log.mjs
  ```
- **Critérios de Aceite:**
  - Zero links relativos quebrados em toda a árvore Markdown (`check-docs`).
  - Exatamente **uma** tarefa marcada como em andamento `[-]` no [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md).
  - Presença de todos os documentos obrigatórios do SDD Tier 1 e Tier 2.
  - Desenhos de arquitetura validados (SVGs nativos, blocos Mermaid suportados, camadas hexagonais e matriz de rastreabilidade).
  - Registro de evidência em [`../../SDD/docs/EVIDENCE.md`](../../SDD/docs/EVIDENCE.md) para tarefas finalizadas.
  - [`../ai-harness/AI_USAGE_LOG.md`](../ai-harness/AI_USAGE_LOG.md) com sessões numeradas, campos obrigatórios e cronologia; no PR, log alterado e ao menos uma sessão nova quando houver mudança de trabalho.

---

### 3.2. Portão de Backend (Java 21 / Spring Boot 3.5.x)
- **Comandos:**
  ```bash
  ./mvnw -q spotless:check
  ./mvnw clean verify
  ```
- **Critérios de Aceite:**
  - **Formatação:** Código 100% conforme o padrão Spotless (Google Java Format).
  - **Arquitetura (ArchUnit):**
    - Pacote `domain` não referencia Spring, Hibernate, JPA ou bibliotecas de terceiros.
    - Pacote `application` conhece apenas `domain`.
    - Pacotes de adaptadores (`adapters.in`, `adapters.out`) dependem apenas de portas da aplicação/domínio.
    - A Camada Anticorrupção (`adapters.out.central.acl`) isola completamente o vocabulário em português da Central.
    - Ausência total de ciclos de dependência de pacotes.
  - **Cobertura de Testes (JaCoCo):**
    - Mínimo de 80% de cobertura de linhas e branches no domínio e casos de uso de aplicação.
  - **Integração:**
    - Repositórios e migrações Flyway validados com PostgreSQL 16 via Testcontainers.

---

### 3.3. Portão de Contratos de API (OpenAPI 3.1)
- **Workflow:** [`.github/workflows/ci-contracts.yml`](../../.github/workflows/ci-contracts.yml) (dispara em `contracts/**`, `.spectral.yaml` e `scripts/hmac-vector.mjs`).
- **Comandos:**
  ```bash
  npx --yes @stoplight/spectral-cli@6 lint contracts/*.yaml --fail-severity=warn
  node scripts/hmac-vector.mjs
  oasdiff breaking <contrato da branch base> contracts/consigport-api.yaml --fail-on ERR
  oasdiff breaking <contrato da branch base> contracts/central-api.yaml --fail-on ERR
  ```
- **Critérios de Aceite:**
  - Contratos `contracts/consigport-api.yaml` e `contracts/central-api.yaml` válidos e sem erros no linter Spectral.
  - Ausência de breaking changes em relação ao contrato da branch base; a primeira introdução
    do contrato é aceita e o diff é pulado com aviso no log do workflow (`oasdiff`).
  - Coerência estrita entre endpoints expostos pelo Springdoc (`/v3/api-docs`) e o contrato versionado.
  - Vetor de assinatura HMAC validado deterministicamente contra `contracts/hmac-vector.json`.

---

### 3.4. Portão de Frontend (React 19.2 + RSC + Tailwind 4)
- **Comandos:**
  ```bash
  npm run --prefix frontend lint
  npm run --prefix frontend typecheck
  npm run --prefix frontend test:unit
  npm run --prefix frontend build
  ```
- **Critérios de Aceite:**
  - Zero erros de tipagem estrita no TypeScript (`tsc --noEmit`).
  - Linter ESLint e formatação Prettier 100% verdes.
  - Testes unitários de componentes e hooks com Vitest + React Testing Library aprovados.
  - Build estático / RSC gerado com sucesso sem bundles órfãos.

---

### 3.5. Portão de Testes Ponta a Ponta (Playwright — CI Only)
- **Execução:**
  - O pipeline do GitHub Actions inicia o ambiente completo via `docker compose up -d --wait` (PostgreSQL, Backend, Mock da Central e Frontend).
  - Executa a suite de testes Playwright contra `http://localhost:5173`.
- **Cenários Cobertos:**
  1. Fluxo de Autorização (Opt-in) do beneficiário com replicação no Mock.
  2. Consulta de Agenda de Descontos e listagem de parcelas elegíveis (`OPEN`).
  3. Solicitação de Portabilidade com chave de idempotência e dados de conta.
  4. Acompanhamento assíncrono do polling (transição de `PENDING` para `COMPLETED` ou `REJECTED`).
  5. Visualização da linha do tempo e auditoria imutável de eventos.

---

### 3.6. Portão de Performance (k6 — CI & Nightly)
- **Execução:**
  - Executado via runner de k6 no CI contra os serviços em Docker.
- **Thresholds Estabelecidos:**
  - `http_req_duration`:
    - Consultas (`GET /beneficiaries/{id}/agenda`, `GET /portability-operations/{id}`): p95 < 200ms.
    - Mutações (`POST /portability-operations`, `POST /authorizations`): p95 < 500ms.
  - `http_req_failed`: taxa de erro < 0.1% (falhas inesperadas).
  - Respostas 202/204 no fluxo assíncrono contabilizadas dentro do padrão acordado.
- **Artefatos:**
  - Geração de relatório HTML consolidado (`k6-report.html`) anexado como artefato do workflow no GitHub Actions.

---

### 3.7. Portão de Segurança e Imagens
- **Execução:**
  - Trivy escaneia as imagens OCI construídas (`backend`, `central-convenios-mock`, `frontend`).
- **Critérios de Aceite:**
  - Nenhuma vulnerabilidade `CRITICAL` ou `HIGH` sem mitigação conhecida nos pacotes base das imagens.

---

## 4. Política de Bloqueio e Resolução de Falhas

1. Se qualquer portão falhar no PR, o merge é estritamente bloqueado pela governança do GitHub (Branch Protection Rules em `develop`, `staging` e `main`).
2. O agente ou engenheiro responsável deve:
   - Reproduzir a falha localmente (para gates locais).
   - Analisar o log do step correspondente no GitHub Actions.
   - Aplicar a correção atômica, incluir evidência e atualizar o PR.
