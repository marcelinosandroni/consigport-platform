# ConsigPort — Arquitetura, Domínio e Contratos

> Fonte de verdade técnica. Regras de negócio: [`APP.md`](./APP.md). Stack:
> [`APP-STACK.md`](./APP-STACK.md). Decisões: [`../docs/adr/`](../docs/adr/).
> Tarefas: [`specs/BACKLOG.md`](./specs/BACKLOG.md) e [`specs/PLAN.md`](./specs/PLAN.md).

---

## 1. Visão dos serviços

```text
┌──────────────────────┐        HTTPS/JSON        ┌──────────────────────┐
│  frontend (5173)     │ ───────────────────────► │  backend (8080)      │
│  React 19.2 + RSC    │ ◄─────────────────────── │  Java 21 + Boot 3.5  │
│  Vite, sem Next      │      OpenAPI /api/v1     │  Hexagonal + DDD     │
└──────────────────────┘                          └──────────┬───────────┘
                                                             │ JDBC
                                                             ▼
                                                  ┌──────────────────────┐
                                                  │  PostgreSQL 16 (5432)│
                                                  │  Flyway              │
                                                  └──────────────────────┘
                                                             │
                                          HMAC-SHA256 + JSON │
                                                             ▼
                                                  ┌──────────────────────┐
                                                  │ central mock (8081)  │
                                                  │ Spring MVC simples   │
                                                  │ memória + scheduler  │
                                                  └──────────────────────┘
```

- O **front** conhece apenas `contracts/consigport-api.yaml`; não importa código do backend.
- O **backend** é o único que fala com a Central; usa ACL para traduzir PT ↔ EN.
- O **mock** simula a Central externa e não compartilha banco nem domínio com o core.
- O **PostgreSQL** guarda autorizações, operações e eventos; a agenda vive na Central.

---

## 2. Contextos delimitados (DDD)

| Contexto | Agregado / modelo | Responsabilidade |
|---|---|---|
| `authorization` | `Authorization` | Opt-in único por beneficiário + convênio |
| `agenda` | `BeneficiaryAgenda` (read model) | Consulta de parcelas futuras na Central |
| `portability` | `PortabilityOperation` | Ciclo de vida, idempotência, polling e conta |
| `audit` | `OperationEvent` | Trilha append-only por operação |

### Agregados e invariantes

**`Authorization`**
- `AuthorizationId`, `BeneficiaryId`, `AgreementId`, `createdAt`.
- Único por `(beneficiaryId, agreementId)`.
- Registro repetido devolve a autorização existente.

**`BeneficiaryAgenda`** (não persistido)
- Lista imutável de `Installment`.
- `Installment`: `InstallmentId`, `AgreementId`, `Money amount`, `LocalDate discountDate`, `InstallmentStatus OPEN|CLOSED`.
- Só `OPEN` é elegível.

**`PortabilityOperation`** (aggregate root)
- `OperationId`, `IdempotencyKey`, `Protocol`, `BeneficiaryId`, `AgreementId`.
- `InstallmentSnapshot[]`: `installmentId`, `agreementId`, `amount`, `discountDate`.
- `AssigneeCreditor`, `ReceivingAccount`, `Money totalAmount`.
- `OperationStatus status`, `RejectionReason rejectionReason`, `retryCount`, `nextPollAt`.
- Invariantes: ao menos 1 parcela; todas do mesmo beneficiário; todas `OPEN`; total = soma; conta não vazia; transições somente pela máquina de estados.
- Emite eventos de domínio; a aplicação persiste o evento na mesma transação.

**`OperationEvent`**
- `eventId` (bigserial), `operationId`, `previousStatus`, `newStatus`, `eventType`, `details`, `rawPayload`, `createdAt`.
- Append-only. Nunca atualizado ou removido.

### Value objects (EN)

`BeneficiaryId`, `AgreementId`, `InstallmentId`, `IdempotencyKey`, `Protocol`, `Money` (`BigDecimal`, 2 casas, não negativo), `ReceivingAccount` (bank, branch, account não vazios), `HmacSignature`, `RejectionReason` (sealed).

