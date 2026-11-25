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

## Fase atual: 0 — Fundação

> Objetivo: projeto executável em Docker, contratos antes do código, CI/CD real e
> governança de branch. Saída: tag `v0.1.0`.

### Tasks

- [x] **0.1 — Refinamento SDD completo**
      Specs em pt-BR, 6 ADRs, harness de IA, gates de qualidade, CI de documentação e
      correção dos links do `AGENTS.md` raiz. Branch `docs/000-sdd-foundation`.
- [x] **0.2 — Gitflow, PR template, CODEOWNERS e proteção de branch documentada**
      Branch `feat/002-gitflow`. Template de PR, CODEOWNERS e docs/process/GITFLOW.md.
- [x] **0.3 — `.gitignore`, `.env.example` e `compose.yaml` com PostgreSQL 16**
      Branch `feat/003-local-env`. Configuração de ambiente base e banco PostgreSQL 16 com volume e healthcheck.
- [x] **0.4 — Maven Wrapper e reactor raiz com `backend` e `central-convenios-mock`**
      Branch `feat/004-maven-reactor`. Wrapper 3.3.4 (only-script) apontando para Maven
      3.10.2, reactor com Java 21 e Spring Boot 3.5.16, módulos mínimos `backend` e
      `central-convenios-mock`.
- [x] **0.5 — Contratos `consigport-api.yaml`, `central-api.yaml` e vetor HMAC**
      Branch `feat/005-contracts`. Contrato primeiro: superfície pública em EN,
      contrato da Central fiel ao edital em PT e vetor compartilhado de assinatura.
- [x] **0.6 — Scaffold do backend (pom, aplicação, `application.yml`)**
      Branch `feat/006-backend-scaffold`. Aplicação Spring Boot com web, validation,
      JPA, Flyway, actuator e springdoc; porta 8080, perfil `local` via `DATABASE_URL`
      e health respondendo `UP`.
- [ ] 0.7 — Scaffold do mock (pom, aplicação, `application.yml`)
- [ ] 0.8 — Base do frontend RSC (package, Vite, TypeScript, Tailwind)
- [ ] 0.9 — Shell RSC, tema Matrix e fontes locais
- [ ] 0.10 — `ci.yml` completo e `nightly.yml`
- [ ] 0.11 — `release-please`, `release.yml`, `cd-staging.yml` e imagens GHCR

### Critérios de saída da fase

- [ ] Todas as tasks `[x]` com evidência colada
- [ ] `docker compose config -q` verde
- [ ] `./mvnw -N validate` e scaffolds compilando
- [ ] `npm ci --prefix frontend && npm run --prefix frontend build` verde
- [ ] Contratos válidos no Spectral
- [ ] Job de documentação verde no PR
- [ ] `SDD/docs/EVIDENCE.md` atualizado
- [ ] Tag `v0.1.0`

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
**Commit:** `type(scope): descrição. (Agent: OpenCode - DeepSeek V4.1 Flash)`
```

Gate completo e o que cada comando prova: [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).
