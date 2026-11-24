# 🏛️ Arquitetura do Sistema e Matriz de Rastreabilidade — ConsigPort

> Documento de referência visual e rastreabilidade bidirecional entre os requisitos do desafio técnico, regras de negócio de domínio (BR), decisões arquiteturais (ADRs), topologia de execução do monorepo e portões de qualidade.
>
> 🌐 **Visualizador Interativo:** Para explorar a matriz com filtros de busca, abas dinâmicas e visualização gráfica, abra o arquivo [`traceability-matrix.html`](./traceability-matrix.html) no navegador.

---

## 1. Visão Geral da Topologia do Monorepo

O ConsigPort é construído como um monorepo com três módulos executáveis desacoplados e um banco de dados relacional, orquestrados via Docker Compose:

```mermaid
flowchart TB
    subgraph Client["Cliente / Navegador"]
        Browser["👤 Operador ConsigPort (Browser)"]
    end

    subgraph Platform["Plataforma ConsigPort (Docker Network: consigport-net)"]
        Frontend["🌐 frontend/\nReact 19.2 + Vite RSC + Tailwind 4\n(Porta: 5173)"]
        Backend["⚙️ backend/\nJava 21 + Spring Boot 3.5.x\nHexagonal + DDD + ACL\n(Porta: 8080)"]
        Postgres[("🗄️ PostgreSQL 16\nOperações, Auditoria, Idempotência\n(Porta: 5432)")]
    end

    subgraph External["Ambiente Externo Simulado"]
        Mock["🏛️ central-convenios-mock/\nCentral de Convênios Simulado\nStateful em memória\n(Porta: 8081)"]
    end

    Browser -->|HTTP/SSR/JSON| Frontend
    Frontend -->|REST API /api/v1| Backend
    Backend -->|JDBC / Flyway| Postgres
    Backend -->|REST + HMAC-SHA256\nContrato em PT| Mock

    classDef core fill:#161b22,stroke:#58a6ff,stroke-width:2px,color:#fff;
    classDef ext fill:#161b22,stroke:#d29922,stroke-width:2px,color:#fff;
    classDef db fill:#161b22,stroke:#bc8cff,stroke-width:2px,color:#fff;
    class Frontend,Backend core;
    class Mock ext;
    class Postgres db;
```

---

## 2. Camadas da Arquitetura Hexagonal (Ports & Adapters)

O módulo `backend/` adota isolamento estrito de domínio, prevenindo que dependências de infraestrutura, Spring Framework, JPA ou o contrato em português da Central contaminem as regras de negócio:

```mermaid
flowchart TD
    subgraph InboundAdapters["Adaptadores de Entrada (Inbound Adapters)"]
        WebCtrl["REST Controllers\n/api/v1/authorizations\n/api/v1/portability-operations"]
        IdemFilter["IdempotencyFilter\nValidação de Header & Cache"]
    end

    subgraph Application["Camada de Aplicação (Application Layer)"]
        InPorts["Portas de Entrada (Use Cases)\n- RegisterAuthorizationUseCase\n- RequestPortabilityUseCase\n- PollPortabilityStatusUseCase"]
        OutPorts["Portas de Saída (Output Ports)\n- PortabilityRepositoryPort\n- CentralClientPort\n- EventAuditRepositoryPort"]
    end

    subgraph Domain["Núcleo de Domínio Puro (Domain Core - Zero Frameworks)"]
        Agg["PortabilityOperation (Agregado Raiz)"]
        AuthEnt["Authorization (Entidade)"]
        VOs["Value Objects\nReceivingAccount, Installment, Money"]
        Events["OperationEvent (Imutável)"]
        StateMachine["Máquina de Estados\nCREATED ➔ PENDING ➔ CONFIRMED ➔ COMPLETED\n(ou REJECTED / INTEGRATION_ERROR)"]
    end

    subgraph ACL_Layer["Camada Anticorrupção (ACL)"]
        ACL["CentralAntiCorruptionLayer\nTradução PT ⇄ EN\nPENDENTE ⇄ PENDING\nbeneficiarioId ⇄ beneficiaryId"]
    end

    subgraph OutboundAdapters["Adaptadores de Saída (Outbound Adapters)"]
        JpaRepo["PostgresJpaRepositoryAdapter\nSpring Data JPA + @Immutable Trigger"]
        CentralClient["CentralRestClientAdapter\nWebClient + HMAC-SHA256 + Resilience4j"]
    end

    WebCtrl --> InPorts
    IdemFilter --> InPorts
    InPorts --> Agg
    InPorts --> AuthEnt
    Agg -.-> Events
    Agg -.-> StateMachine
    InPorts --> OutPorts
    OutPorts --> JpaRepo
    OutPorts --> ACL
    ACL --> CentralClient

    classDef dom fill:#0d2818,stroke:#3fb950,stroke-width:2px,color:#fff;
    classDef app fill:#0c2d48,stroke:#58a6ff,stroke-width:2px,color:#fff;
    classDef acl fill:#3d2c00,stroke:#d29922,stroke-width:2px,color:#fff;
    class Domain dom;
    class Application app;
    class ACL_Layer acl;
```