### Eventos de domínio

`OperationCreated`, `OperationSubmitted`, `PollAttempted`, `OperationConfirmed`, `OperationRejected`, `AccountUpdated`, `OperationCompleted`, `IntegrationFailed`.

`eventType` persistido: `OPERATION_CREATED`, `OPERATION_SUBMITTED`, `POLL_ATTEMPT`, `OPERATION_CONFIRMED`, `OPERATION_REJECTED`, `ACCOUNT_UPDATED`, `OPERATION_COMPLETED`, `INTEGRATION_FAILED`.

---

## 3. Backend — árvore hexagonal

```text
backend/src/main/java/br/com/consigport/
├── ConsigPortApplication.java
├── domain/
│   ├── authorization/   Authorization, AuthorizationRepository, exceptions
│   ├── agenda/          BeneficiaryAgenda, Installment, AgendaGateway
│   ├── portability/     PortabilityOperation, OperationStatus, Money,
│   │                    ReceivingAccount, PortabilityOperationRepository,
│   │                    CentralPortabilityPort, OperationPolicy
│   ├── audit/           OperationEvent, OperationEventRepository
│   └── shared/          BeneficiaryId, AgreementId, InstallmentId,
│                        IdempotencyKey, Protocol, DomainError
├── application/
│   ├── authorization/   RegisterAuthorization, FindAuthorization
│   ├── agenda/          QueryAgenda
│   ├── portability/     StartOperation, PollOperation, CompleteOperation,
│   │                    FindOperation, ListOperations
│   └── audit/           ListOperationEvents, AppendOperationEvent
├── adapters/
│   ├── in/web/          AuthorizationResource, AgendaResource,
│   │                    PortabilityOperationResource, OperationEventResource,
│   │                    ApiExceptionHandler, dto/, i18n/
│   ├── out/persistence/ JPA entities, repositories, mappers, outbox? (não)
│   └── out/central/     CentralHttpClient, CentralProperties,
│                        acl/CentralAcl, dto/, HmacSigner
└── config/              CompositionRoot, ResilienceConfig, SchedulingConfig,
                         MessageSourceConfig, OpenApiConfig
```

Regras:
- `domain/` não importa Spring, JPA, HTTP, Jackson ou logger.
- `application/` importa apenas `domain/`.
- Adaptadores implementam interfaces de `domain/`.
- `config/` é o único lugar que instancia implementações concretas.
- Mapeamento explícito; sem Lombok, MapStruct ou reflection no domínio.

### Portas principais

| Porta | Implementação |
|---|---|
| `AuthorizationRepository` | JPA |
| `AgendaGateway` | Cliente da Central via ACL |
| `PortabilityOperationRepository` | JPA |
| `OperationEventRepository` | JPA append-only |
| `CentralPortabilityPort` | HTTP + HMAC |
| `Clock` | `Clock.systemUTC()` |

### Camada anticorrupção (ACL)

`adapters/out/central/acl` traduz **somente** entre o contrato externo PT e o domínio EN.
Nenhum DTO externo atravessa a ACL. Nenhum tipo de domínio é serializado para a Central.

| Externo (PT) | Domínio / API pública (EN) |
|---|---|
| `beneficiarioId` | `beneficiaryId` |
| `convenioId` | `agreementId` |
| `parcelaId` | `installmentId` |
| `valor` | `amount` |
| `dataDesconto` | `discountDate` |
| `ABERTA` | `OPEN` |
| `credorCessionario` | `assigneeCreditor` |
| `contaRecebimento.banco` | `receivingAccount.bank` |
| `contaRecebimento.agencia` | `receivingAccount.branch` |
| `contaRecebimento.conta` | `receivingAccount.account` |
| `assinatura` | `HmacSignature` |
| `protocolo` | `protocol` |
| `motivoRejeicao` | `rejectionReason` |
| `PENDENTE` | `PENDING` |
| `CONFIRMADA` | `CONFIRMED` |
| `REJEITADA` | `REJECTED` |
| `MARGEM_CONSIGNADA_INSUFICIENTE` | `MARGIN_INSUFFICIENT` |
| `autorizacaoId` | `authorizationId` |

