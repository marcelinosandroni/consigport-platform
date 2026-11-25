# Diário de Uso de IA (AI Usage Log) — ConsigPort

> Registro cronológico de sessões de desenvolvimento com agentes de Inteligência Artificial, suas decisões tomadas, modelos utilizados e entregas associadas.
> Referências: [`HARNESS_OVERVIEW.md`](./HARNESS_OVERVIEW.md) e [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md).

---

## Sessão #01 — Refinamento Inicial e Fundação SDD

- **Data:** 2026-11-23
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `docs/000-sdd-foundation`
- **Objetivo:** Estruturação da metodologia SDD (Spec-Driven Development), catálogo de tarefas e elaboração das decisões arquiteturais fundacionais (ADRs).

### Atividades Realizadas:
1. **Definição de Domínio e Stack:**
   - Criação de [`../../SDD/APP.md`](../../SDD/APP.md), [`../../SDD/APP-STACK.md`](../../SDD/APP-STACK.md) e [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md).
   - Estabelecimento do monorepo: `backend/` (Java 21, Spring Boot 3.5.x, Hexagonal), `central-convenios-mock/` (Java 21 em memória), `frontend/` (React 19.2 + Vite RSC + Tailwind).
2. **Planejamento e Fatiamento Atômico:**
   - Criação de [`../../SDD/specs/ROADMAP.md`](../../SDD/specs/ROADMAP.md) (visão macro em 5 fases).
   - Criação de [`../../SDD/specs/BACKLOG.md`](../../SDD/specs/BACKLOG.md) com 50 tasks atômicas detalhadas com critérios de aceite e verificação.
   - Abertura de [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md) com a Fase 0 e Task 0.1 ativada em voo `[-]`.
3. **Registro de Decisões de Arquitetura (ADRs):**
   - [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md): Hexagonal, DDD, ACL e mock isolado.
   - [ADR-0002](../adr/0002-integridade-hmac-e-idempotencia.md): Integridade HMAC-SHA256 e idempotência.
   - [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md): Assincronismo 202/204, polling backoff e trilha de auditoria append-only.
   - [ADR-0004](../adr/0004-contrato-openapi-versionamento-e-i18n.md): Contrato OpenAPI 3.1 first, versionamento e modelo bilíngue (código EN / docs PT-BR / mensagens i18n).
   - [ADR-0005](../adr/0005-frontend-rsc-design-e-renderizacao.md): Frontend RSC sem Next.js, streaming SSR e tema Matrix.
   - [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md): Gitflow, pipelines GitHub Actions, testes ponta a ponta e performance k6.
4. **Pausa para Alinhamento:**
   - Submissão dos 4 pontos-chave para aprovação do desenvolvedor responsável (modelo híbrido de idioma, RSC sem Next, k6 para performance e governança de branches).

---

## Sessão #02 — Conclusão da Task 0.1 (Refinamento SDD Completo)

- **Data:** 2026-11-23
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash (High)
- **Branch:** `docs/000-sdd-foundation`
- **Objetivo:** Finalizar todos os entregáveis da Task 0.1 do PLAN, sanar inconsistências de links, formalizar os Portões de Qualidade e o Harness de IA, e disponibilizar o CI de documentação.

### Atividades Realizadas:
1. **Formalização dos Portões de Qualidade:**
   - Criação de [`../quality/GATES.md`](../quality/GATES.md) detalhando a matriz de portões locais vs. CI/CD vs. Nightly (Playwright e k6 rodando exclusivamente no CI/CD).
2. **Governança do AI Harness:**
   - Criação de [`HARNESS_OVERVIEW.md`](./HARNESS_OVERVIEW.md) e inicialização deste [`AI_USAGE_LOG.md`](./AI_USAGE_LOG.md).
3. **Rastreabilidade e Evidências:**
   - Inicialização de [`../../SDD/docs/EVIDENCE.md`](../../SDD/docs/EVIDENCE.md) para registro contínuo das evidências de cada task finalizada.
