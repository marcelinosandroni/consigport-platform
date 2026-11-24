# 🔀 Política de Branches e Gitflow — ConsigPort

> Guia operacional de branches, governança de commits, isolamento por Git Worktrees e promoção de releases no ConsigPort.
> Referências: [ADR-0006](../../docs/adr/0006-gitflow-ci-cd-docker-e-performance.md) e [`GATES.md`](../../docs/quality/GATES.md).

---

## 1. Visão Geral da Topologia de Branches

O ConsigPort adota um modelo estrito de **Gitflow adaptado com proteção de branches**:

```mermaid
gitGraph
   commit id: "Initial Commit"
   branch develop
   checkout develop
   commit id: "Base Develop"
   branch feat/002-gitflow
   checkout feat/002-gitflow
   commit id: "feat: gitflow rules"
   checkout develop
   merge feat/002-gitflow id: "PR #4 (to develop)"
   branch staging
   checkout staging
   commit id: "CD Staging"
   checkout main
   merge staging id: "Release Prod"
```

---

## 2. Papel de Cada Branch

| Branch | Papel | Proteção / Políticas | Destino de Merge Permitido |
|---|---|---|---|
| **`main`** | **Produção estável.** Código em execução em ambiente produtivo. | Protegida. Proibido push direto (`force push` desabilitado). Merge **somente vindo de `staging`**. | Deploy de produção via release aprovada. |
| **`staging`** | **Homologação.** Ambiente pré-produtivo para testes integrados. | Protegida. Recebe merges de `develop` via PR após suites de validação. | Merge para `main`. |
| **`develop`** | **Integração contínua.** Linha de base para todo o desenvolvimento ativo. | Protegida. **Nunca recebe commits diretos.** Recebe apenas PRs de branches de task. | Merge para `staging`. |
| **`feat/*`**<br>**`fix/*`**<br>**`docs/*`**<br>**`chore/*`** | **Branches atômicas de trabalho.** Criadas para cada task do [`PLAN.md`](../../SDD/specs/PLAN.md). | Devem ser desenvolvidas em **Git Worktrees** isolados. Destino obrigatório de PR: `develop`. | Merge em `develop`. |

> 🛑 **REGRA DE OURO:** NUNCA abrir PR ou fazer push direto de uma branch de feature para a `main`. O fluxo é sempre:
> `branch de task` $\rightarrow$ `develop` $\rightarrow$ (testado e integrado) $\rightarrow$ `staging` $\rightarrow$ `main`.

---

## 3. Uso Obrigatório de Git Worktrees

Para evitar poluição da árvore de trabalho e garantir que cada tarefa atômica seja desenvolvida e testada de forma independente, os desenvolvedores e agentes devem utilizar **Git Worktrees**:

### 3.1. Criando um Worktree para uma Nova Task
```bash
# Estando na raiz do repositório consigport-platform na branch develop atualizada:
git checkout develop
git pull origin develop

# Cria o worktree em diretório irmão com a branch da task:
git worktree add ../consigport-task-0.X -b feat/00X-nome-da-task develop
```

### 3.2. Trabalhando no Worktree
```bash
# Navegue até o worktree:
cd ../consigport-task-0.X

# Desenvolva, teste e valide localmente:
node SDD/SKILLS/check-docs/check-docs.mjs .
node scripts/check-specs.mjs
node scripts/check-architecture.mjs

# Commit seguindo Conventional Commits:
git add .
git commit -m "feat(scope): descrição da entrega. (Agent: NomeDoAgente - Modelo)"

# Envie para o GitHub:
git push -u origin feat/00X-nome-da-task
```

### 3.3. Abrindo o Pull Request
```bash
# Crie o PR apontando estritamente para develop:
gh pr create --base develop --head feat/00X-nome-da-task --title "feat(scope): ..." --body-file ...
```

### 3.4. Limpando o Worktree após o Merge
```bash
# Retorne à raiz principal:
cd ../consigport-platform
git checkout develop
git pull origin develop

# Remova o worktree finalizado:
git worktree remove ../consigport-task-0.X
git branch -d feat/00X-nome-da-task
```

---

## 4. Proteção de Branches via GitHub CLI (`gh`)

Para assegurar que nenhum commit não autorizado chegue a `main`, `staging` ou `develop`, as regras de proteção podem ser aplicadas via GitHub CLI:

```bash
# Exemplo de configuração de branch protection rules para develop:
gh api -X PUT /repos/marcelinosandroni/consigport-platform/branches/develop/protection \
  -F required_status_checks[strict]=true \
  -F required_status_checks[contexts][]="Validate Markdown Links & Specs" \
  -F enforce_admins=false \
  -F required_pull_request_reviews[dismiss_stale_reviews]=true \
  -F required_pull_request_reviews[require_code_owner_reviews]=true \
  -F restrictions=null
```

---

## 5. Padrão de Commits

O projeto segue a convenção **Conventional Commits**:
* `feat(scope)`: Nova funcionalidade.
* `fix(scope)`: Correção de defeito.
* `docs(scope)`: Documentação e especificações.
* `chore(scope)`: Manutenção de build, configurações ou ferramentas.
* `test(scope)`: Adição ou refatoração de testes.