---

## 3. Fluxo de Vida Assíncrono com Polling e Idempotência

```mermaid
sequenceDiagram
    autonumber
    actor Operador as 👤 Operador (Front)
    participant Backend as ⚙️ Backend Core
    participant DB as 🗄️ PostgreSQL 16
    participant Central as 🏛️ Central Mock

    Operador->>Backend: POST /api/v1/portability-operations [Idempotency-Key: UUID]
    Note over Backend,DB: Valida chave de idempotência
    Backend->>DB: Salva operação (Status: CREATED) + Evento 1
    Backend->>Central: POST /operacoes-portabilidade [Assinado HMAC-SHA256]
    Central-->>Backend: 202 Accepted { protocolo: "PRT-987" }
    Backend->>DB: Atualiza (Status: PENDING, protocolo) + Evento 2
    Backend-->>Operador: 202 Accepted { id: "...", status: "PENDING", protocol: "PRT-987" }

    loop Polling com Backoff (honrando Retry-After)
        Backend->>Central: GET /operacoes-portabilidade/PRT-987 [HMAC-SHA256]
        alt Ainda processando
            Central-->>Backend: 204 No Content [Retry-After: 2]
            Backend->>DB: Registra evento POLLING_ATTEMPT
        else Processamento concluído
            Central-->>Backend: 200 OK { status: "CONFIRMADA" }
        end
    end

    Backend->>DB: Atualiza conta de recebimento & status: COMPLETED + Evento Final
    Backend->>DB: Commita transação imutável
    Operador->>Backend: GET /api/v1/portability-operations/{id}
    Backend-->>Operador: 200 OK { status: "COMPLETED", auditTrail: [...] }
```

---

## 4. Matriz de Rastreabilidade Resumida

