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
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
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
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
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
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
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
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
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
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
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
