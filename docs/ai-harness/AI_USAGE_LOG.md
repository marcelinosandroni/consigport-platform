# Diário de Uso de IA (AI Usage Log) — ConsigPort

> Registro cronológico de sessões de desenvolvimento com agentes de Inteligência Artificial, suas decisões tomadas, modelos utilizados e entregas associadas.
> Referências: [`HARNESS_OVERVIEW.md`](./HARNESS_OVERVIEW.md) e [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md).

---

## Estratégia de Modelos: 100% Free

O ConsigPort é desenvolvido **sem nenhum custo de API**. A escolha do modelo é uma
decisão de projeto, não um detalhe da ferramenta: toda a Fase 0 roda em modelos
gratuitos e leves, para que qualquer pessoa possa reproduzir a entrega.

| Sessões | Tarefas | Harness | Provider | Modelo |
|---|---|---|---|---|
| #01, #07–#09 | 0.1, 0.4, 0.5 | OpenCode | — | DeepSeek V4.1 Flash |
| #02–#06 | 0.1 a 0.4 | Antigravity | Google | Gemini 3.8 Flash |
| #10 em diante | 0.6 em diante | OpenCode | NVIDIA | MiniMax-M3.1-Flash (`nvidia/minimaxai/minimax-m3.1-flash`) |

- **Harness estável:** `OpenCode` cobre a fase inteira; o que muda entre as tasks é o
  provider e o modelo atendidos por ele.
- **Modelo leve:** `MiniMax-M3.1-Flash` pertence à família Flash — resposta rápida e
  consumo de contexto baixo, as duas características que mantêm viável o ciclo SDD
  (task de até uma hora, evidência colada, commit pequeno) sem pagar por token.
- **Custo zero:** nenhum modelo pago entra no desenvolvimento. Adotar um exige
  decisão explícita do desenvolvedor e registro nesta política.
- **Atribuição real:** o par agente/modelo registrado aqui e o trailer
  `(Agent: ...)` do commit descrevem a sessão em que o trabalho foi feito, nunca o
  de uma sessão anterior.

> **Fonte de verdade.** Este diário é o registro autoritativo de atribuição. O
> trailer `(Agent: ...)` já gravado em commits antigos pode ter ficado desatualizado
> quando o modelo mudou no meio da fase; quando divergirem, este diário prevalece.
> O histórico de commits não é reescrito: ele é registro imutável, como a trilha de
> auditoria do domínio.

Política completa: [`HARNESS_OVERVIEW.md`](./HARNESS_OVERVIEW.md) §4.

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
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
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
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
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
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
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
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
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
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
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
9. **Run Verde do PR (`45622026445`):**
   - Nove jobs verdes: `docs`, `backend`, `mock`, `frontend`, `contracts`, `compose`, `smoke`, `security` e `detect`.
   - `E2E — Playwright` e `Performance Smoke — k6` pulados porque as suítes ainda não existem; o job `detect` prova a ausência em vez de fingir verde.
   - Artefatos anexados: `backend-reports`, `mock-reports`, `frontend-dist` e `stack-health`.
10. **Entrega:** branch `feat/010-ci-pipeline` com commit e PR #12 para `develop`; task fechada com o run verde acima.

---

## Sessão #15 — Task 0.11 (Release e Entrega Contínua)

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Branch:** `feat/011-release-cd`
- **Objetivo:** Conventional Commits viram changelog e tag SemVer, as três imagens são publicadas no GHCR e a tag dispara o deploy de staging.

### Atividades Realizadas:
1. **Imagens Docker:**
   - `backend/Dockerfile`, `central-convenios-mock/Dockerfile` e `frontend/Dockerfile` em build de múltiplos estágios, a partir do contexto da raiz (o reactor Maven precisa do `pom.xml` pai).
   - Estágio de runtime sem privilégio (`eclipse-temurin:21-jre-alpine` com usuário `consigport`; `node:24-alpine` com usuário `node`), `HEALTHCHECK` e `.dockerignore` na raiz.
2. **Versionamento:**
   - `release-please-config.json` com o tipo `simple` (uma versão para a plataforma inteira) e `.release-please-manifest.json` começando em `0.0.0`, para que a primeira release seja a `v0.1.0` da fase.
3. **Publicação no GHCR:**
   - `.github/workflows/release.yml` roda o `release-please-action` no push em `main` e publica as três imagens quando a tag é criada, com as tags `vX.Y.Z`, `X.Y.Z` e `latest` (a `latest` nunca é movida por uma pré-release).
4. **Deploy de staging:**
   - `.github/workflows/cd-staging.yml` dispara na tag, sobe `compose.staging.yaml` com as imagens publicadas, espera os healthchecks e valida backend, mock e frontend.
5. **Validação:**
   - `docker build` das três imagens → exit 0; `actionlint` (v1.7.12) sem erros nos quatro workflows; `docker compose config -q` dos dois compose → exit 0.
   - Stack de staging real com os quatro serviços `healthy`: backend e mock em `UP`, frontend em HTTP 200.
   - `release-please` em dry-run → `v0.1.0`.
6. **Defeito Encontrado pelo Dry-Run:**
   - A primeira execução propôs `1.0.0` em vez de `0.1.0`: sem release anterior, o release-please não usa a versão do manifesto, e sim `initialReleaseVersion()`, cujo padrão é `1.0.0`.
   - Correção: `initial-version: "0.1.0"` no `release-please-config.json` (commit `de2d260`).
   - O token nunca entra no argv: o dry-run roda pela API programática (`release-please/build/src/index.js`) com o token vindo de uma variável de ambiente.
7. **Defeitos Encontrados pelo Deploy de Staging:**
   - O reactor Maven exigia o `pom.xml` dos dois módulos; a imagem do frontend exigia o `vite.config.ts` e a propriedade de `node_modules`; o healthcheck do frontend precisou de `127.0.0.1` em vez de `localhost` (o busybox resolve `::1` primeiro).
