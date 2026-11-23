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
