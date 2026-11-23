# ConsigPort — Regras de Produto e Domínio

> Plataforma de portabilidade de convênios de desconto em folha. Este documento é a
> fonte de verdade do negócio. Arquitetura e contratos ficam em
> [`ARCHITECTURE.md`](./ARCHITECTURE.md); stack em [`APP-STACK.md`](./APP-STACK.md);
> tarefa ativa em [`specs/PLAN.md`](./specs/PLAN.md).

---

## 1. Visão

O ConsigPort assume a posição de novo credor de parcelas de convênios de desconto em
folha. Para isso, captura a autorização do beneficiário, consulta a agenda futura na
Central de Convênios, registra uma operação de portabilidade e acompanha a confirmação
assíncrona até atualizar a conta de recebimento.

O sistema é um monorepo com três executáveis:

| Módulo | Papel | Porta |
|---|---|---|
| `backend/` | Core ConsigPort: autorização, agenda, operação, auditoria | `8080` |
| `central-convenios-mock/` | Central externa simulada, stateful e determinística | `8081` |
| `frontend/` | SPA React com RSC, design Matrix | `5173` |

O PostgreSQL 16 roda na porta `5432`.

---

## 2. Atores

| Ator | Responsabilidade |
|---|---|
| Beneficiário | Autoriza a consulta da agenda e escolhe as parcelas (via operador) |
| Operador ConsigPort | Executa o fluxo na interface |
| ConsigPort (credor cessionário) | Solicita a portabilidade e recebe as parcelas |
| Central de Convênios | Mantém autorizações, agenda e confirmação assíncrona |
| Sistema | Garante idempotência, integridade HMAC e trilha imutável |

Autenticação de usuário está fora do escopo do desafio. A integridade entre ConsigPort
e Central é garantida por HMAC-SHA256.

---

## 3. Glossário (domínio em inglês)

| Termo de domínio | Equivalente PT-BR | Definição |
|---|---|---|
| `Beneficiary` | Beneficiário | Pessoa titular das parcelas |
| `Agreement` | Convênio | Origem do desconto em folha |
| `Installment` | Parcela | Desconto futuro com valor e data |
| `Authorization` | Autorização / opt-in | Consentimento do beneficiário para consultar a agenda |
| `Agenda` | Agenda de descontos | Lista de parcelas futuras de um beneficiário autorizado |
| `PortabilityOperation` | Operação de portabilidade | Agregado que representa a solicitação e seu ciclo de vida |
| `AssigneeCreditor` | Credor cessionário | Empresa que passa a receber as parcelas (ConsigPort) |
| `ReceivingAccount` | Conta de recebimento | Conta de destino informada na operação |
| `Protocol` | Protocolo | Identificador devolvido pela Central no `202 Accepted` |
| `OperationEvent` | Evento da operação | Registro imutável de criação, envio, polling, erro ou transição |
| `AuditTrail` | Trilha de auditoria | Conjunto ordenado dos eventos de uma operação |
| `Anti-Corruption Layer` | Camada anticorrupção (ACL) | Tradução entre o contrato externo em PT e o domínio em inglês |
| `Central` | Central de Convênios | Serviço externo simulado pelo mock |

Identificadores, entidades, status, tabelas e eventos usam inglês. Mensagens humanas da
API usam pt-BR por padrão, com `en` e `es` via `Accept-Language`.

---

## 4. Regras de negócio

