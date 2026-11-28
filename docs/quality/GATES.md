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

| Portão | Escopo | Job no CI | Execução Local | CI no PR (`develop`/`staging`/`main`) | Nightly (Cron) |
|---|---|---|:---:|:---:|:---:|
| **Docs & Links** | Markdown, links relativos, integridade SDD | `docs` | ✅ | ✅ | — |
| **AI Usage Log** | Estrutura das sessões e atualização no PR | `docs` | ✅ | ✅ | — |
| **Design Tokens** | Cópia verbatim de `themes/matrix/tokens.css` | `docs` | ✅ | ✅ | — |
| **Backend Lint** | Spotless (formatação Java) | `backend` | ✅ | ✅ | — |
| **Backend Unit & Arch** | JUnit 5 + AssertJ + ArchUnit | `backend` | ✅ | ✅ | — |
| **Backend Integration** | Testcontainers (PostgreSQL 16) + JaCoCo | `backend` | ✅ | ✅ | — |
| **Mock Unit** | JUnit 5 da Central simulada | `mock` | ✅ | ✅ | — |
| **Contratos OpenAPI** | Spectral + HMAC + oasdiff (breaking change contra `main`) + documento publicado | `contracts` e `smoke` | ✅ | ✅ | ✅ (contra `main`) |
| **Frontend Checks** | ESLint + TypeScript (`tsc`) + Vitest + Build | `frontend` | ✅ | ✅ | — |
| **Docker Compose** | Validação sintática e compose config | `compose` | ✅ | ✅ | — |
| **Stack Smoke** | Backend e mock contra PostgreSQL 16 | `smoke` | ❌ | ✅ | — |
| **E2E Smoke** | Playwright (fluxo completo em Docker) | `e2e` | ❌ | ✅ (quando a suíte existe) | ✅ |
| **Performance Smoke** | k6 smoke test (thresholds estritos) | `k6-smoke` | ❌ | ✅ (quando o script existe) | — |
| **Performance Load** | k6 load test sustentado + relatório HTML | `k6-load` | ❌ | ❌ | ✅ |
| **Contrato Dinâmico** | Schemathesis (property-based) contra a API em execução | `contracts` e `schemathesis` | ✅ | ✅ | ✅ |
| **Segurança** | `npm audit` + Trivy (fs e imagens) | `security` | ❌ | ✅ | ✅ |
| **Release** | release-please: PR de release, CHANGELOG e tag `vX.Y.Z` | `release-please` | ❌ | — | ✅ (push em `main`) | — |
| **Imagens GHCR** | Build e push das três imagens (tag SemVer + `latest`) | `publish-images` | ❌ | — | ✅ (tag) | — |
| **Deploy de staging** | Stack da tag no GHCR com healthchecks verdes | `deploy-staging` | ❌ | — | ✅ (tag) | — |

> Os jobs `e2e`, `k6-smoke`, `k6-load`, `schemathesis` e `security-images` ficam
> condicionados à existência da suíte no commit: eles acendem sozinhos quando o
> artefato chega, em vez de fingir um verde que não existe. O portão do contrato
> dinâmico roda no job `contracts` de todo PR e também no nightly, com a mesma
> configuração — [`tests/contract/schemathesis.toml`](../../tests/contract/schemathesis.toml).

---

## 3. Detalhamento de Cada Portão

### 3.1. Portão de Documentação, Especificação (SDD) e Arquitetura
- **Workflow:** [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml), job `docs`.
- **Comandos:**
  ```bash
  node SDD/SKILLS/check-docs/check-docs.mjs .
  node scripts/check-specs.mjs
  node scripts/check-architecture.mjs
  node scripts/check-theme-tokens.mjs
  node scripts/check-ai-log.mjs
  ```
