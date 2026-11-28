# 📜 EVIDENCE — Registro Central de Evidências

> Registro histórico e auditável das evidências de verificação de cada tarefa concluída no ConsigPort.
> Todas as tarefas `[x]` devem colar aqui a prova de execução (comandos verdes, testes e links).
> Referências: [`../specs/PLAN.md`](../specs/PLAN.md), [`../specs/BACKLOG.md`](../specs/BACKLOG.md) e [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).

---

## Fase 0 — Fundação

### Task 0.1 — Refinamento SDD completo

- **Data de Conclusão:** 2026-11-23
- **Branch:** `docs/000-sdd-foundation`
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash (High)
- **Objetivo:** Specs em pt-BR, 6 ADRs, harness de IA, gates de qualidade, CI de documentação e correção dos links do `AGENTS.md` raiz.

#### Comandos de Verificação e Saídas:

1. **Validação de Links Relativos em Markdown:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   ```
   *Saída esperada / obtida:*
   ```text
   ✓ 27 documents, every relative link resolves.
   ```

2. **Validação de Invariantes de Especificação (SDD):**
   ```bash
   node scripts/check-specs.mjs
   ```
   *Saída esperada / obtida:*
   ```text
   ✓ SDD specifications valid:
     - Tier 1 and Tier 2 core documents exist.
     - 6 Architecture Decision Records (ADRs) present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN match BACKLOG.md.
   ```

#### Arquivos Tocados:
- `docs/quality/GATES.md`
- `docs/ai-harness/HARNESS_OVERVIEW.md`
- `docs/ai-harness/AI_USAGE_LOG.md`
- `SDD/docs/EVIDENCE.md`
- `AGENTS.md`
- `scripts/check-specs.mjs`
- `.github/workflows/ci-docs.yml`
- `SDD/specs/PLAN.md`


---

### Task 0.2 — Gitflow, PR template e CODEOWNERS

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/002-gitflow` (desenvolvido em worktree `consigport-task-0.2`)
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Objetivo:** Materializar o fluxo de branches, política de revisão obrigatória, template de PR e documentação operacional de Gitflow.

#### Comandos de Verificação e Saídas:

1. **Validação de Links Relativos em Markdown:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   ```
   *Saída obtida:*
   ```text
   ✓ 60 documents, every relative link resolves.
   ```

2. **Validação de Invariantes de Especificação (SDD):**
   ```bash
   node scripts/check-specs.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid:
     - Required core documents and ADRs present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN.md are cataloged in BACKLOG.md.
   ```

3. **Validação de Desenhos e Arquitetura:**
   ```bash
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso!
   ```

4. **Verificação de Ignoração Git:**
   ```bash
   git check-ignore .github/PULL_REQUEST_TEMPLATE.md .github/CODEOWNERS docs/process/GITFLOW.md
   # Exit code 1 (Arquivos não são ignorados pelo .gitignore)
   ```

#### Arquivos Tocados:
- `.github/PULL_REQUEST_TEMPLATE.md`
- `.github/CODEOWNERS`
- `docs/process/GITFLOW.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`


---

### Task 0.3 — Ambiente local e PostgreSQL

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/003-local-env`
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Objetivo:** Estabelecer a infraestrutura base local através de `.gitignore`, `.env.example` e `compose.yaml` com o serviço `postgres:16-alpine`, volume e healthcheck.

#### Comandos de Verificação e Saídas:

1. **Validação de Sintaxe e Estrutura do Docker Compose:**
   ```bash
   docker compose config -q
   # Exit code 0 (Sintaxe e variáveis válidas)
   ```

2. **Verificação das Regras de Ignoração Git (.gitignore):**
   ```bash
   git check-ignore -v .env target/ node_modules/ .idea/ .env.example compose.yaml
   ```
   *Saída obtida:*
   ```text
   .gitignore:8:.env	.env
   .gitignore:18:target/	target/
   .gitignore:35:node_modules/	node_modules/
   .gitignore:52:.idea/	.idea/
   ```
   *(.env.example e compose.yaml rastreados; .env e artefatos ignorados).*

3. **Validação de Invariantes e Documentação:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-specs.mjs
   node scripts/check-architecture.mjs
   # Todos com exit code 0.
   ```

#### Arquivos Tocados:
- `.gitignore`
- `.env.example`
- `compose.yaml`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`

---

### Task 0.4 — Maven Wrapper e reactor raiz

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/004-maven-reactor`
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Objetivo:** Build Java reprodutível sem Maven global. Wrapper oficial 3.3.4
  (`only-script`) apontando para Maven 3.10.2, reactor raiz em Java 21 + Spring Boot
  3.5.16 e módulos mínimos `backend` e `central-convenios-mock`.

#### Comandos de Verificação e Saídas:

1. **Wrapper resolve o Maven distribuído e encontra o Java 21:**
   ```powershell
   .\mvnw.cmd -v
   ```
   *Saída obtida:*
   ```text
   Apache Maven 3.10.2 (61b8bc355f4c4325463c22aa9afe4e7811a290f8)
   Maven home: C:\Users\marcelino\.m2\wrapper\dists\apache-maven-3.10.2\...
   Java version: 21.0.12, vendor: Oracle Corporation
   exit=0
   ```

2. **Reactor válido na raiz (não recursivo):**
   ```powershell
   .\mvnw.cmd -N validate
   ```
   *Saída obtida:*
   ```text
   [INFO] BUILD SUCCESS
   exit=0
   ```

3. **Build completo dos dois módulos sem testes:**
   ```powershell
   .\mvnw.cmd -q -DskipTests package
   ```
   *Saída obtida:*
   ```text
   exit=0
   ```

4. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid: ... (exit 0)
   ✓ 60 documents, every relative link resolves. (exit 0)
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso! (exit 0)
   ```

5. **Portão do AI Usage Log (hook e estrutura):**
   ```powershell
   node scripts/check-ai-log.mjs
   node scripts/check-ai-log.mjs --staged   # com trabalho staged e sem o log
   ```
   *Saída obtida:*
   ```text
   ✓ AI usage log: 7 sessions, structure valid. (exit 0)
   ✗ AI usage log: staging 1 project file(s) requires staging
     docs/ai-harness/AI_USAGE_LOG.md with a new session. (exit 1, esperado)
   ```

#### Arquivos Tocados:
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/maven-wrapper.properties`
- `.mvn/jvm.config`
- `.gitattributes`
- `backend/pom.xml`
- `central-convenios-mock/pom.xml`
- `scripts/check-ai-log.mjs`
- `.githooks/pre-commit`
- `.github/workflows/ci-docs.yml`
- `docs/ai-harness/AI_USAGE_LOG.md`
- `docs/ai-harness/HARNESS_OVERVIEW.md`
- `docs/quality/GATES.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`

---

### Task 0.5 — Contratos OpenAPI e Vetor HMAC

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/005-contracts`
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Objetivo:** Contrato primeiro. Superfície pública `/api/v1` em inglês, contrato da
  Central fiel ao edital em português, vetor HMAC compartilhado e verificação
  automatizada de ambos.

#### Comandos de Verificação e Saídas:

1. **Contratos validados pelo Spectral (zero erros e zero avisos):**
   ```powershell
   npx @stoplight/spectral-cli@6 lint contracts/consigport-api.yaml contracts/central-api.yaml --fail-severity=warn
   ```
   *Saída obtida:*
   ```text
   No results with a severity of 'warn' or higher found!
   exit=0
   ```

2. **Vetor HMAC reproduzido deterministicamente:**
   ```powershell
   node scripts/hmac-vector.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ HMAC vector: 3 vectors reproduced with HMAC-SHA256.
   exit=0
   ```

3. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid: ... (exit 0)
   ✓ 60 documents, every relative link resolves. (exit 0)
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso! (exit 0)
   ```

4. **Portão de contratos no CI (Spectral, HMAC e oasdiff):**
   ```text
   Workflow: CI Contracts (.github/workflows/ci-contracts.yml)
   - npx --yes @stoplight/spectral-cli@6 lint contracts/*.yaml --fail-severity=warn
   - node scripts/hmac-vector.mjs
   - node scripts/check-ai-log.mjs          (AI_LOG_BASE_SHA no pull request)
   - oasdiff breaking <contrato da base> <contrato atual> --fail-on ERR
   Baseline ausente na branch base → diff pulado com aviso, sem quebrar o pipeline.
   ```

#### Conteúdo Entregue:
- **API pública:** autorizações, agenda, criação/listagem/detalhe de operações e trilha
  de eventos, com `Idempotency-Key`, `Idempotent-Replay`, paginação e erros localizados.
- **API da Central:** `POST /autorizacoes`, `GET /beneficiarios/{id}/agenda`,
  `POST /operacoes-portabilidade`, `GET /operacoes-portabilidade/{protocolo}` com
  `202`/`204` + `Retry-After`, HMAC, idempotência, caos e administração.
- **Vetor HMAC:** três requisições canônicas (POST de portabilidade, GET de status e
  POST de autorização) com segredo de teste e assinatura verificável por script.
- **Portão de CI:** workflow dedicado que reprova contrato inválido, vetor HMAC divergente
  e breaking change contra a branch base.
- **Catálogo de erros:** códigos `BENEFICIARY_NOT_FOUND` no core e
  `REQUISICAO_INVALIDA`, `SEM_AUTORIZACAO`, `BENEFICIARIO_NAO_ENCONTRADO` e
  `PARCELA_INVALIDA` na Central, refletidos no `ARCHITECTURE.md`.

#### Arquivos Tocados:
- `contracts/consigport-api.yaml`
- `contracts/central-api.yaml`
- `contracts/hmac-vector.json`
- `scripts/hmac-vector.mjs`
- `.spectral.yaml`
- `.github/workflows/ci-contracts.yml`
- `docs/quality/GATES.md`
- `SDD/ARCHITECTURE.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.6 — Scaffold do Backend

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/006-backend-scaffold`
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Aplicação Spring Boot vazia, compilando, com health respondendo `UP` e
  configuração única (`application.yml`) dirigida por `DATABASE_URL`.

#### Comandos de Verificação e Saídas:

1. **Build do módulo no reactor (compilação + testes):**
   ```powershell
   .\mvnw.cmd -q -pl backend -am verify
   ```
   *Saída obtida:*
   ```text
   Tomcat started on port 54011 (http) with context path '/'
   Init duration for springdoc-openapi is: 180 ms
   Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in br.com.consigport.ConsigPortApplicationTests
   verify_exit=0
   ```

2. **Health e contrato expostos por HTTP (dentro do teste, servidor real):**
   ```text
   GET /actuator/health → 200 {"status":"UP"}
   GET /v3/api-docs     → 200 {"openapi":"3.1.0",...}
   ```

3. **Reactor válido (critério de saída da fase):**
   ```powershell
   .\mvnw.cmd -N validate
   ```
   *Saída obtida:*
   ```text
   [INFO] BUILD SUCCESS
   reactor_validate_exit=0
   ```

4. **Invariantes de documentação e especificação:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:* exit 0 nos três.

#### Conteúdo Entregue:
- **Dependências:** `web`, `validation`, `data-jpa`, `actuator`, `flyway-core`,
  `flyway-database-postgresql`, driver PostgreSQL (runtime), springdoc e
  `spring-boot-starter-test`; H2 apenas em escopo de teste.
- **Configuração:** porta `8080` (`SERVER_PORT`), `DATABASE_URL`/`POSTGRES_*` vindos do
  `.env`, Hibernate em `ddl-auto: validate` (o schema é do Flyway), `open-in-view: false`,
  timezone UTC, `shutdown: graceful`, actuator expondo `health`/`info` com probes e
  Swagger em `/swagger-ui.html`.
- **Contrato alinhado:** springdoc configurado com `openapi_3_1`, então `/v3/api-docs`
  publica OpenAPI `3.1.0`, a mesma versão dos contratos versionados.
- **Perfil `local`:** mesmo bloco YAML apontando para o PostgreSQL do `compose.yaml` em
  `localhost:5432`, com log `DEBUG` do pacote `br.com.consigport`.
- **Teste de fumaça:** sobe o contexto completo em porta aleatória e prova
  `GET /actuator/health` → `UP` e `GET /v3/api-docs` → 200.

#### Débito Registrado:
- O teste do scaffold usa H2 em memória para não depender de Docker; a task de
  persistência troca por Testcontainers PostgreSQL 16 e reativa o Flyway, cobrindo as
  migrações de verdade.

#### Arquivos Tocados:
- `backend/pom.xml`
- `backend/src/main/java/br/com/consigport/ConsigPortApplication.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/com/consigport/ConsigPortApplicationTests.java`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.7 — Scaffold do Mock da Central

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/007-mock-scaffold`
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Central simulada vazia, sem banco e sem Flyway, compilando e respondendo
  `UP` na porta 8081, pronta para receber o contrato em português.

#### Comandos de Verificação e Saídas:

1. **Build do módulo no reactor:**
   ```powershell
   .\mvnw.cmd -q -pl central-convenios-mock -am verify
   ```
   *Saída obtida:*
   ```text
   Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.68 s
     -- in br.com.consigport.central.CentralConveniosMockApplicationTests
   verify_exit=0
   ```

2. **Processo real (jar empacotado) respondendo por HTTP:**
   ```powershell
   java -jar central-convenios-mock/target/central-convenios-mock-0.1.0-SNAPSHOT.jar --server.port=18081
   ```
   *Saída obtida:*
   ```text
   GET /actuator/health → 200 {"status":"UP","groups":["liveness","readiness"]}
   GET /v3/api-docs     → 200 {"openapi":"3.1.0","info":{"title":"Open...
   stopped_pid=27772
   ```

3. **Invariantes de documentação e especificação:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:* exit 0 nos três.

#### Conteúdo Entregue:
- **Dependências:** `web`, `validation`, `actuator`, springdoc e `spring-boot-starter-test`;
  nenhum JPA, nenhum Flyway, nenhum driver de banco.
- **Configuração:** porta `8081` (`SERVER_PORT:8081`), `shutdown: graceful`, actuator
  expondo `health`/`info` com probes, Swagger em `/swagger-ui.html` e springdoc em
  `openapi_3_1` para espelhar `contracts/central-api.yaml`.
- **Perfil `local`:** log `DEBUG` de `br.com.consigport.central`, sem datasource.
- **Testes:** health `UP` e `/v3/api-docs` publicando `3.1.0`, além da prova em processo
  real com o jar empacotado.

#### Arquivos Tocados:
- `central-convenios-mock/pom.xml`
- `central-convenios-mock/src/main/java/br/com/consigport/central/CentralConveniosMockApplication.java`
- `central-convenios-mock/src/main/resources/application.yml`
- `central-convenios-mock/src/test/java/br/com/consigport/central/CentralConveniosMockApplicationTests.java`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.8 — Base do Frontend RSC

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/008-frontend-base`
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Projeto React 19.2 com Vite RSC, TypeScript e Tailwind 4, sem Next.js,
  consumindo os tokens do `DESIGN.md` e com os scripts de qualidade exigidos pelos gates.

#### Comandos de Verificação e Saídas:

1. **Instalação reprodutível e build dos três ambientes RSC:**
   ```powershell
   npm ci --prefix frontend
   npm run --prefix frontend build
   ```
   *Saída obtida:*
   ```text
   added 277 packages, and audited 284 packages in 5s
   ci_exit=0

   [3/5] build rsc environment...  ✓ 57 modules transformed.
   dist/rsc/index.js                                   424.60 kB
   [4/5] build client environment... ✓ 42 modules transformed.
   dist/client/assets/index-C6Rif_XN.js                223.16 kB │ gzip: 70.40 kB
   [5/5] build ssr environment...   ✓ 52 modules transformed.
   dist/ssr/index.js                                1,582.08 kB
   build_exit=0
   ```

2. **Qualidade do frontend:**
   ```powershell
   npm run --prefix frontend lint
   npm run --prefix frontend typecheck
   npm run --prefix frontend test:unit
   ```
   *Saída obtida:*
   ```text
   lint_exit=0
   typecheck_exit=0
   Test Files  1 passed (1)
        Tests  1 passed (1)
   test_exit=0
   ```

3. **Prova de vida com o servidor de desenvolvimento:**
   ```powershell
   npm run --prefix frontend dev -- --port 15173 --strictPort
   ```
   *Saída obtida:*
   ```text
   VITE v7.3.8 ready in 476 ms
   dev_status=200
   <!DOCTYPE html><html lang="pt-BR"><head>...<title>ConsigPort</title></head>
   <body><main class="p-8"><h1 class="text-headline-lg">ConsigPort</h1>...
   ```

4. **Tokens sem deriva:**
   ```powershell
   node scripts/check-theme-tokens.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css byte for byte.
   tokens_exit=0
   ```

5. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:* exit 0 nos três.

#### Conteúdo Entregue:
- **Template oficial:** `npm create vite@latest frontend -- --template rsc`, com os ambientes
  `rsc`, `ssr` e `client` do `@vitejs/plugin-rsc` e as entradas em `src/framework/`.
- **Versões:** React 19.2.9, React DOM 19.2.9, Vite 7.3.8, Tailwind CSS 4.3.5,
  TypeScript 5.9, ESLint 9.39, Vitest 4.1.12 e Testing Library 16, conforme `APP-STACK`.
- **Scripts:** `dev`, `build`, `preview`, `lint`, `typecheck` e `test:unit`.
- **Tema:** `src/theme.css` como cópia verbatim de `SDD/themes/matrix/tokens.css`, consumida
  por `src/styles.css` com `@import "tailwindcss"`; `scripts/check-theme-tokens.mjs`
  bloqueia a deriva entre a fonte e a cópia.
- **Tipagem estrita:** `strict`, `noUncheckedIndexedAccess` e `noImplicitOverride`, com
  alias `@/*` para `src/*`.
- **Correções no template:** `override` em `state`/`render` do `error-boundary`,
  `action(...args)`, `catch` sem binding e `const link` no `entry.browser`.

#### Arquivos Tocados:
- `frontend/package.json`, `frontend/package-lock.json`
- `frontend/vite.config.ts`, `frontend/vitest.config.ts`, `frontend/tsconfig.json`, `frontend/eslint.config.js`
- `frontend/src/framework/*`, `frontend/src/root.tsx`, `frontend/src/client.tsx`, `frontend/src/action.tsx`
- `frontend/src/styles.css`, `frontend/src/theme.css`
- `frontend/src/test/setup.ts`, `frontend/src/test/client-counter.test.tsx`
- `scripts/check-theme-tokens.mjs`
- `SDD/specs/PLAN.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.9 — Shell RSC e Design Matrix

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/009-shell-matrix`
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Casca visual navegável dark-only, conforme o `DESIGN.md`, com tokens
  verbatim, fontes locais, primitivas reutilizáveis e as três viewports do §4b provadas.

#### Comandos de Verificação e Saídas:

1. **Build dos três ambientes RSC:**
   ```powershell
   npm run --prefix frontend build
   ```
   *Saída obtida:*
   ```text
   dist/rsc/assets/index-CQ-rpez6.css              20.23 kB
   dist/client/assets/index-CQ-rpez6.css           20.23 kB │ gzip: 4.50 kB
   dist/client/assets/index-D2VVaZFS.js           221.90 kB │ gzip: 69.82 kB
   dist/ssr/index.js                            1,581.90 kB
   build_exit=0
   ```

2. **Qualidade do frontend:**
   ```powershell
   npm run --prefix frontend lint
   npm run --prefix frontend typecheck
   npm run --prefix frontend test:unit
   ```
   *Saída obtida:*
   ```text
   lint_exit=0
   typecheck_exit=0
    Test Files  3 passed (3)
         Tests  7 passed (7)
   test_exit=0
   ```

3. **Auditoria geométrica do `DESIGN.md` §4b em navegador real (Chrome headless, CDP):**
   ```text
   mobile 390x844:
     scrollWidth=390  clientWidth=390  offenders=[]  small=[]
   desktop 1440x1000:
     scrollWidth=1440 clientWidth=1440 offenders=[]  small=[]
   404 em 390x844:
     scrollWidth=390  clientWidth=390  offenders=[]  small=[]
   ```
   Nenhum elemento fora da viewport, nenhum alvo de toque abaixo de 44×44px. O único
   texto abaixo de 13px é o chrome `label-mono` (navegação, badge e rótulos de stat
   card), explicitamente permitido pelo §4b.2.3.

4. **Tokens sem deriva:**
   ```powershell
   node scripts/check-theme-tokens.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css (line endings normalized).
   tokens_exit=0
   ```
   O guard também roda no workflow de documentação e dispara quando
   `frontend/src/theme.css` ou `SDD/themes/**` mudam.

5. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:* exit 0 nos três.

#### Conteúdo Entregue:
- **Shell (`src/app/shell.tsx`):** navegação de vidro (`backdrop-blur`, 82% de opacidade)
  sobre o canvas Tier 0 com grade de micro-pontos, coluna de conteúdo de 1320px e
  rodapé; alvos de toque de 44px e navegação que quebra em vez de estourar a largura.
- **Rotas (`src/app/router.tsx`):** `/`, `/portabilidade`, `/agenda` e `/auditoria`, com
  barra final normalizada e 404 renderizada dentro do mesmo shell.
- **Primitivas (`src/shared/ui/`):** `Button` (primary, secondary, ghost), `Badge`
  (neutral, highlight), `Card` e `StatCard` — sem literal hexadecimal, só tokens.
- **Fontes:** `@fontsource` de Manrope, JetBrains Mono e Playfair Display, apenas subset
  latino, servidas localmente.
- **Testes:** quatro casos de rota (home, seção, barra final, 404) e dois casos de
  stat card (conteúdo e status).

#### Arquivos Tocados:
- `frontend/package.json`, `frontend/package-lock.json`
- `frontend/src/styles.css`, `frontend/src/root.tsx`
- `frontend/src/app/shell.tsx`, `frontend/src/app/router.tsx`, `frontend/src/app/router.test.tsx`
- `frontend/src/app/pages/home.tsx`, `frontend/src/app/pages/section.tsx`, `frontend/src/app/pages/not-found.tsx`
- `frontend/src/shared/ui/button.tsx`, `badge.tsx`, `card.tsx`, `stat-card.tsx`, `stat-card.test.tsx`
- `.gitignore`
- `.github/workflows/ci-docs.yml`
- `scripts/check-theme-tokens.mjs`
- `SDD/specs/PLAN.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.10 — Pipeline de Integração Contínua

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/010-ci-pipeline`
- **PR:** [#12](https://github.com/marcelinosandroni/consigport-platform/pull/12)
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Um pipeline único com jobs paralelos que provam cada PR, mais o nightly
  com os portões pesados (drift de contrato, E2E completo, carga, Schemathesis e
  scan das imagens).

#### Comandos de Verificação e Saídas:

1. **Lint dos workflows (actionlint v1.7.12):**
   ```powershell
   actionlint .github/workflows/ci.yml .github/workflows/nightly.yml
   ```
   *Saída obtida:* nenhuma diagnóstico; `actionlint_exit=0`.

2. **Auditoria de dependências de produção do frontend:**
   ```powershell
   npm audit --prefix frontend --omit=dev --audit-level=high
   ```
   *Saída obtida:*
   ```text
   found 0 vulnerabilities
   audit_exit=0
   ```

3. **Portão de segurança no CI (run `45622026445`, job `Security`):**
   ```text
   ✓ Audit the frontend dependencies
   ✓ Scan the repository with Trivy
   Total: 0 (HIGH: 0, CRITICAL: 0)
   ```
   O primeiro run (`45620040274`) reprovou com `org.postgresql:postgresql` 42.7.11 e
   `CVE-2026-54291` (HIGH, corrigida em 42.7.12); o driver foi sobrescrito para
   42.7.15 no `pom.xml`. O run `45621324609` provou que o `dependency-review` falha
   neste repositório (Dependency Graph API desabilitada) e o passo foi removido: o
   Trivy já lê `pom.xml` e `package-lock.json`.

4. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   node scripts/check-ai-log.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid:
     - Required core documents and ADRs present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN.md are cataloged in BACKLOG.md.

   ✓ 61 documents, every relative link resolves.

   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso!

   ✓ AI usage log: 14 sessions, structure valid.
   ```

5. **Pipeline completo do PR — run verde `45622026445`:**
   ```text
   ✓ feat/010-ci-pipeline CI marcelinosandroni/consigport-platform#12 · 45622026445
   Triggered via pull_request

   JOBS
   ✓ Backend — build, unit tests and coverage report in 25s
   ✓ Detect the suites available in this commit in 5s
   ✓ Docs, Specs, Architecture and AI Log in 7s
   ✓ Stack Smoke — backend and mock against PostgreSQL 16 in 37s
   ✓ Docker Compose — configuration in 6s
   ✓ Frontend — lint, typecheck, unit tests and build in 27s
   ✓ Contracts — Spectral, HMAC vector and breaking changes in 16s
   ✓ Security — dependency audit and vulnerability scan in 23s
   ✓ Central Mock — build and unit tests in 17s
   - E2E — Playwright
   - Performance Smoke — k6

   ARTIFACTS
   backend-reports
   stack-health
   frontend-dist
   mock-reports
   ```
   Os dois jobs pulados são os condicionais: a suíte Playwright e os scripts k6 ainda
   não existem no repositório, e o job `detect` prova isso em vez de fingir verde.
   O job `smoke` é real: sobe o PostgreSQL 16 do `compose.yaml`, empacota os jars,
   inicia mock e backend e exige `200` em `/actuator/health` dos dois.

#### Conteúdo Entregue:
- **`.github/workflows/ci.yml`:** jobs `detect`, `docs`, `backend`, `mock`, `frontend`,
  `contracts`, `compose`, `smoke`, `e2e`, `k6-smoke` e `security`, com
  `permissions: contents: read`, `concurrency` por PR e Node 24 / npm 12.0.2 /
  Temurin 21 fixados.
- **`.github/workflows/nightly.yml`:** cron diário às 03:00 UTC com `contract-drift`
  (oasdiff contra `main`), `e2e-full`, `k6-load`, `schemathesis` e `security-images`.
- **Consolidação:** `ci-docs.yml` e `ci-contracts.yml` removidos; as mesmas
  verificações vivem nos jobs `docs` e `contracts`, sem duplicar runner.
- **Cache e artefatos:** Maven via `actions/setup-java`, npm via `actions/setup-node`
  com `cache-dependency-path: frontend/package-lock.json`; relatórios Surefire,
  `frontend/dist` e payloads de health anexados ao run.

#### Arquivos Tocados:
- `.github/workflows/ci.yml`
- `.github/workflows/nightly.yml`
- `.github/workflows/ci-docs.yml` (removido)
- `.github/workflows/ci-contracts.yml` (removido)
- `pom.xml`
- `docs/quality/GATES.md`
- `SDD/specs/PLAN.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.11 — Release e Entrega Contínua

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/011-release-cd`
- **PR:** [#15](https://github.com/marcelinosandroni/consigport-platform/pull/15) (implementação) e [#16](https://github.com/marcelinosandroni/consigport-platform/pull/16) (evidência e correção do gatilho de publicação)
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Objetivo:** Uma versão para a plataforma inteira (release-please), as três imagens
  publicadas no GHCR por tag SemVer e o deploy de staging disparado pela tag.

#### Comandos de Verificação e Saídas:

1. **Build das três imagens (contexto na raiz do repositório):**
   ```powershell
   docker build -f backend/Dockerfile -t consigport-backend:local .
   docker build -f central-convenios-mock/Dockerfile -t consigport-central-mock:local .
   docker build -f frontend/Dockerfile -t consigport-frontend:local .
   ```
   *Saída obtida:*
   ```text
   #20 naming to docker.io/library/consigport-backend:local done
   #20 DONE 0.1s
   backend_build_exit=0
   #20 naming to docker.io/library/consigport-central-mock:local done
   #20 DONE 0.1s
   mock_build_exit=0
   #19 naming to docker.io/library/consigport-frontend:local done
   #19 DONE 0.1s
   frontend_build_exit=0
   ```
   O contexto é a **raiz** porque o reactor Maven precisa do `pom.xml` pai. Os três
   estágios de runtime rodam sem privilégio (`consigport` nos jars, `node` no frontend)
   e trazem `HEALTHCHECK` próprio.

2. **Lint dos workflows (actionlint v1.7.12):**
   ```powershell
   actionlint .github/workflows/ci.yml .github/workflows/nightly.yml `
             .github/workflows/release.yml .github/workflows/cd-staging.yml
   ```
   *Saída obtida:* nenhum diagnóstico; `actionlint_exit=0`.

3. **Validação dos dois arquivos de compose:**
   ```powershell
   docker compose config -q
   docker compose -f compose.staging.yaml config -q
   ```
   *Saída obtida:* `compose_exit=0` e `staging_compose_exit=0`.

4. **Stack de staging real (imagens construídas localmente, tags GHCR emuladas):**
   ```powershell
   docker tag consigport-backend:local ghcr.io/marcelinosandroni/consigport-platform/backend:latest
   docker tag consigport-central-mock:local ghcr.io/marcelinosandroni/consigport-platform/central-convenios-mock:latest
   docker tag consigport-frontend:local ghcr.io/marcelinosandroni/consigport-platform/frontend:latest
   docker compose -f compose.staging.yaml up -d --wait --wait-timeout 180
   ```
   *Saída obtida:*
   ```text
   up_exit=0
   backend: Up 8 minutes (healthy)
   central-convenios-mock: Up 8 minutes (healthy)
   frontend: Up About a minute (healthy)
   postgres: Up 8 minutes (healthy)
   backend_health={"status":"UP","groups":["liveness","readiness"]}
   mock_health={"status":"UP","groups":["liveness","readiness"]}
   frontend_status=200
   ```
   A primeira tentativa reprovou e revelou quatro defeitos reais, corrigidos antes do
   fechamento:
   - O reactor Maven exigia o `pom.xml` dos **dois** módulos: `backend` e
     `central-convenios-mock` precisam existir no contexto do build.
   - A imagem do frontend precisa do `vite.config.ts`: sem ele o `vite preview` sobe
     sem o plugin RSC e responde `404` em todas as rotas.
   - O `vite preview` escreve em `node_modules/.vite-temp`, então a árvore precisa
     pertencer ao usuário `node` (`chown -R node:node`).
   - O healthcheck do frontend usa `127.0.0.1`: o servidor escuta só IPv4 e o busybox
     resolve `localhost` para `::1` primeiro, o que produz `Connection refused`.

5. **Dry-run do release-please (API programática, token fora do argv):**
   O CLI só aceita `--token` por argv e o `npm notice run` ecoa a linha de comando
   inteira — foi assim que um token vazou nesta fase. O dry-run roda pela API
   programática (`release-please/build/src/index.js`), com o token lido de um arquivo
   temporário privado e passado por variável de ambiente:
   ```powershell
   $env:RELEASE_PLEASE_TOKEN = (Get-Content -Raw <arquivo privado>).Trim()
   node release-dry-run.mjs
   ```
   *Saída obtida:*
   ```text
   > Fetching release-please-config.json from branch feat/011-release-cd
   > Fetching .release-please-manifest.json from branch feat/011-release-cd
   √ Building strategies by path
   > .: simple
   √ Splitting 36 commits by path
   √ Building candidate release pull request for path: .
   > type: simple
   > targetBranch: feat/011-release-cd
   > commits: 36
   Would open 1 pull requests
   title: chore(feat/011-release-cd): release 0.1.0
   branch: release-please--branches--feat/011-release-cd--components--consigport-platform
   draft: false
   body: :robot: I have created a release *beep* *boop*
   ---

   ## 0.1.0 (2026-11-25)

   ### Features

   * **backend:** scaffold the spring boot application with health and openapi. (Agent: OpenCode - DeepSeek V4.1 Flash) ([#8](https://github.com/marcelinosandroni/consigport-platform/issues/8)) ([ce7226f](https://github.com/marcelinosandroni/consigport-platform/commit/ce7226faf8762c8ca27fa3af73864a3da5828761))
   * **contracts:** define public api, central mock and hmac vector ([#7](https://github.com/marcelinosandroni/consigport-platform/issues/7)) ([69a8310](https://github.com/marcelinosandroni/consigport-platform/commit/69a83101ce5a0debf0e92cb116a203a431dab56a))
   * **frontend:** add the rsc shell with the matrix design system ([#11](https://github.com/marcelinosandroni/consigport-platform/issues/11)) ([06c9195](https://github.com/marcelinosandroni/consigport-platform/commit/06c9195c246de49ac7831a2c424c893e535b126b))
   * **gitflow:** add pull request template, CODEOWNERS and gitflow process documentation. (Agent: Antigravity - Gemini 3.8 Flash) ([1bd31fa](https://github.com/marcelinosandroni/consigport-platform/commit/1bd31fa58ae7a2c4d05f879feb00c546b7033df8))
   * **gitflow:** governança de branches, template de PR e CODEOWNERS ([d623c50](https://github.com/marcelinosandroni/consigport-platform/commit/d623c50a1272b29c6f9762fda101e9d69d2bd769))
   * **infra:** add monorepo gitignore, env template and compose with postgres 16. (Agent: Antigravity - Gemini 3.8 Flash) ([985e87a](https://github.com/marcelinosandroni/consigport-platform/commit/985e87af95231460056ebfc8faf9af22ff261e03))
   * **infra:** ambiente local, .env.example e compose com postgresql 16 ([322fac0](https://github.com/marcelinosandroni/consigport-platform/commit/322fac0e39d033f8f4e6221c9621241749f7307e))
   * **mock:** scaffold the central mock with health and openapi. (Agent: OpenCode - DeepSeek V4.1 Flash) ([#9](https://github.com/marcelinosandroni/consigport-platform/issues/9)) ([d584249](https://github.com/marcelinosandroni/consigport-platform/commit/d584249d878d08b4468be5d7a38cbe5f207fa6f4))

   ### Bug Fixes

   * **release:** set the initial version so the first release is v0.1.0. (Agent: OpenCode - MiniMax-M3.1-Flash) ([de2d260](https://github.com/marcelinosandroni/consigport-platform/commit/de2d260573c1c9ca2a17780c50c1f4254082074c))

   updates: 3
     CHANGELOG.md: Changelog
     .release-please-manifest.json: ReleasePleaseManifest
   Index: .release-please-manifest.json
   @@ -1,3 +1,3 @@
    {
   -  ".": "0.0.0"
   +  ".": "0.1.0"
    }

   dry_run_exit=0
   ```
   **O dry-run encontrou um defeito real.** A primeira execução propôs `1.0.0`, e não
   `0.1.0`. A causa está em `BaseStrategy.buildNewVersion()`: sem release anterior, o
   release-please não usa a versão do manifesto, e sim `initialReleaseVersion()`, cujo
   padrão é `1.0.0`. A correção foi a chave `initial-version: "0.1.0"` no
   `release-please-config.json` (commit `de2d260`). A tag gerada é `v0.1.0`, que é a
   saída da Fase 0.

6. **Invariantes de especificação, links, arquitetura e diário de IA:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   node scripts/check-ai-log.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid:
     - Required core documents and ADRs present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN.md are cataloged in BACKLOG.md.

   ✓ 62 documents, every relative link resolves.

   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso!

   ✓ AI usage log: 16 sessions, structure valid.
   ```

#### Conteúdo Entregue:
- **Imagens:** `backend/Dockerfile`, `central-convenios-mock/Dockerfile` e
  `frontend/Dockerfile` em build de múltiplos estágios, a partir do contexto da raiz,
  com runtime sem privilégio e `HEALTHCHECK`; `.dockerignore` na raiz.
- **Versionamento:** `release-please-config.json` com o tipo `simple` (uma versão para
  a plataforma inteira), `initial-version: 0.1.0` e manifesto começando em `0.0.0`.
- **Publicação:** `.github/workflows/release.yml` roda o release-please no push em
  `main` e publica as três imagens no GHCR com as tags `vX.Y.Z`, `X.Y.Z` e `latest`
  (a `latest` nunca é movida por uma pré-release).
  A publicação é disparada **pelo push da tag**, não pelo push em `main`: o job
  `publish-images` declarava `needs: release-please`, e no push da tag o job
  `release-please` é pulado (`if: github.ref_type == 'branch'`) — um `needs` pulado
  leva o dependente junto, então o caminho de publicação documentado nunca executava.
  O job passou a ser independente, com `IMAGE_TAG` vindo de `github.ref_name` e o
  checkout fixado no commit etiquetado.
- **Deploy:** `.github/workflows/cd-staging.yml` dispara na tag, sobe
  `compose.staging.yaml` com as imagens publicadas, exige os healthchecks dos três
  serviços e imprime os digests.
- **Nightly:** `.github/workflows/nightly.yml` passa a construir e escanear as três
  imagens com Trivy; `.github/workflows/ci.yml` valida o compose de staging.
- **Documentação:** `docs/process/RELEASE.md` (fluxo, imagens, rollback e segredos),
  portão 3.8 e matriz em `docs/quality/GATES.md`.
- **Governança de IA:** atribuição de agente/modelo corrigida nas tasks 0.6 a 0.11 e
  política de modelos gratuitos registrada em `docs/ai-harness/HARNESS_OVERVIEW.md` §4.

#### Arquivos Tocados:
- `.dockerignore`
- `backend/Dockerfile`, `central-convenios-mock/Dockerfile`, `frontend/Dockerfile`
- `release-please-config.json`, `.release-please-manifest.json`
- `.github/workflows/release.yml`, `.github/workflows/cd-staging.yml`
- `.github/workflows/ci.yml`, `.github/workflows/nightly.yml`
- `compose.staging.yaml`
- `docs/process/RELEASE.md`, `docs/quality/GATES.md`
- `docs/ai-harness/AI_USAGE_LOG.md`, `docs/ai-harness/HARNESS_OVERVIEW.md`
- `docs/adr/0006-gitflow-ci-cd-docker-e-performance.md`
- `SDD/specs/PLAN.md`, `SDD/specs/BACKLOG.md`, `SDD/docs/EVIDENCE.md`

---

### Task 0.12 — Promoção da release `v0.1.0` e arquivamento da Fase 0

- **Data de Conclusão:** 2026-11-26
- **Branch:** `chore/012-phase-0-archive`
- **PR:** [#21](https://github.com/marcelinosandroni/consigport-platform/pull/21) (back-merge da `main` + arquivamento)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Promover a `v0.1.0` até a `main`, publicar as três imagens no GHCR,
  validar o staging e arquivar a Fase 0 em `specs/history/phases/`.

#### Comandos de Verificação e Saídas:

1. **Tag e release publicadas:**
   ```text
   git tag --list "v*"        → v0.1.0
   gh release view v0.1.0     → v0.1.0 (published 2026-11-25T16:58:02Z)
   ```
2. **Imagens no GHCR (tags `v0.1.0`, `0.1.0` e `latest`):**
   ```text
   gh api user/packages/container/consigport-platform%2Fbackend/versions \
     --jq '.[0].metadata.container.tags[]'  → latest 0.1.0 v0.1.0
   ```
   Digests: backend `sha256:bc42cbb3…c452a1`, mock `sha256:caadf344…a37cb5`,
   frontend `sha256:278a1113…2bc2f3`.
3. **Staging real (run `45661533860`, tag `v0.1.0`):** os quatro containers
   `healthy`, `/actuator/health` do backend e do mock → `{"status":"UP"}`, teardown
   verde.
4. **Gates de especificação, links, arquitetura e diário:**
   ```text
   node scripts/check-specs.mjs                        → exit 0
   node SDD/SKILLS/check-docs/check-docs.mjs .         → exit 0
   node scripts/check-architecture.mjs                 → exit 0 (22/22)
   node scripts/check-ai-log.mjs                       → exit 0 (18 sessões)
   node scripts/check-theme-tokens.mjs                 → exit 0
   ```

#### Conteúdo Entregue:
- Fase 0 arquivada em `SDD/specs/history/phases/phase-0-finished.md`.
- `PLAN.md` limpo, com a Fase 0 concluída e a próxima task (1.1) registrada.
- `ROADMAP.md` sincronizado com a realidade: `v0.1.0`, Fase 0 fechada, 51 tasks.
- `CHANGELOG.md` sem as entradas duplicadas geradas pelos merges
  `develop → staging → main`.
- `docs/process/GITFLOW.md` registra que a proteção de branches permanece desativada
  por decisão do responsável, com merges integrados via `gh pr merge`.

#### Arquivos Tocados:
- `SDD/specs/history/phases/phase-0-finished.md`
- `SDD/specs/PLAN.md`, `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`
- `docs/process/GITFLOW.md`
- `CHANGELOG.md`

---

### Task 1.1 — Store em memória, fixtures e administração

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/101-mock-store`
- **PR:** [#22](https://github.com/marcelinosandroni/consigport-platform/pull/22)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Estado determinístico reiniciável com `ConcurrentHashMap`, fixtures do
  `ARCHITECTURE.md` §4 e os endpoints administrativos de reset e inspeção.

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **`POST /_admin/reset` e `GET /_admin/state` no processo real (porta 18081):**
   ```text
   POST /_admin/reset → HTTP 204
   GET /_admin/state  → 200 com as quatro fixtures
   {"beneficiarios":{"BEN-001":[{"parcelaId":"PAR-001","convenioId":"AGR-001","valor":450.00,"dataDesconto":"2026-12-05","status":"ABERTA"}, …],"BEN-MARGIN":[…],"BEN-REJ":[…],"BEN-TIMEOUT":[…]},"autorizacoes":{},"operacoes":{},"contadores":{"leiturasDeStatus":{}}}
   ```
3. **Determinismo (dois resets comparados byte a byte):**
   ```text
   curl -s /_admin/state > s1.json
   curl -s -X POST /_admin/reset
   curl -s /_admin/state > s2.json
   diff -q s1.json s2.json → estados idênticos
   ```

#### Conteúdo Entregue:
- `store/InMemoryCentralStore.java` — `ConcurrentHashMap` para agenda, autorizações,
  operações e contadores; `reset()` restaura as fixtures e `snapshot()` devolve o estado
  com chaves ordenadas.
- `store/SeedData.java` — as quatro fixtures do `ARCHITECTURE.md` §4 (`BEN-001`,
  `BEN-REJ`, `BEN-MARGIN`, `BEN-TIMEOUT`), todas `ABERTA` e com valores em `BigDecimal`.
- `api/AdminController.java` — `POST /_admin/reset` → `204` e `GET /_admin/state` → `200`.
- `domain/` — `Installment`, `Authorization`, `Operation` e `OperationStatus`, base das
  tasks 1.2–1.5.
- `InMemoryCentralStoreTests` — 3 testes de integração, incluindo os dois resets
  comparados.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/SeedData.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/AdminController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/domain/Installment.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/domain/Authorization.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/domain/Operation.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/domain/OperationStatus.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/store/InMemoryCentralStoreTests.java`

---

### Task 1.2 — Consulta de agenda

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/102-mock-agenda`
- **PR:** [#23](https://github.com/marcelinosandroni/consigport-platform/pull/23)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Expor as parcelas futuras de um beneficiário autorizado, com `404` para
  beneficiário desconhecido e `403` sem opt-in (BR-01).

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **MockMvc (`AgendaControllerTests`):**
   ```text
   BEN-001 com opt-in   → 200, 3 parcelas (PAR-001, PAR-002, PAR-003)
   BEN-UNKNOWN          → 404 BENEFICIARIO_NAO_ENCONTRADO
   BEN-REJ sem opt-in   → 403 SEM_AUTORIZACAO
   ```
3. **Processo real (porta 18082):**
   ```text
   GET /beneficiarios/BEN-001/agenda     → 403 {"codigo":"SEM_AUTORIZACAO","mensagem":"O beneficiário não possui autorização para este convênio.","timestamp":"..."}
   GET /beneficiarios/BEN-UNKNOWN/agenda → 404 {"codigo":"BENEFICIARIO_NAO_ENCONTRADO","mensagem":"Beneficiário não encontrado.","timestamp":"..."}
   ```

#### Conteúdo Entregue:
- `AgendaController` — `GET /beneficiarios/{beneficiarioId}/agenda` devolve o array de
  `Parcela` exatamente como o contrato define; por isso não há envelope `AgendaResponse`.
  Apenas parcelas `ABERTA` são devolvidas (BR-03).
- `InstallmentResponse` — DTO da `Parcela` com `BigDecimal` e `LocalDate`.
- `CentralException` + `ApiExceptionHandler` — erros no formato `ErroCentral`
  (`codigo`, `mensagem`, `timestamp`), reutilizados pelas próximas tasks.
- `InMemoryCentralStore` ganhou `registrarAutorizacao` e `possuiAutorizacao`, base do
  opt-in da task 1.3.
- `AgendaControllerTests` — 3 testes MockMvc cobrindo `200`, `403` e `404`.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/AgendaController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/ApiExceptionHandler.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/CentralException.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/InstallmentResponse.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/ErroCentralResponse.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/AgendaControllerTests.java`

---

### Task 1.3 — Opt-in de autorização

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/103-mock-authorization`
- **PR:** [#24](https://github.com/marcelinosandroni/consigport-platform/pull/24)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Registrar o consentimento do beneficiário, único por par
  `(beneficiarioId, convenioId)` (BR-02), e liberar a agenda (BR-01).

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Processo real (porta 18083):**
   ```text
   POST /autorizacoes {"beneficiarioId":"BEN-001","convenioId":"AGR-001"}
   → 201 {"autorizacaoId":"AUT-20261126-0001"}

   POST /autorizacoes (mesmo par)
   → 200 {"autorizacaoId":"AUT-20261126-0001"}

   POST /autorizacoes {"beneficiarioId":"","convenioId":"AGR-001"}
   → 400 {"codigo":"REQUISICAO_INVALIDA","mensagem":"O campo beneficiarioId é obrigatório.",...}

   GET /beneficiarios/BEN-001/agenda
   → 200 com PAR-001, PAR-002 e PAR-003
   ```
3. **MockMvc (`AuthorizationControllerTests`):** criação (`201`), repetição (`200` com o
   mesmo id), campos vazios (`400`) e agenda liberada após o opt-in.

#### Conteúdo Entregue:
- `AuthorizationController` — `POST /autorizacoes` devolve `201` na criação e `200` na
  repetição, com o mesmo `autorizacaoId`.
- `AuthorizationRequest` — validação `@NotBlank` e `@Size(max = 64)` nos dois campos.
- `AuthorizationResponse` — corpo `{"autorizacaoId": "..."}`.
- `InMemoryCentralStore.autorizar` — índice único por par, geração determinística
  `AUT-yyyyMMdd-NNNN` e sequência zerada pelo reset.
- `ApiExceptionHandler` — validação e corpo ilegível viram `400 REQUISICAO_INVALIDA`.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/AuthorizationController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/ApiExceptionHandler.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/AuthorizationRequest.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/AuthorizationResponse.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/AuthorizationControllerTests.java`

---

### Task 1.4 — Submissão de portabilidade

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/104-mock-portability`
- **PR:** [#25](https://github.com/marcelinosandroni/consigport-platform/pull/25)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Aceitar a operação de portabilidade com protocolo único, estado inicial
  `PENDENTE` e `422` para parcela inexistente ou não elegível (BR-04).

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Processo real (porta 18084):**
   ```text
   POST /operacoes-portabilidade (1ª) → 202 {"protocolo":"PRT-20261126-0001"}
   POST /operacoes-portabilidade (2ª) → 202 {"protocolo":"PRT-20261126-0002"}
   POST /operacoes-portabilidade (PAR-999)
     → 422 {"codigo":"PARCELA_INVALIDA","mensagem":"A parcela PAR-999 não existe.",...}
   GET /_admin/state → operação com status PENDENTE
   ```
3. **MockMvc (`PortabilityControllerTests`):** `202` com protocolo no padrão, estado
   `PENDENTE` no snapshot, protocolos únicos (`-0001`, `-0002`), `422` e `400`.

#### Conteúdo Entregue:
- `PortabilityController` — `POST /operacoes-portabilidade` valida as parcelas do
  beneficiário e devolve `202` com o protocolo.
- `PortabilityRequest` — DTO com `beneficiarioId`, `parcelas`, `credorCessionario`,
  `contaRecebimento` (aninhado) e `assinatura`, com Bean Validation.
- `PortabilityAcceptedResponse` — corpo `{"protocolo": "..."}`.
- `InMemoryCentralStore.criarOperacao` — protocolo determinístico
  `PRT-yyyyMMdd-NNNN`, operação `PENDENTE` e sequência zerada pelo reset.
- `operacao(protocolo)` — leitura usada pela consulta de status da task 1.5.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/PortabilityController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/PortabilityRequest.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/PortabilityAcceptedResponse.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/PortabilityControllerTests.java`

---

### Task 1.5 — Consulta de status com `Retry-After`

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/105-mock-status`
- **PR:** [#26](https://github.com/marcelinosandroni/consigport-platform/pull/26)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Simular a confirmação assíncrona: `204` com `Retry-After` enquanto
  `PENDENTE`, `200` com o status terminal depois do delay e `404` para protocolo
  desconhecido.

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Processo real com `MOCK_CONFIRMATION_DELAY_MS=200` (porta 18085):**
   ```text
   POST /operacoes-portabilidade → 202 {"protocolo":"PRT-20261126-0001"}
   GET  /operacoes-portabilidade/PRT-20261126-0001 (imediato)
     → 204, Retry-After: 2
   GET  /operacoes-portabilidade/PRT-20261126-0001 (após 600 ms)
     → 200 {"status":"CONFIRMADA","motivoRejeicao":null}
   GET  /operacoes-portabilidade/PRT-20260101-9999
     → 404 {"codigo":"OPERACAO_NAO_ENCONTRADA",...}
   ```
3. **MockMvc (`PortabilityStatusTests`):** `204` + `Retry-After`, `CONFIRMADA`,
   `REJEITADA` por sufixo `-REJ`, `REJEITADA` por soma acima de `5000.00` e `404`.

#### Conteúdo Entregue:
- `PortabilityController.consultarStatus` — `GET /operacoes-portabilidade/{protocolo}`
  com `204` + `Retry-After: 2` enquanto pendente e `200` no estado terminal.
- `TransitionScheduler` — executor agendado de thread única que aplica a transição após
  `mock.confirmation-delay-ms` (env `MOCK_CONFIRMATION_DELAY_MS`, padrão 2000 ms).
- `InMemoryCentralStore.concluir` — `CONFIRMADA` no caminho feliz; `REJEITADA` com
  `MARGEM_CONSIGNADA_INSUFICIENTE` para sufixo `-REJ` ou soma acima de `5000.00`
  (BR-14). A comparação por identidade impede que uma tarefa antiga altere uma operação
  nova criada após o reset.
- `StatusResponse` — corpo `StatusOperacao` do contrato.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/PortabilityController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/dto/StatusResponse.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/TransitionScheduler.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/main/resources/application.yml`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/PortabilityStatusTests.java`

---

### Task 1.6 — HMAC e idempotência

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/106-mock-hmac`
- **PR:** [#27](https://github.com/marcelinosandroni/consigport-platform/pull/27)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** garantir integridade das mensagens com HMAC-SHA256 (BR-06) e impedir
  operações duplicadas com `Idempotency-Key` (BR-07).

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Vetor compartilhado (`contracts/hmac-vector.json`):**
   ```text
   HmacValidatorTests → reproduz canônico e assinatura de todos os vetores
   ```
3. **Processo real (porta 18086):**
   ```text
   POST assinado (X-Signature + X-Timestamp + Idempotency-Key)
     → 202 {"protocolo":"PRT-20261126-0001"}
   POST mesma key + mesmo payload        → 202 {"protocolo":"PRT-20261126-0001"}
   POST mesma key + payload diferente    → 409 IDEMPOTENCIA_CONFLITO
   POST assinatura divergente            → 401 ASSINATURA_INVALIDA
   POST timestamp expirado (-400 s)      → 401 ASSINATURA_INVALIDA
   GET  /beneficiarios/BEN-001/agenda    → 401 ASSINATURA_INVALIDA (sem assinatura)
   ```

#### Conteúdo Entregue:
- `CanonicalRequest` — JSON com chaves ordenadas, sem espaços e sem o campo `assinatura`;
  canônico `METHOD\nPATH_WITH_QUERY\nTIMESTAMP\nSHA256_HEX(body)`.
- `HmacValidator` — HMAC-SHA256 hexadecimal minúsculo, segredo e tolerância de ±300 s
  vindos do vetor compartilhado.
- `HmacFilter` — protege `/autorizacoes`, `/beneficiarios/**` e
  `/operacoes-portabilidade/**`, devolve `401 ASSINATURA_INVALIDA` e mantém o corpo
  legível para os controllers.
- `IdempotencyRegistry` — guarda hash do payload e protocolo por `Idempotency-Key`; o
  reset limpa o registry.
- `PortabilityController` — key obrigatória (`400 REQUISICAO_INVALIDA`), mesmo payload
  devolve o mesmo protocolo e payload divergente devolve `409 IDEMPOTENCIA_CONFLITO`.
- `application.yml`, `.env.example` e `compose.staging.yaml` — `MOCK_HMAC_SECRET` e
  `MOCK_HMAC_TOLERANCE_SECONDS`.

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/security/CanonicalRequest.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/security/HmacValidator.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/security/HmacFilter.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/security/CachedBodyHttpServletRequest.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/IdempotencyRegistry.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/PortabilityController.java`
- `central-convenios-mock/src/main/resources/application.yml`
- `central-convenios-mock/src/test/java/br/com/consigport/central/security/HmacValidatorTests.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/security/HmacFilterTests.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/IdempotencyTests.java`
- `.env.example`, `compose.staging.yaml`

---

### Task 1.7 — Caos determinístico e contrato publicado

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/107-mock-chaos`
- **PR:** [#28](https://github.com/marcelinosandroni/consigport-platform/pull/28)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** entregar os cenários de rejeição e falha que o E2E consome e publicar o
  contrato da Central idêntico a `contracts/central-api.yaml`.

#### Comandos de Verificação e Saídas:

1. **Build e testes do módulo:**
   ```text
   ./mvnw -B -pl central-convenios-mock -am verify
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Processo real (porta 18087, `MOCK_TIMEOUT_DELAY_MS=800`):**
   ```text
   BEN-REJ  → {"status":"REJEITADA","motivoRejeicao":"MARGEM_CONSIGNADA_INSUFICIENTE"}
   BEN-MARGIN (6000.00) → {"status":"REJEITADA","motivoRejeicao":"MARGEM_CONSIGNADA_INSUFICIENTE"}
   BEN-TIMEOUT 1ª leitura → HTTP 200 em 0.806 s
   BEN-TIMEOUT 2ª leitura → HTTP 200 em 0.805 s
   BEN-TIMEOUT 3ª leitura → HTTP 200 em 0.004 s
   X-Mock-Delay-Ms: 500 → HTTP 200 em 0.505 s
   X-Mock-Force-Error: 503 (status) → 503 ERRO_FORCADO
   X-Mock-Force-Error: 500 (agenda) → 500 ERRO_FORCADO
   contadores antes do reset → {"leiturasDeStatus": {"PRT-20261126-0003": 4}}
   POST /_admin/reset → 204
   contadores após o reset → {"leiturasDeStatus": {}}
   ```
3. **Contrato exportado:**
   ```text
   diff contracts/central-api.yaml \
     central-convenios-mock/src/main/resources/openapi/central-api.yaml
   → sem diferença
   ```
4. **MockMvc (`ChaosTests`, `ContractTest`):** headers de atraso e erro, duas primeiras
   leituras do `-TIMEOUT`, reset dos contadores e contrato byte a byte.

#### Conteúdo Entregue:
- `ChaosPolicy` — atraso explícito por header, erro forçado `500/502/503` e contador
  determinístico por operação para beneficiários `-TIMEOUT`.
- `AgendaController` e `PortabilityController` — aplicam `X-Mock-Delay-Ms` e
  `X-Mock-Force-Error` somente à requisição que os carrega.
- `InMemoryCentralStore` — snapshot dos contadores e reset do caos junto com o estado.
- `central-convenios-mock/src/main/resources/openapi/central-api.yaml` — cópia publicada
  do contrato, garantida por `ContractTest`.
- `application.yml` — `mock.timeout-delay-ms` (`MOCK_TIMEOUT_DELAY_MS`, padrão 6000 ms).

#### Arquivos Tocados:
- `central-convenios-mock/src/main/java/br/com/consigport/central/chaos/ChaosPolicy.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/AgendaController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/api/PortabilityController.java`
- `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`
- `central-convenios-mock/src/main/resources/openapi/central-api.yaml`
- `central-convenios-mock/src/test/java/br/com/consigport/central/api/ChaosTests.java`
- `central-convenios-mock/src/test/java/br/com/consigport/central/ContractTest.java`

---

### Fase 1 — Central mock: arquivamento e promoção da `v0.2.0`

- **Data de Conclusão:** 2026-11-26
- **Branch:** `chore/017-phase-1-archive`
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar a Fase 1 (7 tasks), promover a release `v0.2.0` e abrir a Fase 2.

#### Comandos de Verificação e Saídas:

1. **Gates locais antes da promoção:**
   ```text
   node scripts/check-specs.mjs               → OK
   node SDD/SKILLS/check-docs/check-docs.mjs  → OK
   node scripts/check-architecture.mjs        → OK
   node scripts/check-ai-log.mjs              → OK
   node scripts/check-theme-tokens.mjs        → OK
   ./mvnw -B verify
   → ConsigPort Platform ................................ SUCCESS
   → ConsigPort Backend ................................. SUCCESS
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   Tests run: 39, Failures: 0, Errors: 0, Skipped: 0 (central-convenios-mock)
   ```
2. **Promoção e release:**
   ```text
   PR #29  develop → staging   → CI verde → merge
   PR #30  staging → main      → CI verde → merge
   PR #31  chore(main): release 0.2.0 → merge
   → tag v0.2.0 e release publicada
   → Release (45817926979) — Publish backend/frontend/central-convenios-mock to GHCR: success
   ```
3. **Imagens e staging:**
   ```text
   backend              sha256:b036e79c7e9cae9396154b74bfe263c5b8b7fd9cc2471d1449f920b2d049c75a
   central-convenios-mock sha256:8097fae4e6b83cf52e058c5c99b063fad368baf697016e227ed551c58fe43417
   frontend             sha256:788b083540757b20b8b788782e8853af31e490fa151ba27c93b728c6b6d4196d
   CD Staging (45818653277): {"status":"UP"} em backend e mock; frontend responde 200
   ```

#### Conteúdo Entregue:
- `SDD/specs/history/phases/phase-1-finished.md` — as 7 tasks, os PRs, os critérios de
  saída, a evidência da release e as dívidas conhecidas.
- `SDD/specs/PLAN.md` — Fase 1 concluída e Fase 2 aberta com a task 2.1 em voo.
- `SDD/specs/ROADMAP.md` — Fase 1 fechada com a tag `v0.2.0` e a Fase 2 como fase atual.
- `docs/ai-harness/AI_USAGE_LOG.md` — sessão #26.

#### Nota Operacional:
- O `release-please` cria a tag pela API do GitHub, o que **não** dispara o evento `push`
  da tag. As imagens foram publicadas por `workflow_dispatch` em `release.yml` com a tag
  `v0.2.0`, caminho já documentado em [`../../docs/process/RELEASE.md`](../../docs/process/RELEASE.md) §6.

#### Arquivos Tocados:
- `SDD/specs/history/phases/phase-1-finished.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 2.1 — Esqueleto hexagonal e regras ArchUnit

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/021-backend-hexagonal-skeleton`
- **PR:** [#33](https://github.com/marcelinosandroni/consigport-platform/pull/33)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar as camadas do backend e impedir dependências proibidas desde o
  primeiro commit (domínio puro, aplicação só no domínio, `config` como único composition
  root).

#### Comandos de Verificação e Saídas:

1. **Verificação da task (comando do BACKLOG):**
   ```text
   ./mvnw -q -pl backend test -Dtest=HexagonalArchitectureTest
   → Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Módulo backend completo:**
   ```text
   ./mvnw -B -pl backend test
   → HexagonalArchitectureTest: Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
   → ConsigPortApplicationTests: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
3. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Platform ................................ SUCCESS
   → ConsigPort Backend ................................. SUCCESS
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS (39 testes no mock)
   ```
4. **Prova de vida do guard (violação temporária no código de produção):**
   ```text
   → class domain/authorization/TemporaryViolation.java com @Component (Spring)
   → Tests run: 9, Failures: 1, Errors: 0, Skipped: 0
   → Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside in a
     package '..domain..' should depend on classes that reside in any package
     ['org.springframework..', ...]' was violated (2 times)
   → BUILD FAILURE
   → arquivo temporário removido após a prova
   ```

#### Conteúdo Entregue:
- `domain`, `application`, `adapters` e `config` — as quatro camadas declaradas com
  `package-info.java` que documenta a fronteira de cada uma.
- `config/CompositionRoot` — composition root com o bean `Clock.systemUTC()`; único lugar
  que conhece implementações concretas.
- `HexagonalArchitectureTest` — seis regras ArchUnit verificadas explicitamente:
  domínio sem framework/ORM/HTTP/logger, aplicação importando apenas `domain` e o JDK,
  domínio e aplicação nunca alcançando adaptadores, adaptadores de entrada nunca
  alcançando adaptadores de saída, `config` privado e ausência de ciclos entre camadas.
- Fixtures em `br.com.consigport.architecture.fixture` — violações deliberadas que
  provam que cada regra rejeita o defeito que descreve.
- `backend/pom.xml` — dependência de teste `archunit-junit5` (versão gerenciada no
  reactor).

#### Descobertas e Decisões:
- O engine JUnit 5 do ArchUnit 1.4.2 **não descobre** os campos `@ArchTest` sob o JUnit
  Platform 1.12.2: uma regra deliberadamente inválida passou sem rodar. Por isso as regras
  são declaradas como constantes e avaliadas em métodos `@Test` comuns — o mesmo conjunto
  de regras, sem depender da descoberta do engine.
- O ArchUnit falha por padrão quando uma regra não tem classes para checar
  (`failOnEmptyShould`). Só a regra "adaptador de entrada não alcança adaptador de saída"
  usa `allowEmptyShould(true)`: a camada HTTP nasce na task 2.9 e a regra passa a valer
  a partir do primeiro adaptador de entrada — o fixture prova que ela rejeita a violação.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/package-info.java`
- `backend/src/main/java/br/com/consigport/application/package-info.java`
- `backend/src/main/java/br/com/consigport/adapters/package-info.java`
- `backend/src/main/java/br/com/consigport/config/package-info.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/test/java/br/com/consigport/architecture/HexagonalArchitectureTest.java`
- `backend/src/test/java/br/com/consigport/architecture/fixture/` (4 fixtures)
- `backend/pom.xml`

---

### Task 2.2 — Value objects compartilhados

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/022-backend-value-objects`
- **PR:** [#34](https://github.com/marcelinosandroni/consigport-platform/pull/34)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** trocar `String` e `BigDecimal` soltos por tipos fortes, com invariante
  validada na construção e igualdade por valor.

#### Comandos de Verificação e Saídas:

1. **Módulo backend completo:**
   ```text
   ./mvnw -B -pl backend test
   → PortabilityValueObjectsTest$MoneyTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → PortabilityValueObjectsTest$ReceivingAccountTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → SharedValueObjectsTest$BeneficiaryIdTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → SharedValueObjectsTest$AgreementIdTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → SharedValueObjectsTest$InstallmentIdTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → SharedValueObjectsTest$IdempotencyKeyTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → HexagonalArchitectureTest: Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
   → ConsigPortApplicationTests: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.008 s]
   → ConsigPort Backend ................................. SUCCESS [ 25.484 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida dos invariantes (remoção temporária da validação de valor negativo):**
   ```text
   → guard `amount.signum() < 0` removido de domain/portability/Money.java
   → ./mvnw -B -pl backend test -Dtest=PortabilityValueObjectsTest
   → Tests run: 6, Failures: 1, Errors: 0, Skipped: 0 -- in
     br.com.consigport.domain.portability.PortabilityValueObjectsTest$MoneyTest
   → BUILD FAILURE
   → guard restaurado; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `domain/shared/BeneficiaryId`, `AgreementId`, `InstallmentId` — identificadores não
  vazios, no máximo 64 caracteres, normalizados com `trim` e comparados por valor.
- `domain/shared/IdempotencyKey` — UUID obrigatório (BR-07), com `parse` a partir do
  texto e forma canônica em `toString`.
- `domain/portability/Money` — `BigDecimal` com exatamente 2 casas, nunca negativo,
  escala normalizada (`10` e `10.00` são o mesmo valor).
- `domain/portability/ReceivingAccount` — `bank`, `branch` e `account` obrigatórios,
  com os limites do contrato (`64`, `32`, `64` caracteres).
- `SharedValueObjectsTest` e `PortabilityValueObjectsTest` — 28 testes de unidade,
  incluindo todos os casos inválidos.

#### Descobertas e Decisões:
- `ARCHITECTURE.md` §3 posiciona `Money` e `ReceivingAccount` em `domain/portability/` e
  `BeneficiaryId`, `AgreementId`, `InstallmentId` e `IdempotencyKey` em `domain/shared/`.
  A árvore do `ARCHITECTURE.md` foi seguida; a linha de arquivos do `BACKLOG.md` lista os
  seis nomes em sequência sob o prefixo de `domain/shared/`.
- O invariante de valor não negativo foi provado com uma mutação temporária do código de
  produção: sem o guard, o teste de `Money` falha. O guard foi restaurado e a suíte
  voltou a ficar verde.
- Nenhum VO depende de framework: `domain/` continua obeying as regras do
  `HexagonalArchitectureTest` da task 2.1.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/shared/BeneficiaryId.java`
- `backend/src/main/java/br/com/consigport/domain/shared/AgreementId.java`
- `backend/src/main/java/br/com/consigport/domain/shared/InstallmentId.java`
- `backend/src/main/java/br/com/consigport/domain/shared/IdempotencyKey.java`
- `backend/src/main/java/br/com/consigport/domain/portability/Money.java`
- `backend/src/main/java/br/com/consigport/domain/portability/ReceivingAccount.java`
- `backend/src/test/java/br/com/consigport/domain/shared/SharedValueObjectsTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityValueObjectsTest.java`

---

### Task 2.3 — Agregado de autorização

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/023-backend-authorization-aggregate`
- **PR:** [#35](https://github.com/marcelinosandroni/consigport-platform/pull/35)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** modelar o opt-in único por beneficiário e convênio (BR-02), com a porta de
  persistência declarada no domínio.

#### Comandos de Verificação e Saídas:

1. **Testes da task:**
   ```text
   ./mvnw -B -pl backend test -Dtest='AuthorizationTest,AuthorizationRepositoryContractTest'
   → AuthorizationRepositoryContractTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → AuthorizationTest: Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
   → AuthorizationTest$AuthorizationIdTest: Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 57, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.006 s]
   → ConsigPort Backend ................................. SUCCESS [ 25.636 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida do invariante (remoção temporária da validação do convênio):**
   ```text
   → guard `agreementId == null` removido de domain/authorization/Authorization.java
   → ./mvnw -B -pl backend test -Dtest=AuthorizationTest
   → Tests run: 7, Failures: 1, Errors: 0, Skipped: 0 -- in
     br.com.consigport.domain.authorization.AuthorizationTest
   → BUILD FAILURE
   → guard restaurado; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `domain/authorization/Authorization` — agregado imutável com `AuthorizationId`
  (UUID), `BeneficiaryId`, `AgreementId` e `createdAt`; a fábrica `register` cria a
  identidade e o chamador injeta o instante lido do relógio.
- `domain/authorization/AuthorizationRepository` — porta com
  `findByBeneficiaryAndAgreement` e `save`, sem anotação de framework.
- `domain/authorization/AuthorizationAlreadyExistsException` — erro de domínio para o par
  já autorizado; o adaptador de persistência traduz a constraint única para ele.
- `AuthorizationTest` e `AuthorizationRepositoryContractTest` — 18 testes de unidade: o
  agregado e o contrato da porta, com o fake em memória
  `InMemoryAuthorizationRepository` (sem mocks).

#### Descobertas e Decisões:
- `AuthorizationId` é um record aninhado em `Authorization`: a árvore do
  `ARCHITECTURE.md` §3 mantém `domain/shared/` para os identificadores compartilhados e o
  `BACKLOG.md` lista apenas três arquivos para a task 2.3.
- A unicidade do par `(beneficiaryId, agreementId)` é invariante do repositório: o
  agregado não conhece outras autorizações. O caso de uso da task 2.6 vai consultar a
  porta antes de gravar e devolver o registro existente (BR-02).
- O fake da porta é escrito à mão, conforme `stacks/java.md`: o contrato da porta é
  verificado em tempo de compilação, e não escondido atrás de stubs de runtime.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/authorization/Authorization.java`
- `backend/src/main/java/br/com/consigport/domain/authorization/AuthorizationRepository.java`
- `backend/src/main/java/br/com/consigport/domain/authorization/AuthorizationAlreadyExistsException.java`
- `backend/src/test/java/br/com/consigport/domain/authorization/AuthorizationTest.java`
- `backend/src/test/java/br/com/consigport/domain/authorization/AuthorizationRepositoryContractTest.java`
- `backend/src/test/java/br/com/consigport/domain/authorization/InMemoryAuthorizationRepository.java`

---

### Task 2.4 — Agregado de portabilidade e máquina de estados

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/024-backend-portability-aggregate`
- **PR:** [#36](https://github.com/marcelinosandroni/consigport-platform/pull/36)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** concentrar no domínio as invariantes da operação e as transições do
  ciclo de vida, recusando qualquer transição fora da tabela do `ARCHITECTURE.md` §7.

#### Comandos de Verificação e Saídas:

1. **Testes da task:**
   ```text
   ./mvnw -B -pl backend test -Dtest='PortabilityOperationTest,InstallmentSnapshotTest,RejectionReasonTest'
   → InstallmentSnapshotTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → PortabilityOperationTest: Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
   → PortabilityOperationTest$Creation: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → PortabilityOperationTest$OperationIdTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → RejectionReasonTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 120, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.006 s]
   → ConsigPort Backend ................................. SUCCESS [ 26.897 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida da máquina de estados (transição indevida liberada temporariamente):**
   ```text
   → `CREATED` incluído como origem válida de `confirm()` em PortabilityOperation.java
   → ./mvnw -B -pl backend test -Dtest=PortabilityOperationTest
   → Tests run: 43, Failures: 1, Errors: 0, Skipped: 0 -- in
     br.com.consigport.domain.portability.PortabilityOperationTest
   → BUILD FAILURE
   → máquina de estados restaurada; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `domain/portability/PortabilityOperation` — agregado imutável: identidade
  (`OperationId`), `IdempotencyKey`, beneficiário, convênio, snapshot das parcelas,
  credor cessionário, conta de recebimento, total, status, protocolo, motivo de rejeição,
  contador de tentativas, próxima consulta e instante de atualização da conta.
- Invariantes no construtor: ao menos uma parcela, total igual à soma do snapshot,
  credor cessionário não vazio, protocolo nulo enquanto `CREATED`, motivo obrigatório
  apenas em `REJECTED` e contador de tentativas não negativo.
- Transições: `submit`, `registerIntegrationFailure`, `pollAttempt`, `confirm`, `reject` e
  `complete` — cada uma devolve uma nova instância e recusa as combinações ausentes da
  tabela do `ARCHITECTURE.md` §7 com `InvalidTransitionException`.
- `domain/portability/OperationStatus` — o vocabulário do ciclo de vida, com
  `isTerminal()` para `COMPLETED` e `REJECTED`.
- `domain/portability/InstallmentSnapshot` — o retrato congelado da parcela (BR-05).
- `domain/portability/RejectionReason` — hierarquia selada com o motivo conhecido
  (`MARGIN_INSUFFICIENT`), resolvido pelo código público.
- Testes: 36 casos parametrizados (6 estados × 6 gatilhos) mais 27 casos de invariantes.

#### Descobertas e Decisões:
- `submit` e `pollAttempt` têm o mesmo destino (`PENDING`); a distinção da tabela é o
  gatilho, então cada método declara as origens aceitas e a máquina fica em um único
  lugar.
- Os estados terminais não aparecem como origem em nenhuma transição, e o guard
  consulta `isTerminal()` antes de aceitar: um estado terminal nunca sai do lugar.
- O total nunca é informado pelo chamador: `create` soma o snapshot e o construtor
  recusa qualquer total divergente, então a invariante vale também para as operações
  reconstruídas pela persistência.
- O snapshot é copiado com `List.copyOf`: a lista recebida pelo chamador não altera o
  agregado.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperation.java`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationStatus.java`
- `backend/src/main/java/br/com/consigport/domain/portability/InstallmentSnapshot.java`
- `backend/src/main/java/br/com/consigport/domain/portability/RejectionReason.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityOperationTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InstallmentSnapshotTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/RejectionReasonTest.java`

---

### Task 2.5 — Evento de auditoria append-only

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/025-backend-audit-event`
- **PR:** [#37](https://github.com/marcelinosandroni/consigport-platform/pull/37)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** registrar cada mudança de estado em uma trilha imutável, sem caminho no
  domínio para alterar ou apagar um evento.

#### Comandos de Verificação e Saídas:

1. **Testes da task:**
   ```text
   ./mvnw -B -pl backend test -Dtest='OperationEventTest,OperationEventRepositoryContractTest,EventTypeTest'
   → EventTypeTest: Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
   → OperationEventRepositoryContractTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → OperationEventTest: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 139, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [ 27.333 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida do guard append-only (mutador público exposto temporariamente):**
   ```text
   → método público `setDetails` added a OperationEvent.java
   → ./mvnw -B -pl backend test -Dtest=OperationEventTest
   → Tests run: 12, Failures: 1, Errors: 0, Skipped: 0 -- in
     br.com.consigport.domain.audit.OperationEventTest
   → OperationEventTest.exposesNoPublicMutator — "setDetails", ["setDetails"]
   → BUILD FAILURE
   → mutador removido; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `domain/audit/OperationEvent` — evento imutável com os campos do `ARCHITECTURE.md` §2:
  `eventId`, `operationId`, `previousStatus`, `newStatus`, `eventType`, `details`,
  `rawPayload` e `createdAt`.
- `domain/audit/EventType` — o vocabulário fechado de oito tipos, igual ao enum do
  contrato público.
- `domain/audit/OperationEventRepository` — porta com `append` e `findByOperationId`; não
  existe método de atualização nem de remoção.
- Testes: 19 casos, incluindo a prova por reflexão de que o evento não expõe mutador
  público, de que todo campo de instância é final e de que a porta só expõe `append` e a
  consulta.

#### Descobertas e Decisões:
- O `eventId` é sequencial no banco (`BIGSERIAL`), então o evento nasce sem identificador
  e a porta devolve o evento já armazenado, com o id atribuído pelo armazenamento.
- O `rawPayload` é o JSON bruto da Central e fica no domínio como texto: o domínio não
  conhece Jackson nem o tipo da coluna `JSONB`.
- O `previousStatus` é nulo apenas no evento de criação, como mostra o exemplo do
  `ARCHITECTURE.md` §8.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/audit/OperationEvent.java`
- `backend/src/main/java/br/com/consigport/domain/audit/OperationEventRepository.java`
- `backend/src/main/java/br/com/consigport/domain/audit/EventType.java`
- `backend/src/test/java/br/com/consigport/domain/audit/OperationEventTest.java`
- `backend/src/test/java/br/com/consigport/domain/audit/OperationEventRepositoryContractTest.java`
- `backend/src/test/java/br/com/consigport/domain/audit/EventTypeTest.java`
- `backend/src/test/java/br/com/consigport/domain/audit/InMemoryOperationEventRepository.java`

---

### Task 2.6 — Registro e consulta de autorização

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/026-backend-register-authorization`
- **PR:** [#38](https://github.com/marcelinosandroni/consigport-platform/pull/38)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** caso de uso idempotente do opt-in, com a replicação do consentimento na
  Central e o relógio injetado.

#### Comandos de Verificação e Saídas:

1. **Testes da task:**
   ```text
   ./mvnw -B -pl backend test -Dtest='RegisterAuthorizationTest,FindAuthorizationTest'
   → FindAuthorizationTest: Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
   → RegisterAuthorizationTest: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 148, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.002 s]
   → ConsigPort Backend ................................. SUCCESS [ 26.218 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida da idempotência (retorno do registro existente removido):**
   ```text
   → guarda `if (existing.isPresent()) return existing.get();` removida de
     RegisterAuthorization.java
   → ./mvnw -B -pl backend test -Dtest=RegisterAuthorizationTest
   → Tests run: 6, Failures: 1, Errors: 0, Skipped: 0 -- in
     br.com.consigport.application.authorization.RegisterAuthorizationTest
   → BUILD FAILURE
   → guarda restaurada; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `application/authorization/RegisterAuthorization` — caso de uso idempotente: repete o
  opt-in, devolve o registro existente e replica o consentimento na Central.
- `application/authorization/FindAuthorization` — consulta do registro do par.
- `domain/authorization/CentralAuthorizationPort` — porta da replicação do opt-in na
  Central.
- Testes: 9 casos com o repositório e a porta em memória, incluindo a corrida entre dois
  registros do mesmo par.

#### Descobertas e Decisões:
- A replicação na Central acontece **antes** do armazenamento local: se a chamada
  falhar, não fica autorização local que a Central desconhece — o caso oposto deixaria o
  beneficiário sem agenda depois de um retry, porque a repetição devolveria o registro
  existente sem replicar. A replicação da Central é idempotente por par, então o retry
  é seguro.
- Na repetição do opt-in a Central não é chamada de novo: o registro local só existe
  depois de uma replicação bem-sucedida.
- Quando o armazenamento perde a corrida para outro registro do mesmo par, o caso de uso
  devolve o registro vencedor em vez de propagar o erro.
- O `BACKLOG.md` não lista a porta da replicação entre os arquivos da task 2.6; ela foi
  criada em `domain/authorization/`, seguindo o padrão das outras portas do domínio
  (`AgendaGateway` em `domain/agenda/` e `CentralPortabilityPort` em
  `domain/portability/`).

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/application/authorization/RegisterAuthorization.java`
- `backend/src/main/java/br/com/consigport/application/authorization/FindAuthorization.java`
- `backend/src/main/java/br/com/consigport/domain/authorization/CentralAuthorizationPort.java`
- `backend/src/test/java/br/com/consigport/application/authorization/RegisterAuthorizationTest.java`
- `backend/src/test/java/br/com/consigport/application/authorization/FindAuthorizationTest.java`
- `backend/src/test/java/br/com/consigport/application/authorization/InMemoryCentralAuthorizationPort.java`

---

### Task 2.7 — Consulta de agenda com ACL

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/027-backend-agenda-acl`
- **PR:** [#39](https://github.com/marcelinosandroni/consigport-platform/pull/39)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** traduzir o contrato PT da Central para o domínio EN, sem deixar o DTO
  externo escapar do adaptador.

#### Comandos de Verificação e Saídas:

1. **Testes da task:**
   ```text
   ./mvnw -B -pl backend test -Dtest='HexagonalArchitectureTest,CentralAclTest,QueryAgendaTest'
   → HexagonalArchitectureTest: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
   → QueryAgendaTest: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   → CentralAclTest: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
   → CentralAclTest$StatusMapping: Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
   → CentralAclTest$ErrorMapping: Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Reator completo:**
   ```text
   ./mvnw -B verify
   → ConsigPort Backend: Tests run: 165, Failures: 0, Errors: 0, Skipped: 0
   → Central de Convênios Mock: Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.002 s]
   → ConsigPort Backend ................................. SUCCESS [ 27.696 s]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   ```
3. **Prova de vida do mapeamento (`ABERTA` lido como `CLOSED`):**
   ```text
   → mapeamento `ABERTA → CLOSED` aplicado temporariamente em CentralAcl.java
   → ./mvnw -B -pl backend test -Dtest=CentralAclTest
   → Tests run: 7, Failures: 2, Errors: 0, Skipped: 0 -- in
     br.com.consigport.adapters.out.central.acl.CentralAclTest$StatusMapping
   → Tests run: 2, Failures: 2, Errors: 0, Skipped: 0 -- in
     br.com.consigport.adapters.out.central.acl.CentralAclTest
   → BUILD FAILURE
   → mapeamento restaurado; suíte verde de novo
   ```

#### Conteúdo Entregue:
- `domain/agenda/Installment` e `domain/agenda/InstallmentStatus` — a parcela e a sua
  situação (`OPEN`, `CLOSED`).
- `domain/agenda/BeneficiaryAgenda` — o read model imutável da agenda, com
  `openInstallments()` para a elegibilidade (BR-03).
- `domain/agenda/AgendaGateway` — a porta de leitura da agenda na Central.
- `domain/shared/DomainError` — hierarquia selada de erros com o código estável da API
  pública (`AUTHORIZATION_REQUIRED`, `BENEFICIARY_NOT_FOUND`).
- `application/agenda/QueryAgenda` — o caso de uso da consulta.
- `adapters/out/central/dto/AgendaResponse` — o DTO da Central, com os nomes JSON em
  português.
- `adapters/out/central/acl/CentralAcl` — a tradução: `ABERTA ↔ OPEN`,
  `FECHADA ↔ CLOSED`, e `403`/`404` da Central viram erro de domínio.
- `HexagonalArchitectureTest` — nova regra: nenhum DTO externo fora do adaptador de
  saída da Central, provada por um fixture que viola a regra.

#### Descobertas e Decisões:
- O `403` sem opt-in e o `404` de beneficiário desconhecido chegam da Central; a ACL os
  traduz para `DomainError`, e o caso de uso apenas os repassa. O `DomainError` foi
  criado em `domain/shared/`, como na árvore do `ARCHITECTURE.md` §3.
- O mapeamento da situação é bidirecional: a ACL lê o português da Central e escreve o
  português da Central, e o teste prova o ciclo completo.
- `InstallmentStatus` e `BeneficiaryAgenda` não estão na lista de arquivos do
  `BACKLOG.md`, mas estão na árvore do `ARCHITECTURE.md` §3 (`domain/agenda/`) e foram
  criados para que o read model e a elegibilidade vivam no domínio.
- A regra ArchUnit nova garante que nenhum DTO da Central escape do adaptador de saída:
  o `domain` e o `application` nunca alcançam `adapters`, e o fixture prova que a regra
  morde.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/agenda/Installment.java`
- `backend/src/main/java/br/com/consigport/domain/agenda/InstallmentStatus.java`
- `backend/src/main/java/br/com/consigport/domain/agenda/BeneficiaryAgenda.java`
- `backend/src/main/java/br/com/consigport/domain/agenda/AgendaGateway.java`
- `backend/src/main/java/br/com/consigport/domain/shared/DomainError.java`
- `backend/src/main/java/br/com/consigport/application/agenda/QueryAgenda.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/AgendaResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/acl/CentralAcl.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/acl/CentralAclTest.java`
- `backend/src/test/java/br/com/consigport/application/agenda/QueryAgendaTest.java`
- `backend/src/test/java/br/com/consigport/application/agenda/InMemoryAgendaGateway.java`
- `backend/src/test/java/br/com/consigport/architecture/HexagonalArchitectureTest.java`
- `backend/src/test/java/br/com/consigport/architecture/fixture/application/ApplicationUsingCentralDto.java`

---

### Task 2.8 — Persistência e migração V1

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/028-backend-persistence-v1`
- **PR:** [#40](https://github.com/marcelinosandroni/consigport-platform/pull/40)
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar as três tabelas, os índices, as constraints e o trigger append-only
  do `ARCHITECTURE.md` §6, com as entidades JPA e os repositórios de autorização e
  portabilidade.

#### Comandos de Verificação e Saídas:

1. **Migração e constraints (Testcontainers PostgreSQL 16):**
   ```text
   ./mvnw -B -pl backend test -Dtest=PersistenceConstraintsTest
   → Successfully applied 1 migration to schema "public", now at version v1
     (execution time 00:00.055s)
   → Initialized JPA EntityManagerFactory for persistence unit 'default'
   → Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 26.24 s -- in
     br.com.consigport.adapters.out.persistence.PersistenceConstraintsTest
   → Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
2. **Round trip dos agregados sobre o schema V1:**
   ```text
   ./mvnw -B -pl backend test -Dtest=JpaRepositoriesTest
   → Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 25.16 s -- in
     br.com.consigport.adapters.out.persistence.JpaRepositoriesTest
   → Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
3. **Reator completo:**
   ```text
   ./mvnw -B verify
   → Tests run: 186, Failures: 0, Errors: 0, Skipped: 0
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [ 57.110 s]
   → Central de Convênios Mock .......................... SUCCESS [ 32.209 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
4. **Prova de vida da constraint de status (`CHECK` virado em `status IS NOT NULL`):**
   ```text
   → CHECK de status quebrado temporariamente
   → ./mvnw -B -pl backend test -Dtest=PersistenceConstraintsTest
   → Tests run: 11, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 18.85 s
     <<< FAILURE! -- in br.com.consigport.adapters.out.persistence.PersistenceConstraintsTest
   → PersistenceConstraintsTest.refusesAStatusOutsideTheStateMachine:47
     Expecting code to raise a throwable.
   → BUILD FAILURE
   → CHECK de status restaurado
   → ./mvnw -B verify → Tests run: 186, Failures: 0, Errors: 0, Skipped: 0 → BUILD SUCCESS
   ```
5. **Gates locais:**
   ```text
   node scripts/check-specs.mjs               → OK
   node SDD/SKILLS/check-docs/check-docs.mjs  → OK
   node scripts/check-architecture.mjs        → OK
   node scripts/check-ai-log.mjs              → OK
   node scripts/check-theme-tokens.mjs        → OK
   ```

#### Conteúdo Entregue:
- `backend/src/main/resources/db/migration/V1__create_schema.sql` — o DDL do
  `ARCHITECTURE.md` §6: `authorizations`, `portability_operations` e `operation_events`,
  os dois índices, a `CHECK` de status, a `CHECK` de snapshot não vazio, a `CHECK` de
  total não negativo e o trigger append-only.
- `domain/portability/PortabilityOperationRepository` — a porta de armazenamento das
  operações, com `save`, `findById`, `findByIdempotencyKey` e `findByProtocol`.
- `adapters/out/persistence/AuthorizationJpaEntity` e `JpaAuthorizationRepository` — a
  entidade e o adaptador da porta `AuthorizationRepository`.
- `adapters/out/persistence/PortabilityOperationJpaEntity` e
  `JpaPortabilityOperationRepository` — a entidade, o mapeamento do snapshot em JSONB e
  o adaptador da porta de operações.
- `adapters/out/persistence/InstallmentSnapshotRow` — a linha do snapshot no formato do
  `ARCHITECTURE.md` §6.
- `adapters/out/persistence/OperationEventJpaEntity` e `JpaOperationEventRepository` — a
  terceira tabela, mapeada e com a trilha growing only.
- `adapters/out/persistence/RawPayloadCodec` — a codificação do payload bruto da
  Central em JSONB.
- `backend/src/test/java/br/com/consigport/support/PostgreSqlTestSupport.java` — o
  PostgreSQL 16 compartilhado pelos testes de integração.
- `PersistenceConstraintsTest` e `JpaRepositoriesTest` — 21 testes de integração.

#### Descobertas e Decisões:
- O trigger append-only rejeita `UPDATE` e `DELETE` com SQLSTATE `P0001`, traduzido pelo
  driver como `UncategorizedSQLException`. O isolamento dos testes usa `TRUNCATE`, que
  não dispara trigger de linha: a garantia append-only sobrevive à limpeza.
- Dentro do método do adaptador a falha do banco ainda é a exceção do Hibernate; a
  tradução para o erro de domínio só acontece na fronteira do bean. A constraint
  única do par é reconhecida pelo SQLSTATE `23505` e traduzida em
  `AuthorizationAlreadyExistsException`.
- O JSONB do PostgreSQL normaliza o documento: reordena as chaves de um objeto e
  recusa um texto que não é JSON. Como o payload bruto da Central é a evidência do que
  a Central respondeu, ele é guardado como string JSON (o `RawPayloadCodec`), e volta
  byte a byte — inclusive quando não é JSON.
- O schema do `ARCHITECTURE.md` §6 não tem coluna de convênio na operação: o convênio
  é o da primeira parcela do snapshot, e o pedido escolhe parcelas de um só convênio.
- Um container por classe de teste quebrava os contextos Spring: o contexto guardava a
  porta do container anterior. Passou a haver um container por JVM, com as propriedades
  do datasource apontadas por `@DynamicPropertySource`.
- O teste de scaffold (`ConsigPortApplicationTests`) usava H2 com o Flyway desligado, e
  o próprio arquivo avisava que a task de persistência o substituiria. Hoje ele sobe
  contra o PostgreSQL 16 real, e a dependência do H2 saiu do `backend/pom.xml`.

#### Arquivos Tocados:
- `backend/src/main/resources/db/migration/V1__create_schema.sql`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/AuthorizationJpaEntity.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaAuthorizationRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/PortabilityOperationJpaEntity.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/InstallmentSnapshotRow.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaPortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/OperationEventJpaEntity.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaOperationEventRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/RawPayloadCodec.java`
- `backend/src/test/java/br/com/consigport/support/PostgreSqlTestSupport.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/PersistenceConstraintsTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/JpaRepositoriesTest.java`
- `backend/src/test/java/br/com/consigport/ConsigPortApplicationTests.java`
- `backend/pom.xml`

---

### Task 2.9 — Início da operação com idempotência

- **Data de Conclusão:** 2026-11-26
- **Branch:** `feat/029-backend-start-operation-idempotency`
- **PR:** #41 — https://github.com/marcelinosandroni/consigport-platform/pull/41
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar a operação, congelar o snapshot das parcelas e responder `202`, com
  a idempotência da chave e o replay sinalizado no header.

#### Comandos de Verificação e Saídas:

1. **Caso de uso (unitário):**
   ```text
   ./mvnw -B -pl backend test -Dtest=StartOperationTest
   → Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.038 s -- in
     br.com.consigport.application.portability.StartOperationTest
   → BUILD SUCCESS
   ```
2. **Recurso HTTP sobre PostgreSQL 16 (Testcontainers):**
   ```text
   ./mvnw -B -pl backend test -Dtest=PortabilityOperationResourceTest
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 29.08 s -- in
     br.com.consigport.adapters.in.web.PortabilityOperationResourceTest
   → BUILD SUCCESS
   ```
3. **Reator completo:**
   ```text
   ./mvnw -B verify
   → Tests run: 203, Failures: 0, Errors: 0, Skipped: 0
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.002 s]
   → ConsigPort Backend ................................. SUCCESS [01:55 min]
   → Central de Convênios Mock .......................... SUCCESS
   → BUILD SUCCESS
   → EXIT=0
   ```
4. **Prova de vida da comparação de payload (comparação das parcelas virando `true`):**
   ```text
   → comparação de payload quebrada temporariamente
   → ./mvnw -B -pl backend test -Dtest=StartOperationTest
   → Tests run: 9, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.822 s
     <<< FAILURE! -- in br.com.consigport.application.portability.StartOperationTest
   → StartOperationTest.refusesTheKeyWhenThePayloadIsAnother:120
     Expecting code to raise a throwable.
   → BUILD FAILURE
   → comparação de payload restaurada
   → ./mvnw -B -pl backend test -Dtest=StartOperationTest
   → Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   ```
5. **Gates locais:**
   ```text
   node scripts/check-specs.mjs               → OK
   node SDD/SKILLS/check-docs/check-docs.mjs  → OK
   node scripts/check-architecture.mjs        → OK
   node scripts/check-ai-log.mjs              → OK
   node scripts/check-theme-tokens.mjs        → OK
   ```

#### Conteúdo Entregue:
- `domain/portability/OperationRequest` — o pedido da operação, com a regra que diz o
  que faz dois pedidos serem o mesmo.
- `domain/portability/OperationPolicy` — a regra de elegibilidade (BR-03, BR-04) e o
  congelamento do snapshot (BR-05).
- `domain/portability/IdempotencyKeyAlreadyUsedException` — o sinal do repositório
  quando a chave já foi tomada.
- `domain/shared/DomainError` — a hierarquia selada ganhou `IdempotencyConflict`,
  `IdempotencyKeyRequired`, `InstallmentNotEligible` e `CentralUnavailable`.
- `domain/shared/TransactionRunner` — a porta da fronteira transacional, para escrever a
  operação e o evento de criação na mesma transação (BR-12).
- `application/portability/StartOperation` — o caso de uso: replay, conflito, elegibilidade,
  snapshot congelado, evento de criação e a corrida da chave.
- `adapters/in/web/PortabilityOperationResource` — a aresta HTTP do
  `POST /api/v1/portability-operations`, com o header `Idempotent-Replay`.
- `adapters/in/web/ApiExceptionHandler` — a tradução das falhas em erros da API, com
  `code`, `message`, `locale`, `timestamp`, `path` e `details`.
- `adapters/in/web/dto/` — `PortabilityOperationRequest`, `ReceivingAccountRequest`,
  `OperationAcceptedResponse`, `ErrorResponse` e `ErrorDetail`.
- `adapters/out/persistence/SpringTransactionRunner` — a implementação transacional da
  porta.
- `adapters/out/persistence/UniqueViolations` — o reconhecimento da violação única do
  PostgreSQL (SQLSTATE `23505`), com o nome da constraint.
- `CompositionRoot` — o wiring do caso de uso e da porta da agenda.
- `application.yml` — `spring.web.locale: pt-BR`, o locale padrão do contrato (BR-16).
- Testes: 9 unitários do caso de uso e 8 de integração HTTP, com duas chamadas
  concorrentes e uma divergente.

#### Descobertas e Decisões:
- O "mesmo payload" da idempotência é derivado do que o pedido carrega: beneficiário,
  convênio, conjunto de parcelas, credor cessionário e conta de recebimento. A ordem
  das parcelas não conta e os valores não contam: eles pertencem ao snapshot, que muda
  com a agenda, e o pedido nunca os carrega.
- A corrida da chave é resolvida pelo próprio banco: o segundo `INSERT` espera o
  primeiro commit e falha com a violação única; o adaptador traduz o SQLSTATE `23505`
  da constraint `uk_portability_operations_idempotency_key` e o caso de uso relê a
  operação vencedora e responde com ela.
- O instante da criação é truncado para microssegundos, a precisão do `TIMESTAMPTZ`:
  sem isso, a resposta da criação e a resposta do replay divergiam nos nanossegundos.
- A regra ArchUnit da camada de aplicação proibia a camada de depender de si mesma, e o
  record do resultado do caso de uso vivia nela. A regra agora permite as dependências
  intra-camada e continua proibindo os adaptadores e os frameworks; a regra da camada
  HTTP, que estava vazia com `allowEmptyShould(true)`, deixou de ser vazia quando a
  aresta HTTP nasceu e a exceção foi removida.
- O `TransactionRunner` vive no domínio porque a garantia de escrever a operação e o
  evento na mesma transação é do domínio (BR-12); a camada de aplicação continua
  usando apenas o domínio e o JDK.
- O cliente HTTP da Central chega na task 2.10, com o assinante que a Central exige.
  Até lá, o `AgendaGateway` do composition root responde `503 CENTRAL_UNAVAILABLE`; o
  teste de integração serve a agenda no lugar da Central.
- As mensagens do erro são os padrões pt-BR do handler; os bundles dos locales chegam
  com a task de i18n e trocam as mensagens sem mudar a forma da resposta.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/portability/OperationRequest.java`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationPolicy.java`
- `backend/src/main/java/br/com/consigport/domain/portability/IdempotencyKeyAlreadyUsedException.java`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/domain/shared/DomainError.java`
- `backend/src/main/java/br/com/consigport/domain/shared/TransactionRunner.java`
- `backend/src/main/java/br/com/consigport/application/portability/StartOperation.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/PortabilityOperationResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/PortabilityOperationRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ReceivingAccountRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationAcceptedResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ErrorResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ErrorDetail.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/SpringTransactionRunner.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/UniqueViolations.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaAuthorizationRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaPortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/com/consigport/application/portability/StartOperationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/PortabilityOperationResourceTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryPortabilityOperationRepository.java`
- `backend/src/test/java/br/com/consigport/architecture/HexagonalArchitectureTest.java`

### Task 2.10 — Assinatura HMAC e envio à Central

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/030-backend-central-hmac-submission`
- **PR:** #42 — https://github.com/marcelinosandroni/consigport-platform/pull/42
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** assinar a operação na forma canônica da Central, enviá-la e absorver o
  protocolo; uma falha depois das tentativas deixa a operação retomável, nunca duplicada.

#### Comandos de Verificação e Saídas:

1. **Assinante, cliente HTTP e caso de uso (unitário):**
   ```text
   ./mvnw -B -pl backend test -Dtest='HmacSignerTest,CentralHttpClientTest,StartOperationTest'
   → Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.486 s -- in
     br.com.consigport.application.portability.StartOperationTest
   → Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.174 s -- in
     br.com.consigport.adapters.out.central.CentralHttpClientTest
   → Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in
     br.com.consigport.adapters.out.central.HmacSignerTest
   → Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Recurso HTTP sobre PostgreSQL 16 (Testcontainers):**
   ```text
   ./mvnw -B -pl backend test -Dtest='PortabilityOperationResourceTest'
   → Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 39.34 s -- in
     br.com.consigport.adapters.in.web.PortabilityOperationResourceTest
   → Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 228, Failures: 0, Errors: 0, Skipped: 0
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → Reactor Summary for ConsigPort Platform 0.1.0-SNAPSHOT:
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [01:05 min]
   → Central de Convênios Mock .......................... SUCCESS [ 24.806 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
4. **Vetor HMAC conferido por script independente (Python, fora do código Java):**
   ```text
   python3 (contracts/hmac-vector.json, recomputando SHA-256 e HMAC-SHA256)
   → post-portability: canonical=OK signature=OK
   → get-status: canonical=OK signature=OK
   → post-authorization: canonical=OK signature=OK
   ```
5. **Prova de vida da forma canônica (ordenação das chaves virando `false`):**
   ```text
   → HmacSigner com ORDER_MAP_ENTRIES_BY_KEYS quebrado temporariamente
   → ./mvnw -q -B -pl backend test -Dtest='HmacSignerTest'
   → Tests run: 5, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 3.161 s
     <<< FAILURE! -- in br.com.consigport.adapters.out.central.HmacSignerTest
   → HmacSignerTest.sortsTheKeysOfTheNestedObjects:57
     expected: "{"contaRecebimento":{"agencia":"1234","banco":"001","conta":"56789-0"}}"
      but was: "{"contaRecebimento":{"conta":"56789-0","banco":"001","agencia":"1234"}}"
   → HmacSignerTest.signsTheSameBodyWhateverTheOrderOfItsKeys:66
     expected: "c8c9e93a0b1ae6aca8098c1816a5034fcd3f757001ff28f17e94ca34493b757a"
      but was: "f3a2035f987b4963d165687296e5b0c9553df6dc0f5f43f7bb8c2f6c5b5cb2e2"
   → EXIT=1
   → forma canônica restaurada
   → ./mvnw -q -B -pl backend test -Dtest='HmacSignerTest'
   → EXIT=0
   ```
6. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`, configuração padrão com read timeout de
   3 s):
   ```text
   === 2. aquecimento do mock: autorização, agenda e submissão ===
   POST /autorizacoes          -> 201 0.413004 | {"autorizacaoId":"AUT-20261127-0001"}
   GET  /beneficiarios/BEN-001/agenda -> 200 0.044438 | [{"parcelaId":"PAR-001",...}]
   POST /operacoes-portabilidade (aquecimento) -> 202 0.075875 | {"protocolo":"PRT-20261127-0001"}

   === 5. criação da operação: o backend assina e a Central aceita ===
   --- primeira chamada (1304 ms): HTTP/1.1 202  | Idempotent-Replay: false
   {"operationId":"89fb650a-c03a-4f7e-922f-a57a4687310c","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":750.00,"createdAt":"2026-11-27T02:12:18.138888Z"}
   --- replay da mesma chave (39 ms): HTTP/1.1 202  | Idempotent-Replay: true
   {"operationId":"89fb650a-c03a-4f7e-922f-a57a4687310c","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":750.00,"createdAt":"2026-11-27T02:12:18.138888Z"}
   --- corpos byte a byte iguais: True
   --- mesma chave, payload diferente (25 ms): HTTP/1.1 409
   {"code":"IDEMPOTENCY_CONFLICT","message":"A chave de idempotência já foi usada com outro conteúdo.","locale":"pt-BR",...}

   === 6. o que o backend gravou (PostgreSQL) ===
    id                                  | status  |     protocol      | total_amount |          created_at
   --------------------------------------+---------+-------------------+--------------+-------------------------------
    89fb650a-c03a-4f7e-922f-a57a4687310c | PENDING | PRT-20261127-0002 |       750.00 | 2026-11-27 02:12:18.445488+00

    id |     event_type      | previous_status | new_status |          created_at
   ----+---------------------+-----------------+------------+-------------------------------
    17 | OPERATION_CREATED   |                 | CREATED    | 2026-11-27 02:12:18.445488+00
    18 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-27 02:12:19.871956+00

   === 7. o que a Central recebeu (estado do mock) ===
   "PRT-20261127-0002": {"protocolo": "PRT-20261127-0002", "beneficiarioId": "BEN-001",
                         "parcelas": ["PAR-001", "PAR-002"], "status": "CONFIRMADA",
                         "motivoRejeicao": null}

   === 8. status da operação na Central (PENDENTE -> confirmada pelo mock) ===
   protocolo: PRT-20261127-0002
   consulta 1: HTTP/1.1 200 ... {"status":"CONFIRMADA","motivoRejeicao":null}

   === 9. falha da Central: a operação fica retomável (INTEGRATION_ERROR) ===
   mock com a mesma chave e outro payload -> {"protocolo":"PRT-20261127-0003"} | 202
   --- backend com a Central recusando (106 ms): HTTP/1.1 202
   {"operationId":"91f72b1a-24d9-4e05-b8df-a81d62a5c985","protocol":null,"status":"INTEGRATION_ERROR","totalAmount":750.00,"createdAt":"2026-11-27T02:15:33.443060Z"}
    19 | OPERATION_CREATED   |                 | CREATED           | Operation created
    20 | INTEGRATION_FAILED  | CREATED         | INTEGRATION_ERROR | Central did not accept the operation: IDEMPOTENCIA_CONFLITO (status 409)

    id                  |      status       |     protocol      | retry_count |         next_poll_at
   --------------------------------------+-------------------+-------------------+-------------+-------------------------------
    89fb650a-c03a-4f7e-922f-a57a4687310c | PENDING           | PRT-20261127-0002 |           0 |
    91f72b1a-24d9-4e05-b8df-a81d62a5c985 | INTEGRATION_ERROR |                   |           1 | 2026-11-27 02:16:33.490704+00

   === 10. encerramento dos processos e dos containers ===
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
7. **Gates locais:**
   ```text
   node scripts/check-specs.mjs               → OK (exit 0)
   node SDD/SKILLS/check-docs/check-docs.mjs  → OK (46 documentos, exit 0)
   node scripts/check-architecture.mjs        → OK (22 testes de validação, exit 0)
   node scripts/check-ai-log.mjs              → OK (35 sessões, exit 0)
   node scripts/check-theme-tokens.mjs        → OK (exit 0)
   ```

#### Conteúdo Entregue:
- `domain/portability/CentralPortabilityPort` — a porta de envio da operação, no domínio.
- `domain/portability/CentralSubmissionException` — a falha da Central com o status e o
  código estáveis dela; nunca é fatal para a operação.
- `domain/portability/ConcurrentOperationUpdateException` — o sinal do armazenamento
  quando outra transação mudou a operação (bloqueio otimista).
- `adapters/out/central/HmacSigner` — o dono da forma canônica: `canonicalJson`,
  `canonicalBody` (sem `assinatura`), `canonicalString` e `sign`.
- `adapters/out/central/CentralProperties` — URL, segredo, tentativas, backoff e timeouts.
- `adapters/out/central/CentralHttpClient` — implementa `AgendaGateway` e
  `CentralPortabilityPort`: assina, envia, repete 3 vezes com backoff exponencial e só
  repete 408/429/5xx.
- `adapters/out/central/acl/PortabilityAcl` — traduz a operação para o contrato PT da
  Central e a resposta de erro para `CentralSubmissionException`.
- `adapters/out/central/dto/` — `CentralPortabilityRequest` (com `@JsonInclude(NON_NULL)`,
  para que `assinatura: null` não entre no corpo assinado), `CentralPortabilityResponse`
  e `CentralErrorResponse`.
- `application/portability/StartOperation` — criação em uma transação, submissão fora
  dela e gravação de `PENDING`/`OPERATION_SUBMITTED` ou
  `INTEGRATION_ERROR`/`INTEGRATION_FAILED` em outra transação.
- `adapters/out/persistence/JpaPortabilityOperationRepository` — traduz o bloqueio
  otimista para `ConcurrentOperationUpdateException`.
- `config/CompositionRoot` — beans de `CentralProperties`, `HmacSigner`, `RestClient`,
  ACLs, `CentralHttpClient` e `StartOperation`.
- `application.yml` — `central.base-url`, `central.hmac.secret`, `central.retry.*`,
  `central.timeout.*` e `central.integration.backoff`.
- Testes: 5 do assinante (o vetor compartilhado), 10 do cliente HTTP (com
  `MockRestServiceServer`) e 16 do caso de uso, mais 11 de integração HTTP.

#### Descobertas e Decisões:
- A forma canônica é do adaptador, não do domínio: `HmacSigner` é o único dono do
  `METHOD\nPATH_WITH_QUERY\nTIMESTAMP\nSHA256_HEX(body)`, e o corpo assinado é o JSON
  com as chaves ordenadas — inclusive as dos objetos aninhados — sem espaços e sem o
  campo `assinatura`, que é apenas o espelho da assinatura no corpo.
- O mock da Central valida a assinatura do cabeçalho contra o corpo que recebeu,
  removendo `assinatura` antes de canonicalizar: por isso o DTO do pedido usa
  `@JsonInclude(NON_NULL)`, senão o `null` da assinatura entraria no corpo assinado e a
  Central recusaria a requisição.
- A falha de transporte que acontece na leitura da resposta não é um
  `ResourceAccessException`: o `RestClient` a embrulha em `RestClientException` e ela
  escapava do cliente como erro 500, com a operação já gravada. O cliente agora traduz
  qualquer falha de transporte para `CentralSubmissionException`, e a operação fica
  `INTEGRATION_ERROR`, retomável e com o evento `INTEGRATION_FAILED` — foi o que o
  end-to-end expôs na primeira execução.
- A criação da operação e a submissão não podem estar na mesma transação: a submissão
  é uma chamada a outro sistema. O replay de uma operação ainda `CREATED` completa a
  submissão com a mesma `Idempotency-Key`, para a resposta ser estável; uma operação em
  `INTEGRATION_ERROR` não é reenviada pela API, porque o backoff de 60 s pertence ao
  worker, que a retoma depois.
- A corrida da atualização da operação foi resolvida pelo bloqueio otimista do
  `@Version`: o perdedor relê a operação armazenada e responde com ela, porque o
  chamador pediu a operação da chave, não uma versão dela.
- O primeiro pedido de cada caminho novo de uma JVM fria custa de 1,5 s a 4,5 s nesta
  máquina EC2 (carregamento de classes), acima do read timeout de 3 s. O end-to-end
  aquece o mock (autorização, agenda e submissão) e o cliente HTTP do backend antes de
  medir, para evidenciar o comportamento da integração e não o aquecimento da máquina.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/portability/CentralPortabilityPort.java`
- `backend/src/main/java/br/com/consigport/domain/portability/CentralSubmissionException.java`
- `backend/src/main/java/br/com/consigport/domain/portability/ConcurrentOperationUpdateException.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/HmacSigner.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralProperties.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralHttpClient.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/acl/PortabilityAcl.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralPortabilityRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralPortabilityResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralErrorResponse.java`
- `backend/src/main/java/br/com/consigport/application/portability/StartOperation.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaPortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/com/consigport/adapters/out/central/HmacSignerTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/CentralHttpClientTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/StartOperationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/PortabilityOperationResourceTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryCentralPortabilityPort.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryPortabilityOperationRepository.java`

### Task 2.11 — Polling resiliente

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/031-backend-resilient-polling`
- **PR:** #43 — https://github.com/marcelinosandroni/consigport-platform/pull/43
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** acompanhar a Central até o estado terminal, com worker de 2 s e lote de
  20, honrando o `Retry-After`, repetindo com backoff e circuit breaker, e reenviando a
  operação que falhou com a mesma `Idempotency-Key`.

#### Comandos de Verificação e Saídas:

1. **Worker, backoff, circuito e cliente HTTP (unitário):**
   ```text
   ./mvnw -B -pl backend test -Dtest='PollOperationTest,IntegrationBackoffTest,CircuitBreakerTest,CentralHttpClientTest'
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.199 s -- in
     br.com.consigport.application.portability.IntegrationBackoffTest
   → Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.326 s -- in
     br.com.consigport.application.portability.PollOperationTest
   → Tests run: 17, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.335 s -- in
     br.com.consigport.adapters.out.central.CentralHttpClientTest
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.042 s -- in
     br.com.consigport.adapters.out.resilience.CircuitBreakerTest
   → Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Persistência do polling, worker e agregado (PostgreSQL 16 via Testcontainers):**
   ```text
   ./mvnw -B -pl backend test -Dtest='PollingPersistenceTest,SchedulingConfigTest,PortabilityOperationTest'
   → Tests run: 45, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.703 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest
   → Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 23.02 s -- in
     br.com.consigport.adapters.out.persistence.PollingPersistenceTest
   → Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in
     br.com.consigport.config.SchedulingConfigTest
   → Tests run: 64, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 273, Failures: 0, Errors: 0, Skipped: 0
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
   → ConsigPort Backend ................................. SUCCESS [ 53.521 s]
   → Central de Convênios Mock .......................... SUCCESS [ 23.317 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
4. **Prova de vida do `Retry-After` (o próximo poll ignorando o cabeçalho da Central):**
   ```text
   → PollOperation com answeredAt.plus(Duration.ZERO) no lugar do Retry-After
   → ./mvnw -q -B -pl backend test -Dtest='PollOperationTest'
   → Tests run: 13, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.630 s
     <<< FAILURE! -- in br.com.consigport.application.portability.PollOperationTest
   → PollOperationTest.recordsTheAttemptAndHonoursTheRetryAfterOfTheCentral:122
     expected: 2026-11-27T10:15:35Z
      but was: 2026-11-27T10:15:30Z
   → PollOperationTest.schedulesTheNextPollFromTheAnswerOfTheCentralAndNotFromTheStartOfTheBatch:111
     expected: 2026-11-27T10:15:37Z
      but was: 2026-11-27T10:15:35Z
   → EXIT=1
   → Retry-After restaurado
   → ./mvnw -q -B -pl backend test -Dtest='PollOperationTest'
   → EXIT=0
   ```
5. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`; o mock confirma em 8 s e a sua falha
   determinística de timeout dura 4 s, acima do read timeout de 3 s do backend):
   ```text
   === 5. fase A: a operação entra em PENDING e o worker acompanha a Central ===
   Idempotency-Key: 3085ffa1-71ca-431a-82f9-bbe6ae5b84de
   POST /api/v1/portability-operations -> {"operationId":"8c22e86d-c93a-4658-bd99-2ce7792504f6","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":750.00,"createdAt":"2026-11-27T06:03:54.252213Z"} | 202 1.037958
   status CONFIRMED apos 5 ciclos de 2 s do worker

   === 6. fase A: o que o backend gravou (o 204 virou POLL_ATTEMPT) ===
    id |     event_type      | previous_status | new_status |          created_at           |                                details
   ----+---------------------+-----------------+------------+-------------------------------+------------------------------------------------------------------------
   59 | OPERATION_CREATED   |                 | CREATED    | 2026-11-27 06:03:54.252213+00 | Operation created
   60 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-27 06:03:55.185084+00 | Central accepted the operation with protocol PRT-20261127-0002
   61 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 06:03:55.964406+00 | The Central is still processing the operation; the next poll is in 2 s
   62 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 06:03:58.005691+00 | The Central is still processing the operation; the next poll is in 2 s
   63 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 06:04:00.121605+00 | The Central is still processing the operation; the next poll is in 2 s
   64 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 06:04:02.202531+00 | The Central is still processing the operation; the next poll is in 2 s
   65 | OPERATION_CONFIRMED | PENDING         | CONFIRMED  | 2026-11-27 06:04:04.25457+00  | The Central confirmed the operation

   === 7. fase A: o intervalo entre as tentativas de poll (Retry-After honrado) ===
   POLL_ATTEMPT|2026-11-27 06:03:55.964406+00|00:00:00.779322
   POLL_ATTEMPT|2026-11-27 06:03:58.005691+00|00:00:02.041285
   POLL_ATTEMPT|2026-11-27 06:04:00.121605+00|00:00:02.115914
   POLL_ATTEMPT|2026-11-27 06:04:02.202531+00|00:00:02.080926
   OPERATION_CONFIRMED|2026-11-27 06:04:04.25457+00|00:00:02.052039

   === 9. fase B: o beneficiario -TIMEOUT faz a Central estourar o read timeout ===
   POST /api/v1/portability-operations (BEN-TIMEOUT) -> {"operationId":"9fa953ce-83b5-4e2b-901a-2874c73f8557","protocol":"PRT-20261127-0003","status":"PENDING","totalAmount":500.00,"createdAt":"2026-11-27T06:04:06.898824Z"} | 202 0.067818
    67 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-27 06:04:06.930323+00 | Central accepted the operation with protocol PRT-20261127-0003
    68 | OPERATION_CONFIRMED | PENDING         | CONFIRMED  | 2026-11-27 06:04:15.808875+00 | The Central confirmed the operation

   --- as leituras de status que a Central countou (o timeout deterministico leu 2 vezes) ---
       "contadores": {
           "leiturasDeStatus": {
               "PRT-20261127-0003": 3
           }
       }

   === 10. fase C: o reenvio usa a mesma Idempotency-Key ===
   mock com a chave K e outro payload -> {"protocolo":"PRT-20261127-0004"} | 202
   POST /api/v1/portability-operations -> {"operationId":"604bebe1-d997-4b30-80a5-8a86144b438c","protocol":null,"status":"INTEGRATION_ERROR","totalAmount":450.00,"createdAt":"2026-11-27T06:04:18.379816Z"} | 202 0.072037
   o worker reenviou e a Central recusou de novo (retry_count=2) apos 30 ciclos
    70 | INTEGRATION_FAILED  | CREATED           | INTEGRATION_ERROR | 2026-11-27 06:04:18.418668+00 | Central did not accept the operation: IDEMPOTENCIA_CONFLITO (status 409)
    71 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 06:05:20.114594+00 | the Central did not accept the operation: IDEMPOTENCIA_CONFLITO

   --- a Central nao criou uma segunda operacao: o reenvio carregou a mesma chave ---
       "operacoes": { "PRT-20261127-0001": {...}, "PRT-20261127-0002": {...},
                      "PRT-20261127-0003": {...}, "PRT-20261127-0004": {...} }

   === 11. fase D: a Central cai durante o polling ===
   POST /api/v1/portability-operations -> {"operationId":"8feaef21-def8-46ed-808f-19560076142a","protocol":"PRT-20261127-0005","status":"PENDING","totalAmount":300.00,"createdAt":"2026-11-27T06:05:22.128636Z"} | 202 0.134268
   mock derrubado
   a falha da Central ficou registrada apos 2 ciclos de 2 s
    74 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 06:05:25.782009+00 | the Central did not accept the operation: CENTRAL_INDISPONIVEL
    75 | INTEGRATION_FAILED  | PENDING           | INTEGRATION_ERROR | 2026-11-27 06:05:27.354524+00 | the Central did not answer the status of the operation

                     id                  |      status       |     protocol      | retry_count |         next_poll_at
   --------------------------------------+-------------------+-------------------+-------------+-------------------------------
    604bebe1-d997-4b30-80a5-8a86144b438c | INTEGRATION_ERROR |                   |           3 | 2026-11-27 06:05:33.782009+00
    8feaef21-def8-46ed-808f-19560076142a | INTEGRATION_ERROR | PRT-20261127-0005 |           1 | 2026-11-27 06:05:29.354524+00

   === 13. encerramento dos processos e dos containers ===
   containers restantes: 0
   processes restantes: 0
   === fim ===
   ```
6. **Gates locais:**
   ```text
   node scripts/check-specs.mjs               → OK (exit 0)
   node SDD/SKILLS/check-docs/check-docs.mjs  → OK (46 documentos, exit 0)
   node scripts/check-architecture.mjs        → OK (22 testes de validação, exit 0)
   node scripts/check-ai-log.mjs              → OK (37 sessões, exit 0)
   node scripts/check-theme-tokens.mjs        → OK (exit 0)
   ```

#### Conteúdo Entregue:
- `domain/portability/CentralStatus` — o estado da operação na Central, selado em
  `Pending` (com o `Retry-After`), `Confirmed` e `Rejected`, sempre com o corpo bruto da
  Central, para a trilha da auditoria provar o que ela respondeu.
- `domain/portability/CentralStatusPort` — a porta de leitura do status, no domínio.
- `domain/portability/PortabilityOperation.renewIntegrationBackoff` — a falha de uma
  operação que já está em `INTEGRATION_ERROR` renova o backoff e a contagem de falhas sem
  mudar o status, porque a tabela de estados do `ARCHITECTURE.md` §7 não tem a
  auto-transição de `INTEGRATION_ERROR` para `INTEGRATION_ERROR`.
- `application/portability/PollOperation` — o worker: lê o lote das operações devidas,
  pergunta o status, reenvia a operação sem protocolo com a mesma chave e grava a
  operação e o evento na mesma transação.
- `application/portability/IntegrationBackoff` — a espera que dobra com cada falha e
  nunca passa de 60 s.
- `adapters/out/central/CentralHttpClient.statusOf` — a leitura do status: `204` com
  `Retry-After`, `200` terminal, `404` e corpo fora do contrato como falha, com 3
  tentativas e o circuito.
- `adapters/out/central/acl/PortabilityAcl.toCentralStatus` e o DTO
  `CentralStatusResponse` — a tradução do contrato PT para o domínio EN.
- `adapters/out/resilience/CircuitBreaker` — janela de 10 chamadas, abre em 50% de
  falhas, fica 10 s aberto e fecha depois de 3 chamadas em half-open.
- `adapters/out/persistence/JpaPortabilityOperationRepository.findDueForPolling` — as
  operações `PENDING` e `INTEGRATION_ERROR` com `next_poll_at <= now()`, as mais antigas
  primeiro, limitadas ao lote.
- `config/ResilienceConfig` e `config/SchedulingConfig` — o circuito, o backoff e o
  worker (`fixedDelay` de 2 s, lote de 20, desligável com `central.poll.enabled`).
- `config/CompositionRoot` e `application.yml` — os beans e a seção `central.poll.*` e
  `central.circuit.*`.
- Testes: 13 do worker, 6 do backoff, 8 do circuito, 17 do cliente HTTP, 7 de
  integração do polling sobre PostgreSQL 16, 2 do agendamento e 45 do agregado.

#### Descobertas e Decisões:
- O `Retry-After` é contado a partir da **resposta** da Central, não do começo do lote:
  a primeira execução do end-to-end mostrou um poll que durou 8,9 s (as duas tentativas
  com timeout do mock) gravando o `next_poll_at` com o instante do começo do lote, já no
  passado — a operação seria consultada de novo sem espera. O worker agora data a
  transição com o instante da resposta, e o teste
  `schedulesTheNextPollFromTheAnswerOfTheCentralAndNotFromTheStartOfTheBatch` prova a
  regra.
- A tabela de estados do `ARCHITECTURE.md` §7 não lista a transição de
  `INTEGRATION_ERROR` para `INTEGRATION_ERROR`, mas o fluxo do worker (§11, passo 6)
  manda registrar `INTEGRATION_FAILED`, incrementar `retry_count` e agendar o backoff
  quando a falha acontece em uma operação que já estava em `INTEGRATION_ERROR`. O
  agregado ganhou `renewIntegrationBackoff`, que muda só a agenda e a contagem; o
  status permanece, e a máquina de estados testada em `PortabilityOperationTest` segue
  intacta.
- O `404` do contrato (`OPERACAO_NAO_ENCONTRADA`) é falha da integração, não estado
  terminal: o contrato da Central não tem motivo de rejeição para ele e o domínio só
  conhece `MARGEM_INSUFFICIENTE`. A operação fica `INTEGRATION_ERROR`, retomável, com o
  motivo da recusa na trilha da auditoria.
- Um motivo de rejeição que o domínio não conhece também vira falha de integração, pelo
  mesmo motivo: o `RejectionReason` é selado e o contrato não fecha a lista de motivos.
- O circuito conta uma falha por chamada lógica, não por tentativa: a janela de 10
  mede as chamadas que a Central realmente recebeu.
- O worker é uma instância só (`@Scheduled` com um `TaskScheduler` de uma thread), como
  o trade-off documentado do `ARCHITECTURE.md` §11; o `@Version` otimista do banco
  resolve a concorrência entre instâncias, e o perdedor do bloqueio descarta a
  transição, porque o estado armazenado é a verdade.
- O reenvio da operação que a Central nunca recebeu leva a mesma `Idempotency-Key`: o
  end-to-end provou isso pelo `409 IDEMPOTENCIA_CONFLITO` que o mock devolveu no
  reenvio — com outra chave, o mock teria aceitado a operação.
- O primeiro pedido de cada caminho novo de uma JVM fria custa de 1,5 s a 4,5 s nesta
  máquina EC2 (carregamento de classes), acima do read timeout de 3 s. O end-to-end
  aquece o mock (autorização, agenda e submissão) e o cliente HTTP do backend antes de
  medir, para evidenciar o comportamento da integração e não o aquecimento da máquina.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/portability/CentralStatus.java`
- `backend/src/main/java/br/com/consigport/domain/portability/CentralStatusPort.java`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperation.java`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/domain/shared/DomainError.java`
- `backend/src/main/java/br/com/consigport/application/portability/PollOperation.java`
- `backend/src/main/java/br/com/consigport/application/portability/IntegrationBackoff.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralHttpClient.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralProperties.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/acl/PortabilityAcl.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralStatusResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/out/resilience/CircuitBreaker.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaPortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/main/java/br/com/consigport/config/ResilienceConfig.java`
- `backend/src/main/java/br/com/consigport/config/SchedulingConfig.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/com/consigport/application/portability/PollOperationTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/IntegrationBackoffTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/resilience/CircuitBreakerTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/CentralHttpClientTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/PollingPersistenceTest.java`
- `backend/src/test/java/br/com/consigport/config/SchedulingConfigTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityOperationTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryCentralStatusPort.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryPortabilityOperationRepository.java`
- `backend/src/test/java/br/com/consigport/support/MutableClock.java`
- `backend/src/test/java/br/com/consigport/support/PostgreSqlTestSupport.java`
- `backend/src/test/java/br/com/consigport/application/portability/StartOperationTest.java`

### Task 2.12 — Auto-transição de integração e respostas tipadas da Central

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/032-backend-typed-central-answers`
- **PR:** #44 — https://github.com/marcelinosandroni/consigport-platform/pull/44
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** alinhar a máquina de estados, a trilha e o fluxo do worker, e distinguir as
  respostas da Central que antes caíam no mesmo balde de falha: o `404` da Central e a
  rejeição com um motivo que o contrato não declara.

#### Comandos de Verificação e Saídas:

1. **Máquina de estados, razão selada, worker, cliente HTTP e ACL (unitário):**
   ```text
   ./mvnw -B -pl backend test -Dtest='PortabilityOperationTest,RejectionReasonTest,PollOperationTest,CentralHttpClientTest,PortabilityAclTest'
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest$OperationIdTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.166 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest$Creation
   → Tests run: 45, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.713 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in
     br.com.consigport.domain.portability.RejectionReasonTest
   → Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.218 s -- in
     br.com.consigport.application.portability.PollOperationTest
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.478 s -- in
     br.com.consigport.adapters.out.central.acl.PortabilityAclTest
   → Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.752 s -- in
     br.com.consigport.adapters.out.central.CentralHttpClientTest
   → Tests run: 104, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 289, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.007 s]
   → ConsigPort Backend ................................. SUCCESS [ 54.543 s]
   → Central de Convênios Mock .......................... SUCCESS [ 23.195 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **Prova de vida da auto-transição (a máquina de estados sem a linha da §7):**
   ```text
   → matriz de transições sem a linha INTEGRATION_ERROR → INTEGRATION_ERROR
   → ./mvnw -q -B -pl backend test -Dtest='PortabilityOperationTest'
   → [ERROR] Tests run: 45, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.851 s <<< FAILURE! -- in br.com.consigport.domain.portability.PortabilityOperationTest
   → [ERROR] br.com.consigport.domain.portability.PortabilityOperationTest.followsOnlyTheTransitionsOfTheStateMachine(OperationStatus, Trigger, boolean, OperationStatus)[17] -- Time elapsed: 0.027 s <<< FAILURE!
   → [ERROR]   PortabilityOperationTest.followsOnlyTheTransitionsOfTheStateMachine:117
   → [ERROR] Expecting code to raise a throwable.
   → EXIT=1
   → linha da §7 restaurada
   → ./mvnw -q -B -pl backend test -Dtest='PortabilityOperationTest'
   → EXIT=0
   ```
4. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`; o mock confirma em 8 s). O reset
   administrativo (`POST /_admin/reset → 204`) faz a Central **esquecer** as operações
   sem sair do ar: é assim que o `404` do contrato é provado sem indisponibilidade:
   ```text
   === 5. sete operacoes aceitas pela Central (protocolos que ela vai esquecer) ===
   POST /api/v1/portability-operations -> {"operationId":"23bac40b-595b-444b-94c1-8fc954579581","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:57.884945Z"} | 202 0.591029
   POST /api/v1/portability-operations -> {"operationId":"ad3e20ce-3957-44df-987a-2af4161f683c","protocol":"PRT-20261127-0003","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:58.492699Z"} | 202 0.056787
   POST /api/v1/portability-operations -> {"operationId":"e670216d-a005-47f4-aa0a-0ee73e728d34","protocol":"PRT-20261127-0004","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:58.605586Z"} | 202 0.061435
   POST /api/v1/portability-operations -> {"operationId":"0ee95f69-c7c4-42b6-92c2-d0322a5b2306","protocol":"PRT-20261127-0005","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:58.736799Z"} | 202 0.052582
   POST /api/v1/portability-operations -> {"operationId":"6542a498-43de-4e78-bce5-12b181e3c823","protocol":"PRT-20261127-0006","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:58.861720Z"} | 202 0.068968
   POST /api/v1/portability-operations -> {"operationId":"1fd9d022-231e-4a78-9a52-1373e836dc03","protocol":"PRT-20261127-0007","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:58.985701Z"} | 202 0.040046
   POST /api/v1/portability-operations -> {"operationId":"444292be-e918-4110-b6f2-a7a4a9554645","protocol":"PRT-20261127-0008","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:03:59.098880Z"} | 202 0.048561

   === 6. o reset administrativo: a Central esquece as operacoes, mas continua no ar ===
   POST /_admin/reset -> 204

   === 7. o worker recebe 404: a resposta tipada mantem a operacao retomavel ===
   as 7 operacoes registraram o 404 da Central apos 2 ciclos de 2 s
                     id                  |      status       |     protocol      | rejection_reason | retry_count |         next_poll_at
   --------------------------------------+-------------------+-------------------+------------------+-------------+-------------------------------
    23bac40b-595b-444b-94c1-8fc954579581 | INTEGRATION_ERROR | PRT-20261127-0002 |                  |           1 | 2026-11-27 10:04:04.751961+00
    ad3e20ce-3957-44df-987a-2af4161f683c | INTEGRATION_ERROR | PRT-20261127-0003 |                  |           2 | 2026-11-27 10:04:06.770605+00

   --- os eventos: o 404 virou INTEGRATION_FAILED com o corpo bruto da Central ---
    id  |     event_type     |  previous_status  |    new_status     |                                                                           raw_payload
   -----+--------------------+-------------------+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------
    156 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.505272785Z\"}"
    157 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.562912808Z\"}"
    158 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.617225668Z\"}"
    159 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.645248785Z\"}"
    160 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.684236838Z\"}"
    161 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:00.710929061Z\"}"
    162 | INTEGRATION_FAILED | PENDING           | INTEGRATION_ERROR | "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada para o protocolo informado.\",\"timestamp\":\"2026-11-27T10:04:02.750061046Z\"}"

   === 8. a segunda falha e a auto-transicao (previous_status = new_status = INTEGRATION_ERROR) ===
   as 7 operacoes registraram a segunda falha apos 1 ciclos de 2 s
    162 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.751961+00 | the Central does not know the protocol PRT-20261127-0002
    163 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.770605+00 | the Central does not know the protocol PRT-20261127-0003
    164 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.787956+00 | the Central does not know the protocol PRT-20261127-0004
    165 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.814885+00 | the Central does not know the protocol PRT-20261127-0005
    166 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.838666+00 | the Central does not know the protocol PRT-20261127-0006
    167 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.855522+00 | the Central does not know the protocol PRT-20261127-0007
    168 | INTEGRATION_FAILED  | INTEGRATION_ERROR | INTEGRATION_ERROR | 2026-11-27 10:04:02.88488+00  | the Central does not know the protocol PRT-20261127-0008

   --- o intervalo entre as falhas: o backoff dobrou (2 s, 4 s, 8 s) ---
   INTEGRATION_FAILED|INTEGRATION_ERROR|INTEGRATION_ERROR|2026-11-27 10:04:02.770605+00|00:00:02.244012
   INTEGRATION_FAILED|INTEGRATION_ERROR|INTEGRATION_ERROR|2026-11-27 10:04:04.937696+00|00:00:02.185735
   INTEGRATION_FAILED|INTEGRATION_ERROR|INTEGRATION_ERROR|2026-11-27 10:04:06.978625+00|00:00:04.208020
   INTEGRATION_FAILED|INTEGRATION_ERROR|INTEGRATION_ERROR|2026-11-27 10:04:06.999302+00|00:00:04.211346

   === 9. o circuito nao foi poluido pelo 404: uma nova operacao e aceita e confirmada ===
   POST /autorizacoes BEN-001 -> {"autorizacaoId":"AUT-20261127-0001"} | 201
   POST /api/v1/portability-operations -> {"operationId":"99202f4b-e46f-4168-b560-605d1686bc1d","protocol":"PRT-20261127-0001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T10:12:05.287875Z"} | 202 0.060706
   a nova operacao foi confirmada pela Central (o circuito continuou fechado) apos 5 ciclos de 2 s
    99202f4b-e46f-4168-b560-605d1686bc1d | CONFIRMED         | PRT-20261127-0001 |                  |           0 | 2026-11-27 10:12:15.316945+00

   === 10. prova de vida: rejeicao com motivo fora do contrato vira REJECTED terminal ===
   --- patch temporario no mock: MARGEM_CONSIGNADA_INSUFICIENTE vira LIMITE_DE_PARCELAS ---
   34:  private static final String MOTIVO_MARGEM = "LIMITE_DE_PARCELAS";
   POST /autorizacoes BEN-REJ -> {"autorizacaoId":"AUT-20261127-0001"} | 201
   POST /api/v1/portability-operations -> {"operationId":"be0f7632-2b77-4ae9-a024-ef66bb43aa98","protocol":"PRT-20261127-0001","status":"PENDING","totalAmount":250.00,"createdAt":"2026-11-27T10:12:40.204855Z"} | 202 1.066940
   a operacao foi encerrada como REJECTED apos 5 ciclos de 2 s
                     id                  |  status  |  rejection_reason
   --------------------------------------+----------+--------------------
    be0f7632-2b77-4ae9-a024-ef66bb43aa98 | REJECTED | UNMAPPED_REJECTION

   --- o evento da rejeicao: o motivo original da Central no corpo bruto ---
    id  |     event_type     | previous_status | new_status |          created_at           |                        details                         |                              raw_payload
   -----+--------------------+-----------------+------------+-------------------------------+--------------------------------------------------------+------------------------------------------------------------------------
    254 | OPERATION_REJECTED | PENDING         | REJECTED   | 2026-11-27 10:12:50.138168+00 | The Central rejected the operation: UNMAPPED_REJECTION | "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"LIMITE_DE_PARCELAS\"}"

   --- o codigo publico e UNMAPPED_REJECTION ---
   UNMAPPED_REJECTION

   === 11. o patch do mock e revertido e o jar recompilado ===
   34:  private static final String MOTIVO_MARGEM = "MARGEM_CONSIGNADA_INSUFICIENTE";

   === 12. encerramento dos processos e dos containers ===
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
5. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (38 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `domain/portability/PortabilityOperation.registerIntegrationFailure` — a falha da
  integração vem de `CREATED`, de `PENDING` **e de `INTEGRATION_ERROR`**: a auto-transição
  `INTEGRATION_ERROR` → `INTEGRATION_ERROR` da §7, com o evento `INTEGRATION_FAILED`.
  `renewIntegrationBackoff` deixou de existir: o caminho de falha é único no domínio e no
  worker.
- `domain/portability/CentralStatus.NotFound` — a Central não conhece o protocolo: uma
  resposta tipada, com o corpo bruto, em vez de uma falha genérica de disponibilidade.
- `domain/portability/RejectionReason.Unmapped` — o motivo que o contrato da Central não
  declara, com o código público estável `UNMAPPED_REJECTION`.
- `adapters/out/central/acl/PortabilityAcl.toRejectionReason` — traduz o motivo
  desconhecido para `Unmapped`; a rejeição sem motivo continua violando o contrato.
- `adapters/out/central/CentralHttpClient.statusOf` — o `404` vira `NotFound` e conta como
  **chamada bem-sucedida** no circuito: a Central respondeu, ela está disponível.
- `application/portability/PollOperation` — trata o `NotFound` como falha retomável, grava
  o corpo bruto da Central no evento e usa `registerIntegrationFailure` em todos os
  caminhos de falha.
- `SDD/ARCHITECTURE.md` §7 — a linha da auto-transição; §3 — a tradução do motivo
  desconhecido; §11 — o passo do `404` no fluxo do worker.
- `docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md` — o protocolo de polling
  com a auto-transição, o `404` e a rejeição não mapeada.
- Testes: 45 do agregado, 16 do worker, 19 do cliente HTTP, 8 da ACL e 6 da razão selada.

#### Descobertas e Decisões:
- A tabela de estados da §7 **já usava** auto-transição (`PENDING` → `PENDING` no poll
  `204`), o que prova que a linha ausente para `INTEGRATION_ERROR` era omissão da tabela,
  e não decisão de design. Com a linha declarada, a tabela, o fluxo do worker (§11, passo
  6), a trilha imutável e o teste parametrizado passam a dizer a mesma coisa.
- A trilha imutável registrava `(INTEGRATION_ERROR → INTEGRATION_ERROR)` com o método
  `renewIntegrationBackoff`, que dizia ser uma "renovação", não uma transição: o banco
  contradizia a máquina de estados. Agora `previous_status` e `new_status` iguais são uma
  transição declarada.
- O `404` da Central é uma **resposta**, não uma indisponibilidade: ele nascia como
  `CentralUnavailable` e contava como falha no circuito, o que podia bloquear operações
  saudáveis de outras operações. Agora é `CentralStatus.NotFound`, conta como sucesso no
  circuito e a operação continua retomável, com o corpo bruto (`OPERACAO_NAO_ENCONTRADA`)
  na trilha.
- Uma rejeição com motivo fora do contrato é **decisão de negócio** da Central: tratá-la
  como falha de integração mantinha a operação em polling eterno e, se a Central mudasse a
  resposta depois, a operação terminaria `CONFIRMED` e apagaria a rejeição. Agora ela fecha
  como `REJECTED` com o código `UNMAPPED_REJECTION`, e o motivo original da Central fica
  no `rawPayload` do evento. O contrato público não mudou: `rejectionReason` é texto
  livre em `contracts/consigport-api.yaml`.
- O end-to-end encontrou um defeito **pré-existente**, fora do escopo desta task: a
  sequência de protocolos da Central mock reinicia a cada `POST /_admin/reset` e a cada
  reinício do processo, e o esquema da §6 garante `protocol` único — quando a Central
  reemite um protocolo já guardado, a violação da restrição chega como
  `DataIntegrityViolationException` e a API responde `500` em vez de um erro mapeado. A
  dívida foi registrada no `BACKLOG.md` com a evidência.
- O `ARCHITECTURE.md` §11 e o ADR-0003 dizem "Falhas até `INTEGRATION_ERROR`: 5", mas a
  implementação entra em `INTEGRATION_ERROR` assim que o cliente HTTP esgota as 3
  tentativas. A divergência também foi registrada no `BACKLOG.md`: implementar o limiar de
  5 ou corrigir a regra.
- O `404` continua sem teto de tentativas: a operação que a Central não conhece é
  consultada a cada 60 s, para sempre. Registrado como dívida no `BACKLOG.md`.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperation.java`
- `backend/src/main/java/br/com/consigport/domain/portability/RejectionReason.java`
- `backend/src/main/java/br/com/consigport/domain/portability/CentralStatus.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/acl/PortabilityAcl.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralHttpClient.java`
- `backend/src/main/java/br/com/consigport/application/portability/PollOperation.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityOperationTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/RejectionReasonTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/PollOperationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/CentralHttpClientTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/acl/PortabilityAclTest.java`
- `SDD/ARCHITECTURE.md`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.13 — Confirmação e atualização de conta

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/033-backend-complete-operation`
- **PR:** #45 — https://github.com/marcelinosandroni/consigport-platform/pull/45
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar o ciclo da operação em uma única transação: a confirmação da
  Central vira `CONFIRMED` e, na mesma transação, a conta recebe a atualização e a
  operação vira `COMPLETED` com `account_updated_at`; a rejeição fecha a operação como
  `REJECTED` com o motivo.

#### Comandos de Verificação e Saídas:

1. **Caso de uso, worker, agregados e persistência (unitário e integração):**
   ```text
   ./mvnw -B -pl backend test -Dtest='CompleteOperationTest,PollOperationTest,CompletionPersistenceTest,PollingPersistenceTest,PortabilityOperationTest,SchedulingConfigTest'
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.032 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest$OperationIdTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.105 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest$Creation
   → Tests run: 48, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.745 s -- in
     br.com.consigport.domain.portability.PortabilityOperationTest
   → Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.079 s -- in
     br.com.consigport.application.portability.CompleteOperationTest
   → Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.161 s -- in
     br.com.consigport.application.portability.PollOperationTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 20.88 s -- in
     br.com.consigport.adapters.out.persistence.CompletionPersistenceTest
   → Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.448 s -- in
     br.com.consigport.adapters.out.persistence.PollingPersistenceTest
   → Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in
     br.com.consigport.config.SchedulingConfigTest
   → Tests run: 93, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 301, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.010 s]
   → ConsigPort Backend ................................. SUCCESS [ 50.046 s]
   → Central de Convênios Mock .......................... SUCCESS [ 22.345 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **Prova de vida do rollback (a trilha gravada fora da transação):**
   ```text
   → TransitionRecorder grava os eventos fora de transactions.run(...)
   → ./mvnw -q -B -pl backend test -Dtest='CompletionPersistenceTest'
   → [ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 22.58 s <<< FAILURE! -- in br.com.consigport.adapters.out.persistence.CompletionPersistenceTest
   → [ERROR] br.com.consigport.adapters.out.persistence.CompletionPersistenceTest.rollsBackEverythingWhenTheTrailFailsBetweenTheSteps -- Time elapsed: 1.883 s <<< FAILURE!
   → [ERROR] expected: PENDING
   →  but was: COMPLETED
   → [ERROR]   CompletionPersistenceTest.rollsBackEverythingWhenTheTrailFailsBetweenTheSteps:118
   → EXIT=1
   → a trilha voltou para dentro da transação
   → ./mvnw -q -B -pl backend test -Dtest='CompletionPersistenceTest'
   → EXIT=0
   ```
4. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`; o mock confirma em 8 s):
   ```text
   === 4. a operacao confirmada: CONFIRMADA -> CONFIRMED -> COMPLETED ===
   POST /autorizacoes BEN-001 -> {"autorizacaoId":"AUT-20261127-0001"} | 201
   POST /api/v1/portability-operations -> {"operationId":"e3f9106f-0dac-4958-9266-db246d0c08b0","protocol":"PRT-20261127-0001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T11:35:47.316980Z"} | 202 0.554699
   a operacao foi completada apos 4 ciclos de 2 s
   --- a operacao: o status final e o instante da atualizacao da conta ---
                     id                  |  status   |     protocol      | rejection_reason | retry_count |      account_updated_at      |          created_at
   --------------------------------------+-----------+-------------------+------------------+-------------+------------------------------+------------------------------
    e3f9106f-0dac-4958-9266-db246d0c08b0 | COMPLETED | PRT-20261127-0001 |                  |           0 | 2026-11-27 11:35:56.39997+00 | 2026-11-27 11:35:47.31698+00

   === 5. a trilha imutavel: o caminho inteiro ===
    id  |     event_type      | previous_status | new_status |          created_at           |                                details                                 |                      raw_payload
   -----+--------------------+-----------------+------------+-------------------------------+------------------------------------------------------------------------+-------------------------------------------------------
    269 | OPERATION_CREATED   |                 | CREATED    | 2026-11-27 11:35:47.31698+00  | Operation created                                                      |
    270 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-27 11:35:47.690617+00 | Central accepted the operation with protocol PRT-20261127-0001         |
    271 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 11:35:48.261837+00 | The Central is still processing the operation; the next poll is in 2 s |
    272 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 11:35:50.307053+00 | The Central is still processing the operation; the next poll is in 2 s |
    273 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 11:35:52.336279+00 | The Central is still processing the operation; the next poll is in 2 s |
    274 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-27 11:35:54.363893+00 | The Central is still processing the operation; the next poll is in 2 s |
    275 | OPERATION_CONFIRMED | PENDING         | CONFIRMED  | 2026-11-27 11:35:56.39997+00  | The Central confirmed the operation                                    | "{\"status\":\"CONFIRMADA\",\"motivoRejeicao\":null}"
    276 | ACCOUNT_UPDATED     | CONFIRMED       | COMPLETED  | 2026-11-27 11:35:56.39997+00  | The receiving account of the operation was updated                     |
    277 | OPERATION_COMPLETED | CONFIRMED       | COMPLETED  | 2026-11-27 11:35:56.39997+00  | The operation was completed                                            |
   (9 rows)

   === 6. a operacao rejeitada: REJEITADA -> REJECTED com o motivo ===
   POST /autorizacoes BEN-REJ -> {"autorizacaoId":"AUT-20261127-0002"} | 201
   POST /api/v1/portability-operations -> {"operationId":"aaa1339d-19bb-4808-b1f6-3980eb2a314e","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":250.00,"createdAt":"2026-11-27T11:35:57.180884Z"} | 202 0.050598
   a operacao foi encerrada como REJECTED apos 5 ciclos de 2 s
   --- as operacoes: a confirmada completed e a rejeitada rejected ---
                     id                  |  status   |     protocol      |  rejection_reason   | retry_count |      account_updated_at      |          created_at
   --------------------------------------+-----------+-------------------+---------------------+-------------+------------------------------+-------------------------------
    e3f9106f-0dac-4958-9266-db246d0c08b0 | COMPLETED | PRT-20261127-0001 |                     |           0 | 2026-11-27 11:35:56.39997+00 | 2026-11-27 11:35:47.31698+00
    aaa1339d-19bb-4808-b1f6-3980eb2a314e | REJECTED  | PRT-20261127-0002 | MARGIN_INSUFFICIENT |           0 |                              | 2026-11-27 11:35:57.180884+00

   --- a trilha da rejeicao ---
    id  |     event_type     | previous_status | new_status |                         details                         |                                    raw_payload
   -----+--------------------+-----------------+------------+---------------------------------------------------------+------------------------------------------------------------------------------------
    284 | OPERATION_REJECTED | PENDING         | REJECTED   | The Central rejected the operation: MARGIN_INSUFFICIENT | "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"MARGEM_CONSIGNADA_INSUFICIENTE\"}"
   (1 row)

   === 7. encerramento dos processos e dos containers ===
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
5. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (39 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `application/portability/CompleteOperation.java` — o caso de uso que fecha o ciclo: a
  confirmação vira `CONFIRMED` e, na mesma transação, a conta é atualizada e a operação
  vira `COMPLETED`; a rejeição fecha a operação como `REJECTED` com o motivo.
- `application/portability/TransitionRecorder.java` — a fronteira de transação e a
  política do lock otimista em um lugar só: o estado da operação e os eventos da
  transição são gravados juntos ou não são gravados.
- `application/portability/PollOperation.java` — o worker entrega a resposta terminal da
  Central ao `CompleteOperation`; ele não escreve mais o estado terminal.
- `domain/portability/PortabilityOperation.java` — invariante nova: `account_updated_at`
  existe apenas quando a operação está `COMPLETED`.
- `config/CompositionRoot.java` — o wiring dos dois casos de uso novos.
- `docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md` — o protocolo de polling
  com o fechamento do ciclo.
- Testes: 7 do `CompleteOperation`, 3 de integração da persistência, 7 do polling
  persistido, 16 do worker, 48 do agregado e 2 do agendamento.

#### Descobertas e Decisões:
- A operação parava em `CONFIRMED`: o `§7` declara `CONFIRMED` → `COMPLETED` com
  `ACCOUNT_UPDATED`, `OPERATION_COMPLETED` e `account_updated_at`, mas nada no código
  executava essa segunda transição. O ciclo agora fecha, e a primeira execução do
  end-to-end com o jar antigo provou a lacuna: a operação ficou em `CONFIRMED` e a
  trilha parou em `OPERATION_CONFIRMED`.
- O estado intermediário `CONFIRMED` **não** é gravado: a confirmação, a atualização da
  conta e o fechamento são uma transição só, gravada em uma transação só. A trilha
  prova o caminho (`PENDING` → `CONFIRMED` → `COMPLETED`), o banco guarda o estado
  final.
- O `rawPayload` da Central fica só no evento `OPERATION_CONFIRMED`: os eventos
  `ACCOUNT_UPDATED` e `OPERATION_COMPLETED` são fatos locais, não respostas da Central.
- A transação e a política do lock otimista saíram do `PollOperation` para o
  `TransitionRecorder`: os dois casos de uso gravam "estado + trilha" do mesmo jeito, e
  o conflito de concorrência continua sendo resolvido do mesmo jeito — o estado
  gravado é a verdade.
- O rollback é real, não uma afirmação: o teste de integração quebra a trilha no
  segundo evento, sobre PostgreSQL 16, e prova que a operação continua `PENDING` e que
  o evento já escrito não existe. A prova de vida (eventos fora da transação) deixou o
  teste vermelho com `expected: PENDING but was: COMPLETED`.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/application/portability/CompleteOperation.java`
- `backend/src/main/java/br/com/consigport/application/portability/TransitionRecorder.java`
- `backend/src/main/java/br/com/consigport/application/portability/PollOperation.java`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperation.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/test/java/br/com/consigport/application/portability/CompleteOperationTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/PollOperationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/CompletionPersistenceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/PollingPersistenceTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityOperationTest.java`
- `backend/src/test/java/br/com/consigport/config/SchedulingConfigTest.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.14 — Consulta de operações e trilha

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/034-backend-operation-queries`
- **PR:** #46 — https://github.com/marcelinosandroni/consigport-platform/pull/46
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** expor a leitura paginada das operações com filtro por status, o detalhe
  de uma operação e a trilha da auditoria em ordem cronológica; a operação que não
  existe responde `404 OPERATION_NOT_FOUND`.

#### Comandos de Verificação e Saídas:

1. **Recorte da task (casos de uso, domínio, persistência e borda HTTP):**
   ```text
   ./mvnw -B -pl backend test -Dtest='FindOperationTest,ListOperationsTest,ListOperationEventsTest,SharedValueObjectsTest,QueryPersistenceTest,OperationQueryResourceTest'
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$IdempotencyKeyTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$PageTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$InstallmentIdTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$AgreementIdTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$BeneficiaryIdTest
   → Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in FindOperationTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in ListOperationsTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in ListOperationEventsTest
   → Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in QueryPersistenceTest
   → Tests run: 9, Failures: 0, Errors: 0, Skipped: 0 -- in OperationQueryResourceTest
   → Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 332, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [ 54.153 s]
   → Central de Convênios Mock .......................... SUCCESS [ 22.098 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`; o mock confirma em 8 s; roteiro
   `/tmp/opencode/e2e-2.14.sh`, log `/tmp/opencode/e2e-2.14.log`):
   ```text
   === 0. ambiente ===
   data: 2026-11-27T14:09:58Z
     Container consigport-postgres Healthy
   === 1. mock da Central em :8081 (confirmacao em 8 s) ===
   mock saudável em 9s
   === 2. limpeza das tabelas e backend em :8080 (JVM fria) ===
   TRUNCATE TABLE
   backend saudável em 16s
   === 3. quatro operacoes: tres confirmadas e uma rejeitada ===
   POST /autorizacoes BEN-001 -> {"autorizacaoId":"AUT-20261127-0001"} | 201
   POST /autorizacoes BEN-REJ -> {"autorizacaoId":"AUT-20261127-0002"} | 201
   POST /api/v1/portability-operations -> {"operationId":"2bae1e18-e54f-4e6c-91ba-61ee931bd5b6","protocol":"PRT-20261127-0001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.000927Z"} | 202 1.115476
   POST /api/v1/portability-operations -> {"operationId":"b69e8dd8-ed26-467f-933d-ede8d850a722","protocol":"PRT-20261127-0002","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.554459Z"} | 202 0.110633
   POST /api/v1/portability-operations -> {"operationId":"da049309-294e-459c-98b4-8b93aafcbe32","protocol":"PRT-20261127-0003","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.719190Z"} | 202 0.071502
   POST /api/v1/portability-operations -> {"operationId":"be11d340-9cda-4783-811d-66b463f76722","protocol":"PRT-20261127-0004","status":"PENDING","totalAmount":250.00,"createdAt":"2026-11-27T14:10:40.847504Z"} | 202 0.048923

   === 4. a lista paginada, logo depois do envio ===
   --- GET /api/v1/portability-operations?page=0&size=2 ---
   {"items":[{"operationId":"2bae1e18-e54f-4e6c-91ba-61ee931bd5b6","protocol":"PRT-20261127-0001","beneficiaryId":"BEN-001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.000927Z","updatedAt":"2026-11-27T14:10:40.354209Z"},{"operationId":"b69e8dd8-ed26-467f-933d-ede8d850a722","protocol":"PRT-20261127-0002","beneficiaryId":"BEN-001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.554459Z","updatedAt":"2026-11-27T14:10:40.598013Z"}],"page":0,"size":2,"totalElements":4,"totalPages":2}
   [status 200 em 0.119408s]
   --- GET /api/v1/portability-operations?page=1&size=2 ---
   {"items":[{"operationId":"da049309-294e-459c-98b4-8b93aafcbe32","protocol":"PRT-20261127-0003","beneficiaryId":"BEN-001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.719190Z","updatedAt":"2026-11-27T14:10:40.756997Z"},{"operationId":"be11d340-9cda-4783-811d-66b463f76722","protocol":"PRT-20261127-0004","beneficiaryId":"BEN-REJ","status":"PENDING","totalAmount":250.00,"createdAt":"2026-11-27T14:10:40.847504Z","updatedAt":"2026-11-27T14:10:40.873239Z"}],"page":1,"size":2,"totalElements":4,"totalPages":2}
   [status 200 em 0.015074s]
   --- GET /api/v1/portability-operations?status=PENDING&size=50 ---
   {"items":[... 4 operacoes ...],"page":0,"size":50,"totalElements":4,"totalPages":1}
   [status 200 em 0.026242s]

   === 5. as operacoes chegam ao estado terminal ===
   tres operacoes foram completadas apos 4 ciclos de 2 s
   uma operacao foi rejeitada apos 1 ciclos de 2 s
                     id                  |  status   |     protocol      |  rejection_reason   | retry_count |      account_updated_at       |          created_at           |          updated_at
    --------------------------------------+-----------+-------------------+---------------------+-------------+-------------------------------+-------------------------------+-------------------------------
    2bae1e18-e54f-4e6c-91ba-61ee931bd5b6 | COMPLETED | PRT-20261127-0001 |                     |           0 | 2026-11-27 14:10:49.768553+00 | 2026-11-27 14:10:40.000927+00 | 2026-11-27 14:10:49.773078+00
    b69e8dd8-ed26-467f-933d-ede8d850a722 | COMPLETED | PRT-20261127-0002 |                     |           0 | 2026-11-27 14:10:49.805537+00 | 2026-11-27 14:10:40.554459+00 | 2026-11-27 14:10:49.808591+00
    da049309-294e-459c-98b4-8b93aafcbe32 | COMPLETED | PRT-20261127-0003 |                     |           0 | 2026-11-27 14:10:49.83032+00  | 2026-11-27 14:10:40.71919+00  | 2026-11-27 14:10:49.834731+00
    be11d340-9cda-4783-811d-66b463f76722 | REJECTED  | PRT-20261127-0004 | MARGIN_INSUFFICIENT |           0 |                               | 2026-11-27 14:10:40.847504+00 | 2026-11-27 14:10:49.867472+00
   (4 rows)

   === 6. a lista filtrada pelo estado terminal ===
   --- GET /api/v1/portability-operations?status=COMPLETED ---
   {"items":[... 3 operacoes COMPLETED ...],"page":0,"size":20,"totalElements":3,"totalPages":1}
   [status 200 em 0.013517s]
   --- GET /api/v1/portability-operations?status=REJECTED ---
   {"items":[{"operationId":"be11d340-9cda-4783-811d-66b463f76722","protocol":"PRT-20261127-0004","beneficiaryId":"BEN-REJ","status":"REJECTED","totalAmount":250.00,"createdAt":"2026-11-27T14:10:40.847504Z","updatedAt":"2026-11-27T14:10:49.867472Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}
   [status 200 em 0.014023s]

   === 7. o detalhe de uma operacao ===
   GET /api/v1/portability-operations/2bae1e18-e54f-4e6c-91ba-61ee931bd5b6
   {"operationId":"2bae1e18-e54f-4e6c-91ba-61ee931bd5b6","protocol":"PRT-20261127-0001","beneficiaryId":"BEN-001","status":"COMPLETED","totalAmount":450.00,"createdAt":"2026-11-27T14:10:40.000927Z","updatedAt":"2026-11-27T14:10:49.773078Z","agreementId":"AGR-001","installments":[{"installmentId":"PAR-001","agreementId":"AGR-001","amount":450.00,"discountDate":"2026-12-05"}],"assigneeCreditor":"ConsigPort","receivingAccount":{"bank":"001","branch":"1234","account":"56789-0"},"rejectionReason":null,"retryCount":0,"accountUpdatedAt":"2026-11-27T14:10:49.768553Z"}
   [status 200 em 0.021853s]

   === 8. a operacao que nao existe: 404 OPERATION_NOT_FOUND ===
   {"code":"OPERATION_NOT_FOUND","message":"Operação não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T14:10:52.489497495Z","path":"/api/v1/portability-operations/e15e3c0a-7528-4370-941d-db9a1b2c2005","details":[]}
   [status 404 em 0.030967s]

   === 9. os parametros recusados: 400 VALIDATION_ERROR ===
   --- GET /api/v1/portability-operations?page=-1 ---
   {"code":"VALIDATION_ERROR","message":"A requisição é inválida.","locale":"pt-BR","timestamp":"2026-11-27T14:10:52.514347452Z","path":"/api/v1/portability-operations","details":[]}
   [status 400 em 0.006868s]
   --- GET /api/v1/portability-operations?size=101 ---
   {"code":"VALIDATION_ERROR","message":"A requisição é inválida.","locale":"pt-BR","timestamp":"2026-11-27T14:10:52.528767588Z","path":"/api/v1/portability-operations","details":[]}
   [status 400 em 0.004534s]
   --- GET /api/v1/portability-operations?status=UNKNOWN ---
   {"code":"VALIDATION_ERROR","message":"A requisição é inválida.","locale":"pt-BR","timestamp":"2026-11-27T14:10:52.555392898Z","path":"/api/v1/portability-operations","details":[]}
   [status 400 em 0.019155s]

   === 10. a trilha da operacao, em ordem cronologica ===
    id  |     event_type      | previous_status | new_status |          created_at           |                                details
   -----+---------------------+-----------------+------------+-------------------------------+------------------------------------------------------------------------
    285 | OPERATION_CREATED   |                 | CREATED    | 2026-11-27 14:10:40.000927+00 | Operation created
    286 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-27 14:10:40.347019+00 | Central accepted the operation with protocol PRT-20261127-0001
    ... (16 POLL_ATTEMPT) ...
    309 | OPERATION_CONFIRMED | PENDING         | CONFIRMED  | 2026-11-27 14:10:49.768553+00 | The Central confirmed the operation
    310 | ACCOUNT_UPDATED     | CONFIRMED       | COMPLETED  | 2026-11-27 14:10:49.768553+00 | The receiving account of the operation was updated
    311 | OPERATION_COMPLETED | CONFIRMED       | COMPLETED  | 2026-11-27 14:10:49.768553+00 | The operation was completed
    ...
    318 | OPERATION_REJECTED  | PENDING         | REJECTED   | 2026-11-27 14:10:49.864348+00 | The Central rejected the operation: MARGIN_INSUFFICIENT
   (34 rows)

   === 11. encerramento dos processos e dos containers ===
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
4. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (40 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `application/portability/ListOperations.java` — a consulta paginada e filtrada pelo
  estado; a página carrega o total do conjunto, então quem chama sabe quantas páginas
  existem sem carregar todas.
- `application/portability/FindOperation.java` — o detalhe de uma operação; a operação
  inexistente vira `DomainError.OperationNotFound` com o código `OPERATION_NOT_FOUND`.
- `application/audit/ListOperationEvents.java` — a trilha da auditoria da operação, em
  ordem cronológica; a operação inexistente responde o mesmo `404`.
- `domain/shared/Page.java` — a página imutável com o total do conjunto e o número de
  páginas.
- `domain/portability/OperationFilter.java` — o filtro da consulta: todas as operações ou
  as de um estado; não existe `null` que signifique "sem filtro".
- `domain/portability/OperationSummary.java` e `OperationDetail.java` — os modelos de
  leitura da consulta.
- `adapters/out/persistence/JpaPortabilityOperationRepository.java` — as duas consultas
  (contagem e página), com a ordem estável `createdAt, id`.
- `adapters/in/web/PortabilityOperationResource.java` — `GET /portability-operations` e
  `GET /portability-operations/{id}`.
- `adapters/in/web/dto/` — `OperationSummaryResponse`, `OperationDetailResponse`,
  `OperationPageResponse`, `InstallmentSnapshotResponse` e `ReceivingAccountResponse`.
- `adapters/in/web/ApiExceptionHandler.java` — `OPERATION_NOT_FOUND` traduzido em `404`.
- Testes: 2 do `FindOperation`, 6 do `ListOperations`, 3 do `ListOperationEvents`, 6 do
  `Page`, 5 de integração da persistência (25 operações) e 9 da borda HTTP.

#### Descobertas e Decisões:
- O agregado `PortabilityOperation` não carrega o instante da última escrita da linha:
  `updated_at` pertence ao armazenamento, e o contrato público exige `updatedAt` no
  resumo e no detalhe. A consulta sai por **modelos de leitura** (`OperationSummary` e
  `OperationDetail`) em vez de forçar a coluna para dentro do agregado, que é a máquina
  de estados e não o bookkeeping da linha.
- A ordenação da página é `createdAt, id`: sem o desempate pelo identificador, duas
  operações criadas no mesmo instante poderiam trocar de lugar entre uma página e a
  outra, e o E2E leria a mesma operação duas vezes.
- `OPERATION_NOT_FOUND` já estava documentado no contrato e no `ARCHITECTURE.md` §8, mas
  o handler não o mapeava: a consulta de uma operação inexistente respondia `500`. Agora
  responde `404` com o código estável.
- Os parâmetros fora do alcance são recusados com `400 VALIDATION_ERROR`: página negativa,
  tamanho acima de 100 e estado que não existe.
- A rota REST da trilha (`GET /portability-operations/{id}/events`) fica para a task
  2.15, que entrega `OperationEventResource` junto com os recursos de autorização e de
  agenda: a trilha já está pronta na aplicação, lida em ordem cronológica.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/application/portability/FindOperation.java`
- `backend/src/main/java/br/com/consigport/application/portability/ListOperations.java`
- `backend/src/main/java/br/com/consigport/application/audit/ListOperationEvents.java`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationSummary.java`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationDetail.java`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationFilter.java`
- `backend/src/main/java/br/com/consigport/domain/portability/PortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/domain/shared/Page.java`
- `backend/src/main/java/br/com/consigport/domain/shared/DomainError.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaPortabilityOperationRepository.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/PortabilityOperationResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationSummaryResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationDetailResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationPageResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/InstallmentSnapshotResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ReceivingAccountResponse.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/test/java/br/com/consigport/application/portability/FindOperationTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/ListOperationsTest.java`
- `backend/src/test/java/br/com/consigport/application/audit/ListOperationEventsTest.java`
- `backend/src/test/java/br/com/consigport/domain/shared/SharedValueObjectsTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/InMemoryPortabilityOperationRepository.java`
- `backend/src/test/java/br/com/consigport/application/portability/StartOperationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/QueryPersistenceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/OperationQueryResourceTest.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.15 — Recursos REST e tratamento de erros

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/035-backend-rest-resources`
- **PR:** #47 — https://github.com/marcelinosandroni/consigport-platform/pull/47
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** completar a superfície `/api/v1` com o opt-in, a consulta do opt-in, a
  agenda e a trilha da auditoria, e fechar o tratamento de erros com o formato do
  contrato público (`code`, `message`, `locale`, `timestamp`, `path`, `details`) e os
  códigos EN estáveis.

#### Comandos de Verificação e Saídas:

1. **Recorte da task (caso de uso, adaptador da Central e borda HTTP):**
   ```text
   ./mvnw -B -pl backend test -Dtest='ApiExceptionHandlerTest,AuthorizationResourceTest,AgendaResourceTest,OperationEventResourceTest,RegisterAuthorizationTest,FindAuthorizationTest,AuthorizationRepositoryContractTest,CentralHttpClientTest'
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in AuthorizationRepositoryContractTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in FindAuthorizationTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in RegisterAuthorizationTest
   → Tests run: 23, Failures: 0, Errors: 0, Skipped: 0 -- in CentralHttpClientTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in AgendaResourceTest
   → Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 -- in ApiExceptionHandlerTest
   → Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in AuthorizationResourceTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in OperationEventResourceTest
   → Tests run: 59, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 357, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [01:27 min]
   → Central de Convênios Mock .......................... SUCCESS [ 22.659 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **End-to-end com o backend real contra o mock real da Central** (PostgreSQL 16 do
   compose, backend em `:8080`, mock em `:8081`; o mock confirma em 8 s; roteiro
   `/tmp/opencode/e2e-2.15.sh`, log `/tmp/opencode/e2e-2.15.log`):
   ```text
   === 0. ambiente ===
   data: 2026-11-27T20:34:36Z
    Container consigport-postgres Waiting 
    Container consigport-postgres Healthy 

   === 1. mock da Central em :8081 (confirmacao em 8 s) ===
   mock saudável em 9s

   === 1b. aquecimento do mock: a primeira chamada carrega as classes do JCE e do MVC ===
   POST /autorizacoes (aquecimento, BEN-WARMUP) -> 201 em 0.211683s
   POST /_admin/reset (devolve o mock ao estado inicial) -> 204

   === 2. limpeza das tabelas e backend em :8080 (JVM fria) ===
   TRUNCATE TABLE
   backend saudável em 17s

   === 3. o opt-in novo responde 201 Created ===
   --- POST /api/v1/authorizations (BEN-001 / AGR-001) ---
   {"authorizationId":"1ddf7b6c-7af9-4868-96ae-dc11dd3d2abb","beneficiaryId":"BEN-001","agreementId":"AGR-001","createdAt":"2026-11-27T20:35:10.553236409Z"}
   [status 201]

   === 4. o mesmo opt-in repetido responde 200 OK, sem duplicar ===
   --- POST /api/v1/authorizations (BEN-001 / AGR-001) de novo ---
   {"authorizationId":"1ddf7b6c-7af9-4868-96ae-dc11dd3d2abb","beneficiaryId":"BEN-001","agreementId":"AGR-001","createdAt":"2026-11-27T20:35:10.553236Z"}
   [status 200]
                     id                  | beneficiary_id | agreement_id |          created_at           
   --------------------------------------+----------------+--------------+-------------------------------
    1ddf7b6c-7af9-4868-96ae-dc11dd3d2abb | BEN-001        | AGR-001      | 2026-11-27 20:35:10.553236+00
   (1 row)


   === 5. a consulta do opt-in: 200 e 404 AUTHORIZATION_NOT_FOUND ===
   --- GET /api/v1/authorizations/BEN-001 ---
   {"authorizationId":"1ddf7b6c-7af9-4868-96ae-dc11dd3d2abb","beneficiaryId":"BEN-001","agreementId":"AGR-001","createdAt":"2026-11-27T20:35:10.553236Z"}
   [status 200]
   --- GET /api/v1/authorizations/BEN-404 ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Autorização não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T20:35:11.244584466Z","path":"/api/v1/authorizations/BEN-404","details":[]}
   [status 404]

   === 6. o opt-in sem o beneficiario: 400 VALIDATION_ERROR com detalhes ===
   --- POST /api/v1/authorizations sem beneficiaryId ---
   {"code":"VALIDATION_ERROR","message":"A requisição é inválida.","locale":"pt-BR","timestamp":"2026-11-27T20:35:11.320575647Z","path":"/api/v1/authorizations","details":[{"field":"beneficiaryId","message":"não deve estar em branco"}]}
   [status 400]

   === 7. a agenda do beneficiario autorizado: 200 com as parcelas ===
   --- GET /api/v1/beneficiaries/BEN-001/agenda ---
   {"beneficiaryId":"BEN-001","installments":[{"installmentId":"PAR-001","agreementId":"AGR-001","amount":450.00,"discountDate":"2026-12-05","status":"OPEN"},{"installmentId":"PAR-002","agreementId":"AGR-001","amount":300.00,"discountDate":"2027-01-05","status":"OPEN"},{"installmentId":"PAR-003","agreementId":"AGR-002","amount":1200.00,"discountDate":"2027-02-05","status":"OPEN"}]}
   [status 200]

   === 8. a agenda sem opt-in: 403 AUTHORIZATION_REQUIRED ===
   --- GET /api/v1/beneficiaries/BEN-MARGIN/agenda ---
   {"code":"AUTHORIZATION_REQUIRED","message":"É necessário registrar a autorização antes de consultar a agenda.","locale":"pt-BR","timestamp":"2026-11-27T20:35:13.255567786Z","path":"/api/v1/beneficiaries/BEN-MARGIN/agenda","details":[]}
   [status 403]

   === 9. a agenda de um beneficiario que a Central nao conhece: 404 BENEFICIARY_NOT_FOUND ===
   --- GET /api/v1/beneficiaries/BEN-404/agenda ---
   {"code":"BENEFICIARY_NOT_FOUND","message":"Beneficiário não encontrado.","locale":"pt-BR","timestamp":"2026-11-27T20:35:13.360934006Z","path":"/api/v1/beneficiaries/BEN-404/agenda","details":[]}
   [status 404]

   === 10. a operacao de portabilidade, para ter uma trilha ===
   POST /api/v1/portability-operations -> {"operationId":"e1cb7c4e-4cc8-494c-b66c-392c68d2d477","protocol":"PRT-20261127-0001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-27T20:35:13.638974Z"} | 202
   a operacao foi completada apos 5 ciclos de 2 s
   operacao: e1cb7c4e-4cc8-494c-b66c-392c68d2d477

   === 11. a trilha da operacao, em ordem cronologica ===
   --- GET /api/v1/portability-operations/e1cb7c4e-4cc8-494c-b66c-392c68d2d477/events ---
   {"operationId":"e1cb7c4e-4cc8-494c-b66c-392c68d2d477","events":[{"eventId":346,"previousStatus":null,"newStatus":"CREATED","eventType":"OPERATION_CREATED","details":"Operation created","rawPayload":null,"createdAt":"2026-11-27T20:35:13.638974Z"},{"eventId":347,"previousStatus":"CREATED","newStatus":"PENDING","eventType":"OPERATION_SUBMITTED","details":"Central accepted the operation with protocol PRT-20261127-0001","rawPayload":null,"createdAt":"2026-11-27T20:35:15.187142Z"},{"eventId":348,"previousStatus":"PENDING","newStatus":"PENDING","eventType":"POLL_ATTEMPT","details":"The Central is still processing the operation; the next poll is in 2 s","rawPayload":null,"createdAt":"2026-11-27T20:35:16.829442Z"},{"eventId":349,"previousStatus":"PENDING","newStatus":"PENDING","eventType":"POLL_ATTEMPT","details":"The Central is still processing the operation; the next poll is in 2 s","rawPayload":null,"createdAt":"2026-11-27T20:35:18.877088Z"},{"eventId":350,"previousStatus":"PENDING","newStatus":"PENDING","eventType":"POLL_ATTEMPT","details":"The Central is still processing the operation; the next poll is in 2 s","rawPayload":null,"createdAt":"2026-11-27T20:35:20.909524Z"},{"eventId":351,"previousStatus":"PENDING","newStatus":"PENDING","eventType":"POLL_ATTEMPT","details":"The Central is still processing the operation; the next poll is in 2 s","rawPayload":null,"createdAt":"2026-11-27T20:35:22.943375Z"},{"eventId":352,"previousStatus":"PENDING","newStatus":"CONFIRMED","eventType":"OPERATION_CONFIRMED","details":"The Central confirmed the operation","rawPayload":{"status":"CONFIRMADA","motivoRejeicao":null},"createdAt":"2026-11-27T20:35:24.977757Z"},{"eventId":353,"previousStatus":"CONFIRMED","newStatus":"COMPLETED","eventType":"ACCOUNT_UPDATED","details":"The receiving account of the operation was updated","rawPayload":null,"createdAt":"2026-11-27T20:35:24.977757Z"},{"eventId":354,"previousStatus":"CONFIRMED","newStatus":"COMPLETED","eventType":"OPERATION_COMPLETED","details":"The operation was completed","rawPayload":null,"createdAt":"2026-11-27T20:35:24.977757Z"}]}
   [status 200]

   === 12. a trilha de uma operacao que nao existe: 404 OPERATION_NOT_FOUND ===
   {"code":"OPERATION_NOT_FOUND","message":"Operação não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T20:35:26.084746844Z","path":"/api/v1/portability-operations/940bb99e-4beb-4657-b9a5-5be16020b59c/events","details":[]}
   [status 404]

   === 13. o erro publico tem os seis campos do contrato ===
   campos: ['code', 'details', 'locale', 'message', 'path', 'timestamp']
   code: AUTHORIZATION_NOT_FOUND
   message: Autorização não encontrada.
   locale: pt-BR
   timestamp: 2026-11-27T20:35:26.159549924Z
   path: /api/v1/authorizations/BEN-404
   details: []

   === 14. a Central recebeu a replica do opt-in ===
   autorizacoes na Central: {"AUT-20261127-0001": {"autorizacaoId": "AUT-20261127-0001", "beneficiarioId": "BEN-001", "convenioId": "AGR-001", "criadaEm": "2026-11-27T20:35:10.180556222Z"}}

   === 15. encerramento dos processos e dos containers ===
    Container consigport-postgres Removed 
    Network consigport-net Removing 
    Network consigport-net Removed 
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
4. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (41 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `adapters/in/web/AuthorizationResource.java` — `POST /api/v1/authorizations`
  (`201` no opt-in novo, `200` no opt-in repetido) e
  `GET /api/v1/authorizations/{beneficiaryId}` (`200` ou `404 AUTHORIZATION_NOT_FOUND`).
- `adapters/in/web/AgendaResource.java` — `GET /api/v1/beneficiaries/{beneficiaryId}/agenda`,
  com as parcelas no formato do contrato (`OPEN`/`CLOSED`).
- `adapters/in/web/OperationEventResource.java` — `GET /api/v1/portability-operations/{id}/events`,
  a trilha em ordem cronológica e o payload bruto da Central como objeto.
- `adapters/in/web/dto/` — `AuthorizationRequest`, `AuthorizationResponse`, `AgendaResponse`
  e `OperationEventsResponse`.
- `adapters/in/web/ApiExceptionHandler.java` — `AUTHORIZATION_NOT_FOUND` traduzido em `404`,
  tratador genérico para `500 INTERNAL_ERROR` com a causa apenas no log do servidor.
- `adapters/out/central/CentralHttpClient.java` — a quarta porta do domínio
  (`CentralAuthorizationPort`): replica o consentimento em `POST /autorizacoes`, assinado
  em HMAC, com o circuito e o tratamento de falha da Central.
- `adapters/out/central/dto/CentralAuthorizationRequest.java` e
  `CentralAuthorizationResponse.java` — o contrato PT da Central para o opt-in.
- `application/authorization/RegisterAuthorization.java` — devolve o registro e se foi
  criado agora, para o recurso distinguir `201` de `200`.
- `application/authorization/FindAuthorization.java` — a consulta do opt-in mais recente.
- `domain/authorization/AuthorizationRepository.java` e
  `adapters/out/persistence/JpaAuthorizationRepository.java` — a porta
  `findLatestByBeneficiary`, com a ordem estável `createdAt, id`.
- `domain/shared/DomainError.java` — o erro `AUTHORIZATION_NOT_FOUND`, com o
  beneficiário desconhecido.
- `config/CompositionRoot.java` — os beans `queryAgenda`, `registerAuthorization` e
  `findAuthorization`.
- Testes: 10 do handler (um caso por código estável), 5 do recurso de autorização,
  3 do recurso de agenda, 3 do recurso de trilha, 4 novos do cliente da Central e o
  fake do repositório de autorizações com a porta nova.

#### Descobertas e Decisões:
- O opt-in novo não tinha adaptador de produção: a porta `CentralAuthorizationPort` estava
  declarada no domínio e só existia como fake em memória. `CentralHttpClient` passou a
  responder a quarta porta, com o mesmo HMAC, o mesmo circuito e a mesma tradução de
  falha das outras chamadas.
- `RegisterAuthorization` devolvia apenas o registro: o recurso não conseguia distinguir o
  opt-in novo do repetido, e o contrato exige `201` e `200`. O caso de uso devolve agora o
  registro e o sinal de criação, sem mudar o que ele grava.
- `GET /authorizations/{beneficiaryId}` devolve **um** registro, como manda o contrato, e o
  backend pode ter um opt-in por convênio: a resposta é o mais recente (`createdAt`, depois
  `id` como desempate), nunca um registro arbitrário.
- `AUTHORIZATION_NOT_FOUND` não era mapeado: a consulta de um beneficiário sem opt-in
  respondia `500`. Agora responde `404` com o código estável do contrato.
- O `ApiExceptionHandler` não tinha tratador para as falhas não classificadas: elas
  escapavam do advice. Agora há um tratador genérico que devolve `500 INTERNAL_ERROR` no
  formato do contrato e registra a causa real no log do servidor.
- A primeira chamada do mock da Central leva ~8 s numa JVM fria (o `Mac.init` carrega as
  classes do JCE e a validação do corpo carrega o MVC), acima do timeout de leitura de 3 s
  do backend. O E2E aquece o mock com uma chamada assinada antes das asserções e zera o
  estado com `POST /_admin/reset`; com o mock aquecido, a primeira chamada real do backend
  responde em milissegundos. Diagnóstico: `jstack` do mock durante a chamada mostrou a
  thread do Tomcat em `javax.crypto.Mac.init` → `Provider.newInstanceOf`.
- `POST /api/v1/authorizations` responde `503 CENTRAL_UNAVAILABLE` quando a Central não
  registra o consentimento, mas o contrato público lista `400` e `500` para essa rota: a
  divergência está registrada como dívida da Fase 2, para alinhar contrato e mapeamento
  com a autorização do responsável.

#### Arquivos Tocados:
- `backend/src/main/java/br/com/consigport/adapters/in/web/AuthorizationResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/AgendaResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/OperationEventResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AuthorizationRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AuthorizationResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AgendaResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationEventsResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/CentralHttpClient.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralAuthorizationRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/out/central/dto/CentralAuthorizationResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/out/persistence/JpaAuthorizationRepository.java`
- `backend/src/main/java/br/com/consigport/application/authorization/RegisterAuthorization.java`
- `backend/src/main/java/br/com/consigport/application/authorization/FindAuthorization.java`
- `backend/src/main/java/br/com/consigport/domain/authorization/AuthorizationRepository.java`
- `backend/src/main/java/br/com/consigport/domain/shared/DomainError.java`
- `backend/src/main/java/br/com/consigport/config/CompositionRoot.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/AuthorizationResourceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/AgendaResourceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/OperationEventResourceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/ApiExceptionHandlerTest.java`
- `backend/src/test/java/br/com/consigport/adapters/out/central/CentralHttpClientTest.java`
- `backend/src/test/java/br/com/consigport/application/authorization/RegisterAuthorizationTest.java`
- `backend/src/test/java/br/com/consigport/domain/authorization/InMemoryAuthorizationRepository.java`
- `backend/src/test/java/br/com/consigport/support/FixedClockConfig.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.16 — i18n, Swagger e snapshot OpenAPI

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/036-backend-i18n-openapi`
- **PR:** #48 — https://github.com/marcelinosandroni/consigport-platform/pull/48
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** localizar as mensagens humanas pela negociação do `Accept-Language` (com
  fallback `pt-BR` e o código do erro nunca traduzido), publicar a documentação viva da
  API em `/v3/api-docs` e no Swagger UI, e provar que o contrato gerado é equivalente ao
  snapshot público `contracts/consigport-api.yaml`.

#### Comandos de Verificação e Saídas:

1. **Recorte da task (negociação de locale, handler e contrato gerado):**
   ```text
   ./mvnw -B -pl backend test -Dtest='ApiExceptionHandlerTest,LocaleNegotiationTest,GeneratedContractTest,AuthorizationResourceTest,AgendaResourceTest,OperationEventResourceTest'
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in AgendaResourceTest
   → Tests run: 23, Failures: 0, Errors: 0, Skipped: 0 -- in ApiExceptionHandlerTest
   → Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in AuthorizationResourceTest
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- in LocaleNegotiationTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in OperationEventResourceTest
   → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in GeneratedContractTest
   → Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Reator completo (backend + mock da Central):**
   ```text
   ./mvnw -B verify
   → Tests run: 379, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.002 s]
   → ConsigPort Backend ................................. SUCCESS [01:07 min]
   → Central de Convênios Mock .......................... SUCCESS [ 27.037 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
3. **Prova de vida do teste de contrato (quebra proposital):** a anotação do `404` de
   `GET /api/v1/authorizations/{beneficiaryId}` foi removida do recurso e o teste ficou
   vermelho, apontando a diferença; com a anotação restaurada, o teste voltou ao verde.
   ```text
   (quebra aplicada: a resposta 404 saiu do contrato gerado)
   → Tests run: 1, Failures: 1, Errors: 0, Skipped: 0 -- in GeneratedContractTest
   → Expecting empty but was: ["GET /api/v1/authorizations/{beneficiaryId}: the answer 404 is missing"]
   → BUILD FAILURE
   (anotação restaurada)
   → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in GeneratedContractTest
   → BUILD SUCCESS
   ```
4. **End-to-end com o backend real** (PostgreSQL 16 do compose, backend em `:8080`;
   roteiro `/tmp/opencode/e2e-2.16.sh`, log `/tmp/opencode/e2e-2.16.log`):
   ```text
   === 0. ambiente ===
   data: 2026-11-27T22:46:14Z
    Container consigport-postgres Waiting
    Container consigport-postgres Healthy

   === 1. backend em :8080 (JVM fria) ===
   backend saudável em 15s

   === 2. a mensagem segue o Accept-Language, o codigo nao muda ===
   --- Accept-Language: en ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Authorization not found.","locale":"en","timestamp":"2026-11-27T22:46:36.318506170Z","path":"/api/v1/authorizations/BEN-404","details":[]}
   --- Accept-Language: es ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Autorización no encontrada.","locale":"es","timestamp":"2026-11-27T22:46:36.380956282Z","path":"/api/v1/authorizations/BEN-404","details":[]}
   --- Accept-Language: pt-BR ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Autorização não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T22:46:36.405976813Z","path":"/api/v1/authorizations/BEN-404","details":[]}
   --- Accept-Language: fr ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Autorização não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T22:46:36.428150147Z","path":"/api/v1/authorizations/BEN-404","details":[]}
   --- sem Accept-Language (fallback pt-BR) ---
   {"code":"AUTHORIZATION_NOT_FOUND","message":"Autorização não encontrada.","locale":"pt-BR","timestamp":"2026-11-27T22:46:36.449335506Z","path":"/api/v1/authorizations/BEN-404","details":[]}

   === 3. o detalhe da validacao tambem vem no idioma do chamador ===
   --- POST sem beneficiaryId, Accept-Language: en ---
   {"code":"VALIDATION_ERROR","message":"The request is invalid.","locale":"en","timestamp":"2026-11-27T22:46:36.627462138Z","path":"/api/v1/authorizations","details":[{"field":"beneficiaryId","message":"must not be blank"}]}
   --- POST sem beneficiaryId, Accept-Language: es ---
   {"code":"VALIDATION_ERROR","message":"La solicitud no es válida.","locale":"es","timestamp":"2026-11-27T22:46:36.652625071Z","path":"/api/v1/authorizations","details":[{"field":"beneficiaryId","message":"no debe estar en blanco"}]}

   === 4. o contrato publicado pelo backend ===
   openapi: 3.1.0
   info: ConsigPort API 1.0.0
   GET   /api/v1/portability-operations                   operationId=listPortabilityOperations  params=['status', 'page', 'size', 'Accept-Language'] responses=['200', '400', '500']
   POST  /api/v1/portability-operations                   operationId=startPortabilityOperation  params=['Idempotency-Key', 'Accept-Language'] responses=['202', '400', '409', '422', '500', '503']
   POST  /api/v1/authorizations                           operationId=registerAuthorization      params=['Accept-Language'] responses=['200', '201', '400', '500']
   GET   /api/v1/portability-operations/{operationId}     operationId=findPortabilityOperation   params=['operationId', 'Accept-Language'] responses=['200', '404', '500']
   GET   /api/v1/portability-operations/{operationId}/events operationId=listOperationEvents        params=['operationId', 'Accept-Language'] responses=['200', '404', '500']
   GET   /api/v1/beneficiaries/{beneficiaryId}/agenda     operationId=getBeneficiaryAgenda       params=['beneficiaryId', 'Accept-Language'] responses=['200', '403', '404', '500']
   GET   /api/v1/authorizations/{beneficiaryId}           operationId=findAuthorization          params=['beneficiaryId', 'Accept-Language'] responses=['200', '404', '500']
   schemas: ['AgendaResponse', 'Authorization', 'AuthorizationRequest', 'ErrorDetail', 'ErrorResponse', 'Installment', 'InstallmentSnapshot', 'OperationAcceptedResponse', 'OperationDetail', 'OperationEvent', 'OperationEventsResponse', 'OperationPage', 'OperationSummary', 'PortabilityOperationRequest', 'ReceivingAccount']

   === 5. o Swagger UI ===
   GET /swagger-ui.html -> 302 (http://localhost:8080/swagger-ui/index.html)
   GET /swagger-ui/index.html -> 200

   === 6. encerramento dos processos e dos containers ===
    Network consigport-net Removing
    Network consigport-net Removed
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
5. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (42 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `backend/src/main/resources/i18n/messages_pt_BR.properties`, `messages_en.properties` e
  `messages_es.properties` — os catálogos das mensagens humanas; a chave de cada
  entrada é o código estável do erro, e as mensagens da Bean Validation têm chave
  `jakarta.validation.constraints.*.message`.
- `backend/src/main/java/br/com/consigport/config/MessageSourceConfig.java` — o
  `MessageSource` (basename `i18n/messages`, UTF-8, `fallbackToSystemLocale=false`), o
  `AcceptHeaderLocaleResolver` (padrão `pt-BR`, idiomas `pt-BR`/`en`/`es`) e o
  `LocalValidatorFactoryBean` ligado aos mesmos catálogos.
- `backend/src/main/java/br/com/consigport/config/OpenApiConfig.java` — a informação da
  API (título `ConsigPort API`, versão `1.0.0`, contato, licença MIT, servidor local, quatro
  tags) e o `OpenApiCustomizer` que injeta o header `Accept-Language` em todas as
  operações.
- `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java` — o
  mapa fixo de mensagens pt-BR foi removido: a mensagem vem do catálogo, resolvida pelo
  código e pelo locale negociado.
- `backend/src/main/java/br/com/consigport/adapters/in/web/AuthorizationResource.java`,
  `AgendaResource.java`, `OperationEventResource.java` e `PortabilityOperationResource.java`
  — `@Tag`, `@Operation` com o `operationId` do contrato público, `@ApiResponse` por
  status, `Idempotency-Key` publicado como obrigatório e o header `Idempotent-Replay` no
  `202`.
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/` — `@Schema(name=...)` e
  `@Schema(requiredProperties=...)` em todos os DTOs; `ReceivingAccountRequest` e
  `ReceivingAccountResponse` colapsados em `ReceivingAccount`, como manda o contrato.
- `backend/pom.xml` — a dependência `jackson-dataformat-yaml`, que o springdoc usa para
  ler o YAML do contrato e que o teste de contrato usa para ler o snapshot.
- Testes: `LocaleNegotiationTest` (8 casos: `en`, `es`, `pt-BR`, `pt`, `en-US,en;q=0.9`,
  `fr`, sem header, e o detalhe da validação localizado),
  `ApiExceptionHandlerTest` (23 casos: cada código estável tem mensagem em cada locale),
  `GeneratedContractTest` com o `ContractComparator` (comparação canônica do contrato
  gerado com o snapshot).

#### Descobertas e Decisões:
- O `ApiExceptionHandler` tinha as mensagens em um mapa fixo, em pt-BR: o `locale` do
  erro era negociado, mas a mensagem nunca mudava. Agora a chave do catálogo é o
  próprio código, e a mensagem sai do catálogo do locale negociado.
- A validação de jakarta não negociava o locale: as mensagens vinham no idioma do
  locale da JVM. O `LocalValidatorFactoryBean` ligado ao `MessageSource` faz o detalhe
  da falha de validação viajar no idioma do chamador.
- `AcceptHeaderLocaleResolver` com a lista de idiomas suportados faz o fallback para
  `pt-BR` quando o chamador pede um idioma que a API não fala (`fr`), e também quando o
  header é `pt` sem região.
- `@Schema(requiredMode=REQUIRED)` no nível do tipo **não** propaga para as
  propriedades; `@Schema(requiredProperties={...})` no tipo **gera** o array `required`
  do componente. A regra foi verificada empiricamente antes de ser aplicada nos DTOs.
- O `@Schema(name=...)` no tipo foi verificado empiricamente: `AuthorizationResponse` passa
  a ser o componente `Authorization` do contrato.
- O contrato público publica **um** schema para a conta de recebimento
  (`ReceivingAccount`), enquanto o backend tinha dois DTOs (`ReceivingAccountRequest` e
  `ReceivingAccountResponse`) com os mesmos campos. Os dois foram colapsados em um só
  DTO, usado nos dois sentidos.
- O `Idempotency-Key` era publicado com `required: false`, porque o parâmetro do Spring
  é opcional (`required = false`, para devolver `400 IDEMPOTENCY_KEY_REQUIRED` no
  formato do contrato). O contrato público exige o header: a anotação `@Parameter` o
  publica como obrigatório, sem trocar o comportamento do servidor.
- O springdoc não sobe sem `jackson-dataformat-yaml` no classpath: o bean `OpenAPI`
  declarava a dependência do YAML e o backend falhava ao subir. A dependência foi
  declarada explicitamente no `backend/pom.xml`.
- O comparador do contrato precisa resolver as referências **de cada documento no seu
  próprio `components`**: resolver a referência de um documento no `components` do outro
  produzia diferenças falsas (o `beneficiaryId` do contrato, por exemplo, era lido como
  um parâmetro vazio).
- A comparação canônica ignora o que não muda o contrato do consumidor: descrições,
  exemplos, `format`, limites numéricos e o nome dos componentes. O que compara é rota,
  método, `operationId`, parâmetros (nome, lugar, obrigatoriedade), status, corpo
  obrigatório e o shape do schema (propriedades, tipos, enums e obrigatoriedade).
- O contrato público e o `SDD/ARCHITECTURE.md` **não** foram alterados: esta task toca
  só o backend, e o snapshot é a fonte de verdade da comparação.

#### Arquivos Tocados:
- `backend/pom.xml`
- `backend/src/main/java/br/com/consigport/config/MessageSourceConfig.java`
- `backend/src/main/java/br/com/consigport/config/OpenApiConfig.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/AuthorizationResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/AgendaResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/OperationEventResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/PortabilityOperationResource.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AuthorizationResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AgendaResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ErrorDetail.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ErrorResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/InstallmentSnapshotResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationAcceptedResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationDetailResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationEventsResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationPageResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/OperationSummaryResponse.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/PortabilityOperationRequest.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ReceivingAccount.java`
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ReceivingAccountRequest.java` (removido)
- `backend/src/main/java/br/com/consigport/adapters/in/web/dto/ReceivingAccountResponse.java` (removido)
- `backend/src/main/resources/i18n/messages_pt_BR.properties`
- `backend/src/main/resources/i18n/messages_en.properties`
- `backend/src/main/resources/i18n/messages_es.properties`
- `backend/src/test/java/br/com/consigport/adapters/in/web/LocaleNegotiationTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/ApiExceptionHandlerTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/AuthorizationResourceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/AgendaResourceTest.java`
- `backend/src/test/java/br/com/consigport/adapters/in/web/OperationEventResourceTest.java`
- `backend/src/test/java/br/com/consigport/contracts/GeneratedContractTest.java`
- `backend/src/test/java/br/com/consigport/contracts/ContractComparator.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.17 — Gate de cobertura do núcleo

- **Data de Conclusão:** 2026-11-27
- **Branch:** `feat/037-backend-coverage-gate`
- **PR:** #49 — https://github.com/marcelinosandroni/consigport-platform/pull/49
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** ativar o portão de cobertura do backend sobre o núcleo (domínio e casos de
  uso), cobrar **80% de linhas e de branches** como manda o portão 3.2 do
  `docs/quality/GATES.md`, fechar as lacunas de branch e publicar o relatório no CI.

#### Comandos de Verificação e Saídas:

1. **Medição da cobertura antes das correções** (o JaCoCo foi ativado no módulo e o
   portão cobrou 80% de branches):
   ```text
   ./mvnw -B -pl backend -am verify
   → Tests run: 379, Failures: 0, Errors: 0, Skipped: 0
   → --- jacoco:0.8.12:report (report) @ consigport-backend ---
   → --- jacoco:0.8.12:check (check) @ consigport-backend ---
   → [WARNING] Rule violated for bundle consigport-backend: branches covered ratio is 0.74, but expected minimum is 0.80
   → [ERROR] ConsigPort Backend ................................. FAILURE [01:34 min]
   → [ERROR] BUILD FAILURE
   → EXIT=1
   ```
   Relatório do bundle (`backend/target/site/jacoco/jacoco.xml`):
   ```text
   INSTRUCTION missed= 340 covered=2687 ratio=88.77%
   BRANCH     missed=  87 covered= 256 ratio=74.64%
   LINE       missed=  71 covered= 621 ratio=89.74%
   CLASS      missed=   0 covered=  59 ratio=100.00%
   ```
   Lacunas por classe (branches perdidos):
   ```text
   domain/portability.OperationDetail     23 perdidos de 42 (45,2%)
   domain/portability.PortabilityOperation 20 perdidos de 70 (71,4%)
   domain/portability.OperationRequest     15 perdidos de 32 (53,1%)
   domain/portability.OperationSummary      6 perdidos de 12 (50,0%)
   domain/agenda.Installment                5 perdidos de 10 (50,0%)
   domain/shared.Page                       2 perdidos de 16 (87,5%)
   domain/portability.PortabilityOperation$OperationId  2 perdidos de 6
   domain/portability.CentralSubmissionException        2 perdidos de 4
   domain/portability.CentralStatus$Pending             2 perdidos de 4
   domain/portability.CentralStatus$NotFound             2 perdidos de 4
   domain/agenda.BeneficiaryAgenda            2 perdidos de 6
   application/portability.IntegrationBackoff 2 perdidos de 14
   domain/portability.RejectionReason         1 perdido de 7
   domain/portability.OperationFilter$WithStatus 1 perdido de 2
   domain/portability.CentralStatus$Rejected   1 perdido de 2
   domain/audit.OperationEvent                1 perdido de 16
   ```
2. **Reator completo depois das correções:**
   ```text
   ./mvnw -B verify
   → Tests run: 478, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.003 s]
   → ConsigPort Backend ................................. SUCCESS [01:36 min]
   → Central de Convênios Mock .......................... SUCCESS [ 21.095 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
   Cobertura final do núcleo (domínio + aplicação):
   ```text
   INSTRUCTION   98.58%  (2984 de 3027)
   BRANCH        99.71%  (342 de 343)
   LINE          98.12%  (679 de 692)
   CLASS        100.00%  (59 de 59)
   ```
3. **Prova de vida do portão (limiar artificialmente inalcançável):** o limiar de branches
   foi elevado para `1.00` e o build ficou vermelho, com a regra apontada; com o limiar
   restaurado em `0.80`, o build voltou ao verde.
   ```text
   (limiar de branches elevado para 1.00)
   → [WARNING] Rule violated for bundle consigport-backend: branches covered ratio is 0.99, but expected minimum is 1.00
   → [ERROR] ConsigPort Backend ................................. FAILURE [  6.293 s]
   → [ERROR] BUILD FAILURE
   → EXIT=1
   (limiar restaurado em 0.80)
   → BUILD SUCCESS
   → EXIT=0
   ```
4. **Testes novos do núcleo** (recorte da task):
   ```text
   ./mvnw -B -pl backend test -Dtest='OperationReadModelsTest,OperationRequestTest,AgendaReadModelsTest,CentralStatusTest,PortabilityOperationTest,SharedValueObjectsTest,OperationEventTest'
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$FilterTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$SubmissionFailureTest
   → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$ConfirmedTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$NotFoundTest
   → Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$RejectedTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in CentralStatusTest$PendingTest
   → Tests run: 18, Failures: 0, Errors: 0, Skipped: 0 -- in OperationReadModelsTest$DetailTest
   → Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in OperationReadModelsTest$SummaryTest
   → Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- in OperationRequestTest$MatchesTest
   → Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 -- in OperationRequestTest$InvariantsTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in PortabilityOperationTest$OperationIdTest
   → Tests run: 18, Failures: 0, Errors: 0, Skipped: 0 -- in PortabilityOperationTest$Creation
   → Tests run: 52, Failures: 0, Errors: 0, Skipped: 0 -- in PortabilityOperationTest
   → Tests run: 9, Failures: 0, Errors: 0, Skipped: 0 -- in SharedValueObjectsTest$PageTest
   → Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- in OperationEventTest
   → Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in AgendaReadModelsTest$BeneficiaryAgendaTest
   → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in AgendaReadModelsTest$InstallmentTest
   → BUILD SUCCESS
   ```
5. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (exatamente 1 task em voo, tasks catalogadas, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (43 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `pom.xml` — a propriedade `jacoco.branch.coverage` (0,80, ao lado da
  `jacoco.line.coverage`), a configuração do `pluginManagement` do JaCoCo com os
  `includes` do núcleo (`br/com/consigport/domain/**` e
  `br/com/consigport/application/**`) e o segundo limite da regra (`BRANCH` com
  `COVEREDRATIO`).
- `backend/pom.xml` — a ativação do plugin JaCoCo no módulo, que passa a instrumentar
  os testes, publicar o relatório em `backend/target/site/jacoco` e falhar o build quando
  o portão é descumprido.
- `.github/workflows/ci.yml` — o job `backend` publica o relatório como o artefato
  `backend-coverage`.
- `backend/src/main/java/br/com/consigport/domain/portability/OperationRequest.java` —
  a recusa da parcela nula vem **antes** da cópia defensiva da lista.
- Testes: `OperationReadModelsTest` (25 casos: cada invariante do resumo e do detalhe),
  `OperationRequestTest` (18 casos: os invariantes da seleção e a comparação de
  idempotência), `AgendaReadModelsTest` (10 casos: a parcela e a agenda), `CentralStatusTest`
  (13 casos: os quatro status da Central, a falha da submissão e o filtro), além das
  ampliações em `PortabilityOperationTest`, `SharedValueObjectsTest`, `OperationEventTest`,
  `RejectionReasonTest` e `IntegrationBackoffTest`.

#### Descobertas e Decisões:
- O JaCoCo já estava declarado no `pom.xml` raiz (com `pluginManagement`, a propriedade
  `jacoco.line.coverage` e as execuções `prepare-agent`, `report` e `check`), mas
  **nenhum módulo declarava o plugin**: o portão de cobertura existia no papel e não
  existia no build.
- O portão do `GATES.md` §3.2 exige 80% de **linhas e de branches**; a raiz só cobrava
  linhas. O segundo limite foi adicionado, com a propriedade própria
  `jacoco.branch.coverage`.
- O `GATES.md` §3.2 restringe a cobertura ao domínio e aos casos de uso: a medição sem
  `includes` contaria os adaptadores, a composição e a borda HTTP, o que tornaria o
  portão um número sem relação com o núcleo. Os `includes` ficaram na raiz, e o módulo
  `backend` é o único que declara o plugin.
- A lacuna de branch não era feita de código morto: eram os invariantes recusados pelo
  domínio que nenhum teste exercitava — os read models (`OperationDetail`,
  `OperationSummary`), a seleção do pedido, a parcela da agenda, os status da Central e
  o construtor do agregado. Os testes novos exercitam cada recusa, e a cobertura subiu
  de 74,64% para 99,71% de branches.
- **Defeito encontrado pelo portão:** a recusa da parcela nula em `OperationRequest` era
  código morto. `List.copyOf` recusa um elemento nulo com `NullPointerException`, e a
  cópia vinha **antes** da checagem: o invariante declarado pelo domínio
  (`installmentIds must not contain null`) nunca podia ser disparado, e o chamador
  recebia uma `NullPointerException` em vez da `IllegalArgumentException` do contrato do
  domínio. A recusa agora vem antes da cópia.
- O portão 3.2 do `GATES.md` também exige `./mvnw -q spotless:check`, mas nenhum `pom.xml`
  declara o plugin Spotless: o comando falha com "no such goal". Ativar o Spotless exige
  formatar o código existente, o que produz um diff largo que deve ficar em task
  própria. A dívida está registrada no `BACKLOG.md`, nas dívidas da Fase 2.
- A task 2.17 foi dividida em duas antes de começar, como manda a regra de tamanho: o
  portão de cobertura com os testes do núcleo virou a 2.17, e a prova do ciclo de vida
  completo com o PostgreSQL real virou a 2.18.

#### Arquivos Tocados:
- `pom.xml`
- `backend/pom.xml`
- `.github/workflows/ci.yml`
- `backend/src/main/java/br/com/consigport/domain/portability/OperationRequest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/OperationReadModelsTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/OperationRequestTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/CentralStatusTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/PortabilityOperationTest.java`
- `backend/src/test/java/br/com/consigport/domain/portability/RejectionReasonTest.java`
- `backend/src/test/java/br/com/consigport/domain/agenda/AgendaReadModelsTest.java`
- `backend/src/test/java/br/com/consigport/domain/shared/SharedValueObjectsTest.java`
- `backend/src/test/java/br/com/consigport/domain/audit/OperationEventTest.java`
- `backend/src/test/java/br/com/consigport/application/portability/IntegrationBackoffTest.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

### Task 2.18 — Fluxo completo com o banco real

- **Data de Conclusão:** 2026-11-28
- **Branch:** `feat/038-backend-lifecycle-integration`
- **PR:** #50 — https://github.com/marcelinosandroni/consigport-platform/pull/50
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** provar o ciclo de vida inteiro de uma operação de portabilidade contra o
  PostgreSQL 16 real — `CREATED` → `PENDING` → `CONFIRMED` → `COMPLETED` com a conta
  atualizada e a trilha completa, `CREATED` → `PENDING` → `REJECTED` com o motivo e a
  trilha, e o rollback quando o registro do evento falha.

#### Comandos de Verificação e Saídas:

1. **Teste de integração do ciclo de vida (PostgreSQL 16 do Testcontainers):**
   ```text
   ./mvnw -B -pl backend test -Dtest='OperationLifecyclePersistenceTest'
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 32.56 s -- in br.com.consigport.application.portability.OperationLifecyclePersistenceTest
   → Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
   → BUILD SUCCESS
   → EXIT=0
   ```
2. **Prova de vida do rollback (a transação do runner removida de propósito):** sem a
   fronteira transacional, a operação fica `COMPLETED` mesmo com a trilha quebrada no
   meio — e o teste fica vermelho, provando que a asserção do rollback é real.
   ```text
   (removido @Transactional de SpringTransactionRunner.run)
   → [ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0
   → [ERROR] OperationLifecyclePersistenceTest.rollsBackTheClosureWhenTheTrailFailsBetweenTheSteps -- Time elapsed: 0.098 s <<< FAILURE!
   → expected: PENDING
   →  but was: COMPLETED
   → expected: PENDING
   →  but was: COMPLETED
   → [ERROR] BUILD FAILURE
   → EXIT=1
   (código de produção restaurado)
   ```
3. **Prova de vida da trilha completa (o evento `ACCOUNT_UPDATED` removido de
   propósito):** a trilha cai para cinco eventos e o teste fica vermelho.
   ```text
   (removido accountUpdatedEvent de CompleteOperation.confirm)
   → [ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0
   → [ERROR] OperationLifecyclePersistenceTest.closesTheOperationAndTheWholeTrailWhenTheCentralConfirmsIt -- Time elapsed: 2.314 s <<< FAILURE!
   →     ACCOUNT_UPDATED,
   →   [ACCOUNT_UPDATED]
   → [ERROR] BUILD FAILURE
   → EXIT=1
   (código de produção restaurado)
   ```
4. **Reator completo depois da task:**
   ```text
   ./mvnw -B verify
   → Tests run: 481, Failures: 0, Errors: 0, Skipped: 0   (backend)
   → Tests run: 39, Failures: 0, Errors: 0, Skipped: 0    (central-convenios-mock)
   → ConsigPort Platform ................................ SUCCESS [  0.005 s]
   → ConsigPort Backend ................................. SUCCESS [01:29 min]
   → Central de Convênios Mock .......................... SUCCESS [ 29.468 s]
   → BUILD SUCCESS
   → EXIT=0
   ```
   Cobertura do núcleo (domínio + aplicação), inalterada pela task:
   ```text
   BRANCH    99.71%  (342 de 343)
   LINE      98.12%  (679 de 692)
   CLASS    100.00%  (59 de 59)
   ```
5. **E2E real com o PostgreSQL 16 e o mock da Central de verdade** (backend e mock
   empacotados, banco do `compose.yaml`, script `/tmp/opencode/e2e-2.18.sh`):
   ```text
   === 1. mock da Central em :8081 (confirmacao em 8 s) ===
   mock saudável em 9s

   === 2. aquecimento da JVM do mock (JCE) e limpeza do estado ===
   POST /autorizacoes BEN-WARMUP -> {"autorizacaoId":"AUT-20261128-0001"} | 201 0.214232
   POST /_admin/reset -> 204

   === 4. o caminho feliz: CREATED -> PENDING -> CONFIRMED -> COMPLETED ===
   POST /autorizacoes BEN-001 -> {"autorizacaoId":"AUT-20261128-0001"} | 201 0.240969
   POST /api/v1/portability-operations -> {"operationId":"d1e6621e-b9b1-4b59-8bf4-caa912435523","protocol":"PRT-20261128-0001","status":"PENDING","totalAmount":450.00,"createdAt":"2026-11-28T00:56:47.436124Z"} | 202 2.469991
   a operacao foi completada apos 5 ciclos de 2 s
   --- a operacao no PostgreSQL real: o status final e o instante da conta ---
                     id                  |  status   |     protocol      | rejection_reason | retry_count |      account_updated_at       | total_amount
    d1e6621e-b9b1-4b59-8bf4-caa912435523 | COMPLETED | PRT-20261128-0001 |                  |           0 | 2026-11-28 00:56:57.745059+00 |       450.00
   --- a trilha completa da operacao confirmada ---
    id  |     event_type      | previous_status | new_status |          created_at           |                                details
   -----+---------------------+-----------------+------------+-------------------------------+------------------------------------------------------------------------
    371 | OPERATION_CREATED   |                 | CREATED    | 2026-11-28 00:56:47.436124+00 | Operation created
    372 | OPERATION_SUBMITTED | CREATED         | PENDING    | 2026-11-28 00:56:48.399149+00 | Central accepted the operation with protocol PRT-20261128-0001
    373 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-28 00:56:49.446326+00 | The Central is still processing the operation; the next poll is in 2 s
    374 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-28 00:56:51.633607+00 | The Central is still processing the operation; the next poll is in 2 s
    375 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-28 00:56:53.680037+00 | The Central is still processing the operation; the next poll is in 2 s
    376 | POLL_ATTEMPT        | PENDING         | PENDING    | 2026-11-28 00:56:55.709646+00 | The Central is still processing the operation; the next poll is in 2 s
    377 | OPERATION_CONFIRMED | PENDING         | CONFIRMED  | 2026-11-28 00:56:57.745059+00 | The Central confirmed the operation
    378 | ACCOUNT_UPDATED     | CONFIRMED       | COMPLETED  | 2026-11-28 00:56:57.745059+00 | The receiving account of the operation was updated
    379 | OPERATION_COMPLETED | CONFIRMED       | COMPLETED  | 2026-11-28 00:56:57.745059+00 | The operation was completed
   (9 rows)

   === 5. o caminho da rejeicao: CREATED -> PENDING -> REJECTED com o motivo ===
   POST /autorizacoes BEN-REJ -> {"autorizacaoId":"AUT-20261128-0002"} | 201 0.014338
   POST /api/v1/portability-operations -> {"operationId":"d813531d-0364-41fc-9b65-bbb5b7a2d140","protocol":"PRT-20261128-0002","status":"PENDING","totalAmount":250.00,"createdAt":"2026-11-28T00:56:59.742308Z"} | 202 0.059400
   a operacao foi encerrada como REJECTED apos 4 ciclos de 2 s
   --- as duas operacoes no PostgreSQL real ---
    d1e6621e-b9b1-4b59-8bf4-caa912435523 | COMPLETED | PRT-20261128-0001 |                     |           0 | 2026-11-28 00:56:57.745059+00 |       450.00
    d813531d-0364-41fc-9b65-bbb5b7a2d140 | REJECTED  | PRT-20261128-0002 | MARGIN_INSUFFICIENT |           0 |                               |       250.00
   --- a trilha da rejeicao ---
    id  |     event_type      | previous_status | new_status |                                details
   -----+---------------------+-----------------+------------+---------------------------------------------------------
    380 | OPERATION_CREATED   |                 | CREATED    | Operation created
    381 | OPERATION_SUBMITTED | CREATED         | PENDING    | Central accepted the operation with protocol PRT-20261128-0002
    382 | POLL_ATTEMPT        | PENDING         | PENDING    | The Central is still processing the operation; the next poll is in 2 s
    383 | POLL_ATTEMPT        | PENDING         | PENDING    | The Central is still processing the operation; the next poll is in 2 s
    384 | POLL_ATTEMPT        | PENDING         | PENDING    | The Central is still processing the operation; the next poll is in 2 s
    385 | OPERATION_REJECTED  | PENDING         | REJECTED   | The Central rejected the operation: MARGIN_INSUFFICIENT
   (6 rows)
   --- a operacao rejeitada pela API publica ---
   {"operationId":"d813531d-0364-41fc-9b65-bbb5b7a2d140","protocol":"PRT-20261128-0002","beneficiaryId":"BEN-REJ","status":"REJECTED","totalAmount":250.00,"createdAt":"2026-11-28T00:56:59.742308Z","updatedAt":"2026-11-28T00:57:07.882449Z","agreementId":"AGR-003","installments":[{"installmentId":"PAR-101","agreementId":"AGR-003","amount":250.00,"discountDate":"2026-12-15"}],"assigneeCreditor":"ConsigPort","receivingAccount":{"bank":"001","branch":"1234","account":"56789-0"},"rejectionReason":"MARGIN_INSUFFICIENT","retryCount":0,"accountUpdatedAt":null}
   -> HTTP 200

   === 7. encerramento dos processos e dos containers ===
   containers restantes: 0
   processos restantes: 0
   === fim ===
   ```
6. **Gates locais:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → OK (65 documentos, todo link relativo resolve, exit 0)
   node scripts/check-specs.mjs                → OK (tasks catalogadas, no máximo 1 task em voo, exit 0)
   node scripts/check-architecture.mjs         → OK (22 testes de validação dos desenhos, exit 0)
   node scripts/check-theme-tokens.mjs         → OK (exit 0)
   node scripts/check-ai-log.mjs               → OK (44 sessões, estrutura válida, exit 0)
   ```

#### Conteúdo Entregue:
- `backend/src/test/java/br/com/consigport/application/portability/OperationLifecyclePersistenceTest.java`
  — o teste de integração do ciclo de vida: o `@SpringBootTest` sobe o contexto real
  (adaptadores JPA, transações Spring e migração do Flyway) sobre o PostgreSQL 16 do
  Testcontainers, e os casos de uso `StartOperation`, `PollOperation` e
  `CompleteOperation` conduzem o cenário com a Central simulada.
- `backend/src/test/java/br/com/consigport/support/FailingOperationEventRepository.java`
  — o duplo que falha no enésimo append, extraído da classe de teste que o usava e
  agora compartilhado.
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/CompletionPersistenceTest.java`
  — passa a usar o duplo compartilhado (a duplicação saiu).

#### Descobertas e Decisões:
- O ciclo de vida já estava coberto **em partes**: `CompletionPersistenceTest` provava a
  confirmação a partir de `PENDING` e `PollingPersistenceTest` provava o worker, mas
  **nenhum teste percorria o caminho inteiro** com uma operação criada pelo caso de uso
  e fechada pela Central. A lacuna era real: uma quebra entre a criação e o envio, ou
  entre o envio e o primeiro poll, não seria detectada por nenhum teste.
- O teste de integração fica no pacote `application/portability`, e não em
  `adapters/out/persistence`: o que se prova é o **caso de uso** contra o banco real, e
  não o adaptador isolado. A única peça simulada é a Central, porque a regra dos testes
  proíbe chamar uma API de terceiro num teste de integração; o PostgreSQL é a peça real.
- **O rollback só existe por causa da fronteira transacional.** A prova de vida
  mostrou que, sem o `@Transactional` do `SpringTransactionRunner`, o `save` da
  operação e o primeiro evento da trilha são confirmados separadamente: a operação
  ficava `COMPLETED` com a trilha pela metade. O rollback é o efeito da transação, não
  do código do caso de uso — e é por isso que o teste precisa exercitar o adaptador
  real, e não um repositório em memória.
- O duplo que falha no enésimo append estava **duplicado** dentro do
  `CompletionPersistenceTest`. Ele virou um duplo compartilhado em `support/`, porque a
  segunda task que precisa dele (a 2.18) merecia a mesma peça, e não uma cópia.
- O E2E real expôs uma diferença entre o teste e a stack: no banco de verdade o
  `POLL_ATTEMPT` se repete a cada `Retry-After` da Central (quatro eventos no cenário
  de 8 s), enquanto o teste de integração exercita um único 204 antes da confirmação. O
  cenário do teste é determinístico por construção (relógio controlado); o E2E mostra o
  comportamento real do worker.
- As seções deste arquivo estavam fora de ordem cronológica (a 2.17 entre a 2.15 e a
  2.16). A ordem foi corrigida nesta task: 2.15, 2.16, 2.17, 2.18.

#### Arquivos Tocados:
- `backend/src/test/java/br/com/consigport/application/portability/OperationLifecyclePersistenceTest.java`
- `backend/src/test/java/br/com/consigport/support/FailingOperationEventRepository.java`
- `backend/src/test/java/br/com/consigport/adapters/out/persistence/CompletionPersistenceTest.java`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Fase 2 — Backend core: arquivamento e promoção da `v0.3.0`

- **Data de Conclusão:** 2026-11-28
- **Branch:** `chore/018-phase-2-archive`
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar a Fase 2 (18 tasks), promover a release `v0.3.0` e abrir a Fase 3.

#### Comandos de Verificação e Saídas:

1. **Gates locais antes da promoção:**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → ✓ 65 documents, every relative link resolves.
   node scripts/check-specs.mjs                → ✓ SDD specifications valid.
   node scripts/check-architecture.mjs         → 🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso!
   node scripts/check-theme-tokens.mjs         → ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css (line endings normalized).
   node scripts/check-ai-log.mjs               → ✓ AI usage log: 44 sessions, structure valid.
   ```
2. **Reator completo (última task da fase):**
   ```text
   ./mvnw -B verify
   → ConsigPort Platform ................................ SUCCESS [  0.005 s]
   → ConsigPort Backend ................................. SUCCESS [01:29 min]
   → Central de Convênios Mock .......................... SUCCESS [ 29.468 s]
   → BUILD SUCCESS
   → EXIT=0
   Tests run: 481, Failures: 0, Errors: 0, Skipped: 0 (backend)
   Tests run: 39, Failures: 0, Errors: 0, Skipped: 0  (central-convenios-mock)
   Cobertura do núcleo: BRANCH 99.71% (342 de 343) | LINE 98.12% (679 de 692) | CLASS 100.00% (59 de 59)
   ```
3. **Promoção e release:**
   ```text
   PR #50  feat/038-backend-lifecycle-integration → develop → CI verde → merge (793099d)
   PR #51  develop → staging   → CI verde → merge (e9024dd)
   PR #52  staging → main      → CI verde → merge (64f23e7)
   PR #53  chore(main): release 0.3.0 → merge (c472c6a)
   → tag v0.3.0 e release publicada
   → CI do main (46094354043): success
   → Release (46095256699) — Publish backend/frontend/central-convenios-mock to GHCR: success
   ```
4. **Imagens e staging:**
   ```text
   backend              sha256:9cd162f26c72c90b78744f58fe5c4ab98e925b0da6865a3f7eafe53a0b0e1cf0
   central-convenios-mock sha256:f651491272db2cebd7c0b4ff435b2a28b1ea39f902e570ef299026e209f18b3a
   frontend             sha256:fd11ce5fe79d970e977c9ff19928683643f98465d3ce0d4436e15ef4da6336ac
   CD Staging (46095428615): {"status":"UP","groups":["liveness","readiness"]} em backend e mock; frontend responde 200
   ```
5. **Fim da execução:**
   ```text
   containers restantes: 0
   processos restantes: 0
   ```

#### Conteúdo Entregue:
- `SDD/specs/history/phases/phase-2-finished.md` — as 18 tasks, os PRs, os critérios de
  saída, a evidência da release e as dívidas conhecidas.
- `SDD/specs/PLAN.md` — Fase 2 concluída e Fase 3 aberta com a task 3.1 em voo.
- `SDD/specs/ROADMAP.md` — Fase 2 fechada com a tag `v0.3.0` e a Fase 3 como fase atual.

#### Descobertas e Decisões:
- O `release-please` cria a tag pela API do GitHub, o que **não** dispara o evento `push`
  da tag. As imagens foram publicadas por `workflow_dispatch` em `release.yml` com a tag
  `v0.3.0`, caminho já documentado em [`../../docs/process/RELEASE.md`](../../docs/process/RELEASE.md) §6.
- O CI do branch do release-please falha com "This run likely failed because of a workflow
  file issue" (run `46093781359`), sem jobs registrados e com `pull_requests = []`. O
  mesmo padrão ocorreu nas releases anteriores (`45658801729`, na Fase 0, e
  `45816627838`, na Fase 1): é o fechamento do PR antes de o job iniciar, não uma
  regressão do conteúdo — o código idêntico passou no CI do PR #52 e no CI do `main`
  (run `46094354043`, `success`).
- O arquivamento da fase vai para `develop` **depois** da tag, como na Fase 1 (PR #32
  mergeado em 13:00, `v0.2.0` às 12:08): o arquivo de arquivamento entra na release
  seguinte, e não na que fecha a fase.

#### Arquivos Tocados:
- `SDD/specs/history/phases/phase-2-finished.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 3.1 — Cliente OpenAPI tipado

- **Data de Conclusão:** 2026-11-28
- **Branch:** `feat/031-frontend-openapi-client`
- **PR:** a abrir para `develop`
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** dar ao frontend um cliente tipado do contrato público, sem duplicar
  tipos, com a base URL por ambiente e o erro mapeado pelo `code` estável.

#### Comandos de Verificação e Saídas:

1. **Geração dos tipos a partir do contrato:**
   ```text
   $ npm run --prefix frontend generate:types
   generated /home/ec2-user/consigport-platform/frontend/src/shared/api/schema.ts

   $ diff -q /tmp/opencode/schema-before.ts frontend/src/shared/api/schema.ts
   schema.ts idêntico após regenerar (EXIT=0)
   ```
   O `openapi-typescript` v7.13 não lê arquivo de config (`defineConfig` só existe a
   partir da v8, que não foi publicada), e a entrada por string é interpretada como
   **conteúdo YAML**, não como caminho — daí o `openapi-ts.config.ts` carregar a URL
   `file://` e chamar a API programática `openapiTS` + `astToString`.
2. **Portão 3.4 do `GATES.md` (frontend):**
   ```text
   $ npm run --prefix frontend lint
   > eslint .
   EXIT=0

   $ npm run --prefix frontend typecheck
   > tsc --noEmit
   EXIT=0

   $ npm run --prefix frontend test:unit
   > vitest run
    RUN  v4.1.12 /home/ec2-user/consigport-platform/frontend
    Test Files  5 passed (5)
         Tests  20 passed (20)
    Duration  7.08s
   EXIT=0

   $ npm run --prefix frontend build
   ✓ built in 1.22s   (client)
   ✓ built in 2.16s   (ssr)
   dist/ssr/index.js  1,581.90 kB
   EXIT=0
   ```
3. **Portão 3.1 (documentação e SDD):**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → ✓ 66 documents, every relative link resolves.
   node scripts/check-specs.mjs                → ✓ Exactly 1 task in flight in SDD/specs/PLAN.md.
   node scripts/check-architecture.mjs         → 🎉 Todos os 22 testes de validação dos desenhos passaram.
   node scripts/check-theme-tokens.mjs         → ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css.
   node scripts/check-ai-log.mjs               → ✓ AI usage log: 46 sessions, structure valid.
   ```

#### Conteúdo Entregue:
- `frontend/openapi-ts.config.ts` — o gerador dos tipos: carrega o contrato público
  (`../contracts/consigport-api.yaml`) e o destino (`src/shared/api/schema.ts`), e
  escreve o arquivo com a API programática do `openapi-typescript`.
- `frontend/src/shared/api/schema.ts` — os tipos gerados (`paths`, `operations`,
  `components`), versionados: um contrato quebrado quebra o build, não só a próxima
  geração.
- `frontend/src/shared/api/client.ts` — o `openapi-fetch` sobre esses tipos, a resolução
  da base URL por ambiente (`VITE_API_BASE_URL`, fallback `http://localhost:8080`, sem
  barra final) e o `parseErrorResponse`, que transforma uma resposta de erro em
  `ApiError`.
- `frontend/src/shared/api/errors.ts` — o mapeador por `code`: os dez códigos estáveis
  do contrato viram a união `ApiErrorCode`, o corpo da rede é `unknown` até ser
  validado, e um corpo quebrado degrada para `INTERNAL_ERROR` em vez de derrubar a
  tela; a `message` localizada do servidor é preservada e os `details` por campo são
  filtrados.
- `frontend/src/shared/api/client.test.ts` e `frontend/src/shared/api/errors.test.ts` —
  os testes de unidade do parser de erro e da resolução da base URL.
- `frontend/package.json` — `openapi-fetch` como dependência de produção,
  `openapi-typescript` e `@types/node` como de desenvolvimento, e o script
  `generate:types`.
- `frontend/tsconfig.json` — o gerador entra no `typecheck` e os tipos do Node
  entram em `types`.

#### Descobertas e Decisões:
- **O `openapi-typescript` v7 não tem arquivo de config.** A versão mais recente é a
  7.13.0: `defineConfig` não existe nela (chega na v8, que nunca foi publicada) e a
  CLI não lê `openapi-ts.config.ts`. Passar o caminho como string faz a biblioteca
  tratar o caminho como **conteúdo YAML** — o erro é `Can't parse empty schema`. A
  solução é a URL `file://` e a API programática, com o módulo `.ts` fazendo as duas
  coisas: configuração e geração. O Node 24 executa o TypeScript direto, sem
  dependência nova de execução.
- **O `theme.css` é cópia verbatim do `SDD/themes/matrix/tokens.css`** e o portão
  `check-theme-tokens` compara os dois arquivos. Nenhum token novo foi necessário
  nesta task.
- **A UI deve ramificar por `code`, nunca por status HTTP.** O mesmo status carrega
  códigos diferentes conforme a rota (`404` é `AUTHORIZATION_NOT_FOUND`,
  `BENEFICIARY_NOT_FOUND` ou `OPERATION_NOT_FOUND`), então o status não serve de
  decisão de tela. O `ApiError` expõe `code`, `message`, `locale`, `timestamp`,
  `path` e `details`.
- **O contrato público não foi alterado.** `contracts/consigport-api.yaml` é a
  entrada do gerador e segue igual; a comparação com o snapshot do springdoc
  continua valendo no CI.
- `@types/node` entrou como dependência de desenvolvimento porque o gerador é um
  script de Node — sem ele o `typecheck` não enxerga `node:fs/promises`.

#### Arquivos Tocados:
- `frontend/openapi-ts.config.ts`
- `frontend/src/shared/api/schema.ts`
- `frontend/src/shared/api/client.ts`
- `frontend/src/shared/api/errors.ts`
- `frontend/src/shared/api/client.test.ts`
- `frontend/src/shared/api/errors.test.ts`
- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/tsconfig.json`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 3.2 — Catálogos i18n

- **Data de Conclusão:** 2026-11-28
- **Branch:** `feat/032-frontend-i18n-catalogs`
- **PR:** a abrir para `develop`
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** dar à interface três idiomas — pt-BR, en e es — com fallback pt-BR,
  troca de idioma persistida e `Accept-Language` em toda requisição à API.

#### Comandos de Verificação e Saídas:

1. **Portão 3.4 do `GATES.md` (frontend):**
   ```text
   $ npm run --prefix frontend lint
   > eslint .
   EXIT=0

   $ npm run --prefix frontend typecheck
   > tsc --noEmit
   EXIT=0

   $ npm run --prefix frontend test:unit
   > vitest run
    RUN  v4.1.12 /home/ec2-user/consigport-platform/frontend
    Test Files  8 passed (8)
         Tests  36 passed (36)
    Duration  12.48s
   EXIT=0

   $ npm run --prefix frontend build
   [5/5] build ssr environment...
   ✓ 90 modules transformed.
   dist/ssr/assets/entry.rsc-7L_sN9Di.js     36.56 kB
   dist/ssr/index.js                      1,582.37 kB
   ✓ built in 2.40s
   EXIT=0
   ```
2. **Portão 3.1 (documentação e SDD):**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → ✓ 66 documents, every relative link resolves.
   node scripts/check-specs.mjs                → ✓ Exactly 1 task in flight in SDD/specs/PLAN.md.
   node scripts/check-architecture.mjs         → 🎉 Todos os 22 testes de validação dos desenhos passaram.
   node scripts/check-theme-tokens.mjs         → ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css.
   node scripts/check-ai-log.mjs               → ✓ AI usage log: 46 sessions, structure valid.
   ```
3. **Prova de vida — o servidor renderiza nos três idiomas:**
   ```text
   $ npm run --prefix frontend preview -- --host 127.0.0.1 --port 5199

   === cookie=pt-BR ===
   <html lang="pt-BR"
   Portabilidade de convênios sem fricção
   === cookie=en ===
   <html lang="en"
   Consignment portability without friction
   === cookie=es ===
   <html lang="es"
   Portabilidad de consignas sin fricción
   === rota /agenda en ===
   Discount agenda
   === 404 pt-BR ===
   <h1 class="text-headline-lg">Página não encontrada</h1>
   === 404 es ===
   <h1 class="text-headline-lg">Página no encontrada</h1>
   ```
   O `curl` leva o cookie `consigport-locale`; sem ele, a resposta é pt-BR. O
   `<html lang>` acompanha o idioma pedido.
4. **Auditoria de dependências:**
   ```text
   $ npm audit --prefix frontend --omit=dev --audit-level=high
   found 0 vulnerabilities
   EXIT=0

   $ npm audit --prefix frontend
   found 0 vulnerabilities
   ```

#### Conteúdo Entregue:
- `frontend/src/shared/i18n/pt-BR.json`, `en.json`, `es.json` — os três catálogos,
  com o mesmo conjunto de chaves e uma entrada para cada um dos dez códigos de erro do
  contrato público.
- `frontend/src/shared/i18n/locales.ts` — os idiomas suportados, a resolução de
  qualquer tag (`pt-BR`, `pt`, `es-AR`) e a leitura/escrita do cookie
  `consigport-locale`.
- `frontend/src/shared/i18n/i18n.ts` — a instância do `i18next` com fallback pt-BR.
- `frontend/src/shared/i18n/provider.tsx` — a folha cliente que guarda a escolha,
  sincroniza com o idioma que o servidor entregou e persiste o cookie.
- `frontend/src/shared/i18n/language-switcher.tsx` — o `select` nativo da troca de
  idioma.
- `frontend/src/shared/i18n/server.ts` — o tradutor dos Server Components, que não
  podem ler o contexto do navegador.
- `frontend/src/shared/api/client.ts` — o middleware que injeta `Accept-Language` em
  toda requisição.
- `frontend/src/test/mocks/handlers.ts` e `server.ts` — a API falsa com **MSW**, usada
  pelos testes: nenhuma requisição sai do processo.
- `frontend/src/app/navigation.tsx`, `shell.tsx`, `router.tsx`, `pages/home.tsx`,
  `pages/not-found.tsx`, `src/root.tsx`, `src/framework/entry.rsc.tsx` — as páginas e o
  shell passam a falar o idioma do servidor.

#### Descobertas e Decisões:
- **O `i18next` foi fixado em `26.x`, e não em `25.x`.** A série 25 carrega o advisory
  **GHSA-6jcc-5g8w-32mx** (`deepExtend` recursivo sobre objetos embutidos, DoS),
  corrigido apenas a partir da `26.3.4`. O `react-i18next@16` aceita
  `i18next >= 25.10.9`, então a combinação segue suportada. A linha do `APP-STACK.md`
  foi atualizada e a nota do advisory ficou ao lado dela.
- **O `MSW` entrou como dependência de desenvolvimento.** A regra do projeto proíbe
  mock de `fetch`: a API dos testes é falsa no nível do HTTP, com os handlers
  respondendo o formato do contrato público. O postinstall do `msw` é dispensável
  (imprime apenas uma mensagem de boas-vindas) e segue fora da lista `allowScripts`.
- **O idioma é resolvido no servidor, não no cliente.** As páginas são Server
  Components e não podem ler o contexto do navegador; o `entry.rsc.tsx` lê o cookie e
  o `Root` monta a página no idioma pedido, com o `<html lang>` correto. A troca de
  idioma no browser grava o cookie e dispara um novo render do servidor — o
  `entry.browser.tsx` escuta o `replaceState` — para que o servidor e as folhas
  clientes falem o mesmo idioma.
- **O `useEffect` do provedor não pode sincronizar estado.** A regra
  `react-hooks/set-state-in-effect` do ESLint recusa `setState` dentro de efeito: o
  provedor ajusta o estado durante o render, quando o idioma que o servidor entregou
  muda, e usa o efeito apenas para o sistema externo (`Accept-Language`).
- **O `openapi-fetch` consome o corpo da resposta de erro.** Nele, `error` já vem
  parseado (JSON quando possível); `parseErrorResponse` continua existindo para
  respostas cruas. O `ApiError` continua sendo a porta de entrada da UI.
- **O `theme.css` segue cópia verbatim** de `SDD/themes/matrix/tokens.css`; nenhum
  token novo foi necessário nesta task.
- **O contrato público não foi alterado.** `contracts/consigport-api.yaml` segue igual.

#### Arquivos Tocados:
- `frontend/src/shared/i18n/pt-BR.json`
- `frontend/src/shared/i18n/en.json`
- `frontend/src/shared/i18n/es.json`
- `frontend/src/shared/i18n/locales.ts`
- `frontend/src/shared/i18n/i18n.ts`
- `frontend/src/shared/i18n/provider.tsx`
- `frontend/src/shared/i18n/language-switcher.tsx`
- `frontend/src/shared/i18n/server.ts`
- `frontend/src/shared/i18n/i18n.test.ts`
- `frontend/src/shared/i18n/server.test.ts`
- `frontend/src/shared/api/client.ts`
- `frontend/src/shared/api/client.msw.test.ts`
- `frontend/src/test/mocks/handlers.ts`
- `frontend/src/test/mocks/server.ts`
- `frontend/src/app/navigation.tsx`
- `frontend/src/app/shell.tsx`
- `frontend/src/app/router.tsx`
- `frontend/src/app/router.test.tsx`
- `frontend/src/app/pages/home.tsx`
- `frontend/src/app/pages/not-found.tsx`
- `frontend/src/root.tsx`
- `frontend/src/framework/entry.rsc.tsx`
- `frontend/package.json`
- `frontend/package-lock.json`
- `SDD/APP-STACK.md`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 3.3 — Primitivas de UI

- **Data de Conclusão:** 2026-11-28
- **Branch:** `feat/033-frontend-ui-primitives`
- **PR:** a abrir para `develop`
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** entregar as primitivas de UI que as fatias seguintes consomem —
  tokens da Matrix, foco visível, contraste AA e nenhuma cor literal em componente.

#### Comandos de Verificação e Saídas:

1. **Portão 3.4 do `GATES.md` (frontend):**
   ```text
   $ npm run --prefix frontend lint
   > eslint .
   EXIT=0

   $ npm run --prefix frontend typecheck
   > tsc --noEmit
   EXIT=0

   $ npm run --prefix frontend test:unit
   > vitest run
    RUN  v4.1.12 /home/ec2-user/consigport-platform/frontend
    Test Files  13 passed (13)
         Tests  56 passed (56)
    Duration  17.09s
   EXIT=0

   $ npm run --prefix frontend build
   [5/5] build ssr environment...
   ✓ 90 modules transformed.
   dist/ssr/assets/entry.rsc-7L_sN9Di.js     36.56 kB
   dist/ssr/index.js                      1,582.37 kB
   ✓ built in 2.44s
   EXIT=0
   ```
2. **Portão 3.1 (documentação e SDD):**
   ```text
   node SDD/SKILLS/check-docs/check-docs.mjs .  → ✓ 66 documents, every relative link resolves.
   node scripts/check-specs.mjs                → ✓ Exactly 1 task in flight in SDD/specs/PLAN.md.
   node scripts/check-architecture.mjs         → 🎉 Todos os 22 testes de validação dos desenhos passaram.
   node scripts/check-theme-tokens.mjs         → ✓ Theme tokens: frontend/src/theme.css matches SDD/themes/matrix/tokens.css.
   node scripts/check-ai-log.mjs               → ✓ AI usage log: 47 sessions, structure valid.
   ```
3. **Prova de vida — as classes novas entraram no bundle:**
   ```text
   $ ls -la dist/client/assets/*.css
   -rw-r--r-- 1 ec2-user ec2-user 24137 Nov 28 05:10 dist/client/assets/index-Da78kLM6.css

   $ for c in outline-primary bg-error\/8 border-error\/40 text-tertiary min-h-11 overflow-x-auto; do grep -c -- "$c" dist/client/assets/*.css; done
   1
   1
   1
   1
   1
   1
   ```
   O CSS saiu de 20,54 kB para 24,14 kB: o Tailwind encontrou as classes dos
   componentes novos e as gerou.

#### Conteúdo Entregue:
- `frontend/src/shared/ui/field.tsx` — o `Field`, que é o dono da acessibilidade do
  formulário (rótulo, dica e erro ligados por `aria-describedby`, `aria-invalid` no
  controle, `required` no controle e o asterisco só para os olhos), e o `TextField`
  por cima dele.
- `frontend/src/shared/ui/table.tsx` — a `Table` como **região de rolagem nomeada e
  focável** (`role="region"`, `aria-label`, `tabIndex={0}`), mais `TableHead`,
  `TableBody`, `TableRow`, `TableHeader` e `TableCell`.
- `frontend/src/shared/ui/status-badge.tsx` — o conjunto fechado de estados (neutro,
  em andamento, sucesso, técnico, erro) com ponto decorativo.
- `frontend/src/shared/ui/button.tsx` — anel de foco visível em todas as variantes,
  estado `disabled` e estado `loading` (`aria-busy`, sem segundo clique).
- `frontend/src/shared/ui/contrast.test.ts` — o teste que lê `src/theme.css` e
  calcula o contraste de cada par que a interface usa.
- Os testes de componente: `button.test.tsx`, `field.test.tsx`, `table.test.tsx` e
  `status-badge.test.tsx`.

#### Descobertas e Decisões:
- **O contraste AA é verificado, não presumido.** O teste lê os tokens de
  `src/theme.css` — a cópia guardada pelo `check-theme-tokens` — e calcula a razão do
  WCAG. Corpo, botões preenchidos e badges passam dos 4,5:1. O `text-muted`
  (`#56657A`) fica em 3,28:1 sobre `surface-base`: o `DESIGN.md` §2 o reserva ao
  texto decorativo (placeholder e chrome desativada) e §"Inputs & terminal fields"
  fixa o placeholder em `#56657A`, então o piso dele é 3:1, o limiar de texto grande
  e de contraste de UI — e o teste fixa esse limite para que ninguém o promova a
  texto de corpo sem perceber.
- **O `role="region"` com `tabIndex={0}` é o que torna a tabela rolável acessível.**
  Sem ele, quem navega pelo teclado não consegue alcançar a rolagem da tabela no
  celular, e o leitor de tela não anuncia o que a região contém.
- **O estado `loading` desabilita o botão.** A regra é "zero ações sem resposta": o
  botão ocupado marca `aria-busy` e recusa o segundo clique, sem esconder o rótulo.
- **O `StatusBadge` não conhece o domínio.** O significado de um estado pertence à
  feature que o usa; a primitiva só conhece o conjunto fechado de tons. É a regra de
  reuso do `DESIGN.md` §"REUSABILITY": nenhum substantivo de domínio no nome.
- **Nenhum token novo foi necessário** — o `frontend/src/theme.css` segue cópia
  verbatim de `SDD/themes/matrix/tokens.css`.

#### Arquivos Tocados:
- `frontend/src/shared/ui/button.tsx`
- `frontend/src/shared/ui/button.test.tsx`
- `frontend/src/shared/ui/field.tsx`
- `frontend/src/shared/ui/field.test.tsx`
- `frontend/src/shared/ui/table.tsx`
- `frontend/src/shared/ui/table.test.tsx`
- `frontend/src/shared/ui/status-badge.tsx`
- `frontend/src/shared/ui/status-badge.test.tsx`
- `frontend/src/shared/ui/contrast.test.ts`
- `SDD/specs/BACKLOG.md`
- `SDD/specs/PLAN.md`
- `SDD/specs/ROADMAP.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`
