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
