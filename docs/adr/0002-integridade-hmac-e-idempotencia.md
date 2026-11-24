# ADR-0002 — Integridade HMAC e idempotência

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Contrato:** [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md) §8–§10

## Contexto

O desafio exige duas garantias que normalmente vêm de infraestrutura pronta:

1. **Integridade** entre ConsigPort e a Central, sem certificado digital e sem login de
   usuário no escopo.
2. **Idempotência** da criação de operação: duplo clique, retry de rede ou reenvio do
   worker não podem gerar duas portabilidades nem dois débitos.

Também é preciso distinguir "mesma requisição repetida" de "mesma chave com conteúdo
diferente" — o segundo caso é erro do cliente, não replay.

## Decisão

### HMAC-SHA256

```text
canonical = METHOD + "\n" + PATH_WITH_QUERY + "\n" + TIMESTAMP + "\n" + SHA256_HEX(body)
X-Timestamp = epoch seconds UTC
X-Signature = lowercase_hex(HMAC_SHA256(HMAC_SECRET, canonical))
```

- Corpo canônico: JSON com chaves ordenadas, sem espaços, UTF-8.
- No `POST` externo, o corpo assinado exclui o campo `assinatura`; o campo é comparado
  com `X-Signature` depois da validação.
- `GET` assina corpo vazio (`SHA256_HEX("")`).
- Tolerância de timestamp de ±300 s; fora da janela ou divergência → `401`.
- Segredo único por ambiente em `HMAC_SECRET`; vetor compartilhado em
  `contracts/hmac-vector.json`, asserido pelos dois lados.

### Idempotência

- `POST /api/v1/portability-operations` exige `Idempotency-Key` UUID.
- `portability_operations.idempotency_key` tem `UNIQUE`; a criação é uma transação
  única com captura de violação de unicidade.
- Mesma chave + mesmo payload → `202` com a operação existente e
  `Idempotent-Replay: true`.
- Mesma chave + payload diferente → `409 IDEMPOTENCY_CONFLICT`.
- O worker reenvia à Central com a **mesma** chave em todos os retries; a Central também
  mantém o registro de chave, então um reenvio nunca duplica a operação do outro lado.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| mTLS com certificados | Exige PKI e keystore; custo desproporcional ao desafio. |
| JWT assinado | Resolve autenticação, não integridade de corpo e path; expiração e revogação trazem complexidade sem ganho. |
| Sem assinatura, apenas rede interna | Não prova integridade e não atende ao requisito do edital. |
| Deduplicação apenas em memória no backend | Perde o estado no restart e não protege contra concorrência entre instâncias. |
| Chave idempotente derivada do payload | Reenvio legítimo com dado corrigido seria bloqueado; o cliente precisa controlar a chave. |

## Consequências

**Positivas**
- Nenhuma dependência de PKI; segredo único e vetor testável.
- Idempotência garantida pelo banco, não por cache.
- Reenvio do worker é seguro por construção.

**Negativas**
- Relógios precisam estar sincronizados dentro de ±300 s.
- Payload precisa ser canonicalizado antes de assinar; JSON com ordem diferente falha.
- O cliente da API precisa gerar e reutilizar a chave; a UI faz isso uma vez por
  tentativa.

**Mitigação**
- Vetor HMAC no contrato, rodado nos dois lados.
- Teste de concorrência com duas chamadas simultâneas e uma divergente.
- `X-Timestamp` documentado no Swagger e no README.

## Como é verificado

- `HmacSignerTest` e `HmacValidatorTest` reproduzem `contracts/hmac-vector.json`.
- Teste de integração: duas chamadas concorrentes com a mesma chave criam uma operação.
- Teste de integração: payload divergente retorna `409`.
- Teste do worker: todos os retries usam a mesma chave.