| ID | Requisito (Desafio Técnico) | Regra de Domínio | ADR | Componente / Camada | Teste / Gate de Validação |
|:---:|---|:---:|:---:|---|---|
| **RF-01** | Captura de autorização (*opt-in*) | `BR-01, BR-02` | [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) | `backend/authorizations` + `ACL` | JUnit 5 + MockMvc + Mock da Central |
| **RF-02** | Consulta de agenda de descontos | `BR-01, BR-03` | [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) | `backend/agenda` + `CentralClient` | 403 se não autorizado; 200 com parcelas OPEN |
| **RF-03** | Seleção de parcelas e nova portabilidade | `BR-04, BR-05, BR-06` | [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) | `PortabilityOperation` (Agregado) | Validação de domínio pura; snapshot de parcelas |
| **RF-04** | Acompanhamento assíncrono do status | `BR-08, BR-09, BR-10` | [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `OperationStatusPoller` + Scheduler | Respeito a `Retry-After: 2`; terminal em COMPLETED |
| **RF-05** | Linha do tempo / Auditoria por operação | `BR-12, BR-13` | [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `operation_events` + Front Timeline | Trigger PostgreSQL contra UPDATE/DELETE |
| **RF-06** | Tratamento de rejeição e falha | `BR-11, BR-14, BR-15` | [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `GlobalExceptionHandler` + Retry | Cenários `-REJ` e `-TIMEOUT` simulados no mock |
| **RNF-01**| Integridade e assinatura HMAC-SHA256 | `APP-STACK.md §3` | [ADR-0002](../adr/0002-integridade-hmac-e-idempotencia.md) | `HmacSignatureService` | Vetor `contracts/hmac-vector.json` validado |
| **RNF-02**| Idempotência estrita via header | `BR-07, BR-15` | [ADR-0002](../adr/0002-integridade-hmac-e-idempotencia.md) | `IdempotencyFilter` + Tabela única | Teste de concorrência multithread |
| **RNF-03**| Polling assíncrono com backoff e retry | `BR-08, BR-09` | [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `Resilience4j` + Poller | Testes unitários com simulação de 204 |
| **RNF-04**| Erros de integração sem duplicação | `BR-15` | [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md), [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `OperationStatus.INTEGRATION_ERROR` | Reenvio mantém chave de idempotência original |
| **RNF-05**| Trilha de auditoria append-only imutável | `BR-12, BR-13` | [ADR-0003](../adr/0003-assincronismo-polling-e-trilha-imutavel.md) | `@Immutable` + Trigger PostgreSQL | Teste SQL comprovando bloqueio de mutações |
| **RNF-06**| Código limpo e Arquitetura Hexagonal | `clean-code.md` | [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) | Pacotes `domain`, `app`, `adapters` | Testes `ArchUnit` em tempo de compilação |
| **RNF-07**| Cobertura de testes automatizados | `docs/quality/GATES.md`| [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md) | JUnit 5, Vitest, Playwright, k6 | JaCoCo &ge; 80%; E2E e k6 validados no CI |
| **RNF-08**| Contratos OpenAPI 3.1 First e i18n | `BR-16` | [ADR-0004](../adr/0004-contrato-openapi-versionamento-e-i18n.md) | `contracts/*.yaml` + Springdoc | Spectral linter + detecção de breaking change |
| **RNF-09**| Desempenho e SLAs de latência | `GATES.md §3.6` | [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md) | k6 runner (CI smoke e Nightly) | p95 &lt; 200ms em leitura; p95 &lt; 500ms em escrita |
| **REST-01**| Simulação da Central externa em memória | `central-convenios-mock`| [ADR-0001](../adr/0001-hexagonal-ddd-acl-e-mock-isolado.md) | Módulo isolado Java 21 | Sem banco; porta 8081; contrato fiel ao edital |
| **REST-02**| Frontend React 19.2 + Vite RSC + Matrix | `DESIGN.md` | [ADR-0005](../adr/0005-frontend-rsc-design-e-renderizacao.md) | Módulo `frontend/` | Sem Next.js; streaming SSR e hidratação leve |
| **REST-03**| Documentação e governança do AI Harness | `HARNESS_OVERVIEW.md` | [ADR-0006](../adr/0006-gitflow-ci-cd-docker-e-performance.md) | `docs/ai-harness/` + `AGENTS.md` | Sessões de IA registradas e rastreáveis |

---

## 5. Como Executar a Matriz Interativa

Para interagir com a matriz em modo dinâmico (filtros por tipo de requisito, pesquisa por termo em tempo real e visualizadores de camadas hexagonais):

1. Navegue até o diretório `docs/architecture/`.
2. Abra o arquivo `traceability-matrix.html` no seu navegador favorito (ex.: Google Chrome, Firefox ou Edge).
3. Utilize as abas e os campos de busca para filtrar instantaneamente requisitos, ADRs associadas e critérios de teste.