8. **Entrega:** branch `feat/011-release-cd` com commits e PR [#15](https://github.com/marcelinosandroni/consigport-platform/pull/15) para `develop`; task 0.11 fechada e task 0.12 (promoção da `v0.1.0`) aberta.

---

## Sessão #16 — Correção da Atribuição de Modelo e Política 100% Free

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Branch:** `feat/011-release-cd`
- **Objetivo:** Corrigir a atribuição de harness, provider e modelo das sessões da Fase 0 e registrar a política de modelos gratuitos do projeto.

### Atividades Realizadas:
1. **Correção das Sessões #10 a #15:**
   - As tasks 0.6 a 0.11 foram executadas no harness OpenCode com o provider NVIDIA e o modelo `nvidia/minimaxai/minimax-m3.1-flash` (MiniMax-M3.1-Flash); o diário as registrava como DeepSeek V4.1 Flash.
   - As sessões #01 a #09 permanecem como DeepSeek V4.1 Flash e Gemini 3.8 Flash, conforme executado.
2. **Correção das Evidências:**
   - [`../../SDD/docs/EVIDENCE.md`](../../SDD/docs/EVIDENCE.md) atualizado nas tasks 0.6 a 0.10 com o mesmo par provider/modelo.
3. **Política de Modelos Gratuitos:**
   - Nova seção 4 em [`HARNESS_OVERVIEW.md`](./HARNESS_OVERVIEW.md) que fixa a estratégia 100% free: harness OpenCode, provider NVIDIA, modelo leve MiniMax-M3.1-Flash e custo zero de API.
   - O trailer `(Agent: ...)` passa a exigir o par real (harness e modelo) da sessão, nunca o de uma sessão anterior.
4. **Exemplos de Formato Corrigidos:**
   - [`../adr/0006-gitflow-ci-cd-docker-e-performance.md`](../adr/0006-gitflow-ci-cd-docker-e-performance.md), [`../../SDD/specs/BACKLOG.md`](../../SDD/specs/BACKLOG.md) e [`../../SDD/specs/PLAN.md`](../../SDD/specs/PLAN.md) deixam de fixar um modelo antigo.
5. **Histórico Preservado:**
   - Os títulos de commit já escritos mantêm a anotação original: o histórico é append-only e não é reescrito. O diário é a fonte de verdade da atribuição.
6. **Validação:**
   - `node scripts/check-ai-log.mjs` → log válido.
   - `node scripts/check-specs.mjs`, `node SDD/SKILLS/check-docs/check-docs.mjs .` e `node scripts/check-architecture.mjs` → exit 0.

---

## Sessão #17 — Fechamento da Task 0.11: Evidência e Gatilho de Publicação

- **Data:** 2026-11-25
- **Agente / Modelo:** OpenCode (NVIDIA) — MiniMax-M3.1-Flash
- **Branch:** `fix/011-release-closure`
- **Objetivo:** Fechar a task 0.11 com a evidência colada e corrigir o gatilho que publica as imagens no GHCR.

### Atividades Realizadas:
1. **Evidência da Task 0.11:**
   - Dry-run do release-please propondo `v0.1.0`, build das três imagens, `actionlint`, `compose config -q` e stack de staging com os quatro serviços `healthy`.
   - Task 0.11 marcada `[x]` e task 0.12 (promoção da `v0.1.0` e arquivamento da fase) aberta no `PLAN.md`.
2. **Correção do Gatilho de Publicação:**
   - O job `publish-images` declarava `needs: release-please`. No push da tag, o job `release-please` é pulado (`if: github.ref_type == 'branch'`) e um `needs` pulado leva o dependente junto: o caminho de publicação documentado nunca executava.
   - O job passou a ser independente, disparado apenas pelo push da tag, com `IMAGE_TAG` vindo de `github.ref_name` e o checkout fixado no commit etiquetado — as imagens nunca saem da branch que disparou o workflow.
3. **Rollback Real:**
   - O `workflow_dispatch` de `release.yml` passou a aceitar a tag a republicar, reconstruindo as três imagens a partir do commit etiquetado.
   - `docs/process/RELEASE.md` ajustado ao comportamento real do workflow.
4. **Entrega:** branch `fix/011-release-closure` com commits e PR para `develop`.

---

## Sessão #18 — Fechamento da Task 0.12 e Arquivamento da Fase 0

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `chore/012-phase-0-archive`
- **Ambiente:** harness remoto em **EC2 (AWS)**; o desenvolvedor ficou sem o notebook e operou apenas pelo celular.
- **Objetivo:** Fechar a task 0.12 com o arquivamento da Fase 0, sincronizar os documentos de estado e corrigir as inconsistências levantadas na análise do SDD.

### Atividades Realizadas:
1. **Harness remoto:**
   - Provisionamento do ambiente de desenvolvimento em uma instância **EC2 (AWS)** com OpenCode no provider **ZEN** e o modelo **DeepSeek V4.1 Flash Free**, porque o desenvolvedor ficou sem o notebook e passou a operar apenas pelo celular.
   - Instalação de Node.js 24, Java 21 (Corretto), Python 3.13, Docker + Compose e GitHub CLI para permitir o ciclo completo (build, testes, PR e merge) a partir do celular.
2. **Arquivamento da Fase 0:**
   - Criado `SDD/specs/history/phases/phase-0-finished.md` com as 12 tasks, os critérios de saída, a evidência da `v0.1.0` e as dívidas conhecidas.
3. **Sincronização do plano e do roadmap:**
   - `SDD/specs/PLAN.md` marca a Fase 0 como concluída e registra a próxima task (1.1, `[ ]`).
   - `SDD/specs/ROADMAP.md` passa a refletir `v0.1.0`, Fase 0 fechada, 12 tasks na fase e 51 no total.
4. **Correção do CHANGELOG:**
   - Removidas as entradas duplicadas produzidas pelos merges `develop → staging → main`; cada feature permanece uma única vez.
5. **Governança:**
   - `docs/process/GITFLOW.md` registra que a proteção de branches está desativada por decisão do responsável e que os merges são integrados via `gh pr merge`.
6. **Validação:**
   - `node scripts/check-specs.mjs`, `node SDD/SKILLS/check-docs/check-docs.mjs .`, `node scripts/check-architecture.mjs`, `node scripts/check-ai-log.mjs` e `node scripts/check-theme-tokens.mjs` → exit 0.

---

## Sessão #19 — Task 1.1 (Store em Memória, Fixtures e Administração)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/101-mock-store`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Entregar a primeira task da Fase 1: estado determinístico reiniciável, fixtures do `ARCHITECTURE.md` §4 e os endpoints administrativos.

### Atividades Realizadas:
1. **Store em memória:**
   - `InMemoryCentralStore` com `ConcurrentHashMap` para agenda, autorizações, operações e contadores de status.
   - `reset()` restaura as fixtures e zera tudo; `snapshot()` devolve o estado com chaves ordenadas para JSON determinístico.
2. **Fixtures:**
   - `SeedData` reproduz `BEN-001` (3 parcelas), `BEN-REJ` (`PAR-101`), `BEN-MARGIN` (`PAR-301`, 6.000,00) e `BEN-TIMEOUT` (`PAR-201`), todas `ABERTA` e com `BigDecimal`.
3. **Administração:**
   - `AdminController` expõe `POST /_admin/reset` → `204` e `GET /_admin/state` → `200`, conforme `contracts/central-api.yaml`.
4. **Domínio base:**
   - `Installment`, `Authorization`, `Operation` e `OperationStatus`, reutilizados pelas tasks 1.2–1.5.
5. **Validação:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 5 testes verdes.
   - Dois resets consecutivos produzem estados byte a byte idênticos.

---

## Sessão #20 — Task 1.2 (Consulta de Agenda)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/102-mock-agenda`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Expor a agenda de parcelas futuras com o gate de autorização do contrato.

### Atividades Realizadas:
1. **Endpoint de agenda:**
   - `GET /beneficiarios/{beneficiarioId}/agenda` devolve um array de `Parcela` em PT, fiel ao `contracts/central-api.yaml` (sem envelope).
   - Apenas parcelas `ABERTA` são retornadas; a ordem das fixtures é preservada.
2. **Gate de autorização:**
   - Beneficiário desconhecido → `404 BENEFICIARIO_NAO_ENCONTRADO`.
   - Beneficiário sem opt-in → `403 SEM_AUTORIZACAO` (BR-01).
3. **Erros padronizados:**
   - `CentralException` + `ApiExceptionHandler` produzem o corpo `ErroCentral` (`codigo`, `mensagem`, `timestamp`).
4. **Base para o opt-in:**
   - `InMemoryCentralStore` ganhou `registrarAutorizacao` e `possuiAutorizacao`.
5. **Validação:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 8 testes verdes.

---

## Sessão #21 — Task 1.3 (Opt-in de Autorização)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/103-mock-authorization`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Registrar o consentimento do beneficiário e liberar a agenda.

### Atividades Realizadas:
1. **Endpoint de opt-in:**
   - `POST /autorizacoes` devolve `201` com `autorizacaoId` na criação e `200` com o mesmo id na repetição (BR-02).
   - Identificador determinístico no formato `AUT-yyyyMMdd-NNNN`, com sequência zerada pelo reset.
2. **Índice de unicidade:**
   - `InMemoryCentralStore` mantém o índice por par `(beneficiarioId, convenioId)` e o método `autorizar` é sincronizado.
3. **Validação:**
   - `AuthorizationRequest` com `@NotBlank` e `@Size(max = 64)`; o handler converte falhas em `400 REQUISICAO_INVALIDA`.
4. **Integração com a agenda:**
   - Teste prova que o opt-in libera `GET /beneficiarios/BEN-001/agenda`.
5. **Validação geral:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 13 testes verdes.

---

## Sessão #22 — Task 1.4 (Submissão de Portabilidade)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/104-mock-portability`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Aceitar operações de portabilidade com protocolo único e estado inicial `PENDENTE`.

### Atividades Realizadas:
1. **Endpoint de submissão:**
   - `POST /operacoes-portabilidade` devolve `202` com `protocolo` no padrão `PRT-yyyyMMdd-NNNN`.
   - A operação nasce `PENDENTE` no `ConcurrentHashMap` e fica disponível para a consulta de status.
2. **Validação de parcelas (BR-04):**
   - Parcela inexistente ou fora de `ABERTA` → `422 PARCELA_INVALIDA`, com mensagem específica.
3. **DTOs e validação:**
   - `PortabilityRequest` com `@NotBlank`, `@NotEmpty`, `@Size` e `@Valid` na conta de recebimento.
4. **Base para o polling:**
   - `InMemoryCentralStore.operacao(protocolo)` preparado para a task 1.5.
5. **Validação geral:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 17 testes verdes.

---

## Sessão #23 — Task 1.5 (Consulta de Status com Retry-After)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/105-mock-status`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** Simular a confirmação assíncrona da Central com `Retry-After` e status terminal.

### Atividades Realizadas:
1. **Consulta de status:**
   - `GET /operacoes-portabilidade/{protocolo}` devolve `204` com `Retry-After: 2` enquanto a operação está `PENDENTE`.
   - Depois do delay, devolve `200` com `CONFIRMADA` ou `REJEITADA` e o motivo; protocolo desconhecido → `404 OPERACAO_NAO_ENCONTRADA`.
2. **Transição assíncrona:**
   - `TransitionScheduler` agenda a transição em `mock.confirmation-delay-ms` (`MOCK_CONFIRMATION_DELAY_MS`, padrão 2000 ms) em uma thread daemon.
3. **Regras de rejeição (BR-14):**
   - Sufixo `-REJ` ou soma das parcelas acima de `5000.00` → `REJEITADA` com `MARGEM_CONSIGNADA_INSUFICIENTE`.
   - A transição compara a instância da operação, então um reset invalida tarefas antigas.
4. **Validação:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 22 testes verdes.

---

## Sessão #24 — Task 1.6 (HMAC e Idempotência)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/106-mock-hmac`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** validar integridade das requisições com HMAC-SHA256 e evitar operações duplicadas.

### Atividades Realizadas:
1. **Assinatura canônica:**
   - `CanonicalRequest` ordena as chaves do JSON, remove `assinatura` e monta `METHOD\nPATH_WITH_QUERY\nTIMESTAMP\nSHA256_HEX(body)`.
   - `HmacValidator` reproduz o vetor de `contracts/hmac-vector.json` e aplica tolerância de ±300 s.
2. **Filtro de proteção:**
   - `HmacFilter` exige `X-Signature` e `X-Timestamp` em `/autorizacoes`, `/beneficiarios/**` e `/operacoes-portabilidade/**`.
   - Assinatura ausente, divergente ou expirada → `401 ASSINATURA_INVALIDA`.
3. **Idempotência (BR-07):**
   - `IdempotencyRegistry` guarda o hash do payload e o protocolo por `Idempotency-Key`.
   - Mesma key + mesmo payload → mesmo protocolo; payload diferente → `409 IDEMPOTENCIA_CONFLITO`; key ausente → `400 REQUISICAO_INVALIDA`.
   - O reset limpa o registry junto com o estado.
4. **Configuração:**
   - `MOCK_HMAC_SECRET` e `MOCK_HMAC_TOLERANCE_SECONDS` em `application.yml`, `.env.example` e `compose.staging.yaml`.
5. **Validação:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 33 testes verdes.

---

## Sessão #25 — Task 1.7 (Caos Determinístico e Contrato Publicado)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/107-mock-chaos`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** simular rejeições e falhas determinísticas e publicar o contrato idêntico ao compartilhado.

### Atividades Realizadas:
1. **Caos determinístico:**
   - `ChaosPolicy` atrasa a resposta por `X-Mock-Delay-Ms` e força `500/502/503` por `X-Mock-Force-Error`, sempre somente na requisição que carrega o header.
   - Beneficiário `-TIMEOUT` dorme `MOCK_TIMEOUT_DELAY_MS` nas duas primeiras leituras de status; a terceira responde normal.
   - Rejeições por sufixo `-REJ` e soma acima de `5000.00` já vinham da task 1.5 e foram revalidadas.
2. **Contadores no estado:**
   - O snapshot `/_admin/state` expõe `contadores.leiturasDeStatus` e o reset zera o caos junto com as fixtures.
3. **Contrato publicado:**
   - `resources/openapi/central-api.yaml` é cópia byte a byte de `contracts/central-api.yaml`, garantida por `ContractTest`.
4. **Validação:**
   - `./mvnw -B -pl central-convenios-mock -am verify` → 39 testes verdes.

---

## Sessão #26 — Arquivamento da Fase 1 e release v0.2.0

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `chore/017-phase-1-archive`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar a Fase 1, promover a `v0.2.0` e abrir a Fase 2 (backend core).

### Atividades Realizadas:
1. **Promoção da release:**
   - PR #29 `develop → staging` e PR #30 `staging → main`, ambos com CI verde.
   - PR #31 `chore(main): release 0.2.0` mergeado; tag `v0.2.0` e release publicadas.
2. **Imagens e staging:**
   - As três imagens foram publicadas no GHCR por `workflow_dispatch` (o `release-please` cria a tag pela API e não dispara o evento `push` da tag).
   - `CD Staging` verde: backend e mock com `{"status":"UP"}` e frontend respondendo.
3. **Arquivamento da fase:**
   - `SDD/specs/history/phases/phase-1-finished.md` com as 7 tasks, PRs, critérios de saída, digests e dívidas.
   - `PLAN.md` com a Fase 1 concluída e a task 2.1 em voo; `ROADMAP.md` com a `v0.2.0` publicada.
4. **Validação:**
   - `./mvnw -B verify` → reactor verde (backend + mock, 39 testes no mock).

---

## Sessão #27 — Task 2.1 (Esqueleto Hexagonal e Regras ArchUnit)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/021-backend-hexagonal-skeleton`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar as camadas do backend e travar as dependências proibidas desde o
  primeiro commit.

### Atividades Realizadas:
1. **Camadas do backend:**
   - `domain`, `application`, `adapters` e `config` declaradas com `package-info.java` que
     documenta a fronteira e as dependências permitidas de cada uma.
   - `config/CompositionRoot` com o bean `Clock.systemUTC()`: o único lugar que conhece
     implementações concretas.
2. **Regras ArchUnit:**
   - Domínio sem Spring, JPA, Jackson, HTTP ou logger; aplicação importando apenas
     `domain` e o JDK; domínio e aplicação nunca alcançando adaptadores; adaptadores de
     entrada nunca alcançando adaptadores de saída; `config` privado; camadas sem ciclos.
3. **Guard que não pode ficar verde por ausência:**
   - O engine JUnit 5 do ArchUnit 1.4.2 não descobre campos `@ArchTest` sob o JUnit
     Platform 1.12.2 (uma regra inválida passou sem rodar). As regras passaram a ser
     avaliadas em métodos `@Test` comuns.
   - Fixtures com violações deliberadas provam que cada regra rejeita o defeito que
     descreve, e uma violação temporária no código de produção quebrou o build.
4. **Validação:**
   - `./mvnw -q -pl backend test -Dtest=HexagonalArchitectureTest` → 9 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend + mock).

---

## Sessão #28 — Task 2.2 (Value Objects Compartilhados)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/022-backend-value-objects`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** substituir `String` e `BigDecimal` soltos por tipos fortes com invariante
  validada na construção.

### Atividades Realizadas:
1. **Identificadores (`domain/shared/`):**
   - `BeneficiaryId`, `AgreementId` e `InstallmentId`: não vazios, no máximo 64
     caracteres, normalizados com `trim`, igualdade por valor.
   - `IdempotencyKey`: UUID obrigatório (BR-07), com `parse` do texto e forma canônica.
2. **Valores de portabilidade (`domain/portability/`):**
   - `Money`: `BigDecimal` com exatamente 2 casas, nunca negativo, escala normalizada.
   - `ReceivingAccount`: `bank`, `branch` e `account` obrigatórios nos limites do
     contrato.
3. **Testes:** `SharedValueObjectsTest` e `PortabilityValueObjectsTest` — 28 testes de
   unidade cobrindo os casos válidos, inválidos e de igualdade por valor.
4. **Prova de vida:** remoção temporária do guard de valor negativo em `Money` quebrou o
   build (`Tests run: 6, Failures: 1`); o guard foi restaurado e a suíte voltou a ficar
   verde.
5. **Validação:**
   - `./mvnw -B -pl backend test` → 39 testes verdes no backend.
   - `./mvnw -B verify` → reactor verde (backend 39 testes, mock 39 testes).

---

## Sessão #29 — Task 2.3 (Agregado de Autorização)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/023-backend-authorization-aggregate`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** modelar o opt-in único por beneficiário e convênio (BR-02) e declarar a
  porta de persistência no domínio.

### Atividades Realizadas:
1. **Agregado:**
   - `Authorization`: record imutável com `AuthorizationId` (UUID), `BeneficiaryId`,
     `AgreementId` e `createdAt`; a fábrica `register` cria a identidade e o instante
     chega pelo relógio.
   - `AuthorizationAlreadyExistsException`: erro de domínio para o par já autorizado.
2. **Porta:**
   - `AuthorizationRepository` com `findByBeneficiaryAndAgreement` e `save`, sem anotação
     de framework; a unicidade do par é invariante da porta.
3. **Testes:**
   - `AuthorizationTest` (12 casos) e `AuthorizationRepositoryContractTest` (6 casos),
     com o fake em memória `InMemoryAuthorizationRepository` — sem mocks, conforme
     `stacks/java.md`.
4. **Prova de vida:** remoção temporária do guard de convênio nulo quebrou o build
   (`Tests run: 7, Failures: 1`); o guard foi restaurado e a suíte voltou a ficar verde.
5. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='AuthorizationTest,AuthorizationRepositoryContractTest'`
     → 18 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 57 testes, mock 39 testes).

---

## Sessão #30 — Task 2.4 (Agregado de Portabilidade e Máquina de Estados)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/024-backend-portability-aggregate`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** concentrar no domínio as invariantes da operação e as transições do
  ciclo de vida.