- **Critérios de Aceite:**
  - Zero links relativos quebrados em toda a árvore Markdown (`check-docs`).
  - Exatamente **uma** tarefa marcada como em andamento `[-]` no [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md).
  - Presença de todos os documentos obrigatórios do SDD Tier 1 e Tier 2.
  - Desenhos de arquitetura validados (SVGs nativos, blocos Mermaid suportados, camadas hexagonais e matriz de rastreabilidade).
  - `frontend/src/theme.css` idêntico a `SDD/themes/matrix/tokens.css` (`check-theme-tokens`).
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
- **Workflow:** [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml), job `contracts` (e `contract-drift` no nightly, comparando com `main`).
- **Comandos:**
  ```bash
  npx --yes @stoplight/spectral-cli@6 lint contracts/consigport-api.yaml contracts/central-api.yaml --fail-severity=warn
  node scripts/hmac-vector.mjs
  node scripts/check-contract-drift.mjs --base main
  node scripts/check-contract-drift.mjs --self-test
  node scripts/check-contract-drift.mjs --document tests/contracts/springdoc-consigport.json
  ```
  E, no job `smoke`, com o backend em execução:
  ```bash
  node scripts/check-contract-drift.mjs \
    --live http://localhost:8080/v3/api-docs \
    --snapshot tests/contracts/springdoc-consigport.json
  ```
- **O que o script [`check-contract-drift.mjs`](../../scripts/check-contract-drift.mjs) faz:**
  - `--base <ref>`: extrai o contrato da ref (`main`, por padrão) e roda
    `oasdiff breaking <baseline> <contrato> --fail-on ERR` nos dois contratos.
  - `--self-test`: prova que o portão está armado — o par de fixtures com a resposta
    removida **tem** de falhar, e o fixture comparado com ele mesmo **tem** de passar.
    Um portão que nunca falha não é portão.
  - `--document <path>`: compara o documento OpenAPI armazenado com o contrato
    versionado (rotas, métodos, `operationId`, parâmetros, corpo da requisição e
    status das respostas).
  - `--live <url>`: compara o documento publicado pelo backend em execução com o
    contrato versionado e com o snapshot armazenado.
- **Critérios de Aceite:**
  - Contratos `contracts/consigport-api.yaml` e `contracts/central-api.yaml` válidos e sem erros no linter Spectral.
  - Ausência de breaking changes em relação ao contrato de `main`; a primeira introdução
    do contrato é aceita e o diff é pulado com aviso no log do workflow (`oasdiff`).
  - Coerência estrita entre endpoints expostos pelo Springdoc (`/v3/api-docs`) e o contrato versionado.
  - O portão armado: as fixtures da task 4.1 provam que uma resposta removida falha a comparação.
  - Vetor de assinatura HMAC validado deterministicamente contra `contracts/hmac-vector.json`.
- **Por que a comparação do documento publicado é estrutural:** o `oasdiff` acusa
  diferenças de representação — `format: uuid` que o springdoc não publica,
  `minimum` e `maxLength` dos limites que só o contrato declara — e não de contrato.
  O que importa para quem consome a API é a superfície: as rotas, os métodos, o
  `operationId`, os parâmetros, o corpo da requisição e os status das respostas.
  Essa é a mesma comparação do `ContractComparator` do backend, portada para Node.

---

### 3.4. Portão do Contrato Dinâmico (Schemathesis — CI e Nightly)
- **Workflow:** [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml), job `contracts`
  (e job `schemathesis` do [nightly](../../.github/workflows/nightly.yml), que usa a
  mesma configuração).
- **Configuração:** [`tests/contract/schemathesis.toml`](../../tests/contract/schemathesis.toml).
- **Comandos:**
  ```bash
  # A stack no ar: PostgreSQL 16 real, Central simulada, backend real.
  docker compose up -d --wait postgres
  # O opt-in do beneficiário é registrado antes do teste: a API guarda o
  # estado da operação e, sem o opt-in, a criação responde 403 em vez de 202.
  curl -sSf -X POST http://localhost:8080/api/v1/authorizations \
    -H 'Content-Type: application/json' \
    -d '{"beneficiaryId":"BEN-001","agreementId":"AGR-001"}'

  schemathesis --config-file tests/contract/schemathesis.toml \
    run contracts/consigport-api.yaml --no-color

  # A prova de que o portão está armado.
  node scripts/check-schemathesis-gate.mjs
  ```
- **O que o portão mede:** a resposta da API em execução contra o contrato
  versionado. As verificações ligadas são as que medem o contrato —
  `status_code_conformance` (o status não declarado), `response_schema_conformance`
  (o corpo fora do esquema), `content_type_conformance`, `response_headers_conformance`,
  `not_a_server_error` — e três que medem o comportamento da API:
  `ensure_resource_availability` (o recurso inexistente responde 404),
  `missing_required_header` (o header obrigatório ausente responde 400) e
  `object_level_authorization` (o recurso de outro beneficiário responde 404).
