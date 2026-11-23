# ADR — Registros de Decisão de Arquitetura

> Uma decisão por arquivo. Fonte de verdade do "porquê". O "o quê" fica em
> [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md).

| ID | Decisão | Estado |
|---|---|---|
| [0001](./0001-hexagonal-ddd-acl-e-mock-isolado.md) | Hexagonal + DDD + ACL + mock isolado | Aceita |
| [0002](./0002-integridade-hmac-e-idempotencia.md) | Integridade HMAC e idempotência | Aceita |
| [0003](./0003-assincronismo-polling-e-trilha-imutavel.md) | Assincronismo, polling e trilha imutável | Aceita |
| [0004](./0004-contrato-openapi-versionamento-e-i18n.md) | Contrato OpenAPI, versionamento e i18n | Aceita |
| [0005](./0005-frontend-rsc-design-e-renderizacao.md) | Frontend RSC, design e renderização | Aceita |
| [0006](./0006-gitflow-ci-cd-docker-e-performance.md) | Gitflow, CI/CD, Docker e performance | Aceita |

## Formato

Toda ADR contém: **Contexto**, **Decisão**, **Alternativas consideradas**,
**Consequências** e **Como é verificado**. Uma decisão nova entra como `0007` e nunca
reescreve o passado; decisão substituída ganha estado `Substituída por NNNN`.
