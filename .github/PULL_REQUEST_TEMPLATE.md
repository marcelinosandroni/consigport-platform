## 📌 Contexto e Identificação da Tarefa
- **Task do PLAN:** Task `0.X` — [Nome da Task](SDD/specs/PLAN.md)
- **Branch de Origem:** `feat/...` ou `fix/...` ou `docs/...`
- **Branch de Destino:** `develop` (Strict Gitflow: merges ocorrem primeiro em `develop`)

---

## 🎯 Objetivo da Entrega
<!-- Descreva sucintamente o objetivo da tarefa, escopo e requisitos atendidos -->

---

## 📦 Arquivos Alterados (Máximo de 2 a 5 arquivos por task)
<!-- Liste os arquivos criados ou modificados -->
- `...`
- `...`

---

## 🧪 Checklist de Qualidade & Evidências de Execução

> 🛑 **Atenção:** Todo PR deve conter a colagem da execução real dos comandos de teste antes do merge.

- [ ] `node SDD/SKILLS/check-docs/check-docs.mjs .` executado com sucesso (zero links quebrados).
- [ ] `node scripts/check-specs.mjs` executado com sucesso (regras e invariantes SDD preservadas).
- [ ] `node scripts/check-architecture.mjs` executado com sucesso (desenhos de arquitetura íntegros).
- [ ] Testes automatizados do componente executados (ex.: `./mvnw test`, `npm test` ou comandos específicos da task).

### Evidência Colada:
```bash
# Cole aqui a saída dos comandos executados:

```

---

## 🛡️ Rastreabilidade de Requisitos
- **Requisitos Atendidos:** (ex.: RF-01, RNF-02, REST-01)
- **ADRs de Referência:** (ex.: ADR-0001, ADR-0006)
