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