| ID | Regra |
|---|---|
| BR-01 | A agenda só pode ser consultada para beneficiário com `Authorization` registrada. |
| BR-02 | `Authorization` é única por par `(beneficiaryId, agreementId)`. Repetir o opt-in devolve a autorização existente. |
| BR-03 | A agenda é somente leitura e só parcelas `OPEN` são elegíveis. |
| BR-04 | Uma `PortabilityOperation` exige ao menos uma parcela, todas do mesmo beneficiário e todas `OPEN`. |
| BR-05 | A operação guarda um snapshot das parcelas (`installmentId`, `agreementId`, `amount`, `discountDate`) no momento da solicitação. |
| BR-06 | A conta de recebimento é obrigatória: banco, agência e conta, todos não vazios. |
| BR-07 | Todo `POST /portability-operations` exige `Idempotency-Key` (UUID). Mesma chave e mesmo payload devolvem a operação existente; payload diferente retorna `409`. |
| BR-08 | A Central responde `202 Accepted` com `protocol`; a operação nasce `PENDING` e a confirmação é assíncrona. |
| BR-09 | O backend consulta o status até estado terminal, honrando `Retry-After` e usando retry com backoff e circuit breaker. |
| BR-10 | `CONFIRMED` atualiza a conta de recebimento e leva a operação a `COMPLETED` na mesma transação. |
| BR-11 | `REJECTED` é terminal e exige `rejectionReason`. `INTEGRATION_ERROR` é retomável e nunca duplica a submissão. |
| BR-12 | Toda criação, envio, tentativa de polling, falha, transição e atualização de conta gera um `OperationEvent` imutável. |
| BR-13 | Nenhum evento pode ser alterado ou excluído. A garantia existe no domínio, no repositório e no banco. |
| BR-14 | Rejeição por margem: beneficiário com sufixo `-REJ` ou soma das parcelas acima de `5000.00` resulta em `MARGIN_INSUFFICIENT` na Central mock. |
| BR-15 | `INTEGRATION_ERROR` mantém a mesma `Idempotency-Key` ao reenviar e não cria nova operação. |
| BR-16 | Mensagens da API usam pt-BR por padrão; `en` e `es` são suportados. Códigos de erro são estáveis e em inglês. |

---

## 5. Fluxo ponta a ponta

1. Operador informa `beneficiaryId` e `agreementId`; o front chama `POST /authorizations`.
2. Backend registra a autorização local e replica o opt-in na Central (contrato PT).
3. Front consulta `GET /beneficiaries/{beneficiaryId}/agenda`; backend valida a autorização e busca a agenda na Central.
4. Operador seleciona uma ou mais parcelas elegíveis e informa a conta de recebimento.
5. Front envia `POST /portability-operations` com `Idempotency-Key`.
6. Backend valida, cria a operação `CREATED`, registra o primeiro evento e envia o payload assinado à Central.
7. Central responde `202` com protocolo; backend grava o protocolo e muda para `PENDING`.
8. Worker consulta o status. Enquanto `PENDENTE`, a Central responde `204` com `Retry-After: 2`; cada tentativa gera evento.
9. Em `CONFIRMADA`, backend atualiza a conta de recebimento e muda para `COMPLETED`. Em `REJEITADA`, grava o motivo e encerra. Falhas de integração viram `INTEGRATION_ERROR` e são retomadas.
10. Front exibe painel de status e timeline de eventos; a confirmação aparece por polling.

---

## 6. Fronteiras

- **Domínio:** regras, agregados, value objects, eventos e interfaces de repositório. Zero framework.
- **Aplicação:** casos de uso, transações, idempotência e orquestração do polling.
- **Adaptadores de entrada:** HTTP, Swagger, i18n, validação de contrato.
- **Adaptadores de saída:** PostgreSQL/Flyway, cliente HTTP da Central e ACL.
- **Frontend:** apenas contrato OpenAPI; não conhece detalhes internos do backend.

A API pública do ConsigPort é em inglês. O contrato da Central é em português, fiel ao
edital. A ACL faz as duas traduções. Ver
[ADR-0001](../docs/adr/0001-hexagonal-ddd-acl-e-mock-isolado.md).

---

## 7. Fora de escopo

- Login, perfis e permissões de usuário.
- Persistência de agenda no ConsigPort (a fonte é a Central).
- Webhooks, filas e processamento distribuído.
- Cálculo real de margem consignável.
- Certificado digital; HMAC é a simplificação aceita.

---

## 8. Critérios de sucesso

- Fluxo completo executável com `docker compose up`.
- Idempotência comprovada por teste de reenvio concorrente.
- HMAC validado nos dois lados, com vetor de teste compartilhado.
- Estados e eventos consistentes com [`ARCHITECTURE.md`](./ARCHITECTURE.md).
- E2E e performance verdes no GitHub Actions, com relatórios publicados.
- API documentada em Swagger e contrato validado no CI.