4. **Correção de Links e Integridade Documental:**
   - Correção dos links desatualizados no [`../../AGENTS.md`](../../AGENTS.md) raiz (`SDD/stacks/clean-code.md`, `SDD/ARCHITECTURE.md`, `SDD/docs/EVIDENCE.md`).
5. **Script de Verificação de Especificações:**
   - Criação de `scripts/check-specs.mjs` para validar programaticamente as invariantes do SDD (regra de 1 task `[-]` no PLAN, correspondência com BACKLOG, arquivos obrigatórios).
6. **Automação de CI de Documentação:**
   - Criação de `.github/workflows/ci-docs.yml` para assegurar que nenhum PR quebre links ou regras do SDD.
7. **Validação:**
   - Execução bem-sucedida de `node SDD/SKILLS/check-docs/check-docs.mjs .` (0 links quebrados).
   - Execução bem-sucedida de `node scripts/check-specs.mjs` (0 inconsistências de spec).

---

## Sessão #03 — Elaboração da Matriz de Rastreabilidade e Desenhos de Arquitetura

- **Data:** 2026-11-24
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Branch:** `docs/001-traceability-architecture-matrix`
- **Objetivo:** Refinar a documentação técnica com diagramas arquiteturais completos e matriz de rastreabilidade interativa (HTML/CSS) cobrindo requisitos funcionais, não-funcionais, restrições e ADRs.

### Atividades Realizadas:
1. **Rastreamento Integral de Requisitos:**
   - Mapeamento de todos os Requisitos Funcionais (RF-01 a RF-06), Requisitos Não-Funcionais (RNF-01 a RNF-09) e Restrições Técnicas (REST-01 a REST-03) do desafio técnico.
   - Vinculação com as Regras de Domínio (`BR-01` a `BR-16`), Decisões Arquiteturais ([ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) a [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md)), componentes de software e critérios de teste.
2. **Matriz de Rastreabilidade Interativa em HTML/CSS:**
   - Criação de [`../architecture/traceability-matrix.html`](../architecture/traceability-matrix.html) com design system limpo, filtros dinâmicos em tempo real por texto e categoria, abas interativas e diagramas explicativos das camadas hexagonais.
3. **Documentação Markdown com Diagramas:**
   - Criação de [`../architecture/ARCHITECTURE_AND_TRACEABILITY.md`](../architecture/ARCHITECTURE_AND_TRACEABILITY.md) com diagramas Mermaid da topologia de execução Docker Compose, camadas da Arquitetura Hexagonal com Ports & Adapters, Camada Anticorrupção (ACL) e diagrama de sequência do fluxo assíncrono com polling e HMAC.
4. **Validação:**
   - Execução bem-sucedida de `node SDD/SKILLS/check-docs/check-docs.mjs .` (57 documentos com links resolvidos).
   - Execução bem-sucedida de `node scripts/check-specs.mjs`.
5. **README Raiz e Rastreabilidade:**
   - Criação do [`../../README.md`](../../README.md) com visão geral, arquitetura, diagramas e índice das decisões (commit `aa46504`).

---

## Sessão #04 — Task 0.2 (Gitflow, PR Template e CODEOWNERS)

- **Data:** 2026-11-24
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Branch:** `feat/002-gitflow`
- **Objetivo:** Materializar o fluxo de branches, a revisão obrigatória e o template de PR exigido pela Task 0.2.

### Atividades Realizadas:
1. **Template de Pull Request:**
   - Criação de [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md) com contexto da task, arquivos alterados, checklist de qualidade, evidência obrigatória e rastreabilidade de requisitos/ADRs.
2. **Revisão Obrigatória:**
   - Criação de [`.github/CODEOWNERS`](../../.github/CODEOWNERS) cobrindo `SDD/`, `contracts/`, `.github/` e os módulos executáveis.