### Atividades Realizadas:
1. **Agregado:**
   - `PortabilityOperation`: record imutável com identidade, chave de idempotência,
     snapshot, conta de recebimento, total, status, protocolo, motivo, tentativas e
     instantes; cada transição devolve uma nova instância.
   - Invariantes no construtor: ao menos uma parcela, total igual à soma, credor
     cessionário não vazio, protocolo nulo em `CREATED`, motivo obrigatório apenas em
     `REJECTED`.
2. **Máquina de estados:**
   - `submit`, `registerIntegrationFailure`, `pollAttempt`, `confirm`, `reject` e
     `complete`, com as origens aceitas da tabela do `ARCHITECTURE.md` §7.
   - `OperationStatus.isTerminal()` garante que `COMPLETED` e `REJECTED` não saiam do
     estado; a recusa vem de `InvalidTransitionException`.
3. **Tipos de apoio:**
   - `InstallmentSnapshot` (retrato congelado da parcela) e `RejectionReason` selado com
     `MARGIN_INSUFFICIENT`.
4. **Testes:** 36 casos parametrizados cobrindo 6 estados × 6 gatilhos, mais 27 casos de
   invariantes — 63 testes na task.
5. **Prova de vida:** a transição `CREATED → CONFIRMED`, fora da tabela, quebrou o build
   (`Tests run: 43, Failures: 1`); a máquina foi restaurada e a suíte voltou a ficar
   verde.
6. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='PortabilityOperationTest,InstallmentSnapshotTest,RejectionReasonTest'`
     → 63 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 120 testes, mock 39 testes).

---

## Sessão #31 — Task 2.5 (Evento de Auditoria Append-Only)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/025-backend-audit-event`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** registrar cada mudança de estado em uma trilha imutável.

### Atividades Realizadas:
1. **Evento:**
   - `OperationEvent` com os campos do `ARCHITECTURE.md` §2: `eventId`, `operationId`,
     `previousStatus`, `newStatus`, `eventType`, `details`, `rawPayload` e `createdAt`.
   - `EventType` com os oito tipos estáveis do contrato público.
2. **Porta:**
   - `OperationEventRepository` expõe apenas `append` e `findByOperationId` — não existe
     caminho para alterar ou apagar um evento.
3. **Testes:** 19 casos, incluindo a prova por reflexão de que o evento não expõe mutador
   público, de que todo campo de instância é final e de que a porta só expõe `append` e a
   consulta; fake em memória, sem mocks.
4. **Prova de vida:** um mutador público temporário em `OperationEvent` quebrou o build
   (`Tests run: 12, Failures: 1`); o mutador foi removido e a suíte voltou a ficar verde.
5. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='OperationEventTest,OperationEventRepositoryContractTest,EventTypeTest'`
     → 19 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 139 testes, mock 39 testes).

---

## Sessão #32 — Task 2.6 (Registro e Consulta de Autorização)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/026-backend-register-authorization`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** caso de uso idempotente do opt-in, com replicação na Central e relógio
  injetado.

### Atividades Realizadas:
1. **Casos de uso:**
   - `RegisterAuthorization`: repete o opt-in, devolve o registro existente, replica o
     consentimento na Central e data o registro com o `Clock` injetado.
   - `FindAuthorization`: consulta o registro do par e devolve `Optional`.
2. **Porta:**
   - `CentralAuthorizationPort` em `domain/authorization/`, seguindo o padrão das outras
     portas do domínio.
3. **Ordem das operações:** a replicação na Central vem antes do armazenamento local, para
   que uma falha nunca deixe autorização local que a Central desconhece.
4. **Testes:** 9 casos com repositório e porta em memória, incluindo a corrida entre dois
   registros do mesmo par — o caso de uso devolve o vencedor.
5. **Prova de vida:** a remoção do retorno do registro existente quebrou o build
   (`Tests run: 6, Failures: 1`); a guarda foi restaurada e a suíte voltou a ficar verde.
6. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='RegisterAuthorizationTest,FindAuthorizationTest'`
     → 9 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 148 testes, mock 39 testes).

---

## Sessão #33 — Task 2.7 (Consulta de Agenda com ACL)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/027-backend-agenda-acl`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** traduzir o contrato PT da Central para o domínio EN, sem deixar o DTO
  externo escapar do adaptador.

### Atividades Realizadas:
1. **Domínio da agenda:**
   - `Installment`, `InstallmentStatus` (`OPEN`/`CLOSED`) e `BeneficiaryAgenda` com
     `openInstallments()` para a elegibilidade (BR-03).
   - `AgendaGateway` como porta de leitura da agenda na Central.
   - `DomainError` selado em `domain/shared/`, com os códigos estáveis da API pública.
2. **Aplicação:**
   - `QueryAgenda` lê a agenda pela porta e repassa as recusas da Central.
3. **Anti-corruption layer:**
   - `AgendaResponse` no adaptador de saída, com os nomes JSON em português.
   - `CentralAcl` traduz `ABERTA ↔ OPEN`, `FECHADA ↔ CLOSED` e converte `403` e `404` em
     erros de domínio.
4. **Guarda de arquitetura:**
   - Nova regra no `HexagonalArchitectureTest`: nenhum DTO externo fora do adaptador de
     saída da Central, com fixture que prova que a regra morde.
5. **Testes:** 26 casos na task — mapeamento nos dois sentidos, recusas e guarda.
6. **Prova de vida:** o mapeamento `ABERTA → CLOSED` quebrou o build (`Tests run: 7,
   Failures: 2`); o mapeamento foi restaurado e a suíte voltou a ficar verde.
7. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='HexagonalArchitectureTest,CentralAclTest,QueryAgendaTest'`
     → 26 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 165 testes, mock 39 testes).

---

## Sessão #34 — Task 2.8 (Persistência e Migração V1)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/028-backend-persistence-v1`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar as três tabelas, os índices, as constraints e o trigger append-only
  do `ARCHITECTURE.md` §6, com as entidades JPA e os repositórios.

### Atividades Realizadas:
1. **Migração V1:**
   - `V1__create_schema.sql` com `authorizations`, `portability_operations` e
     `operation_events`, os dois índices, a `CHECK` de status, a `CHECK` de snapshot não
     vazio, a `CHECK` de total não negativo e o trigger append-only.
2. **Domínio e adaptadores:**
   - Porta `PortabilityOperationRepository` no domínio.
   - `JpaAuthorizationRepository`, `JpaPortabilityOperationRepository` e
     `JpaOperationEventRepository` sobre as entidades JPA, com o mapeamento do snapshot
     em JSONB e o `Clock` injetado para o `updated_at`.
   - `RawPayloadCodec` guarda o payload bruto da Central como string JSON, byte a byte.
3. **Infraestrutura de teste:**
   - `PostgreSqlTestSupport` com um PostgreSQL 16 por JVM, apontado por
     `@DynamicPropertySource`.
   - `ConsigPortApplicationTests` passou do H2 para o PostgreSQL real, e o H2 saiu do
     `backend/pom.xml`.
4. **Testes:** 21 casos de integração — 11 de constraints (status, snapshot, chaves
   únicas, trigger) e 10 de round trip dos agregados.
5. **Prova de vida:** a `CHECK` de status virada em `status IS NOT NULL` quebrou o build
   (`Tests run: 11, Failures: 1`); a constraint foi restaurada e o reactor voltou a ficar
   verde (186 testes no backend, 39 no mock).
6. **Validação:**
   - `./mvnw -B -pl backend test -Dtest=PersistenceConstraintsTest` → 11 testes verdes.
   - `./mvnw -B -pl backend test -Dtest=JpaRepositoriesTest` → 10 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 186 testes, mock 39 testes).

---

## Sessão #35 — Task 2.9 (Início da Operação com Idempotência)

- **Data:** 2026-11-26
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/029-backend-start-operation-idempotency`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** criar a operação, congelar o snapshot das parcelas e responder `202`, com
  a idempotência da chave e o replay sinalizado no header.

### Atividades Realizadas:
1. **Domínio:**
   - `OperationRequest` (o pedido e a regra do "mesmo payload") e `OperationPolicy`
     (elegibilidade e congelamento do snapshot).
   - `IdempotencyKeyAlreadyUsedException` e a porta `TransactionRunner`.
   - `DomainError` selado com `IdempotencyConflict`, `IdempotencyKeyRequired`,
     `InstallmentNotEligible` e `CentralUnavailable`.
2. **Aplicação:**
   - `StartOperation`: replay, conflito, elegibilidade, snapshot, evento de criação e a
     corrida da chave de idempotência.
3. **Aresta HTTP:**
   - `PortabilityOperationResource` com o header `Idempotent-Replay` e
     `ApiExceptionHandler` com `code`, `message`, `locale`, `timestamp`, `path` e
     `details`.
   - DTOs do pedido, da resposta aceita e do erro.
4. **Persistência:**
   - `SpringTransactionRunner` (operação + evento na mesma transação) e
     `UniqueViolations` (SQLSTATE `23505` com o nome da constraint).
5. **Testes:** 9 unitários do caso de uso e 8 de integração HTTP sobre PostgreSQL 16, com
   duas chamadas concorrentes e uma divergente.
6. **Prova de vida:** a comparação de payload das parcelas virando `true` quebrou o
   build (`Tests run: 9, Failures: 2`); a comparação foi restaurada e a suíte voltou a
   ficar verde.
7. **Validação:**
   - `./mvnw -B -pl backend test -Dtest=StartOperationTest` → 9 testes verdes.
   - `./mvnw -B -pl backend test -Dtest=PortabilityOperationResourceTest` → 8 testes
     verdes.
   - `./mvnw -B verify` → reactor verde (backend 203 testes, mock 39 testes).

## Sessão #36 — Task 2.10 (Assinatura HMAC e Envio à Central)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/030-backend-central-hmac-submission`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** assinar a operação na forma canônica da Central, enviá-la e absorver o
  protocolo; uma falha depois das tentativas deixa a operação retomável, nunca duplicada.

### Atividades Realizadas:
1. **Domínio:**
   - `CentralPortabilityPort` (porta de envio), `CentralSubmissionException` (status e
     código da Central) e `ConcurrentOperationUpdateException` (bloqueio otimista).
2. **Adaptador de saída:**
   - `HmacSigner`, dono da forma canônica (`canonicalJson`, `canonicalBody` sem
     `assinatura`, `canonicalString`, `sign`), reproduzindo o vetor compartilhado.
   - `CentralHttpClient`, que implementa `AgendaGateway` e `CentralPortabilityPort`,
     assina, envia e repete 3 vezes com backoff exponencial, repetindo só 408/429/5xx.
   - `CentralProperties` (URL, segredo, tentativas, backoff e timeouts) e
     `PortabilityAcl` com os DTOs do contrato PT da Central.
3. **Aplicação:**
   - `StartOperation` grava a criação em uma transação, submete fora dela e grava
     `PENDING`/`OPERATION_SUBMITTED` ou `INTEGRATION_ERROR`/`INTEGRATION_FAILED` em
     outra; o replay de uma operação `CREATED` completa a submissão.
4. **Persistência e wiring:**
   - `JpaPortabilityOperationRepository` traduz o bloqueio otimista; `CompositionRoot`
     monta o cliente e o caso de uso; `application.yml` ganha a seção `central`.
5. **Testes:** 5 do assinante (o vetor de `contracts/hmac-vector.json`), 10 do cliente
   HTTP com `MockRestServiceServer`, 16 do caso de uso e 11 de integração HTTP sobre
   PostgreSQL 16.
6. **Prova de vida:** a ordenação das chaves do corpo canônico virando `false` quebrou o
   build (`Tests run: 5, Failures: 2`); a forma canônica foi restaurada e a suíte voltou
   a ficar verde.
7. **End-to-end com o backend real contra o mock real:** `202` com `PENDING` e protocolo
   `PRT-20261127-0002`, replay byte a byte igual, payload divergente `409`, dois eventos
   na trilha e a Central confirmando a operação; uma recusa da Central deixou a
   operação em `INTEGRATION_ERROR`, com `retry_count` 1 e `next_poll_at` em 60 s.
8. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='HmacSignerTest,CentralHttpClientTest,StartOperationTest'` → 31 testes verdes.
   - `./mvnw -B -pl backend test -Dtest='PortabilityOperationResourceTest'` → 11 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 228 testes, mock 39 testes).

## Sessão #37 — Task 2.11 (Polling Resiliente)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider ZEN) — DeepSeek V4.1 Flash Free
- **Branch:** `feat/031-backend-resilient-polling`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** acompanhar a Central até o estado terminal, com worker de 2 s e lote de
  20, honrando o `Retry-After`, repetindo com backoff e circuit breaker, e reenviando a
  operação que falhou com a mesma `Idempotency-Key`.

### Atividades Realizadas:
1. **Domínio:**
   - `CentralStatus` (selado: `Pending` com o `Retry-After`, `Confirmed` e `Rejected`,
     sempre com o corpo bruto da Central) e `CentralStatusPort` (a porta de leitura do
     status).
   - `PortabilityOperation.renewIntegrationBackoff`: a falha de uma operação que já
     está em `INTEGRATION_ERROR` renova o backoff e a contagem sem mudar o status,
     porque a tabela de estados do `ARCHITECTURE.md` §7 não tem a auto-transição.
2. **Aplicação:**
   - `PollOperation` (lote das operações devidas, consulta do status, reenvio sem
     protocolo com a mesma chave, operação e evento gravados na mesma transação) e
     `IntegrationBackoff` (2 s dobrando até 60 s).
3. **Adaptadores:**
   - `CentralHttpClient.statusOf`: `204` com `Retry-After`, `200` terminal, `404` e
     corpo fora do contrato como falha, com 3 tentativas e o circuito.
   - `PortabilityAcl.toCentralStatus` e o DTO `CentralStatusResponse` (tradução PT → EN).
   - `CircuitBreaker` (janela 10, 50% de falhas, 10 s aberto, 3 chamadas em half-open) e
     a consulta `findDueForPolling` do repositório.
4. **Wiring:** `ResilienceConfig`, `SchedulingConfig` (`fixedDelay` de 2 s, lote de 20,
   desligável com `central.poll.enabled`), `CompositionRoot` e `application.yml`.
5. **Testes:** 13 do worker, 6 do backoff, 8 do circuito, 17 do cliente HTTP, 7 de
   integração do polling sobre PostgreSQL 16, 2 do agendamento e 45 do agregado.
6. **Prova de vida:** ignorar o `Retry-After` no worker quebrou o build
   (`Tests run: 13, Failures: 2`); o `Retry-After` foi restaurado e a suíte voltou a
   ficar verde.
7. **End-to-end com o backend real contra o mock real:** `204` virando `POLL_ATTEMPT`
   com o intervalo de 2 s honrado, a confirmação terminal, o timeout determinístico da
   Central sobrevivo pelas 3 tentativas (3 leituras contadas no mock), o reenvio com a
   mesma `Idempotency-Key` (o mock recusou de novo com `409 IDEMPOTENCIA_CONFLITO`) e a
   queda da Central deixando a operação em `INTEGRATION_ERROR` com o backoff de 2 s, 4 s
   e 8 s.
8. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='PollOperationTest,IntegrationBackoffTest,CircuitBreakerTest,CentralHttpClientTest'` → 44 testes verdes.
   - `./mvnw -B -pl backend test -Dtest='PollingPersistenceTest,SchedulingConfigTest,PortabilityOperationTest'` → 64 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 273 testes, mock 39 testes).

## Sessão #38 — Task 2.12 (Auto-transição de integração e respostas tipadas da Central)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/032-backend-typed-central-answers`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** alinhar a máquina de estados, a trilha e o fluxo do worker, e distinguir as
  respostas da Central que antes caíam no mesmo balde de falha: o `404` da Central e a
  rejeição com um motivo que o contrato não declara.

### Atividades Realizadas:
1. **Decisão com o responsável:** três pontos foram levados à decisão — a auto-transição
   `INTEGRATION_ERROR` → `INTEGRATION_ERROR`, o tratamento do `404`
   (`OPERACAO_NAO_ENCONTRADA`) e o motivo de rejeição desconhecido. A escolha foi a
   auto-transição explícita (opção B), com a linha nova na §7 e a autorização para
   editar o `ARCHITECTURE.md`; e a tipagem das respostas da Central com o fechamento da
   rejeição desconhecida (opções 1+2), sem alterar o contrato público.
2. **Domínio:**
   - `PortabilityOperation.registerIntegrationFailure` aceita a origem
     `INTEGRATION_ERROR`; `renewIntegrationBackoff` foi removido e o caminho de falha
     ficou único.
   - `CentralStatus.NotFound`: a Central não conhece o protocolo, com o corpo bruto.
   - `RejectionReason.Unmapped`: o motivo fora do contrato, com o código público
     `UNMAPPED_REJECTION`.
3. **Adaptadores:**
   - `PortabilityAcl.toRejectionReason` traduz o motivo desconhecido para `Unmapped`; a
     rejeição sem motivo continua violando o contrato.
   - `CentralHttpClient.statusOf`: o `404` vira `NotFound` e conta como chamada
     bem-sucedida no circuito — a Central respondeu, ela está disponível.
4. **Aplicação:** `PollOperation` trata o `NotFound` como falha retomável, grava o corpo
   bruto da Central no evento e usa `registerIntegrationFailure` em todos os caminhos de
   falha.
5. **Regras e specs:** `ARCHITECTURE.md` §7 (linha da auto-transição), §3 (tradução do
   motivo desconhecido) e §11 (passo do `404` no fluxo do worker); ADR-0003 (protocolo de
   polling); `BACKLOG.md` (task 2.12, renumeração de 2.12–2.16 para 2.13–2.17 e três
   dívidas registradas); `PLAN.md` e `ROADMAP.md`.
6. **Testes:** 45 do agregado (com a auto-transição na matriz), 16 do worker, 19 do
   cliente HTTP, 8 da nova `PortabilityAclTest` e 6 da razão selada.
7. **Prova de vida:** sem a linha da §7 na matriz, a suíte quebrou
   (`Tests run: 45, Failures: 1`); a linha foi restaurada e a suíte voltou a ficar verde.
8. **End-to-end com o backend real contra o mock real:** o reset administrativo do mock
   (`POST /_admin/reset → 204`) fez a Central esquecer as operações sem sair do ar; o
   `404` virou `INTEGRATION_FAILED` com o corpo bruto (`OPERACAO_NAO_ENCONTRADA`), a
   segunda falha provou a auto-transição (`previous_status = new_status =
   INTEGRATION_ERROR`) com o backoff dobrando de 2 s para 4 s, uma nova operação foi
   aceita e confirmada depois de 14 `404`s (o circuito não foi poluído) e uma rejeição
   com o motivo `LIMITE_DE_PARCELAS` (patch temporário no mock, revertido depois) fechou a
   operação como `REJECTED` com o código `UNMAPPED_REJECTION`.
9. **Descoberta fora do escopo:** o end-to-end expôs um defeito pré-existente — a
   sequência de protocolos do mock reinicia a cada reset e o esquema garante `protocol`
   único, então a colisão respondia `500` em vez de um erro mapeado. Registrado como
   dívida no `BACKLOG.md`.
10. **Validação:**
    - `./mvnw -B -pl backend test -Dtest='PortabilityOperationTest,RejectionReasonTest,PollOperationTest,CentralHttpClientTest,PortabilityAclTest'` → 104 testes verdes.
    - `./mvnw -B verify` → reactor verde (backend 289 testes, mock 39 testes).
    - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #39 — Task 2.13 (Confirmação e atualização de conta)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/033-backend-complete-operation`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar o ciclo da operação em uma única transação: a confirmação da
  Central vira `CONFIRMED` e, na mesma transação, a conta recebe a atualização e a
  operação vira `COMPLETED` com `account_updated_at`; a rejeição fecha a operação como
  `REJECTED` com o motivo.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.13 em voo),
   `SDD/stacks/java.md` e `SDD/stacks/testing.md`; o `ARCHITECTURE.md` §7 e §11 e o
   ADR-0003 já declaravam o caminho `CONFIRMED` → `COMPLETED` — nenhuma regra precisou
   ser alterada.
2. **Aplicação:**
   - `CompleteOperation`: a confirmação fecha a operação (`CONFIRMED` → `COMPLETED` com
     `account_updated_at`) e a rejeição a encerra como `REJECTED` com o motivo.
   - `TransitionRecorder`: a fronteira de transação e a política do lock otimista em um
     lugar só, compartilhada pelo worker e pelo fechamento.
   - `PollOperation`: entrega a resposta terminal da Central ao `CompleteOperation`.
3. **Domínio:** `PortabilityOperation` ganhou o invariante `account_updated_at` existe
   apenas quando a operação está `COMPLETED`.
4. **Wiring:** `CompositionRoot` instancia os dois casos de uso novos.
5. **Testes:** 7 do `CompleteOperation` (caminho completo, uma transação só, o
   `rawPayload` só no evento da Central, transições recusadas), 3 de integração da
   persistência sobre PostgreSQL 16 (fechamento, rejeição e **rollback completo**),
   7 do polling persistido, 16 do worker, 48 do agregado e 2 do agendamento.
6. **Prova de vida:** com os eventos gravados fora da transação, o teste de rollback
   ficou vermelho (`expected: PENDING but was: COMPLETED`); com a trilha dentro da
   transação, voltou a ficar verde.
7. **End-to-end com o backend real contra o mock real:** a operação confirmada percorreu
   `CREATED` → `PENDING` → `CONFIRMED` → `COMPLETED` com `account_updated_at` gravado e
   a trilha com `OPERATION_CONFIRMED`, `ACCOUNT_UPDATED` e `OPERATION_COMPLETED`; a
   rejeitada fechou como `REJECTED` com `MARGIN_INSUFFICIENT` e o corpo bruto da Central
   na trilha. Nenhum processo ou container sobrou.
8. **Descoberta:** a primeira execução do end-to-end usou o jar anterior à task e a
   operação parou em `CONFIRMED` — a lacuna real da task, observada antes de existir a
   correção.
9. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='CompleteOperationTest,PollOperationTest,CompletionPersistenceTest,PollingPersistenceTest,PortabilityOperationTest,SchedulingConfigTest'` → 93 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 301 testes, mock 39 testes).
   - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #40 — Task 2.14 (Consulta de operações e trilha)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/034-backend-operation-queries`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** expor a leitura paginada das operações com filtro por status, o detalhe
  de uma operação e a trilha da auditoria em ordem cronológica; a operação que não
  existe responde `404 OPERATION_NOT_FOUND`.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.14 em voo),
   `SDD/stacks/java.md` e `SDD/stacks/testing.md`; o `ARCHITECTURE.md` §8 e o contrato
   público já declaravam as rotas e o formato do erro — nenhuma regra precisou ser
   alterada.
2. **Aplicação:**
   - `ListOperations`: a consulta paginada e filtrada pelo estado, com o total do
     conjunto na página.
   - `FindOperation`: o detalhe de uma operação; a inexistente vira
     `DomainError.OperationNotFound` com o código `OPERATION_NOT_FOUND`.
   - `ListOperationEvents`: a trilha da auditoria em ordem cronológica.
3. **Domínio:** `Page` (página imutável com o total), `OperationFilter` (todas ou as de
   um estado) e os modelos de leitura `OperationSummary` e `OperationDetail`.
4. **Persistência:** a porta ganhou `list` e `findDetailById`; o adaptador JPA faz a
   contagem e a página, com a ordem estável `createdAt, id`.
5. **Borda HTTP:** `GET /api/v1/portability-operations` (com `status`, `page` e `size`) e
   `GET /api/v1/portability-operations/{id}`, com os DTOs do contrato; o handler passou a
   traduzir `OPERATION_NOT_FOUND` em `404`.
6. **Testes:** 2 do `FindOperation`, 6 do `ListOperations`, 3 do `ListOperationEvents`, 6
   do `Page`, 5 de integração da persistência sobre PostgreSQL 16 (25 operações,
   paginação conferida) e 9 da borda HTTP sobre HTTP real.
7. **End-to-end com o backend real contra o mock real:** quatro operações (três
   confirmadas e uma rejeitada); a lista paginada devolveu `totalElements: 4` e
   `totalPages: 2`, com as duas páginas sem repetir operação; o filtro por estado
   devolveu 3 `COMPLETED` e 1 `REJECTED`; o detalhe veio completo; a operação
   inexistente respondeu `404 OPERATION_NOT_FOUND`; página negativa, tamanho 101 e
   estado inexistente responderam `400 VALIDATION_ERROR`. Nenhum processo ou container
   sobrou.
8. **Descoberta:** `OPERATION_NOT_FOUND` estava no contrato e no `ARCHITECTURE.md` §8,
   mas não estava mapeado no handler — a consulta de uma operação inexistente
   respondia `500` em vez de `404`.
9. **Decisão:** a consulta sai por modelos de leitura, e não pelo agregado: `updated_at`
   pertence à linha do armazenamento e o contrato público exige `updatedAt` no resumo e
   no detalhe.
10. **Validação:**
    - `./mvnw -B -pl backend test -Dtest='FindOperationTest,ListOperationsTest,ListOperationEventsTest,SharedValueObjectsTest,QueryPersistenceTest,OperationQueryResourceTest'` → 49 testes verdes.
    - `./mvnw -B verify` → reactor verde (backend 332 testes, mock 39 testes).
    - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #41 — Task 2.15 (Recursos REST e tratamento de erros)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/035-backend-rest-resources`