---

## 4. Mock da Central — estrutura simples

```text
central-convenios-mock/src/main/java/br/com/consigport/central/
├── CentralConveniosMockApplication.java
├── api/          AuthorizationController, AgendaController,
│                 PortabilityController, AdminController, dto/
├── domain/       Authorization, Installment, Operation, OperationStatus
├── store/        InMemoryCentralStore, SeedData, TransitionScheduler
├── security/     CanonicalRequest, HmacValidator
└── chaos/        ChaosPolicy
```

- Sem banco, sem Flyway, sem camadas hexagonais.
- `ConcurrentHashMap` + `ScheduledExecutorService`.
- Swagger em `/swagger-ui.html`; contrato em `contracts/central-api.yaml`.
- Estado reiniciável por `POST /_admin/reset`; leitura por `GET /_admin/state`.

### Fixtures determinísticas

| Beneficiário | Parcelas | Cenário |
|---|---|---|
| `BEN-001` | `PAR-001` 450,00; `PAR-002` 300,00; `PAR-003` 1.200,00 | caminho feliz (`CONFIRMADA`) |
| `BEN-REJ` | `PAR-101` 250,00 | rejeição por sufixo |
| `BEN-MARGIN` | `PAR-301` 6.000,00 | rejeição por margem (> 5.000,00) |
| `BEN-TIMEOUT` | `PAR-201` 500,00 | dois primeiros GETs de status atrasam 6 s |

Datas de desconto entre `2026-12-05` e `2027-05-05`, todas `ABERTA`.

### Regras de caos (determinísticas)

| Gatilho | Comportamento |
|---|---|
| Beneficiário termina em `-REJ` | após delay, `REJEITADA` com `MARGEM_CONSIGNADA_INSUFICIENTE` |
| Soma das parcelas > `5000.00` | idem |
| Beneficiário termina em `-TIMEOUT` | contador por operação: 1º e 2º GETs de status dormem `MOCK_TIMEOUT_DELAY_MS`; 3º responde normal |
| Header `X-Mock-Delay-Ms: <n>` | atrasa somente aquela resposta em `n` ms |
| Header `X-Mock-Force-Error: 503` | responde `503` somente àquela requisição |
| `POST /_admin/reset` | restaura fixtures e zera operações/contadores |

Sem aleatoriedade. O mesmo estado produz a mesma sequência.

---

## 5. Frontend — React RSC sem Next

```text
frontend/src/
├── app/                 router.tsx, shell.tsx, entry-server.tsx, entry-client.tsx
├── features/
│   ├── authorization/   server/ (RSC), ui/ (ilha cliente do formulário)
│   ├── agenda/          server/, ui/ (seleção de parcelas)
│   ├── portability/     server/, ui/ (conta e envio)
│   ├── operations/      server/, ui/ (painel + polling)
│   └── audit/           server/, ui/ (timeline)
├── shared/
│   ├── api/             cliente gerado do OpenAPI, erros, locale
│   ├── i18n/            pt-BR.json, en.json, es.json, provider
│   ├── ui/              componentes shadcn/Radix + primitives
│   └── format/          moeda, data, status
├── theme.css            cópia verbatim de SDD/themes/matrix/tokens.css
└── styles.css           Tailwind + base
```

- Server Components por padrão; `"use client"` apenas em folhas interativas.
- Shell e rotas estáticas pré-renderizadas no build; rotas dinâmicas em streaming SSR.
- Hidratação seletiva (incremental) do React 19; sem ISR para dados transacionais.
- Polling de status com TanStack Query em ilha cliente; o resto é servidor.
- Sem Next.js, sem Redux/Zustand, sem Axios.
- Design: `DESIGN.md` Matrix, dark-only, tokens em `theme.css`, fontes self-hosted
  (`@fontsource`), responsivo conforme §4b do design.