3. **Processo de Branches:**
   - Criação de [`../process/GITFLOW.md`](../process/GITFLOW.md) descrevendo `main`, `staging`, `develop`, branches de task, squash merge e proteção via `gh api`.
   - Ajuste da política: branches por padrão; worktrees apenas para agentes concorrentes (commit `c07dce8`).
4. **Validação:**
   - `node SDD/SKILLS/check-docs/check-docs.mjs .` → 60 documentos, todos os links resolvidos.
   - `node scripts/check-specs.mjs` e `node scripts/check-architecture.mjs` → exit 0.
5. **Entrega:** PR [#4](https://github.com/marcelinosandroni/consigport-platform/pull/4) mesclado em `develop` (commits `1bd31fa` e `c07dce8`).

---

## Sessão #05 — Task 0.3 (Ambiente Local e PostgreSQL 16)

- **Data:** 2026-11-24
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Branch:** `feat/003-local-env`
- **Objetivo:** Estabelecer a infraestrutura local base com `.gitignore`, `.env.example` e `compose.yaml` com PostgreSQL 16.

### Atividades Realizadas:
1. **Higiene do Repositório:**
   - Criação de [`.gitignore`](../../.gitignore) cobrindo segredos, artefatos Maven, Node, IDEs, relatórios de teste e volumes locais.
2. **Configuração de Ambiente:**
   - Criação de [`.env.example`](../../.env.example) com as variáveis de `POSTGRES_*`, portas, `HMAC_SECRET`, `CENTRAL_BASE_URL` e parâmetros de polling.
3. **Infraestrutura Docker:**
   - Criação de [`compose.yaml`](../../compose.yaml) com `postgres:16-alpine`, volume nomeado e healthcheck.
4. **Validação:**
   - `docker compose config -q` → exit 0.
   - `git check-ignore -v .env target/ node_modules/ .idea/` → regras confirmadas; `.env.example` e `compose.yaml` rastreados.
   - `node SDD/SKILLS/check-docs/check-docs.mjs .`, `node scripts/check-specs.mjs` e `node scripts/check-architecture.mjs` → exit 0.
5. **Entrega:** PR [#5](https://github.com/marcelinosandroni/consigport-platform/pull/5) mesclado em `develop` (commit `985e87a`).

---

## Sessão #06 — Task 0.4 (Início do Maven Wrapper, sem conclusão)

- **Data:** 2026-11-24
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Branch:** `feat/004-maven-reactor`
- **Objetivo:** Iniciar a Task 0.4 com wrapper Maven, reactor raiz e módulos `backend` e `central-convenios-mock`.

### Atividades Realizadas:
1. **Reactor Raiz:**
   - Criação de `pom.xml` com Spring Boot 3.5.0, Java 21, BOMs de ArchUnit, springdoc, Testcontainers e Resilience4j.
2. **Wrapper Manual:**
   - Criação de `mvnw`, `mvnw.cmd` e `.mvn/wrapper/maven-wrapper.properties` escritos à mão, sem passar pelo `wrapper:wrapper`.
3. **Planejamento:**
   - Task 0.4 marcada como `[-]` em [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md).
4. **Bloqueio Encontrado:**
   - O `mvnw.cmd` falhava em `!WRAPPER_JAR_URL!` (delayed expansion ausente), perdia variáveis no `ENDLOCAL` e exigia `JAVA_HOME` mesmo com Java no `PATH`.
   - O build não foi concluído, nenhuma evidência foi registrada e a sessão terminou sem commit.

---

## Sessão #07 — Task 0.4 (Conclusão), Correção do Log e Portão de Fiscalização

- **Data:** 2026-11-24
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/004-maven-reactor`
- **Objetivo:** Retomar a Task 0.4, substituir o wrapper quebrado, fechar o reactor, registrar as evidências, abrir o PR para `develop` e impedir que o AI Usage Log volte a ficar desatualizado.

### Atividades Realizadas:
1. **Wrapper Oficial e Versões:**
   - Regeneração com `wrapper:wrapper` 3.3.4 no modo `only-script`, apontando para Maven 3.10.2 e funcionando sem `JAVA_HOME`.
   - Alinhamento do reactor para Spring Boot 3.5.16 e ArchUnit 1.4.2; propriedade `argLine` vazia para o `@{argLine}` do JaCoCo não quebrar módulos sem agente.
2. **Módulos e Line Endings:**
   - POMs mínimos de `backend` e `central-convenios-mock` para o reactor resolver.
   - Criação de [`.gitattributes`](../../.gitattributes) garantindo LF no `mvnw` e CRLF nos scripts `.cmd`/`.ps1`.
3. **Evidências e Entrega:**
   - Atualização de [`../../SDD/docs/EVIDENCE.md`](../../SDD/docs/EVIDENCE.md) e de [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md) com a Task 0.4 concluída (commit `11e7c2e`).
   - PR [#6](https://github.com/marcelinosandroni/consigport-platform/pull/6) aberto para `develop` com CI de documentação verde.
4. **Correção Deste Log:**
   - Inclusão das sessões #04 a #07, que estavam ausentes desde a sessão #03, e do commit `aa46504` na sessão #03.
5. **Fiscalização Automática do Log:**
   - Criação de `scripts/check-ai-log.mjs` para validar a estrutura das sessões e exigir que todo PR que altere código/specs também altere este arquivo e acrescente ao menos uma sessão.
   - Criação de `.githooks/pre-commit` (instalado via `git config core.hooksPath .githooks`) para bloquear commit local de trabalho sem atualização do log.
   - Integração do script ao job de documentação em `.github/workflows/ci-docs.yml`.
6. **Validação:**
   - `.\mvnw.cmd -v` → Maven 3.10.2 com Java 21.0.12.
   - `.\mvnw.cmd -N validate` e `.\mvnw.cmd -q -DskipTests package` → exit 0.
   - `node scripts/check-ai-log.mjs` → log válido, com todas as sessões e campos obrigatórios.
   - `node scripts/check-specs.mjs`, `node SDD/SKILLS/check-docs/check-docs.mjs .` e `node scripts/check-architecture.mjs` → exit 0.

---

## Sessão #08 — Task 0.5 (Contratos OpenAPI e Vetor HMAC)

- **Data:** 2026-11-24
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/005-contracts`
- **Objetivo:** Implementar o contrato primeiro: superfície pública `/api/v1` em inglês, contrato da Central fiel ao edital em português, vetor HMAC compartilhado e verificação automatizada.

### Atividades Realizadas:
1. **Contrato Público do ConsigPort:**
   - Criação de [`../../contracts/consigport-api.yaml`](../../contracts/consigport-api.yaml) com autorizações, agenda, criação/listagem/detalhe de operações e trilha de eventos.
   - `Idempotency-Key` obrigatório no `POST`, `Idempotent-Replay` na resposta, paginação, `Accept-Language` e códigos de erro estáveis em inglês.
2. **Contrato da Central:**
   - Criação de [`../../contracts/central-api.yaml`](../../contracts/central-api.yaml) fiel ao edital: `POST /autorizacoes`, `GET /beneficiarios/{id}/agenda`, `POST /operacoes-portabilidade` (`202` + protocolo) e `GET /operacoes-portabilidade/{protocolo}` (`200` ou `204` + `Retry-After`).
   - Inclusão dos headers HMAC, da idempotência, dos controles de caos e dos endpoints `/_admin/reset` e `/_admin/state`.
3. **Vetor HMAC Compartilhado:**
   - Criação de [`../../contracts/hmac-vector.json`](../../contracts/hmac-vector.json) com três vetores (POST de portabilidade, GET de status e POST de autorização), segredo de teste e assinaturas em hexadecimal.
   - Criação de `scripts/hmac-vector.mjs` para reproduzir os vetores e de `--write` para regerá-los.
4. **Validação de Contrato:**
   - Configuração de `.spectral.yaml` com o ruleset `spectral:oas`.
   - Spectral sem erros nem avisos nos dois contratos.
5. **Catálogo de Erros:**
   - Inclusão de `BENEFICIARY_NOT_FOUND` no core e de `REQUISICAO_INVALIDA`, `SEM_AUTORIZACAO`, `BENEFICIARIO_NAO_ENCONTRADO` e `PARCELA_INVALIDA` na Central, refletidos em [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md).
6. **Validação:**
   - `node scripts/hmac-vector.mjs` → 3 vetores reproduzidos com HMAC-SHA256.
   - `npx @stoplight/spectral-cli@6 lint ... --fail-severity=warn` → zero erros e zero avisos.
   - `node scripts/check-specs.mjs`, `node SDD/SKILLS/check-docs/check-docs.mjs .` e `node scripts/check-architecture.mjs` → exit 0.
7. **Entrega:** commit e PR para `develop` com a Task 0.5 concluída e evidência em [`../../SDD/docs/EVIDENCE.md`](../../SDD/docs/EVIDENCE.md).

---

## Sessão #09 — Portão de Contratos no CI (fechamento da Task 0.5)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/005-contracts`
- **Objetivo:** Fechar a Task 0.5 transformando a verificação de contrato em portão automático no GitHub Actions, conforme o Gate 3.3 de [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).

### Atividades Realizadas:
1. **Workflow Dedicado de Contratos:**
   - Criação de `.github/workflows/ci-contracts.yml`, disparado por mudanças em `contracts/**`, `.spectral.yaml` e `scripts/hmac-vector.mjs` (consolidado no job `contracts` do `ci.yml` na Task 0.10).
   - Etapas: Spectral com `--fail-severity=warn`, reprodução do vetor HMAC e validação do log de IA com `AI_LOG_BASE_SHA`.
2. **Bloqueio de Breaking Changes:**
   - Instalação do `oasdiff` em versão fixa e comparação `oasdiff breaking` contra o contrato da branch base para os dois contratos.
   - Primeira introdução do contrato é aceita com aviso no log, sem quebrar o pipeline.
3. **Documentação do Portão:**
   - Atualização do Gate 3.3 em [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md) com o workflow, os comandos reais e a baseline do `oasdiff`.
4. **Entrega:**
   - PR #7 para `develop` reunindo os contratos, o vetor HMAC e o portão de CI.
   - Validação local: `node scripts/hmac-vector.mjs` (3 vetores), Spectral sem erros nem avisos, `check-specs`, `check-docs`, `check-architecture` e `check-ai-log` com exit 0.

---

## Sessão #10 — Task 0.6 (Scaffold do Backend)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/006-backend-scaffold`
- **Objetivo:** Aplicação Spring Boot vazia, compilando e com health `UP`, base para as fatias de domínio seguintes.

### Atividades Realizadas:
1. **Dependências do Backend:**
   - [`../../backend/pom.xml`](../../backend/pom.xml) com `web`, `validation`, `data-jpa`, `actuator`, `flyway-core`, `flyway-database-postgresql`, driver PostgreSQL em runtime, springdoc e `spring-boot-starter-test`; H2 apenas em escopo de teste.
   - `spring-boot-maven-plugin` para gerar o jar executável.
2. **Composition Root:**
   - Criação de `backend/src/main/java/br/com/consigport/ConsigPortApplication.java` sem qualquer dependência de domínio, mantendo o pacote `domain` livre de Spring nas próximas tasks.
3. **Configuração Única:**
   - [`../../backend/src/main/resources/application.yml`](../../backend/src/main/resources/application.yml) com porta `8080` (`SERVER_PORT`), `DATABASE_URL` e `POSTGRES_*` vindos do `.env`, `ddl-auto: validate`, `open-in-view: false`, timezone UTC, `shutdown: graceful` e actuator com probes.
   - Perfil `local` no mesmo arquivo apontando para o PostgreSQL do `compose.yaml` em `localhost:5432`.
   - springdoc em `openapi_3_1`, alinhando `/v3/api-docs` ao contrato `3.1.0` de `contracts/`.
4. **Teste de Fumaça:**
   - `ConsigPortApplicationTests` sobe o contexto completo em porta aleatória e prova `GET /actuator/health` → `UP` e `GET /v3/api-docs` → `200` com `"openapi":"3.1.0"`.
5. **Validação:**
   - `.\mvnw.cmd -q -pl backend -am verify` → `Tests run: 2, Failures: 0, Errors: 0`, exit 0.
   - `.\mvnw.cmd -N validate` → `BUILD SUCCESS`, exit 0.
   - `check-specs`, `check-docs` e `check-architecture` → exit 0.
6. **Débito Registrado:**
   - H2 substitui o PostgreSQL no teste do scaffold; a task de persistência troca por Testcontainers PostgreSQL 16 e reativa o Flyway.
7. **Entrega:** branch `feat/006-backend-scaffold` com commit e PR para `develop`.

---

## Sessão #11 — Task 0.7 (Scaffold do Mock da Central)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/007-mock-scaffold`
- **Objetivo:** Central simulada vazia, sem banco e sem Flyway, compilando e com health `UP` na porta 8081.

### Atividades Realizadas:
1. **Dependências do Mock:**
   - [`../../central-convenios-mock/pom.xml`](../../central-convenios-mock/pom.xml) com `web`, `validation`, `actuator`, springdoc e `spring-boot-starter-test`.
   - Nenhum JPA, Flyway ou driver de banco: o estado é em memória e resetável, o que preserva o determinismo dos testes.
2. **Aplicação:**
   - Criação de `central-convenios-mock/src/main/java/br/com/consigport/central/CentralConveniosMockApplication.java`.
3. **Configuração:**
   - [`../../central-convenios-mock/src/main/resources/application.yml`](../../central-convenios-mock/src/main/resources/application.yml) com porta `8081`, `shutdown: graceful`, actuator com probes, Swagger em `/swagger-ui.html` e springdoc em `openapi_3_1`.
   - Perfil `local` com log `DEBUG` de `br.com.consigport.central`.
4. **Testes e Prova em Processo Real:**
   - `CentralConveniosMockApplicationTests` prova `GET /actuator/health` → `UP` e `GET /v3/api-docs` → `3.1.0`.
   - Execução do jar empacotado em `--server.port=18081` com `GET /actuator/health` → `200 {"status":"UP","groups":["liveness","readiness"]}`.
5. **Validação:**
   - `.\mvnw.cmd -q -pl central-convenios-mock -am verify` → `Tests run: 2, Failures: 0`, exit 0.
   - `check-specs`, `check-docs` e `check-architecture` → exit 0.
6. **Entrega:** branch `feat/007-mock-scaffold` com commit e PR para `develop`.

---

## Sessão #12 — Task 0.8 (Base do Frontend RSC)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/008-frontend-base`
- **Objetivo:** Projeto React 19.2 com Vite RSC, TypeScript e Tailwind 4, sem Next.js, consumindo os tokens do `DESIGN.md`.

### Atividades Realizadas:
1. **Template Oficial RSC:**
   - Geração do starter oficial com `npm create vite@latest frontend -- --template rsc` (entradas `rsc`, `ssr` e `client` do `@vitejs/plugin-rsc`).
   - Remoção da demo visual do template (logos Vite/React) e da pasta de assets não usada.
2. **Dependências conforme o `APP-STACK`:**
   - [`../../frontend/package.json`](../../frontend/package.json) com React 19.2.9, Vite 7.3.8, `@vitejs/plugin-rsc`, Tailwind CSS 4.3.5, TypeScript 5.9, ESLint 9, Vitest 4.1 e Testing Library 16.
   - Scripts `dev`, `build`, `preview`, `lint`, `typecheck` e `test:unit`; `allowScripts` libera o postinstall do `esbuild`, exigido pelo Vite.
3. **Configuração:**
   - [`../../frontend/vite.config.ts`](../../frontend/vite.config.ts) mantém os três ambientes do template e acrescenta o plugin do Tailwind 4.
   - [`../../frontend/tsconfig.json`](../../frontend/tsconfig.json) em modo estrito (`strict`, `noUncheckedIndexedAccess`, `noImplicitOverride`) com alias `@/*`.
   - `frontend/eslint.config.js` (flat config) e `frontend/vitest.config.ts` com ambiente `jsdom`.
4. **Tema:**
   - [`../../frontend/src/theme.css`](../../frontend/src/theme.css) como cópia verbatim de `SDD/themes/matrix/tokens.css`, consumida por `frontend/src/styles.css` via `@import "tailwindcss"`.
   - [`../../scripts/check-theme-tokens.mjs`](../../scripts/check-theme-tokens.mjs) impede a deriva entre a fonte e a cópia.
5. **Correções no Template:**
   - `override` em `state` e `render` do `error-boundary`, `action(...args)` no lugar de `apply`, `catch` sem binding e `const link` no `entry.browser`.
6. **Validação:**
   - `npm ci --prefix frontend` → exit 0; `npm run --prefix frontend build` → exit 0 nos três ambientes.
   - `npm run --prefix frontend lint`, `typecheck` e `test:unit` → exit 0 (1 teste).
   - `npm run --prefix frontend dev` → `dev_status=200` com HTML servido em `lang="pt-BR"`.
7. **Entrega:** branch `feat/008-frontend-base` com commits e PR para `develop`.

---

## Sessão #13 — Task 0.9 (Shell RSC e tema Matrix)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/009-shell-matrix`
- **Objetivo:** Casca visual navegável dark-only, conforme o `DESIGN.md`, com tokens verbatim, fontes locais e primitivas reutilizáveis.

### Atividades Realizadas:
1. **Fontes Locais:**
   - `@fontsource/manrope`, `@fontsource/jetbrains-mono` e `@fontsource/playfair-display` adicionados ao `frontend/package.json`, com apenas o subset latino.
   - `src/styles.css` declara `--font-manrope`, `--font-jetbrains-mono` e `--font-playfair`, que o `theme.css` consome; nenhuma requisição externa, nenhum deslocamento de layout.
2. **Tema e Canvas:**
   - `src/theme.css` permanece como cópia verbatim de `SDD/themes/matrix/tokens.css`, guardada por `scripts/check-theme-tokens.mjs`.
   - `src/styles.css` importa Tailwind 4, os tokens e aplica o canvas Tier 0 com a grade de micro-pontos de 1px.
3. **Primitivas (`src/shared/ui/`):**
   - `button.tsx` (primary, secondary e ghost, com `buttonClasses` para âncoras), `badge.tsx` (neutral e highlight), `card.tsx` (superfície Tier 1) e `stat-card.tsx` (cabeçalho mono, número, anotação e status).
   - Nenhum literal hexadecimal em componente: tudo vem dos tokens.
4. **Shell e Rotas:**
   - `src/app/shell.tsx` monta a navegação de vidro sobre o canvas, a coluna de conteúdo de 1320px e o rodapé, com alvos de toque de 44px.
   - `src/app/router.tsx` resolve o caminho para a página e devolve a 404 dentro do mesmo shell; `src/app/pages/` traz a home, a página de seção e a 404.
   - `src/root.tsx` passa a renderizar o shell e a rota resolvida.
5. **Testes:**
   - `src/app/router.test.tsx` cobre home, seção, barra final e 404; `src/shared/ui/stat-card.test.tsx` cobre rótulo, número, anotação e status.
   - Vitest: 3 arquivos e 7 testes verdes.
6. **Auditoria Geométrica (`DESIGN.md` §4b):**
   - Viewports de 390px e 1440px auditadas em navegador real: `scrollWidth` igual a `clientWidth`, nenhum elemento fora da viewport, nenhum alvo de toque abaixo de 44×44px.
   - O texto de 11px detectado é apenas o chrome `label-mono` (navegação, badge e rótulos de stat card), que o `DESIGN.md` §4b.2.3 permite explicitamente.
7. **Guard dos Tokens no CI:**
   - `scripts/check-theme-tokens.mjs` passa a normalizar quebras de linha antes de comparar, porque o `.gitattributes` garante LF no repositório enquanto a árvore de trabalho do Windows pode guardar CRLF.
   - O workflow de documentação executa o guard e dispara quando `frontend/src/theme.css` ou `SDD/themes/**` mudam.
8. **Validação:**
   - `npm run --prefix frontend build` → exit 0 nos três ambientes; CSS do cliente em 20,23 kB (4,50 kB gzip) depois de restringir os subsets das fontes.
   - `npm run --prefix frontend lint`, `typecheck` e `test:unit` → exit 0.
9. **Entrega:** branch `feat/009-shell-matrix` com commits e PR para `develop`.

---

## Sessão #14 — Task 0.10 (Pipeline de Integração Contínua)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Branch:** `feat/010-ci-pipeline`
- **Objetivo:** Um pipeline único com jobs paralelos que provam cada PR, mais o nightly com os portões pesados.

### Atividades Realizadas:
1. **Pipeline Principal (`ci.yml`):**
   - Jobs `docs`, `backend`, `mock`, `frontend`, `contracts`, `compose`, `smoke`, `e2e`, `k6-smoke` e `security`, todos em paralelo, com `concurrency` cancelando execuções antigas do mesmo PR.
   - `permissions: contents: read` no topo e Node 24, npm 12.0.2 e Temurin 21 fixados por variável de ambiente.
   - Cache de Maven via `actions/setup-java` e de npm via `actions/setup-node` com `cache-dependency-path: frontend/package-lock.json`.
2. **Smoke Real da Stack:**
   - O job `smoke` sobe o PostgreSQL 16 do `compose.yaml`, empacota os jars, inicia mock e backend e exige `200` em `/actuator/health` dos dois, anexando os payloads de health como artefato.
3. **Portões Condicionais Honestos:**
   - Um job `detect` procura as suítes no commit (`frontend/e2e`, `tests/k6`, `tests/schemathesis`, `Dockerfile`) e publica os resultados; `e2e`, `k6-smoke` e `security-images` só rodam quando a suíte existe, em vez de fingir verde.
4. **Nightly (`nightly.yml`):**
   - Cron diário às 03:00 UTC com `contract-drift` (oasdiff contra `main`), `e2e-full`, `k6-load`, `schemathesis` e `security-images`.
5. **Consolidação:**
   - Os workflows `ci-docs.yml` e `ci-contracts.yml` foram removidos: as mesmas verificações agora vivem nos jobs `docs` e `contracts`, sem duplicar runner.
6. **Documentação:**
   - [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md) agora mapeia cada portão ao seu job, com a nota de que E2E, k6, Schemathesis e scan de imagens acendem quando a suíte chega.
7. **Validação:**
   - `actionlint` (v1.7.12) sem erros em `ci.yml` e `nightly.yml`.
   - `npm audit --prefix frontend --omit=dev --audit-level=high` → `found 0 vulnerabilities`.
8. **O Portão Encontrou uma Vulnerabilidade Real:**
   - O job `security` reprovou o primeiro PR: `org.postgresql:postgresql` 42.7.11 com `CVE-2026-54291` (downgrade de SCRAM-SHA-256-PLUS), corrigida em 42.7.12.
   - O driver foi sobrescrito no `pom.xml` para 42.7.15, acima da versão corrigida.
   - O `dependency-review` do GitHub foi removido do job: a API de Dependency Graph está desabilitada neste repositório, e a varredura de filesystem do Trivy já lê `pom.xml` e `package-lock.json`.
9. **Entrega:** branch `feat/010-ci-pipeline` com commit e PR para `develop`; a task fecha com os jobs verdes deste PR.