- **PR:** #47 — https://github.com/marcelinosandroni/consigport-platform/pull/47
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** completar a superfície `/api/v1` com o opt-in, a consulta do opt-in, a
  agenda e a trilha da auditoria, e fechar o tratamento de erros com o formato do
  contrato público (`code`, `message`, `locale`, `timestamp`, `path`, `details`) e os
  códigos EN estáveis.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.15 em voo),
   `SDD/stacks/java.md` e `SDD/stacks/testing.md`; o `ARCHITECTURE.md` §8 e o contrato
   público já declaravam as rotas, os status e o formato do erro — nenhuma regra precisou
   ser alterada, e nem `SDD/ARCHITECTURE.md` nem os contratos foram tocados.
2. **Aplicação:**
   - `RegisterAuthorization`: devolve o registro e o sinal de criação, para o recurso
     distinguir `201` do opt-in novo do `200` do opt-in repetido.
   - `FindAuthorization`: a consulta do opt-in mais recente do beneficiário.
   - `QueryAgenda`: o caso de uso da agenda, que exige o opt-in registrado.
3. **Domínio:** `DomainError.AuthorizationNotFound` (código estável
   `AUTHORIZATION_NOT_FOUND`) e a porta `AuthorizationRepository.findLatestByBeneficiary`,
   com a ordem estável `createdAt desc, id desc`.
4. **Persistência:** o adaptador JPA implementa a porta nova; o fake em memória dos testes
   também, para a porta ter um único contrato.
5. **Borda HTTP:** `POST /api/v1/authorizations` (`201`/`200`),
   `GET /api/v1/authorizations/{beneficiaryId}` (`200`/`404`),
   `GET /api/v1/beneficiaries/{beneficiaryId}/agenda` (`200`/`403`/`404`) e
   `GET /api/v1/portability-operations/{id}/events` (`200`/`404`), com os DTOs do contrato.
6. **Tratamento de erros:** o handler traduz `AUTHORIZATION_NOT_FOUND` em `404` e ganhou um
   tratador genérico para `500 INTERNAL_ERROR`, no formato do contrato, com a causa real
   apenas no log do servidor.
7. **Adaptador da Central:** `CentralHttpClient` passou a implementar também a
   `CentralAuthorizationPort` — a porta existia no domínio, mas só tinha fake em memória.
   O consentimento é replicado em `POST /autorizacoes`, assinado em HMAC como as outras
   chamadas; a falha da Central vira `DomainError.CentralUnavailable` (`503`).
8. **Testes:** 5 do recurso de autorização, 3 do recurso de agenda, 3 do recurso de
   trilha, 10 do handler (um caso por código estável), 4 novos do cliente da Central e o
   fake do repositório de autorizações com a porta nova.
9. **End-to-end com o backend real contra o mock real:** o opt-in novo respondeu `201`, o
   repetido `200` sem duplicar no banco; a consulta do opt-in devolveu `200` e `404`; o
   opt-in sem beneficiário devolveu `400 VALIDATION_ERROR` com o campo no detalhe; a
   agenda devolveu `200` com as parcelas, `403 AUTHORIZATION_REQUIRED` sem opt-in e
   `404 BENEFICIARY_NOT_FOUND` para um beneficiário que a Central não conhece; a trilha
   saiu em ordem cronológica, do `CREATED` ao `COMPLETED`, com o payload bruto da Central
   como objeto; a trilha de uma operação inexistente devolveu
   `404 OPERATION_NOT_FOUND`; o erro público exibiu os seis campos do contrato; e a
   Central recebeu a réplica do opt-in. Nenhum processo ou container sobrou.
10. **Descoberta:** a primeira chamada do mock da Central leva ~8 s numa JVM fria (o
    `Mac.init` carrega as classes do JCE e a validação do corpo carrega o MVC), acima do
    timeout de leitura de 3 s do backend — o `POST /api/v1/authorizations` respondia
    `503 CENTRAL_UNAVAILABLE` sem que a Central tivesse recusado a requisição. Diagnóstico
    com proxy TCP e `jstack` do mock durante a chamada. O E2E passa a aquecer o mock e a
    zerar o estado com `POST /_admin/reset` antes das asserções.
11. **Descoberta:** `AUTHORIZATION_NOT_FOUND` não era mapeado no handler — a consulta de um
    beneficiário sem opt-in respondia `500` em vez de `404`.
12. **Decisão:** `GET /authorizations/{beneficiaryId}` devolve **um** registro, como manda o
    contrato, e o backend pode ter um opt-in por convênio: a resposta é o mais recente
    (`createdAt`, depois `id` como desempate), nunca um registro arbitrário.
13. **Dívida registrada:** `POST /api/v1/authorizations` responde `503 CENTRAL_UNAVAILABLE`
    quando a Central não registra o consentimento, mas o contrato público lista apenas
    `400` e `500` para essa rota. A dívida está no `BACKLOG.md`, para alinhar contrato e
    mapeamento com a autorização do responsável; o contrato não foi alterado.
14. **Validação:**
    - `./mvnw -B -pl backend test -Dtest='ApiExceptionHandlerTest,AuthorizationResourceTest,AgendaResourceTest,OperationEventResourceTest,RegisterAuthorizationTest,FindAuthorizationTest,AuthorizationRepositoryContractTest,CentralHttpClientTest'` → 59 testes verdes.
    - `./mvnw -B verify` → reactor verde (backend 357 testes, mock 39 testes).
    - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #42 — Task 2.16 (i18n, Swagger e snapshot OpenAPI)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/036-backend-i18n-openapi`
- **PR:** #48 — https://github.com/marcelinosandroni/consigport-platform/pull/48
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** localizar as mensagens humanas pela negociação do `Accept-Language` (com
  fallback `pt-BR` e o código do erro nunca traduzido), publicar a documentação viva da
  API em `/v3/api-docs` e no Swagger UI, e provar que o contrato gerado é equivalente ao
  snapshot público `contracts/consigport-api.yaml`.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.16 em voo), o
   `ARCHITECTURE.md` §8 e §12 e o ADR-0004 (regras de i18n e OpenAPI). Nenhuma regra
   precisou ser alterada: nem o `SDD/ARCHITECTURE.md` nem os contratos públicos foram
   tocados.
2. **Catálogos das mensagens:** três bundles — `messages_pt_BR.properties`,
   `messages_en.properties` e `messages_es.properties` — com a chave de cada entrada
   sendo o próprio código estável do erro. O código nunca é traduzido; só a mensagem
   humana. As chaves da Bean Validation (`jakarta.validation.constraints.*.message`)
   também estão nos três catálogos.
3. **Negociação do locale:** `config/MessageSourceConfig.java` publica o `MessageSource`
   (basename `i18n/messages`, UTF-8, `fallbackToSystemLocale=false`), o
   `AcceptHeaderLocaleResolver` (padrão `pt-BR`, idiomas `pt-BR`/`en`/`es`) e o
   `LocalValidatorFactoryBean` ligado aos mesmos catálogos.
4. **Handler de erros:** o mapa fixo de mensagens pt-BR do `ApiExceptionHandler` foi
   removido; a mensagem vem do catálogo, resolvida pelo código e pelo locale negociado
   (`LocaleContextHolder.getLocale()`).
5. **Documentação viva:** `config/OpenApiConfig.java` publica a informação da API
   (título `ConsigPort API`, versão `1.0.0`, contato, licença MIT, servidor local, quatro
   tags) e um `OpenApiCustomizer` que injeta o header `Accept-Language` em todas as
   operações.
6. **Borda HTTP:** os quatro recursos receberam `@Tag`, `@Operation` com o `operationId`
   do contrato público (`registerAuthorization`, `findAuthorization`,
   `getBeneficiaryAgenda`, `startPortabilityOperation`, `listPortabilityOperations`,
   `findPortabilityOperation`, `listOperationEvents`), `@ApiResponse` por status, o
   `Idempotency-Key` publicado como obrigatório e o header `Idempotent-Replay` no `202`.
7. **DTOs:** todos receberam `@Schema(name=..., requiredProperties=...)`; os dois DTOs da
   conta de recebimento (`ReceivingAccountRequest` e `ReceivingAccountResponse`) foram
   colapsados em um só (`ReceivingAccount`), como manda o contrato, que publica um
   único schema.
8. **Teste de contrato:** `GeneratedContractTest` sobe o backend real (Testcontainers) e
   compara o `/v3/api-docs` com o snapshot `contracts/consigport-api.yaml`, por meio do
   `ContractComparator`: rotas, métodos, `operationId`, parâmetros (nome, lugar,
   obrigatoriedade), status, corpo obrigatório e o shape dos schemas (propriedades,
   tipos, enums e obrigatoriedade), resolvendo `$ref`/`allOf`/`oneOf`.
9. **Prova de vida:** a anotação do `404` de uma rota foi removida do recurso e o teste
   de contrato ficou vermelho, apontando a diferença; com a anotação restaurada, o
   teste voltou ao verde.
10. **Descoberta:** `@Schema(requiredMode=REQUIRED)` no nível do tipo **não** propaga
    para as propriedades; `@Schema(requiredProperties={...})` no tipo **gera** o array
    `required` do componente. Verificado empiricamente antes de ser aplicado.
11. **Descoberta:** o springdoc não sobe sem `jackson-dataformat-yaml` no classpath — o
    bean `OpenAPI` declara a dependência do YAML e o backend falhava ao subir. A
    dependência foi declarada explicitamente no `backend/pom.xml`.
12. **Descoberta:** o comparador precisa resolver as referências **de cada documento no
    seu próprio `components`**. Resolver a referência de um documento no `components` do
    outro produzia diferenças falsas (o `beneficiaryId` do contrato era lido como um
    parâmetro vazio).
13. **Decisão:** a comparação canônica ignora o que não muda o contrato do consumidor
    (descrições, exemplos, `format`, limites numéricos, nome dos componentes) e compara
    o que muda (rota, método, `operationId`, parâmetros, status, corpo obrigatório e
    shape do schema). O contrato público é a fonte de verdade e não foi alterado.
14. **End-to-end com o backend real:** a mensagem seguiu o `Accept-Language` em `en`,
    `es` e `pt-BR`, com o código `AUTHORIZATION_NOT_FOUND` intacto; um idioma não
    suportado (`fr`) e a ausência de header caíram no fallback `pt-BR`; o detalhe da
    falha de validação também veio no idioma do chamador (`must not be blank` em en,
    `no debe estar en blanco` em es); o `/v3/api-docs` publicado traz os sete
    `operationId` do contrato, o header `Accept-Language` em todas as operações e os
    status esperados; o Swagger UI respondeu `302` e `200`. Nenhum processo ou container
    sobrou no final.
15. **Validação:**
    - `./mvnw -B -pl backend test -Dtest='ApiExceptionHandlerTest,LocaleNegotiationTest,GeneratedContractTest,AuthorizationResourceTest,AgendaResourceTest,OperationEventResourceTest'` → 43 testes verdes.
    - `./mvnw -B verify` → reactor verde (backend 379 testes, mock 39 testes).
    - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #43 — Task 2.17 (Gate de cobertura do núcleo)