- **Por que as verificações de rigidez estão desligadas:** `positive_data_acceptance`
  e `negative_data_rejection` medem se a API aceita ou recusa um conteúdo
  específico — uma pergunta de regra de negócio, não de contrato. O `Accept-Language`
  é opcional e tem valor padrão: a API negocia o idioma e cai no padrão em vez de
  recusar a requisição, o que é um comportamento deliberado. O header é fixado em
  `pt-BR` na configuração para que o teste nunca envie um locale fora do enum.
- **Fases:** `examples` (os exemplos do contrato, enviados como estão escritos) e
  `coverage` (um caso gerado por operação, com todas as verificações). A fase
  `fuzzing` — que gera valores fora dos exemplos — espera o contrato declarar
  todas as respostas da API; o que ela encontrou está registrado como dívida no
  [`../../SDD/specs/BACKLOG.md`](../../SDD/specs/BACKLOG.md).
- **Critérios de Aceite:**
  - Todas as operações do contrato exercitadas contra a API em execução, sem
    nenhuma resposta fora do contrato.
  - Relatório publicado como artefato `schemathesis-report` (JUnit e JSON).
  - O portão armado: o contrato de fixture que esquece o `404` da API reprova o
    teste com a violação esperada (`Undocumented status code`).

---

### 3.5. Portão de Frontend (React 19.2 + RSC + Tailwind 4)
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

### 3.6. Portão de Testes Ponta a Ponta (Playwright — CI Only)
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

### 3.7. Portão de Performance (k6 — CI & Nightly)
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

### 3.8. Portão de Segurança e Imagens
- **Execução:**
  - No PR, o job `security` roda `npm audit --omit=dev --audit-level=high` e a varredura de filesystem com Trivy, que lê `pom.xml` e `package-lock.json` e cobre a superfície de dependências do repositório.
  - No nightly, o job `security-images` constrói as três imagens (`backend`, `central-convenios-mock`, `frontend`) a partir da raiz do repositório e escaneia cada uma com Trivy.
- **Critérios de Aceite:**
  - Nenhuma vulnerabilidade `CRITICAL` ou `HIGH` sem mitigação conhecida nos pacotes base das imagens.
  - Nenhuma dependência de produção com vulnerabilidade `high` ou `critical`.

---

### 3.9. Portão de Release e Entrega Contínua
- **Workflows:** [`.github/workflows/release.yml`](../../.github/workflows/release.yml) e
  [`.github/workflows/cd-staging.yml`](../../.github/workflows/cd-staging.yml).
- **Versionamento:**
  - `release-please` (tipo `simple`, uma versão para a plataforma) lê os Conventional
    Commits e abre o PR de release; o merge desse PR cria a tag `vX.Y.Z` e o
    CHANGELOG. Configuração em
    [`../../release-please-config.json`](../../release-please-config.json) e
    [`../../.release-please-manifest.json`](../../.release-please-manifest.json).
- **Publicação no GHCR:**
  - Cada tag publica as três imagens em `ghcr.io/marcelinosandroni/consigport-platform/<imagem>`
    com as tags `vX.Y.Z`, `X.Y.Z` e `latest`. Uma pré-release nunca move a `latest`.
- **Deploy de staging:**
  - A tag dispara [`../../compose.staging.yaml`](../../compose.staging.yaml): as imagens
    publicadas sob aquela tag sobem com PostgreSQL 16 e os healthchecks de backend,
    mock e frontend são obrigatórios para o run ser verde.
- **Critérios de Aceite:**
  - `release-please` em dry-run propõe a próxima versão SemVer.
  - `docker build` das três imagens e `docker compose config -q` dos dois arquivos de
    compose com exit 0.
  - O run de staging mostra `UP` nos três serviços.

Fluxo completo, com rollback e comandos: [`../process/RELEASE.md`](../process/RELEASE.md).

---

## 4. Política de Bloqueio e Resolução de Falhas

1. Se qualquer portão falhar no PR, o merge é estritamente bloqueado pela governança do GitHub (Branch Protection Rules em `develop`, `staging` e `main`).
2. O agente ou engenheiro responsável deve:
   - Reproduzir a falha localmente (para gates locais).
   - Analisar o log do step correspondente no GitHub Actions.
   - Aplicar a correção atômica, incluir evidência e atualizar o PR.
