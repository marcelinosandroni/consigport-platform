# 🎯 PLAN — a task em voo

> 🛑 **Regra fixa (agente de IA, leia antes de codar):**
> Existe **exatamente uma** task `[-]` neste arquivo. Se houver duas, o agente parou no
> lugar errado.
> Task maior que uma hora **é dividida antes de começar**. Nunca pule etapa: implemente,
> teste, cole a evidência, commite.
> Estados: `[ ]` a fazer, `[-]` em andamento, `[x]` feito.
>
> Catálogo completo: [`BACKLOG.md`](./BACKLOG.md). Visão macro:
> [`ROADMAP.md`](./ROADMAP.md). Regras: [`../AGENTS.md`](../AGENTS.md).

---

## Fase 0 — Fundação: concluída ✅

> Arquivada em [`history/phases/phase-0-finished.md`](./history/phases/phase-0-finished.md).
> Tag [`v0.1.0`](https://github.com/marcelinosandroni/consigport-platform/releases/tag/v0.1.0)
> publicada com as três imagens no GHCR e o staging validado.

### Critérios de saída da fase

- [x] Todas as tasks `[x]` com evidência colada
- [x] `docker compose config -q` verde
- [x] `./mvnw -N validate` e scaffolds compilando
- [x] `npm ci --prefix frontend && npm run --prefix frontend build` verde
- [x] Contratos válidos no Spectral
- [x] Job de documentação verde no PR
- [x] `SDD/docs/EVIDENCE.md` atualizado
- [x] Tag `v0.1.0`

---

## Fase atual: 1 — Central mock

> Objetivo: Central determinística na porta `8081`, com contrato PT e Swagger.
> Task em voo: **1.2 — Consulta de agenda**.

### Tasks

- [x] **1.1 — Store em memória, fixtures e administração**
      Estado determinístico reiniciável com `ConcurrentHashMap`; fixtures do
      `ARCHITECTURE.md` §4; `POST /_admin/reset` → `204` e `GET /_admin/state` com o
      estado completo.
- [-] **1.2 — Consulta de agenda**
      `GET /beneficiarios/{id}/agenda` em PT com `parcelaId`, `convenioId`, `valor`,
      `dataDesconto` e `status`; `404` para beneficiário desconhecido e `403` sem
      autorização registrada (BR-01).

### Como usar este arquivo

1. Copie a próxima task do [`BACKLOG.md`](./BACKLOG.md) para cá, com o ID e o objetivo.
2. Marque `[-]` **antes** de começar e crie a branch da task.
3. Marque `[x]` **depois** de colar a saída verde dos comandos.
4. Ao fechar a fase, arquive em `specs/history/phases/` e crie a tag.

---

## 📋 Checklist de entrega (colar ao fim de cada task)

```markdown
**Evidência:**
- `node scripts/check-specs.mjs` → exit 0
- `node SDD/SKILLS/check-docs/check-docs.mjs` → exit 0
- (comando da task) → exit 0

**Arquivos tocados:** (lista, no máximo 5)
**Commit:** `type(scope): descrição. (Agent: OpenCode - MiniMax-M3.1-Flash)`
```

Gate completo e o que cada comando prova: [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).