---

## 6. Banco de dados

### `authorizations`

```sql
CREATE TABLE authorizations (
    id              UUID PRIMARY KEY,
    beneficiary_id  VARCHAR(64) NOT NULL,
    agreement_id    VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_authorizations_beneficiary_agreement
        UNIQUE (beneficiary_id, agreement_id)
);
```

### `portability_operations`

```sql
CREATE TABLE portability_operations (
    id                  UUID PRIMARY KEY,
    idempotency_key     VARCHAR(64) NOT NULL,
    protocol            VARCHAR(64),
    beneficiary_id      VARCHAR(64) NOT NULL,
    installments        JSONB NOT NULL,
    assignee_creditor   VARCHAR(255) NOT NULL,
    receiving_bank      VARCHAR(64) NOT NULL,
    receiving_branch    VARCHAR(32) NOT NULL,
    receiving_account   VARCHAR(64) NOT NULL,
    total_amount        NUMERIC(15,2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    rejection_reason    VARCHAR(255),
    retry_count         INTEGER NOT NULL DEFAULT 0,
    next_poll_at        TIMESTAMPTZ,
    account_updated_at  TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_portability_operations_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uk_portability_operations_protocol UNIQUE (protocol),
    CONSTRAINT ck_portability_operations_status CHECK (
        status IN ('CREATED','PENDING','CONFIRMED','COMPLETED','REJECTED','INTEGRATION_ERROR')
    ),
    CONSTRAINT ck_portability_operations_installments CHECK (
        jsonb_typeof(installments) = 'array' AND jsonb_array_length(installments) > 0
    ),
    CONSTRAINT ck_portability_operations_total_amount CHECK (total_amount >= 0)
);

CREATE INDEX ix_portability_operations_poll
    ON portability_operations (status, next_poll_at);
```

`installments` guarda o snapshot:

```json
[
  {"installmentId":"PAR-001","agreementId":"AGR-001","amount":450.00,"discountDate":"2026-12-05"},
  {"installmentId":"PAR-002","agreementId":"AGR-001","amount":300.00,"discountDate":"2027-01-05"}
]
```

### `operation_events`

```sql
CREATE TABLE operation_events (
    id              BIGSERIAL PRIMARY KEY,
    operation_id    UUID NOT NULL REFERENCES portability_operations(id),
    previous_status VARCHAR(32),
    new_status      VARCHAR(32) NOT NULL,
    event_type      VARCHAR(48) NOT NULL,
    details         TEXT NOT NULL,
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_operation_events_operation
    ON operation_events (operation_id, created_at);
```

### Imutabilidade no banco

```sql
CREATE OR REPLACE FUNCTION reject_operation_event_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'operation_events is append-only';
END;
$$;

CREATE TRIGGER trg_operation_events_append_only
BEFORE UPDATE OR DELETE ON operation_events
FOR EACH ROW EXECUTE FUNCTION reject_operation_event_mutation();
```

Migrations em `backend/src/main/resources/db/migration/V1__create_schema.sql`.

---

## 7. Máquina de estados

```text
            ┌───────────┐
  criação ─►│  CREATED  │
            └─────┬─────┘
       202 OK     │     falha após retries
                  ▼                 ▼
            ┌───────────┐     ┌───────────────────┐
            │  PENDING  │◄────│ INTEGRATION_ERROR │
            └─────┬─────┘     └───────────────────┘
      CONFIRMADA  │  REJEITADA          ▲
                  ▼                     │ falha
            ┌───────────┐               │
            │ CONFIRMED │───────────────┘
            └─────┬─────┘
                  │ conta atualizada
                  ▼
            ┌───────────┐
            │ COMPLETED │
            └───────────┘

  REJECTED (terminal) ◄── REJEITADA
```

