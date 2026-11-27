# ADR-0003 — Assincronismo, polling e trilha imutável

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Contrato:** [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md) §7 e §11

## Contexto

A Central responde `202 Accepted` com um protocolo e só confirma depois. O ConsigPort
precisa descobrir o resultado sem manter uma conexão aberta e sem perder a ordem dos
fatos. O desafio também exige auditoria: cada tentativa, falha e transição precisa ser
comprovada.

O resultado é eventual por natureza: a operação nasce `CREATED`, vira `PENDING` e só
termina em `COMPLETED` ou `REJECTED`. No meio do caminho há `204` com `Retry-After` e
falhas de integração.

## Decisão

### Protocolo de polling

1. `POST` à Central responde `202` com `protocolo`; a operação passa de `CREATED` para
   `PENDING` na mesma transação que grava o protocolo e o evento.
2. Um worker agendado (`fixedDelay` 2 s, lote 20) busca operações
   `PENDING` ou `INTEGRATION_ERROR` com `next_poll_at <= now()`.
3. `GET` de status: `204` com `Retry-After` → evento `POLL_ATTEMPT` e novo
   `next_poll_at`; `200 CONFIRMADA` → `CONFIRMED` e, na mesma transação, a atualização da
   conta e o fechamento em `COMPLETED` (eventos `OPERATION_CONFIRMED`, `ACCOUNT_UPDATED`
   e `OPERATION_COMPLETED`, com `account_updated_at`); `200 REJEITADA` → `REJECTED` com o
   motivo.
4. Sem protocolo (falha antes do `202`), o worker reenvia com a mesma `Idempotency-Key`
   em vez de criar outra operação.
5. Falha persistente → `INTEGRATION_ERROR` com backoff exponencial até 60 s. O estado é
   retomável, nunca terminal. A falha que acontece com a operação já em
   `INTEGRATION_ERROR` é a auto-transição declarada na §7: o status não muda, o
   `retry_count` cresce e o backoff é renovado.
6. O `404` da Central (`OPERACAO_NAO_ENCONTRADA`) é uma resposta tipada, não uma
   indisponibilidade: a Central respondeu, então não conta como falha no circuito e a
   operação continua retomável, com o corpo bruto da Central na trilha.
7. Uma rejeição com um motivo que o contrato da Central não declara fecha a operação
   como `REJECTED`, com o código `UNMAPPED_REJECTION`: a decisão da Central é terminal
   e o motivo original fica no `rawPayload` do evento.

### Resiliência

| Item | Valor |
|---|---|
| Timeout de conexão / leitura | 2 s / 3 s |
| Retry | 3 tentativas, 500 ms ×2 + jitter 0,5 |
| Circuit breaker | janela 10, falha 50%, aberto 10 s, half-open 3 chamadas |
| Falhas até `INTEGRATION_ERROR` | 5 |

### Trilha imutável

- Toda criação, envio, tentativa, falha, transição e atualização de conta grava um
  `OperationEvent` com `previousStatus`, `newStatus`, `eventType`, `details`,
  `rawPayload` e `createdAt`.
- Imutabilidade em três níveis:
  1. **Domínio:** `OperationEvent` sem setters; eventos só nascem por métodos de
     fábrica.
  2. **Repositório:** porta expõe apenas `append` e consultas; sem `update`/`delete`.
  3. **Banco:** trigger `BEFORE UPDATE OR DELETE` que levanta exceção.
- Eventos são lidos em ordem cronológica e exibidos na timeline do frontend.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Webhook da Central | O contrato do edital é polling; inventar webhook mudaria o enunciado. |
| Fila (RabbitMQ, SQS) | Adiciona infraestrutura sem necessidade; o worker agendado resolve o volume do desafio. |
| `202` + long polling na requisição | Prende conexão e thread por segundos; frágil em proxy e difícil de testar. |
| Tabela de status mutável, sem eventos | Perde a prova de auditoria exigida e permite reescrever o passado. |
| Polling no frontend direto na Central | O front conheceria o contrato em PT e a assinatura; vazaria a ACL para o cliente. |

## Consequências

**Positivas**
- Resultado consistente com o contrato externo.
- Trilha completa e provada no banco, não só em log.
- Retomada segura após falha, sem duplicar operação.

**Negativas**
- Latência mínima de ~2 s entre a confirmação e a UI.
- Polling gera carga periódica na Central.
- Worker em instância única é um trade-off de escala, documentado.

**Mitigação**
- `next_poll_at` com índice composto evita varredura completa.
- `@Version` otimista impede atualização concorrente.
- Migração para lock distribuído registrada no backlog, fora do escopo.

## Como é verificado

- Testes com relógio controlado cobrindo `204`, confirmação, rejeição, falha e retomada.
- Teste da auto-transição `INTEGRATION_ERROR` → `INTEGRATION_ERROR` na matriz da máquina
  de estados e no worker.
- Teste do `404` da Central: resposta tipada, fora do circuito e com o corpo bruto na
  trilha.
- Teste da rejeição com motivo não declarado no contrato: operação fechada como
  `REJECTED` com o código `UNMAPPED_REJECTION`.
- Teste de rollback entre a confirmação e a atualização de conta: a falha no segundo
  evento desfaz o estado da operação e o evento já escrito, sobre PostgreSQL 16.
- Teste que tenta `UPDATE` e `DELETE` em `operation_events` e espera exceção.
- Teste de ordem cronológica da timeline.