- **Data:** 2026-11-27
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/037-backend-coverage-gate`
- **PR:** #49 — https://github.com/marcelinosandroni/consigport-platform/pull/49
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** ativar o portão de cobertura do backend sobre o núcleo (domínio e casos de
  uso), cobrar **80% de linhas e de branches** como manda o portão 3.2 do
  `docs/quality/GATES.md`, fechar as lacunas de branch e publicar o relatório no CI.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.17 em voo) e o
   portão 3.2 do `docs/quality/GATES.md`, que exige 80% de linhas e de branches no
   domínio e nos casos de uso. Nenhuma regra precisou ser alterada.
2. **Divisão da task:** a 2.17 original ("Fechamento do backend") reunia o portão de
   cobertura, os testes do núcleo e a prova do ciclo de vida com o banco real — mais de
   uma hora de trabalho. Foi dividida antes de começar: a **2.17** virou o portão de
   cobertura com os testes do núcleo, e a **2.18** ficou com a prova do ciclo de vida
   completo com o PostgreSQL real. O `PLAN.md`, o `BACKLOG.md` e o `ROADMAP.md` foram
   atualizados (a Fase 2 passa de 17 para 18 tasks; o total do projeto, de 52 para 53).
3. **Medição da baseline:** o JaCoCo foi ativado no módulo `backend` e o portão foi
   medido pela primeira vez: **74,64% de branches** (linhas em 89,74%), abaixo dos 80%
   exigidos, com o build vermelho e a regra apontada.
4. **Mapeamento das lacunas:** as branches perdidas estavam concentradas nos invariantes
   recusados pelo domínio e nunca exercitados — `OperationDetail` (23 perdidas),
   `PortabilityOperation` (20), `OperationRequest` (15), `OperationSummary` (6),
   `Installment` (5), `Page`, `OperationId`, `CentralSubmissionException`, os três status
   tipados da Central, `BeneficiaryAgenda`, `IntegrationBackoff`, `RejectionReason`, o
   filtro e o evento de auditoria.
5. **Portão:** `pom.xml` ganhou a propriedade `jacoco.branch.coverage` (0,80), os
   `includes` que restringem a medição ao núcleo (`br/com/consigport/domain/**` e
   `br/com/consigport/application/**`) e o segundo limite da regra (`BRANCH` com
   `COVEREDRATIO`); `backend/pom.xml` passou a declarar o plugin.
6. **Testes novos:** `OperationReadModelsTest` (25 casos), `OperationRequestTest`
   (18 casos), `AgendaReadModelsTest` (10 casos) e `CentralStatusTest` (13 casos), mais as
   ampliações em `PortabilityOperationTest`, `SharedValueObjectsTest`, `OperationEventTest`,
   `RejectionReasonTest` e `IntegrationBackoffTest`.
7. **Descoberta — defeito no domínio:** a recusa da parcela nula em `OperationRequest` era
   código morto. `List.copyOf` recusa um elemento nulo com `NullPointerException`, e a
   cópia vinha **antes** da checagem: o invariante declarado pelo domínio
   (`installmentIds must not contain null`) nunca podia ser disparado, e o chamador
   recebia uma `NullPointerException` em vez da `IllegalArgumentException` do contrato.
   A recusa agora vem antes da cópia.
8. **Descoberta — portão sem implementação:** o `GATES.md` §3.2 exige
   `./mvnw -q spotless:check`, mas nenhum `pom.xml` declara o plugin Spotless: o comando
   falha com "no such goal". Ativar o Spotless exige formatar o código existente, o que
   produz um diff largo que deve ficar em task própria. A dívida está registrada no
   `BACKLOG.md`, nas dívidas da Fase 2.
9. **CI:** o job `backend` publica o relatório de cobertura como o artefato
   `backend-coverage`.
10. **Prova de vida:** o limiar de branches foi elevado artificialmente para `1,00` e o
    build ficou vermelho, com a regra apontada; com o limiar restaurado em `0,80`, o
    build voltou ao verde.
11. **Validação:**
    - `./mvnw -B -pl backend test -Dtest='OperationReadModelsTest,OperationRequestTest,AgendaReadModelsTest,CentralStatusTest,PortabilityOperationTest,SharedValueObjectsTest,OperationEventTest'` → 179 testes verdes.
    - `./mvnw -B verify` → reactor verde (backend 478 testes, mock 39 testes).
    - Cobertura final do núcleo: **99,71% de branches**, 98,12% de linhas, 100% de classes.
    - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #44 — Task 2.18 (Fluxo completo com o banco real)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/038-backend-lifecycle-integration`
- **PR:** #50 — https://github.com/marcelinosandroni/consigport-platform/pull/50
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** provar o ciclo de vida inteiro de uma operação de portabilidade contra o
  PostgreSQL 16 real — `CREATED` → `PENDING` → `CONFIRMED` → `COMPLETED` com a conta
  atualizada e a trilha completa, `CREATED` → `PENDING` → `REJECTED` com o motivo e a
  trilha, e o rollback quando o registro do evento falha.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/AGENTS.md`, `SDD/specs/PLAN.md` (task 2.18 em voo),
   `SDD/stacks/testing.md` (a regra que proíbe chamar uma API de terceiro num teste de
   integração) e os casos de uso `StartOperation`, `PollOperation` e `CompleteOperation`.
2. **Mapeamento da lacuna:** o ciclo de vida já estava coberto em partes —
   `CompletionPersistenceTest` provava a confirmação a partir de `PENDING` e
   `PollingPersistenceTest` provava o worker — mas nenhum teste percorria o caminho
   inteiro com a operação criada pelo caso de uso e fechada pela Central.
3. **Teste de integração:** `OperationLifecyclePersistenceTest`, no pacote
   `application/portability`, com o `@SpringBootTest` sobre o PostgreSQL 16 do
   Testcontainers: os adaptadores JPA, as transações Spring e a migração do Flyway são
   reais; só a Central é simulada. Três cenários: a confirmação (com um 204 antes), a
   rejeição com o motivo e o rollback.
4. **Prova de vida do rollback:** o `@Transactional` do `SpringTransactionRunner` foi
   removido de propósito — o teste ficou vermelho (`expected: PENDING but was:
   COMPLETED`), provando que o rollback vem da fronteira transacional e que a
   asserção não é vazia. O código de produção foi restaurado.
5. **Prova de vida da trilha:** o evento `ACCOUNT_UPDATED` foi removido de propósito do
   `CompleteOperation` — o teste ficou vermelho (a trilha caiu para cinco eventos). O
   código de produção foi restaurado.
6. **Duplo compartilhado:** o repositório de eventos que falha no enésimo append estava
   duplicado dentro do `CompletionPersistenceTest`; ele virou
   `br.com.consigport.support.FailingOperationEventRepository`, usado pelos dois testes.
7. **E2E real:** backend e mock empacotados, PostgreSQL 16 do `compose.yaml` e o mock da
   Central de verdade. O caminho confirmado levou nove eventos e
   `account_updated_at` preenchido; o caminho rejeitado levou seis eventos e
   `rejection_reason = MARGIN_INSUFFICIENT` com o corpo bruto da Central. Zero
   containers e zero processos no fim.
8. **Correção de ordem no `EVIDENCE.md`:** as seções estavam fora de ordem cronológica
   (a 2.17 entre a 2.15 e a 2.16); a ordem foi corrigida nesta task.
9. **Validação:**
   - `./mvnw -B -pl backend test -Dtest='OperationLifecyclePersistenceTest'` → 3 testes verdes.
   - `./mvnw -B verify` → reactor verde (backend 481 testes, mock 39 testes).
   - Cobertura do núcleo inalterada: **99,71% de branches**, 98,12% de linhas, 100% de classes.
   - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).

## Sessão #45 — Arquivamento da Fase 2 e release `v0.3.0`

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `chore/018-phase-2-archive`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar a Fase 2 (18 tasks), promover a release `v0.3.0` e abrir a Fase 3.

### Atividades Realizadas:
1. **Merge da 2.18:** PR #50 (`feat/038-backend-lifecycle-integration` → `develop`) com CI
   verde e merge commit `793099d`; branch remota deletada.
2. **Promoção da fase:** PR #51 (`develop` → `staging`) e PR #52 (`staging` → `main`),
   ambos com CI verde antes do merge.
3. **Release:** o `release-please` abriu o PR #53 `chore(main): release 0.3.0`; o merge
   criou a tag `v0.3.0` e a release no GitHub. O CI do `main` no push da release ficou
   verde (run `46094354043`).
4. **Publicação das imagens:** o `release-please` cria a tag pela API do GitHub, o que
   não dispara o evento `push` da tag; as três imagens foram publicadas por
   `workflow_dispatch` em `release.yml` com a tag `v0.3.0` (run `46095256699`).
5. **Staging:** `workflow_dispatch` em `cd-staging.yml` com a tag `v0.3.0` (run
   `46095428615`) — postgres, backend, mock e frontend `healthy`;
   `/actuator/health` → `{"status":"UP","groups":["liveness","readiness"]}` nos dois
   serviços; teardown verde.
6. **Diagnóstico do CI do release-please:** o run `46093781359` falha com "This run
   likely failed because of a workflow file issue", sem jobs e com `pull_requests = []`.
   O mesmo padrão ocorreu nas releases da Fase 0 (`45658801729`) e da Fase 1
   (`45816627838`): é o fechamento do PR antes de o job iniciar, não uma regressão do
   conteúdo — o código idêntico passou no CI do PR #52 e no CI do `main`.
7. **Arquivamento:** `SDD/specs/history/phases/phase-2-finished.md` com as 18 tasks, os
   PRs, os critérios de saída, a evidência da release e as dívidas conhecidas; `PLAN.md`
   com a Fase 2 concluída e a Fase 3 aberta com a task 3.1 em voo; `ROADMAP.md` com a
   Fase 2 fechada na `v0.3.0`.
8. **Padrão confirmado:** o arquivamento vai para `develop` depois da tag, como na Fase 1
   (PR #32 mergeado em 13:00, `v0.2.0` às 12:08) — o arquivo de arquivamento entra na
   release seguinte.
9. **Validação:**
   - 5 gates locais verdes (docs, specs, arquitetura, tokens e log de IA).
   - `./mvnw -B verify` verde no reactor completo (backend 481 testes, mock 39 testes).

## Sessão #46 — Task 3.1 (Cliente OpenAPI tipado)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/031-frontend-openapi-client`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** dar ao frontend um cliente tipado do contrato público, sem duplicar
  tipos, com a base URL por ambiente e o erro mapeado pelo `code` estável.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/APP-STACK.md` (bibliotecas aprovadas e versões),
   `SDD/stacks/react.md`, `SDD/stacks/typescript.md`, `SDD/stacks/testing.md`,
   `SDD/stacks/tailwind.md`, `SDD/DESIGN.md`, `SDD/ARCHITECTURE.md` §5 e §8, o
   `ADR-0005` e o `docs/quality/GATES.md`. Dois subagentes em paralelo levantaram as
   regras do frontend e o estado do scaffold, para não redescobrir o que já existe.
2. **Dependências dentro da faixa declarada:** `openapi-fetch@^0.13.8` (produção),
   `openapi-typescript@^7.13.0` e `@types/node@^24` (desenvolvimento). O
   `APP-STACK.md` fixa `openapi-typescript` em 7.x e `openapi-fetch` em 0.13.x.
3. **Descoberta:** o `openapi-typescript` v7.13 não lê arquivo de config —
   `defineConfig` não existe na série 7 e a v8 nunca foi publicada. A CLI ainda
   interpreta um caminho passado como string como **conteúdo YAML**, o que produz
   `Can't parse empty schema`. A solução foi fazer do `openapi-ts.config.ts` o
   próprio gerador, com a URL `file://` e a API programática `openapiTS` +
   `astToString`.
4. **Gerador reprodutível:** `npm run --prefix frontend generate:types` reescreve
   `src/shared/api/schema.ts` byte a byte a partir de `contracts/consigport-api.yaml`
   — verificado com `diff` antes e depois.
5. **Cliente tipado:** `client.ts` monta o `openapi-fetch` sobre os tipos gerados,
   resolve a base URL por ambiente (`VITE_API_BASE_URL`, fallback local, sem barra
   final) e expõe `parseErrorResponse`.
6. **Erro mapeado por `code`:** `errors.ts` transforma o corpo da rede — `unknown`
   até ser validado — em `ApiError` com `code`, `message`, `locale`, `timestamp`,
   `path` e `details`. Um corpo quebrado degrada para `INTERNAL_ERROR` em vez de
   derrubar a tela.
7. **Testes:** `errors.test.ts` (oito casos do parser e do `isApiErrorCode`) e
   `client.test.ts` (quatro casos da base URL).
8. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 5 arquivos, 20 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - 5 gates locais de documentação verdes

## Sessão #47 — Task 3.2 (Catálogos i18n)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/032-frontend-i18n-catalogs`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** dar à interface três idiomas — pt-BR, en e es — com fallback pt-BR,
  troca de idioma persistida e `Accept-Language` em toda requisição à API.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/APP-STACK.md` (a linha dos catálogos e da negociação),
   `SDD/stacks/react.md`, `SDD/stacks/testing.md`, `docs/quality/GATES.md` (portões 3.1
   e 3.4) e o `ADR-0004`, que fixa o par `code` estável / `message` localizada.
2. **Dependências dentro da faixa declarada:** `i18next@^26.7.2` e
   `react-i18next@^16.6.6` em produção, `msw@^2.15.0` em desenvolvimento.
3. **Descoberta de segurança:** a série 25 do `i18next` carrega o advisory
   **GHSA-6jcc-5g8w-32mx** (`deepExtend` recursivo sobre objetos embutidos, DoS),
   corrigido apenas a partir da `26.3.4`. O `npm audit` do CI roda com
   `--audit-level=high`, então a falha moderada não derrubaria o build — mas a
   dependência corrigida foi escolhida, e o `APP-STACK.md` foi atualizado com a nota do
   advisory ao lado da versão.
4. **Catálogos:** `pt-BR.json`, `en.json` e `es.json` com o mesmo conjunto de chaves e
   uma entrada para cada um dos dez códigos de erro do contrato. Um teste compara as
   árvores dos três idiomas e falha se alguma chave divergir.
5. **Idioma no servidor:** `locales.ts` resolve qualquer tag de idioma para um dos três
   suportados (pt-BR como padrão) e lê o cookie `consigport-locale`; o `entry.rsc.tsx`
   resolve o idioma antes de renderizar e o `Root` monta a página com o `<html lang>`
   certo. As páginas continuam Server Components — o tradutor do servidor
   (`server.ts`) caminha o catálogo, sem hooks.
6. **Folhas clientes:** `provider.tsx` guarda a escolha, sincroniza com o idioma que o
   servidor entregou e persiste o cookie; `language-switcher.tsx` é um `select`
   nativo que, depois de gravar o cookie, pede ao servidor um novo render (o
   `entry.browser.tsx` escuta o `replaceState`).
7. **`Accept-Language`:** middleware do `openapi-fetch` que injeta o cabeçalho em toda
   requisição; o `MSW` prova o cabeçalho e a leitura do corpo de erro no nível do
   HTTP, sem mock de `fetch`.
8. **Correção de estilo:** o scaffold do frontend usa aspas simples e sem ponto e
   vírgula; os arquivos novos da 3.1 e da 3.2 foram alinhados a esse padrão.
9. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 8 arquivos, 36 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - `npm audit --prefix frontend` → 0 vulnerabilidades
   - 5 gates locais de documentação verdes
   - prova de vida: o servidor de preview respondeu em pt-BR, en e es, com o
     `<html lang>` de cada idioma
10. **Armadilha registrada:** o cookie do `curl` de verificação estava escrito com um
    caractere a mais (`consignport-locale`), o que fez o idioma cair no fallback. O
    diagnóstico comparou os códigos de caractere dos dois lados e mostrou a diferença;
    o código do projeto estava correto desde o início.

## Sessão #48 — Task 3.3 (Primitivas de UI)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/033-frontend-ui-primitives`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** entregar as primitivas de UI que as fatias seguintes consomem —
  tokens da Matrix, foco visível, contraste AA e nenhuma cor literal em componente.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/DESIGN.md` §5 (componentes), §6 (UX), a lista de
   PROIBIDOS e §4b (as quatro regras duras), `SDD/stacks/react.md`,
   `SDD/stacks/testing.md` e `docs/quality/GATES.md` (portão 3.4).
2. **`field.tsx`** — o `Field` é o dono da acessibilidade do formulário: rótulo
   ligado por `htmlFor`, dica e erro ligados por `aria-describedby`, `aria-invalid`
   no controle, `required` no controle e o asterisco marcado como decorativo. O
   `TextField` monta o `input` por cima dele.
3. **`table.tsx`** — a `Table` é uma região de rolagem nomeada e focável
   (`role="region"`, `aria-label`, `tabIndex={0}`): no celular a tabela rola dentro
   da própria caixa, sem empurrar a página, e o teclado consegue alcançar a rolagem.
4. **`status-badge.tsx`** — conjunto fechado de tons (neutro, em andamento, sucesso,
   técnico, erro) com ponto decorativo; o significado do estado fica com a feature.
5. **`button.tsx`** — anel de foco visível em todas as variantes
   (`focus-visible:outline-primary`, sem deslocamento de layout), estado `disabled`
   e estado `loading` com `aria-busy` e sem segundo clique.
6. **Contraste verificado, não presumido.** Um script calculou a razão do WCAG de
   cada par antes de escrever o teste; o `contrast.test.ts` lê `src/theme.css` e
   fixa os limiares: 4,5:1 para corpo, botões preenchidos e badges; 3:1 para o
   `text-muted`, que o `DESIGN.md` §2 reserva ao texto decorativo.
7. **Testes de componente** para as três primitivas novas e para o botão, com
   Testing Library e checagem de `aria` (`aria-busy`, `aria-invalid`,
   `aria-describedby`, `role="region"`, `role="alert"`, `aria-hidden` do ponto).
8. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 13 arquivos, 56 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - 5 gates locais de documentação verdes
   - prova de vida: o CSS do bundle saiu de 20,54 kB para 24,14 kB e contém as
     classes novas (`outline-primary`, `bg-error/8`, `border-error/40`,
     `text-tertiary`, `min-h-11`, `overflow-x-auto`)
9. **Ajuste de TypeScript:** o `tsconfig` do projeto tem `noUncheckedIndexedAccess`; a
   desestruturação das componentes de cor do teste foi escrita com valores padrão
   para satisfazer o compilador estrito.

## Sessão #49 — Task 3.4 (Fluxo de autorização)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/034-frontend-authorization-flow`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** registrar o opt-in do beneficiário pela interface — Server Component
  renderiza, a mutação passa pela Server Action, o erro sai localizado e o sucesso
  leva à agenda.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/stacks/react.md` (Server Action para mutação, Server
   Component por padrão, `useEffect` não busca dado), `SDD/APP-STACK.md` (a linha do
   Zod), `contracts/consigport-api.yaml` (o `POST /api/v1/authorizations`, com `201`
   na criação e `200` na repetição) e `docs/quality/GATES.md` (portão 3.4).
2. **Dependência:** `zod@^4.6.7` em produção, dentro da faixa `4.x` do `APP-STACK.md`;
   auditoria com zero vulnerabilidades.
3. **Server Action** (`features/authorization/actions.ts`): valida o par
   beneficiário/convênio com Zod, chama a API com o `Accept-Language` da interface e
   devolve um estado serializável. A API fora do ar é capturada e vira
   `INTERNAL_ERROR` — a primeira versão deixava a exceção escapar e o servidor
   respondia `500`; o smoke do formulário sem JavaScript expôs isso.
4. **Folha cliente** (`features/authorization/ui/authorization-form.tsx`): `useActionState`
   sobre a ação, estado pendente, erro localizado pelo `code` estável (catálogo da
   interface, `message` do servidor como fallback) e navegação para
   `/agenda?beneficiaryId=…` no sucesso.
5. **Server Component** (`features/authorization/server/page.tsx`): o cabeçalho da
   jornada e o formulário; a página não busca nada e não guarda estado.
6. **Rota e navegação:** `/autorizacao` no router e `nav.authorization` nos três
   catálogos.
7. **Ajuste no `field.tsx`:** o asterisco de obrigatório passou a ser desenhado pelo
   CSS (`after:content-['*']`), porque o `textContent` do rótulo quebrava a busca
   por nome acessível do controle.
8. **Testes:** `actions.test.ts` (sete casos: criação, replay `200`, validação antes
   da API, erro por `code`, API fora do ar, `Accept-Language` e fallback de idioma) e
   `authorization-form.test.tsx` (cinco casos de componente com MSW: campos nomeados,
   consentimento registrado, ida à agenda, erro localizado e permanência no
   formulário quando o consentimento falha).
9. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 15 arquivos, 68 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - 5 gates locais de documentação verdes
   - smoke no build de produção: a página renderiza em pt-BR e en, e o formulário
     postado na própria URL (caminho sem JavaScript) devolve a página com o erro
     localizado

## Sessão #50 — Task 3.5 (Agenda e seleção de parcelas)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/035-frontend-agenda-selection`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** ler a agenda do beneficiário no servidor e escolher as parcelas
  no navegador — apenas `OPEN` selecionável, total recalculado, estado vazio e
  `403` tratados.

### Atividades Realizadas:
1. **Leitura da regra:** `SDD/specs/PLAN.md` (a task em voo), `SDD/ARCHITECTURE.md`
   §5 (a árvore do frontend e a pasta `shared/format/`), `SDD/DESIGN.md` §4b e §6
   (as quatro regras duras e as regras de interação), o contrato público
   (`GET /api/v1/beneficiaries/{beneficiaryId}/agenda`, com `403` e `404`),
   `SDD/stacks/react.md` (Server Component por padrão) e `docs/quality/GATES.md`
   (portão 3.4).
2. **`shared/format/`** — a pasta que o `ARCHITECTURE.md` §5 reserva para
   "moeda, data, status" nasceu aqui: `money.ts` (reais no formato do leitor e
   soma em centavos, sem a deriva do ponto flutuante), `date.ts` (a data do
   calendário montada pelas partes, para o dia não andar com o fuso) e
   `status.ts` (o tom da Plataforma por status).
3. **Server Component assíncrono** (`features/agenda/server/page.tsx`): a agenda
   é lida durante o render, com o `Accept-Language` da interface; os estados
   (vazio, sem consentimento, beneficiário desconhecido, falha) são decididos
   pelo `code` do erro.
4. **Folha cliente** (`features/agenda/ui/installment-table.tsx`): a seleção
   pertence à ilha; a parcela `CLOSED` aparece mas não pode ser escolhida; o
   total é recalculado a cada mudança; a seleção viaja para a portabilidade pela
   URL.
5. **Resumo** (`features/agenda/ui/selection-summary.tsx`): o total numa região
   `aria-live="polite"`, para quem navega pelo teclado ouvir o número se mover.
6. **`shared/ui/page-header.tsx`** — o cabeçalho das páginas deixou de ser uma
   cópia em três lugares e virou primitiva.
7. **Roteador** (`app/router.tsx` e `root.tsx`): a rota recebe a URL inteira — o
   caminho escolhe a tela, a query carrega o beneficiário — e o tradutor do
   servidor ganhou o fallback da mensagem do servidor.
8. **Catálogos** (`pt-BR.json`, `en.json`, `es.json`): a seção `agenda` foi
   reescrita, com o mesmo conjunto de chaves nos três idiomas (as chaves do
   esqueleto que não tinham uso foram removidas).
9. **API falsa** (`test/mocks/handlers.ts`): a agenda de `BEN-001` (duas abertas
   e uma fechada), `BEN-EMPTY`, `BEN-FORBIDDEN` e o `404` para o resto.
10. **Testes:** `installment-table.test.tsx` (sete casos: a lista, a parcela
    fechada desabilitada, o total recalculado, a contagem, a limpeza, a viagem
    para a portabilidade e a impossibilidade de continuar sem seleção),
    `page.test.tsx` (seis casos: a agenda, o estado vazio, o `403`, o `404`, a
    falha com "Tentar de novo" e o `Accept-Language`), `money.test.ts` e
    `date.test.ts`.
11. **Validação:**
    - `npm run --prefix frontend lint` → EXIT=0
    - `npm run --prefix frontend typecheck` → EXIT=0
    - `npm run --prefix frontend test:unit` → 19 arquivos, 89 testes verdes
    - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
    - 5 gates locais de documentação verdes
    - smoke no build de produção contra uma API falsa local: a agenda renderiza
      em pt-BR e en, os quatro estados aparecem, a parcela fechada está
      desabilitada e o `Accept-Language` chega à API no idioma do leitor

## Sessão #51 — Task 3.6 (Conta de recebimento e envio)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/036-frontend-portability-submit`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** validar a conta de recebimento e criar a operação — validação com
  Zod, `Idempotency-Key` gerada uma vez por tentativa, `409` e `422` exibidos com
  a mensagem do servidor.

### Atividades Realizadas:
1. **Leitura da regra:** o contrato público (`POST /api/v1/portability-operations`,
   com o header `Idempotency-Key` no formato UUID, o `409` de conflito e o `422`
   de parcela inelegível), o `ReceivingAccount` do domínio Java (64/32/64 e não
   vazio), o ADR-0004 (a interface usa o `code` como chave estável e a `message`
   do servidor como fallback), o `SDD/stacks/react.md` (mutação por Server
   Action) e `docs/quality/GATES.md` (portão 3.4).
2. **`idempotency.ts`** — a chave da tentativa: `createIdempotencyKey` (UUID v4,
   com reserva para o `crypto.randomUUID` de contexto seguro) e `isUuid`.
3. **Server Action** (`features/portability/actions.ts`): valida a conta e a
   seleção com **Zod** (os mesmos limites do contrato e do domínio Java), envia
   a `Idempotency-Key` e devolve o estado serializável — inclusive quando a API
   está fora do ar (`INTERNAL_ERROR`, nunca uma exceção).
4. **Folha cliente** (`features/portability/ui/receiving-account-form.tsx`): os
   campos da conta e do credor, o estado pendente e a chave da tentativa. A
   chave é calculada a partir da **impressão digital do conteúdo** e escrita no
   campo oculto no `onSubmit` — o mesmo conteúdo mantém a chave, outro conteúdo
   recebe uma nova.
5. **Resultado** (`features/portability/ui/submit-operation.tsx`): o protocolo e
   o total quando a operação nasce; o erro com duas vozes — o texto do catálogo
   indexado pelo `code` e a mensagem do servidor como detalhe, quando acrescenta
   alguma coisa.
6. **Página** (`features/portability/server/page.tsx`): Server Component com as
   parcelas selecionadas e o formulário; o estado sem seleção aponta de volta
   para a agenda.
7. **Ajuste da 3.5:** a seleção agora viaja com o **convênio**, que o pedido do
   contrato exige — a tabela da agenda conhece o convênio de cada parcela.
8. **Catálogos e API falsa:** a seção `portability` reescrita nos três idiomas;
   a API falsa cria operações, repete com a mesma chave (`Idempotent-Replay`)
   e recusa a mesma chave com outro corpo (`409`).
9. **Testes:** `actions.test.ts` (oito casos: criação, repetição, conflito,
   parcela fechada, validação da conta antes da API, chave criada quando o
   navegador não manda, `Accept-Language` e API fora do ar),
   `receiving-account-form.test.tsx` (seis casos de componente: os campos
   nomeados, a criação com o protocolo, a chave estável para o mesmo conteúdo, a
   chave nova para outro conteúdo, o `409` e o `422` com a mensagem do servidor)
   e `idempotency.test.ts`.
10. **Validação:**
    - `npm run --prefix frontend lint` → EXIT=0
    - `npm run --prefix frontend typecheck` → EXIT=0
    - `npm run --prefix frontend test:unit` → 22 arquivos, 107 testes verdes
    - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
    - 5 gates locais de documentação verdes
    - smoke no build de produção contra uma API falsa local: a página renderiza
      em pt-BR, en e es, o estado sem seleção aparece, e o caminho **sem
      JavaScript** cria a operação e devolve o protocolo e o total

## Sessão #52 — Task 3.7 (Painel de status com polling)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/037-frontend-operations-polling`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** acompanhar a operação até o terminal — TanStack Query com
  intervalo de 2 s, parada em estado terminal, `nextPollAt` respeitado, sem
  ISR.

### Atividades Realizadas:
1. **Leitura da regra:** o contrato público (`GET /api/v1/portability-operations/{operationId}`,
   o `OperationDetail` e o `OperationStatus` com `COMPLETED` e `REJECTED`
   terminais), o `ARCHITECTURE.md` §5 (polling em ilha cliente, o resto
   servidor, sem ISR) e §11 (`Retry-After` honrado no `next_poll_at`), o
   `ADR-0003` (assincronismo, polling e trilha imutável), o
   `SDD/stacks/react.md` (Server Component por padrão, folha cliente como
   exceção) e o `docs/quality/GATES.md` (portão 3.4).
2. **Dependência:** `@tanstack/react-query@^5` (`5.108.4`), dentro da faixa do
   `APP-STACK.md`; `npm audit --omit=dev` → 0 vulnerabilidades.
3. **Provedor do cache** (`shared/api/query-provider.tsx`): o `QueryClient` criado
   uma vez por sessão, embrulhado pelo Shell.
4. **Hook do polling** (`features/operations/ui/use-operation-polling.ts`): a
   aritmética do ritmo isolada em funções puras — `POLL_INTERVAL_MS` (2 s),
   `isTerminalStatus`, `readNextPollAt` e `pollDelay` — e o `useQuery` que as
   usa: `refetchInterval` devolve `false` no estado terminal e no
   `OPERATION_NOT_FOUND`, e o número de milissegundos nos demais casos.
5. **Ilha do painel** (`features/operations/ui/operation-status.tsx`): o estado,
   o protocolo, o total, as parcelas, o credor, a última atualização, o motivo
   da rejeição, a região viva e o botão "Atualizar agora".
6. **Server Component** (`features/operations/server/page.tsx`): o primeiro
   estado lido no render, com o `Accept-Language` da cookie; os estados de
   operação inexistente e de falha com o caminho de saída.
7. **Integração com a 3.6:** o painel de sucesso da criação da operação passou
   a oferecer o caminho `/operacoes?operationId=…`; a rota `/operacoes` entrou
   no roteador e na navegação.
8. **`formatDateTime`** (`shared/format/date.ts`): a hora da última atualização
   — um instante é um momento, não um dia de calendário.
9. **Catálogos e API falsa:** a seção `operations` completada nos três idiomas
   e a entrada da navegação; a API falsa responde à consulta da operação.
10. **Testes:** `use-operation-polling.test.ts` (a aritmética do ritmo),
    `operation-status.test.tsx` (timers falsos: `PENDING` → `COMPLETED` parando
    depois, `REJECTED` com o motivo e sem novo polling, `nextPollAt` de 10 s
    adiado, `Accept-Language` e a falha da API) e `server/page.test.tsx` (o
    estado lido no servidor, o `404`, a falha e o idioma da pergunta).
11. **Validação:**
    - `npm run --prefix frontend lint` → EXIT=0
    - `npm run --prefix frontend typecheck` → EXIT=0
    - `npm run --prefix frontend test:unit` → 25 arquivos, 131 testes verdes
    - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
    - 5 gates locais de documentação verdes
    - smoke no build de produção contra uma API falsa local: o painel renderiza
      em pt-BR, en e es, os estados sem operação e de operação inexistente
      aparecem, e o fluxo da criação leva ao painel com o `operationId` na URL
    - preview e API falsa encerrados: 0 processos e 0 portas presas

## Sessão #53 — Task 3.8 (Timeline de auditoria)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/038-frontend-audit-timeline`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** exibir a trilha imutável de eventos — ordem cronológica, tipo,
  status anterior e novo, detalhe e horário, sem ação de edição ou exclusão.

### Atividades Realizadas:
1. **Leitura da regra:** o contrato público
   (`GET /api/v1/portability-operations/{operationId}/events`, o
   `OperationEvent` com os sete campos e o `EventType` com os oito tipos), o
   `ADR-0003` (a trilha é append-only, lida em ordem cronológica e exibida na
   timeline do frontend), o `ARCHITECTURE.md` §5 (`features/audit/` com
   `server/` e `ui/`) e o `SDD/stacks/react.md` (Server Component por padrão).
2. **Server Component** (`features/audit/server/page.tsx`): a trilha lida no
   render, com o `Accept-Language` da cookie; os estados de operação
   inexistente e de falha com o caminho de saída.
3. **Timeline** (`features/audit/ui/event-timeline.tsx`): a lista ordenada pelo
   instante e, no empate, pelo identificador sequencial do evento — a ordem
   da tela não depende da ordem do array.
4. **Item do evento** (`features/audit/ui/event-item.tsx`): o tipo, o status
   anterior, o novo status, o detalhe, a hora e a resposta crua da Central num
   `<details>` nativo.
5. **Vocabulário do status** (`shared/format/status.ts`): o tom e a chave de
   catálogo de cada estado passaram a morar juntos, e o painel da 3.7 usa a
   mesma tabela.
6. **Integração:** o painel da operação passou a oferecer o caminho para a
   trilha (`/auditoria?operationId=…`); a rota `/auditoria` lê a operação da
   URL; o placeholder `SectionPage` saiu do código.
7. **Catálogos e API falsa:** a seção `audit` completada nos três idiomas (os
   oito tipos de evento e os estados) e a trilha na API falsa.
8. **Testes:** `event-timeline.test.tsx` (eventos mistos fora de ordem, o tipo,
   os dois status, o detalhe, a hora, o status anterior nulo, a resposta da
   Central, a ausência de botão, campo e link, e a formatação em inglês) e
   `server/page.test.tsx` (a trilha, a operação sem eventos, a operação
   inexistente, a falha e o idioma da pergunta).
9. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 27 arquivos, 143 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - 5 gates locais de documentação verdes
   - smoke no build de produção contra uma API falsa local: a trilha renderiza
      em pt-BR, en e es, os estados sem operação e de operação inexistente
      aparecem, e a trilha não oferece nenhum botão
   - preview e API falsa encerrados: 0 processos e 0 portas presas

## Sessão #54 — Task 3.9 (Responsividade, acessibilidade e testes finais)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `feat/039-frontend-responsive-a11y`
- **PR:** a abrir para `develop`
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** fechar a experiência em mobile e teclado — breakpoints do
  `DESIGN.md` §4b, navegação por teclado, `prefers-reduced-motion` respeitado e
  testes de unidade verdes.

### Atividades Realizadas:
1. **Leitura da regra:** o `DESIGN.md` §4 (os três breakpoints) e §4b (as
   quatro regras duras e a prova), o `PLAN.md` (task 3.9), o `BACKLOG.md` (o
   aceite da 3.9) e o `frontend/src/styles.css` (os tokens e a folha de estilo
   global).
2. **Skip link e teclado:** o link "Pular para o conteúdo" no topo do shell,
   fora da tela até o foco chegar, apontando para o `<main id="conteudo"
   tabindex="-1">`; o anel de foco visível nos itens da navegação, na marca,
   na região de rolagem da tabela, no `select` do idioma e no checkbox das
   parcelas.
3. **Breakpoints:** o `sm` do Tailwind (640px) saiu do código — o design
   nomeia 768px e 1024px, e só esses; os grids da home e do painel da
   operação passaram a usar `md`.
4. **`prefers-reduced-motion`:** a regra global na raiz corta animação,
   transição e `scroll-behavior` de tudo que se move.
5. **Semântica corrigida:** as chamadas de ação da home viraram links (um
   botão que navega mente para o teclado), o `<header>` da home deixou de
   ser um segundo *banner* e o painel de sucesso da portabilidade ganhou
   `role="status"`.
6. **Região viva corrigida:** o painel da operação passou a anunciar o
   **estado** da operação; o batimento do polling — que mudava a cada dois
   segundos dentro de um `aria-live` — ficou visível e silencioso.
7. **Testes:** `src/test/responsive.test.ts` (breakpoints, conteúdo
   escondido, largura fixa e piso de 13px, lidos do fonte) e
   `src/test/accessibility.test.tsx` (ordem do tabulador com o teclado de
   verdade, landmarks, nome acessível de todo controle, alvo de 44px, anel de
   foco e ligação entre campo e rótulo).
8. **Validação:**
   - `npm run --prefix frontend lint` → EXIT=0
   - `npm run --prefix frontend typecheck` → EXIT=0
   - `npm run --prefix frontend test:unit` → 29 arquivos, 159 testes verdes
   - `npm run --prefix frontend build` → os três ambientes (rsc, ssr, client) verdes
   - 5 gates locais de documentação verdes
   - smoke no build de produção: o skip link, o `main` com `id` e
     `tabindex`, as CTAs como links, os landmarks e a região viva do painel
     da operação conferidos no HTML; o CSS emitido carrega a regra do
     `prefers-reduced-motion` e as media queries de 48rem e 64rem
   - preview e API falsa encerrados: 0 processos e 0 portas presas

## Sessão #55 — Task 3.10 (Promoção da release `v0.4.0` e arquivamento da Fase 3)

- **Data:** 2026-11-28
- **Agente / Modelo:** OpenCode (provider NVIDIA) — MiniMax M3.1 Flash
- **Branch:** `chore/0310-phase-3-release-archive` (arquivamento) e
  `chore/0311-sync-release-040` (sincronização do manifesto)
- **PR:** #65 (`develop` → `staging`), #66 (`staging` → `main`), #67 (release-please),
  #68 (arquivamento) e #69 (sync do manifesto)
- **Ambiente:** harness remoto em EC2 (AWS); operação pelo celular.
- **Objetivo:** promover, publicar e arquivar a Fase 3 — `develop` → `staging` → `main`,
  tag `v0.4.0` pelo `release-please`, imagens no GHCR, staging verde e fase arquivada.

### Atividades Realizadas:
1. **Promoção `develop` → `staging` (PR #65):** onze checks verdes (contratos, backend,
   mock, frontend, segurança, smoke com PostgreSQL 16 e documentação) e merge por
   merge commit. Nenhum commit direto: o fluxo Gitflow é respeitado.
2. **Promoção `staging` → `main` (PR #66):** o mesmo portão, onze checks verdes.
3. **PR de release (PR #67):** o `release-please` abriu `chore(main): release 0.4.0`
   a partir dos commits promovidos; o merge por merge commit criou a tag `v0.4.0` e a
   release no GitHub, com o CHANGELOG gerado. Ninguém escreveu a tag à mão.
4. **CI do `main` (run 46132773593):** onze jobs, todos verdes — `E2E — Playwright` e
   `Performance Smoke — k6` sem suíte neste commit, como esperado até as tasks 4.3 e
   4.4.
5. **Publicação das imagens (run 46132827648):** `workflow_dispatch` em `release.yml`
   com a tag `v0.4.0`, porque o `release-please` cria a tag pela API e o evento `push`
   da tag não acontece — o job `publish-images` fica `skipped` no push da branch. As
   três imagens saíram com `v0.4.0`, `0.4.0` e `latest`, e os digests foram anotados.
6. **Staging (run 46132952326):** `workflow_dispatch` em `cd-staging.yml` com a tag
   `v0.4.0`; postgres, backend, mock e frontend `Healthy`; `/actuator/health` respondendo
   `{"status":"UP","groups":["liveness","readiness"]}` nos dois serviços; o frontend
   respondendo `200` em `/`; teardown verde.
7. **Arquivamento da fase:** `SDD/specs/history/phases/phase-3-finished.md` com as 10
   tasks, os PRs, os critérios de saída, a evidência da release e as dívidas; o `PLAN.md`
   com a Fase 3 concluída e a Fase 4 aberta com a 4.1 em voo; o `ROADMAP.md` com as
   tabelas de tasks das duas fases; o `BACKLOG.md` com a 3.10 realizada e as dívidas da
   fase 3.
8. **Sincronização do manifesto:** PR próprio Levando `main` (com o manifesto em `0.4.0`
   e o CHANGELOG) para o `develop`, para que a próxima promoção não leve o manifesto de
   volta e o `release-please` não publique a `v0.4.0` de novo.
9. **Validação:** os cinco gates locais de documentação verdes na promoção, no
   arquivamento e na sincronização.