| De | Gatilho | Para | Evento | Efeito |
|---|---|---|---|---|
| — | inserção | `CREATED` | `OPERATION_CREATED` | snapshot + total |
| `CREATED` | Central `202` | `PENDING` | `OPERATION_SUBMITTED` | grava `protocol` |
| `CREATED` | falha após retries | `INTEGRATION_ERROR` | `INTEGRATION_FAILED` | `protocol` nulo |
| `PENDING` | poll `204` | `PENDING` | `POLL_ATTEMPT` | `next_poll_at` = `Retry-After` |
| `PENDING` | poll `CONFIRMADA` | `CONFIRMED` | `OPERATION_CONFIRMED` | limpa erro |
| `PENDING` | poll `REJEITADA` | `REJECTED` | `OPERATION_REJECTED` | grava motivo |
| `PENDING` | falha após retries | `INTEGRATION_ERROR` | `INTEGRATION_FAILED` | backoff |
| `INTEGRATION_ERROR` | reenvio `202` | `PENDING` | `OPERATION_SUBMITTED` | mesma `Idempotency-Key` |
| `INTEGRATION_ERROR` | poll `204` | `PENDING` | `POLL_ATTEMPT` | retoma polling |
| `INTEGRATION_ERROR` | poll terminal | `CONFIRMED`/`REJECTED` | evento correspondente | — |
| `CONFIRMED` | conta atualizada | `COMPLETED` | `ACCOUNT_UPDATED`, `OPERATION_COMPLETED` | `account_updated_at` |

`COMPLETED` e `REJECTED` são terminais. Nenhuma transição fora da tabela é aceita.

---

## 8. API pública ConsigPort

Base: `/api/v1`. Swagger: `/swagger-ui.html`; OpenAPI: `/v3/api-docs`;
snapshot: `contracts/consigport-api.yaml`.

### Headers

| Header | Onde | Regra |
|---|---|---|
| `Accept-Language` | front → backend | `pt-BR` padrão; `en`, `es` |
| `Idempotency-Key` | front → backend | UUID obrigatório no `POST /portability-operations` |
| `Idempotent-Replay` | resposta | `true` quando a chave já existia |
| `Content-Type` | ambos | `application/json` |

Front não assina requisições. HMAC existe apenas entre backend e Central.

### `POST /authorizations`

```json
{ "beneficiaryId": "BEN-001", "agreementId": "AGR-001" }
```

`201 Created`:

```json
{
  "authorizationId": "7f0f5d5e-2d6a-4f2e-9f4e-1b2c3d4e5f60",
  "beneficiaryId": "BEN-001",
  "agreementId": "AGR-001",
  "createdAt": "2026-11-23T12:00:00Z"
}
```

Repetição do mesmo par devolve `200` com o registro existente.

### `GET /authorizations/{beneficiaryId}`

`200` com o mesmo corpo; `404 AUTHORIZATION_NOT_FOUND`.

### `GET /beneficiaries/{beneficiaryId}/agenda`

`200`:

```json
{
  "beneficiaryId": "BEN-001",
  "installments": [
    {"installmentId":"PAR-001","agreementId":"AGR-001","amount":450.00,"discountDate":"2026-12-05","status":"OPEN"},
    {"installmentId":"PAR-002","agreementId":"AGR-001","amount":300.00,"discountDate":"2027-01-05","status":"OPEN"}
  ]
}
```

`403 AUTHORIZATION_REQUIRED` sem opt-in. `404` se a Central não conhecer o beneficiário.

### `POST /portability-operations`

Headers: `Idempotency-Key: 3f0b0d3e-8a6d-4a8f-9b2f-6f1d2c3b4a50`.

```json
{
  "beneficiaryId": "BEN-001",
  "agreementId": "AGR-001",
  "installmentIds": ["PAR-001", "PAR-002"],
  "assigneeCreditor": "ConsigPort",
  "receivingAccount": {"bank":"001","branch":"1234","account":"56789-0"}
}
```

`202 Accepted`:

```json
{
  "operationId": "c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601",
  "protocol": "PRT-20261123-0001",
  "status": "PENDING",
  "totalAmount": 750.00,
  "createdAt": "2026-11-23T12:00:05Z"
}
```

