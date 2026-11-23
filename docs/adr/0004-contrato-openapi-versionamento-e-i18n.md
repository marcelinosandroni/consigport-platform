# ADR-0004 — Contrato OpenAPI, versionamento e i18n

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Contratos:** `contracts/consigport-api.yaml`, `contracts/central-api.yaml`
- **Stack:** [`../../SDD/APP-STACK.md`](../../SDD/APP-STACK.md) §5

## Contexto

Três consumidores dependem do contrato: o frontend, os testes de contrato e a
documentação. Se cada um evoluir por conta própria, o sistema quebra de forma silenciosa
— o tipo gerado no front deixa de bater com a resposta real, e o Swagger vira ficção.

Há ainda uma tensão de idioma:

- O domínio e a API pública usam inglês (código e identificadores).
- A Central externa usa português, fiel ao edital.
- As mensagens humanas precisam de pt-BR, en e es.
- A documentação do projeto é em pt-BR por decisão do responsável.

## Decisão

### Contrato primeiro

1. `contracts/consigport-api.yaml` e `contracts/central-api.yaml` são versionados e são
   a fonte de verdade da interface.
2. O backend gera `/v3/api-docs` com springdoc e o CI compara o resultado com o snapshot
   do contrato. Divergência falha o build.
3. `oasdiff` compara a versão da branch com a de `main` e falha em *breaking change*.
4. `spectral` valida estilo e completude; `schemathesis` exercita a API real contra o
   contrato.
5. O cliente TypeScript é gerado do contrato com `openapi-typescript`; ninguém escreve
   tipo de resposta à mão.

### Versionamento

- API pública sob `/api/v1`; evolução compatível entra em `v1`, quebra entra em `v2`.
- A API da Central **não** tem prefixo de versão: é o contrato externo do edital e não
  nos pertence.
- A versão do produto segue SemVer e é gerada pelo `release-please`.

### i18n

| Superfície | Padrão | Suportados |
|---|---|---|
| API | `pt-BR` | `pt-BR`, `en`, `es` |
| Frontend | `pt-BR` | `pt-BR`, `en`, `es` |
| Central externa | `pt-BR` | `pt-BR` |
| Docs do projeto | `pt-BR` | — |
| Código, domínio, commits, logs técnicos | `en` | — |

- `code` de erro é estável e em inglês (`IDEMPOTENCY_CONFLICT`); `message` é localizada
  por `Accept-Language` com fallback `pt-BR`.
- A UI usa o `code` como chave estável e a `message` do servidor como fallback.
- A documentação em pt-BR é uma decisão explícita do responsável, registrada em
  [`../ai-harness/AI_USAGE_LOG.md`](../ai-harness/AI_USAGE_LOG.md).

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Código primeiro, Swagger como subproduto | O contrato deixaria de ser revisável antes da implementação; drift só apareceria em produção. |
| GraphQL | O desafio pede REST; adicionaria camada sem requisito. |
| Contrato em inglês para a Central | Quebraria a fidelidade ao edital e o exercício de ACL. |
| Mensagens apenas em pt-BR | O desafio pede internacionalização; en e es são baratos. |
| Traduzir códigos de erro | Quebraria clientes e logs; código é contrato, mensagem é apresentação. |

## Consequências

**Positivas**
- Front e testes de contrato compartilham a mesma verdade.
- Quebra de compatibilidade é detectada no PR, não em produção.
- Mensagens localizadas sem sacrificar estabilidade de código.

**Negativas**
- Duas fontes a manter sincronizadas: anotações do springdoc e o snapshot.
- `oasdiff` exige baseline acessível no CI.
- Geração de tipos adiciona um passo ao build do front.

**Mitigação**
- O snapshot é atualizado por script e o diff entra no PR.
- Baseline vem de `origin/main` no checkout do CI.

## Como é verificado

- Job `contracts`: Spectral, `oasdiff` e comparação springdoc × snapshot.
- Job `contracts`: Schemathesis contra o compose.
- Teste de negociação de locale para os três idiomas.
- `npm run --prefix frontend typecheck` prova que os tipos gerados compilam.
