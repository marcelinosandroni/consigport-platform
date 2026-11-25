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
   - Criação de [`.github/workflows/ci-contracts.yml`](../../.github/workflows/ci-contracts.yml), disparado por mudanças em `contracts/**`, `.spectral.yaml` e `scripts/hmac-vector.mjs`.
   - Etapas: Spectral com `--fail-severity=warn`, reprodução do vetor HMAC e validação do log de IA com `AI_LOG_BASE_SHA`.
2. **Bloqueio de Breaking Changes:**
   - Instalação do `oasdiff` em versão fixa e comparação `oasdiff breaking` contra o contrato da branch base para os dois contratos.
   - Primeira introdução do contrato é aceita com aviso no log, sem quebrar o pipeline.
3. **Documentação do Portão:**
   - Atualização do Gate 3.3 em [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md) com o workflow, os comandos reais e a baseline do `oasdiff`.
4. **Entrega:**
   - PR #7 para `develop` reunindo os contratos, o vetor HMAC e o portão de CI.
   - Validação local: `node scripts/hmac-vector.mjs` (3 vetores), Spectral sem erros nem avisos, `check-specs`, `check-docs`, `check-architecture` e `check-ai-log` com exit 0.