Replay: mesmo `202` + `Idempotent-Replay: true`. Conflito de payload: `409
IDEMPOTENCY_CONFLICT`. Parcela inelegível: `422 INSTALLMENT_NOT_ELIGIBLE`.

### `GET /portability-operations?status=PENDING&page=0&size=20`

```json
{
  "items": [
    {
      "operationId": "c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601",
      "protocol": "PRT-20261123-0001",
      "beneficiaryId": "BEN-001",
      "status": "PENDING",
      "totalAmount": 750.00,
      "createdAt": "2026-11-23T12:00:05Z",
      "updatedAt": "2026-11-23T12:00:05Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### `GET /portability-operations/{operationId}`

Inclui `installments`, `assigneeCreditor`, `receivingAccount`, `rejectionReason`,
`accountUpdatedAt`, `retryCount`.

### `GET /portability-operations/{operationId}/events`

```json
{
  "operationId": "c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601",
  "events": [
    {
      "eventId": 1,
      "previousStatus": null,
      "newStatus": "CREATED",
      "eventType": "OPERATION_CREATED",
      "details": "Operação criada com 2 parcelas",
      "rawPayload": null,
      "createdAt": "2026-11-23T12:00:05Z"
    }
  ]
}
```

### Erro

```json
{
  "code": "IDEMPOTENCY_CONFLICT",
  "message": "A chave de idempotência já foi usada com outro conteúdo.",
  "locale": "pt-BR",
  "timestamp": "2026-11-23T12:00:06Z",
  "path": "/api/v1/portability-operations",
  "details": []
}
```

Códigos: `VALIDATION_ERROR`, `AUTHORIZATION_NOT_FOUND`, `AUTHORIZATION_REQUIRED`,
`BENEFICIARY_NOT_FOUND`, `OPERATION_NOT_FOUND`, `IDEMPOTENCY_KEY_REQUIRED`,
`IDEMPOTENCY_CONFLICT`, `INSTALLMENT_NOT_ELIGIBLE`, `CENTRAL_UNAVAILABLE`,
`INTERNAL_ERROR`.

`message` vem de `messages_pt_BR.properties`, `messages_en.properties` ou
`messages_es.properties`. `code` nunca é traduzido.

---

## 9. Contrato externo da Central (mock)

Sem prefixo de versão; fiel ao edital. `contracts/central-api.yaml`.

| Método | Rota | Sucesso |
|---|---|---|
| `POST` | `/autorizacoes` | `201 {"autorizacaoId":"AUT-..."}` |
| `GET` | `/beneficiarios/{beneficiarioId}/agenda` | `200` com `parcelaId`, `convenioId`, `valor`, `dataDesconto`, `status` (`ABERTA`) |
| `POST` | `/operacoes-portabilidade` | `202 {"protocolo":"PRT-..."}` |
| `GET` | `/operacoes-portabilidade/{protocolo}` | `200 {"status":"CONFIRMADA","motivoRejeicao":null}` ou `204` + `Retry-After: 2` |
| `POST` | `/_admin/reset` | `204` |
| `GET` | `/_admin/state` | `200` com o estado em memória |

`POST /operacoes-portabilidade`:

```json
{
  "beneficiarioId": "BEN-001",
  "parcelas": ["PAR-001", "PAR-002"],
  "credorCessionario": "ConsigPort",
  "contaRecebimento": {"banco":"001","agencia":"1234","conta":"56789-0"},
  "assinatura": "f3a1c9..."
}
```

Headers: `X-Signature`, `X-Timestamp`, `Idempotency-Key`. Caos: `X-Mock-Delay-Ms`,
`X-Mock-Force-Error`.

Erros externos são em PT (`ASSINATURA_INVALIDA`, `REQUISICAO_INVALIDA`,
`SEM_AUTORIZACAO`, `BENEFICIARIO_NAO_ENCONTRADO`, `IDEMPOTENCIA_CONFLITO`,
`PARCELA_INVALIDA`, `OPERACAO_NAO_ENCONTRADA`) e mapeados pela ACL para os códigos EN
do core.

---

## 10. HMAC-SHA256

```text
canonical = METHOD + "\n" + PATH_WITH_QUERY + "\n" + TIMESTAMP + "\n" + SHA256_HEX(body)
X-Timestamp = epoch seconds UTC
X-Signature = lowercase_hex(HMAC_SHA256(HMAC_SECRET, canonical))
```

- `body` é o JSON canônico (chaves ordenadas, sem espaços, UTF-8).
- No `POST` externo, `body` é o payload **sem** o campo `assinatura`; o campo espelha
  `X-Signature` e é comparado após a validação.
- `GET` usa body vazio; `SHA256_HEX("")` é o hash.
- Tolerância de timestamp: ±300 s. Fora da janela ou assinatura divergente: `401`.
- Vetor compartilhado em `contracts/hmac-vector.json`, asserido no backend e no mock.

---

## 11. Resiliência e polling

| Item | Valor |
|---|---|
| Timeout de conexão | 2 s |
| Timeout de leitura | 3 s |
| Retry | 3 tentativas, 500 ms ×2 + jitter 0,5 |
| Circuit breaker | janela 10, falha 50%, aberto 10 s, half-open 3 chamadas |
| Scheduler | `fixedDelay` 2 s, lote 20 |
| `Retry-After` | honrado no `next_poll_at` |
| Falhas até `INTEGRATION_ERROR` | 5 |
| Backoff em `INTEGRATION_ERROR` | 60 s (teto) |
| Concorrência | `@Version` otimista; instância única (trade-off documentado) |

Fluxo do worker:
1. Busca operações `PENDING` ou `INTEGRATION_ERROR` com `next_poll_at <= now()`.
2. Se não há `protocol`, reenvia com a mesma `Idempotency-Key`.
3. Se há `protocol`, consulta o status.
4. `204` → `POLL_ATTEMPT`, próximo poll.
5. Terminal → atualiza status e, em `CONFIRMED`, a conta na mesma transação.
6. Falha → `INTEGRATION_FAILED`, incrementa `retry_count`, agenda backoff.

---

## 12. i18n

| Superfície | Padrão | Suportados |
|---|---|---|
| API | `pt-BR` | `pt-BR`, `en`, `es` |
| Frontend | `pt-BR` | `pt-BR`, `en`, `es` |
| Mock externo | `pt-BR` | `pt-BR` |
| Docs | `pt-BR` | — |
| Código, domínio, logs técnicos, commits | `en` | — |

O front envia `Accept-Language` conforme a preferência do usuário; o backend negocia e
devolve `locale` no erro. A UI mantém catálogos próprios e usa o `code` do erro como
chave estável, com `message` do servidor como fallback.

---

## 13. Segurança e observabilidade

- Sem autenticação de usuário no escopo; a demo não expõe dados reais.
- HMAC garante integridade backend ↔ Central; segredo em `HMAC_SECRET`.
- CORS restrito ao front (`5173`) em desenvolvimento.
- Swagger habilitado em dev/demo; Actuator expõe `health` e `info`.
- Logs estruturados com `operationId`, `protocol`, `status` e `attempt`.
- Nenhum payload sensível em log; `rawPayload` é auditado no banco, não no stdout.

---

## 14. Gates e decisões

- Testes, contratos, E2E e performance: [`../docs/quality/GATES.md`](../docs/quality/GATES.md).
- Hexagonal + ACL: [ADR-0001](../docs/adr/0001-hexagonal-ddd-acl-e-mock-isolado.md).
- HMAC + idempotência: [ADR-0002](../docs/adr/0002-integridade-hmac-e-idempotencia.md).
- Polling + auditoria: [ADR-0003](../docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md).
- Contrato + i18n: [ADR-0004](../docs/adr/0004-contrato-openapi-versionamento-e-i18n.md).
- Frontend RSC: [ADR-0005](../docs/adr/0005-frontend-rsc-design-e-renderizacao.md).
- CI/CD: [ADR-0006](../docs/adr/0006-gitflow-ci-cd-docker-e-performance.md).
