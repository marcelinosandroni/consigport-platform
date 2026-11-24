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
