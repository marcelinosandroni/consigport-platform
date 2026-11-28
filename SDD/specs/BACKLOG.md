# 📥 BACKLOG — Catálogo de tasks atômicas

> Cada task abaixo é uma unidade de commit: 2 a 5 arquivos, menos de uma hora, com
> aceite verificável. A visão macro fica em [`ROADMAP.md`](./ROADMAP.md); a task em
> voo, em [`PLAN.md`](./PLAN.md). **Nada entra no PLAN sem existir aqui.**
>
> Regras: uma task por vez, branch própria, PR para `develop`, commit
> `type(scope): descrição. (Agent: OpenCode - MiniMax-M3.1-Flash)`.
> E2E e performance rodam somente no CI/CD — nunca na máquina local.

---

## Fase 0 — Fundação (12 tasks)

### 0.1 — Refinamento SDD completo
- **Objetivo:** specs, ADRs, harness, gates, CI de documentação e correção dos links do `AGENTS.md`.
- **Arquivos:** `SDD/**`, `docs/adr/**`, `docs/ai-harness/**`, `docs/quality/GATES.md`, `scripts/check-specs.mjs`, `.github/workflows/ci-docs.yml`, `AGENTS.md`.
- **Aceite:** todas as specs em pt-BR; 6 ADRs; gates documentados; `check-specs` e `check-docs` verdes.
- **Verificação:** `node scripts/check-specs.mjs` e `node SDD/SKILLS/check-docs/check-docs.mjs` com exit 0.

### 0.2 — Gitflow, PR template e CODEOWNERS
- **Objetivo:** materializar o fluxo de branches e a revisão obrigatória.
- **Arquivos:** `.github/PULL_REQUEST_TEMPLATE.md`, `.github/CODEOWNERS`, `docs/process/GITFLOW.md`.
- **Aceite:** template exige evidência de teste e task do PLAN; CODEOWNERS cobre `SDD/`, `contracts/` e `.github/`; documento descreve `main`/`staging`/`develop` e proteção via `gh`.
- **Verificação:** `git check-ignore` não ignora os arquivos; render do template no PR de abertura.

### 0.3 — Ambiente local e PostgreSQL
- **Objetivo:** um comando sobe a infraestrutura base.
- **Arquivos:** `.gitignore`, `.env.example`, `compose.yaml`.
- **Aceite:** `.env` e artefatos de build ignorados; variáveis do `APP-STACK.md` §3 presentes; serviço `postgres` 16-alpine com volume e healthcheck.
- **Verificação:** `docker compose config -q` e `docker compose up -d postgres` com health `healthy`.

### 0.4 — Maven Wrapper e reactor
- **Objetivo:** build Java reprodutível sem Maven global.
- **Arquivos:** `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.mvn/jvm.config`.
- **Aceite:** módulos `backend` e `central-convenios-mock` declarados; Java 21 e Boot 3.5.x no parent; wrapper commitado.
- **Verificação:** `./mvnw -N validate` e `./mvnw -q -DskipTests package` com exit 0.

### 0.5 — Contratos OpenAPI e vetor HMAC
- **Objetivo:** contrato antes do código, com exemplos executáveis.
- **Arquivos:** `contracts/consigport-api.yaml`, `contracts/central-api.yaml`, `contracts/hmac-vector.json`.
- **Aceite:** API pública `/api/v1` com schemas, erros e `Idempotency-Key`; Central fiel ao edital em PT; vetor HMAC com método, path, timestamp, body, assinatura e segredo de teste.
- **Verificação:** Spectral sem erro; vetor reproduzido por script `node scripts/hmac-vector.mjs`.

### 0.6 — Scaffold do backend
- **Objetivo:** aplicação Spring Boot vazia, compilando e com health.
- **Arquivos:** `backend/pom.xml`, `backend/src/main/java/br/com/consigport/ConsigPortApplication.java`, `backend/src/main/resources/application.yml`.
- **Aceite:** dependências web, validation, JPA, Flyway, actuator, springdoc; porta `8080`; perfil `local` com `DATABASE_URL`.
- **Verificação:** `./mvnw -q -pl backend -am verify`; `GET /actuator/health` → `UP`.

### 0.7 — Scaffold do mock
- **Objetivo:** Central simulada vazia, compilando e com health.
- **Arquivos:** `central-convenios-mock/pom.xml`, `central-convenios-mock/src/main/java/br/com/consigport/central/CentralConveniosMockApplication.java`, `central-convenios-mock/src/main/resources/application.yml`.
- **Aceite:** sem banco e sem Flyway; porta `8081`; actuator e springdoc habilitados.
- **Verificação:** `./mvnw -q -pl central-convenios-mock -am verify`; `GET /actuator/health` → `UP`.

### 0.8 — Base do frontend RSC
- **Objetivo:** projeto React 19.2 com Vite RSC, TypeScript e Tailwind, sem Next.js.
- **Arquivos:** `frontend/package.json`, `frontend/package-lock.json`, `frontend/vite.config.ts`, `frontend/tsconfig.json`, `frontend/src/styles.css`.
- **Aceite:** template oficial `rsc`; scripts `dev`, `build`, `lint`, `typecheck`, `test:unit`; Tailwind 4 consumindo tokens.
- **Verificação:** `npm ci --prefix frontend && npm run --prefix frontend build` com exit 0.

### 0.9 — Shell RSC e design Matrix
- **Objetivo:** casca visual navegável com tema e fontes locais.
- **Arquivos:** `frontend/src/app/shell.tsx`, `frontend/src/app/router.tsx`, `frontend/src/theme.css`, `frontend/src/shared/ui/`.
- **Aceite:** `theme.css` é cópia verbatim do `SDD/themes/matrix/tokens.css`; `@fontsource` para Manrope, JetBrains Mono e Playfair Display; shell dark-only responsivo conforme `DESIGN.md` §4b.
- **Verificação:** `npm run --prefix frontend build` e inspeção visual do shell em `npm run --prefix frontend dev`.

### 0.10 — Pipeline de integração contínua
- **Objetivo:** todo PR provado por jobs paralelos.
- **Arquivos:** `.github/workflows/ci.yml`, `.github/workflows/nightly.yml`.
- **Aceite:** jobs `docs`, `backend`, `mock`, `frontend`, `contracts`, `e2e`, `k6-smoke`, `security`; cache de Maven e npm; artefatos de relatório; E2E e k6 apenas no CI.
- **Verificação:** PR de teste com todos os jobs verdes e relatórios anexados.

### 0.11 — Release e entrega contínua
- **Objetivo:** versão e imagens automáticas.
- **Arquivos:** `release-please-config.json`, `.release-please-manifest.json`, `.github/workflows/release.yml`, `.github/workflows/cd-staging.yml`.
- **Aceite:** Conventional Commits geram changelog e tag; imagens publicadas em GHCR com tag SemVer e `latest`; deploy de staging por tag.
- **Verificação:** `release-please` em modo dry-run e `docker build` das três imagens com exit 0.

### 0.12 — Promoção da release `v0.1.0` e arquivamento da Fase 0
- **Objetivo:** fechar a Fase 0 com a tag `v0.1.0` e as três imagens no GHCR.
- **Arquivos:** `SDD/specs/history/phases/phase-0-finished.md`, `SDD/specs/PLAN.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`.
- **Aceite:** `develop` promovido para `staging` e `main` por PR; tag `v0.1.0` criada pelo release-please; release no GitHub com o CHANGELOG; três imagens no GHCR com `v0.1.0`, `0.1.0` e `latest`; run de staging verde; fase arquivada.
- **Verificação:** `git tag --list "v*"` mostra `v0.1.0`; `gh release view v0.1.0`; run do `cd-staging` verde; `node scripts/check-specs.mjs` com exit 0.

---

## Fase 1 — Central mock (7 tasks)

### 1.1 — Store em memória, fixtures e administração
- **Objetivo:** estado determinístico reiniciável.
- **Arquivos:** `central-convenios-mock/src/main/java/br/com/consigport/central/store/InMemoryCentralStore.java`, `SeedData.java`, `api/AdminController.java`.
- **Aceite:** fixtures do `ARCHITECTURE.md` §4; `POST /_admin/reset` → `204`; `GET /_admin/state` → estado completo; `ConcurrentHashMap`.
- **Verificação:** teste de integração reinicia duas vezes e compara o estado.

### 1.2 — Consulta de agenda
- **Objetivo:** expor parcelas futuras por beneficiário.
- **Arquivos:** `api/AgendaController.java`, `api/dto/AgendaResponse.java`, `api/dto/InstallmentResponse.java`.
- **Aceite:** `GET /beneficiarios/{id}/agenda` em PT com `parcelaId`, `convenioId`, `valor`, `dataDesconto`, `status`; `404` para desconhecido; sem autorização → `403`.
- **Verificação:** `MockMvc` para `BEN-001` (3 parcelas) e `BEN-UNKNOWN` (`404`).

### 1.3 — Opt-in de autorização
- **Objetivo:** registrar o consentimento que libera a agenda.
- **Arquivos:** `api/AuthorizationController.java`, `api/dto/AuthorizationRequest.java`, `api/dto/AuthorizationResponse.java`.
- **Aceite:** `POST /autorizacoes` → `201` com `autorizacaoId`; repetição devolve `200` com o mesmo id; validação de campos vazios.
- **Verificação:** `MockMvc` cobrindo criação, repetição e `400`.

### 1.4 — Submissão de portabilidade
- **Objetivo:** aceitar a operação e devolver protocolo.
- **Arquivos:** `api/PortabilityController.java`, `api/dto/PortabilityRequest.java`, `api/dto/PortabilityAcceptedResponse.java`.
- **Aceite:** `POST /operacoes-portabilidade` → `202` com `protocolo` `PRT-...`; estado inicial `PENDENTE`; parcelas inexistentes → `422`.
- **Verificação:** `MockMvc` conferindo `202`, protocolo único e `422`.

### 1.5 — Consulta de status com `Retry-After`
- **Objetivo:** simular a confirmação assíncrona.
- **Arquivos:** `api/PortabilityController.java`, `store/TransitionScheduler.java`, `api/dto/StatusResponse.java`.
- **Aceite:** antes do delay → `204` com `Retry-After: 2`; depois → `200` com `CONFIRMADA` ou `REJEITADA`; protocolo desconhecido → `404`.
- **Verificação:** teste com `MOCK_CONFIRMATION_DELAY_MS=200` cobrindo `204`, `200` e `404`.

### 1.6 — HMAC e idempotência
- **Objetivo:** garantir integridade e não duplicar operações.
- **Arquivos:** `security/HmacValidator.java`, `security/CanonicalRequest.java`, `store/IdempotencyRegistry.java`.
- **Aceite:** assinatura inválida ou timestamp fora de ±300 s → `401 ASSINATURA_INVALIDA`; mesma `Idempotency-Key` e payload igual → mesmo protocolo; payload diferente → `409 IDEMPOTENCIA_CONFLITO`.
- **Verificação:** teste com `contracts/hmac-vector.json` e três cenários de idempotência.

### 1.7 — Caos determinístico e contrato publicado
- **Objetivo:** produzir os cenários de rejeição e falha que o E2E consome.
- **Arquivos:** `chaos/ChaosPolicy.java`, `api/AdminController.java`, `central-convenios-mock/src/test/java/.../ContractTest.java`, `central-convenios-mock/src/main/resources/openapi/central-api.yaml`.
- **Aceite:** `-REJ` e soma acima de `5000.00` → `REJEITADA` com `MARGEM_CONSIGNADA_INSUFICIENTE`; `-TIMEOUT` atrasa os dois primeiros GETs; headers `X-Mock-Delay-Ms` e `X-Mock-Force-Error`; contrato exportado idêntico a `contracts/central-api.yaml`.
- **Verificação:** `./mvnw -q -pl central-convenios-mock -am verify` e `diff` do contrato sem diferença.

---

## Fase 2 — Backend core (18 tasks)

### 2.1 — Esqueleto hexagonal e regras ArchUnit
- **Objetivo:** criar as camadas e impedir dependências proibidas desde o primeiro commit.
- **Arquivos:** `backend/src/main/java/br/com/consigport/{domain,application,adapters,config}/`, `backend/src/test/java/br/com/consigport/architecture/HexagonalArchitectureTest.java`.
- **Aceite:** `domain` sem Spring, JPA, Jackson ou HTTP; `application` só importa `domain`; `config` é o único composition root.
- **Verificação:** `./mvnw -q -pl backend test -Dtest=HexagonalArchitectureTest`.

### 2.2 — Value objects compartilhados
- **Objetivo:** tipos fortes no lugar de `String` e `BigDecimal` soltos.
- **Arquivos:** `domain/shared/BeneficiaryId.java`, `AgreementId.java`, `InstallmentId.java`, `IdempotencyKey.java`, `Money.java`, `ReceivingAccount.java`.
- **Aceite:** validação no construtor; `Money` com 2 casas e não negativo; `IdempotencyKey` exige UUID; igualdade por valor.
- **Verificação:** testes de unidade para cada VO, incluindo casos inválidos.

### 2.3 — Agregado de autorização
- **Objetivo:** modelar o opt-in único por beneficiário e convênio.
- **Arquivos:** `domain/authorization/Authorization.java`, `AuthorizationRepository.java`, `AuthorizationAlreadyExistsException.java`.
- **Aceite:** identidade, beneficiário, convênio e `createdAt`; repositório com `findByBeneficiaryAndAgreement` e `save`; sem anotação de framework.
- **Verificação:** teste de unidade do agregado e do contrato da porta.

### 2.4 — Agregado de portabilidade e máquina de estados
- **Objetivo:** concentrar invariantes e transições no domínio.
- **Arquivos:** `domain/portability/PortabilityOperation.java`, `OperationStatus.java`, `InstallmentSnapshot.java`, `RejectionReason.java`.
- **Aceite:** ao menos 1 parcela; total = soma; snapshot imutável; transições apenas as da tabela do `ARCHITECTURE.md` §7; terminal não sai de estado.
- **Verificação:** teste parametrizado de transições válidas e inválidas.

### 2.5 — Evento de auditoria append-only
- **Objetivo:** registrar cada mudança de estado de forma imutável.
- **Arquivos:** `domain/audit/OperationEvent.java`, `OperationEventRepository.java`, `EventType.java`.
- **Aceite:** campos do `ARCHITECTURE.md` §2; sem setters; `eventType` restrito ao enum; porta expõe apenas `append` e consultas.
- **Verificação:** teste de unidade garantindo ausência de mutadores públicos.

### 2.6 — Registro e consulta de autorização
- **Objetivo:** caso de uso idempotente de opt-in.
- **Arquivos:** `application/authorization/RegisterAuthorization.java`, `FindAuthorization.java`, testes.
- **Aceite:** repetição devolve o registro existente; replica o opt-in na Central via porta; `Clock` injetado.
- **Verificação:** testes com repositório e gateway em memória.

### 2.7 — Consulta de agenda com ACL
- **Objetivo:** traduzir o contrato PT da Central para o domínio EN.
- **Arquivos:** `domain/agenda/AgendaGateway.java`, `agenda/Installment.java`, `adapters/out/central/acl/CentralAcl.java`, `adapters/out/central/dto/AgendaResponse.java`.
- **Aceite:** `ABERTA` ↔ `OPEN`; nenhum DTO externo fora do adaptador; `403` sem autorização; `404` repassado.
- **Verificação:** teste do mapeamento nos dois sentidos e teste do caso de uso `QueryAgenda`.

### 2.8 — Persistência e migração V1
- **Objetivo:** criar as três tabelas, índices, constraints e trigger.
- **Arquivos:** `backend/src/main/resources/db/migration/V1__create_schema.sql`, entidades JPA e repositórios de `authorization` e `portability`, `adapters/out/persistence/`.
- **Aceite:** DDL do `ARCHITECTURE.md` §6; `status` restrito por `CHECK`; `installments` JSONB com array não vazio; trigger rejeita `UPDATE` e `DELETE`.
- **Verificação:** `@SpringBootTest` com Testcontainers aplica V1 e valida as constraints.

### 2.9 — Início da operação com idempotência
- **Objetivo:** criar a operação, congelar o snapshot e responder `202`.
- **Arquivos:** `application/portability/StartOperation.java`, `adapters/in/web/PortabilityOperationResource.java`, `adapters/in/web/dto/`.
- **Aceite:** exige `Idempotency-Key`; replay devolve `202` com `Idempotent-Replay: true`; payload divergente → `409 IDEMPOTENCY_CONFLICT`; parcela inelegível → `422`.
- **Verificação:** teste de integração com duas chamadas concorrentes e uma divergente.

### 2.10 — Assinatura HMAC e envio à Central
- **Objetivo:** submeter a operação assinada e absorver o protocolo.
- **Arquivos:** `adapters/out/central/HmacSigner.java`, `CentralHttpClient.java`, `adapters/out/central/acl/PortabilityAcl.java`.
- **Aceite:** vetor de `contracts/hmac-vector.json` reproduzido; `CREATED` → `PENDING` com `protocol`; falha após retries → `INTEGRATION_ERROR`.
- **Verificação:** teste com `MockRestServiceServer` e teste do vetor HMAC.

### 2.11 — Polling resiliente
- **Objetivo:** acompanhar a Central até o estado terminal.
- **Arquivos:** `application/portability/PollOperation.java`, `config/ResilienceConfig.java`, `config/SchedulingConfig.java`.
- **Aceite:** intervalo 2 s e lote 20; honra `Retry-After`; retry 3× com backoff e circuit breaker; `204` gera `POLL_ATTEMPT`; reenvio usa a mesma `Idempotency-Key`.
- **Verificação:** testes com relógio controlado cobrindo `204`, sucesso, falha e retomada.

### 2.12 — Auto-transição de integração e respostas tipadas da Central
- **Objetivo:** alinhar a máquina de estados, a trilha e o fluxo do worker, e distinguir as respostas da Central que hoje caem no mesmo balde de falha.
- **Arquivos:** `domain/portability/PortabilityOperation.java`, `domain/portability/RejectionReason.java`, `domain/portability/CentralStatus.java`, `adapters/out/central/acl/PortabilityAcl.java`, `adapters/out/central/CentralHttpClient.java`, `application/portability/PollOperation.java`.
- **Aceite:** auto-transição `INTEGRATION_ERROR` → `INTEGRATION_ERROR` declarada na §7, com caminho único de falha no domínio e no worker; `404` da Central vira resposta tipada, fora do circuito, e mantém a operação retomável com o corpo bruto na trilha; rejeição com motivo fora do contrato fecha a operação como `REJECTED` com o código `UNMAPPED_REJECTION`; contrato público inalterado.
- **Verificação:** teste parametrizado da máquina de estados com a auto-transição; teste do cliente com `404` e motivo desconhecido; teste do worker com o relógio controlado; E2E com a Central real.

### 2.13 — Confirmação e atualização de conta
- **Objetivo:** fechar o ciclo em uma única transação.
- **Arquivos:** `application/portability/CompleteOperation.java`, `application/portability/TransitionRecorder.java`, `application/portability/PollOperation.java`, `PortabilityOperation.java`, testes de integração.
- **Aceite:** `CONFIRMADA` → `CONFIRMED` → `COMPLETED` com `account_updated_at`; `REJEITADA` → `REJECTED` com motivo; dois eventos gravados na mesma transação.
- **Verificação:** teste que simula falha entre as etapas e confirma rollback completo.

### 2.14 — Consulta de operações e trilha
- **Objetivo:** expor leitura paginada e timeline.
- **Arquivos:** `application/portability/FindOperation.java`, `ListOperations.java`, `application/audit/ListOperationEvents.java`, `domain/portability/OperationSummary.java`, `OperationDetail.java`, `OperationFilter.java`, `domain/shared/Page.java`, `adapters/out/persistence/JpaPortabilityOperationRepository.java`, `adapters/in/web/PortabilityOperationResource.java`, `adapters/in/web/dto/OperationSummaryResponse.java`, `OperationDetailResponse.java`, `OperationPageResponse.java`, `InstallmentSnapshotResponse.java`, `ReceivingAccountResponse.java`, `adapters/in/web/ApiExceptionHandler.java`.
- **Aceite:** filtro por status, página e tamanho; eventos em ordem cronológica; `404 OPERATION_NOT_FOUND`.
- **Verificação:** testes de integração com 25 operações e paginação conferida.
- **Realizado:** a consulta paginada e o detalhe saem pela porta com o modelo de leitura
  (`OperationSummary`, `OperationDetail`), porque o instante da última escrita da linha
  pertence ao armazenamento e não ao agregado; `GET /portability-operations` aceita
  `status`, `page` e `size` (1 a 100) e `GET /portability-operations/{id}` devolve o
  detalhe; a operação inexistente responde `404 OPERATION_NOT_FOUND`; a trilha continua
  sendo lida em ordem cronológica por `ListOperationEvents` (a rota REST da trilha
  entra na task 2.15, com `OperationEventResource`).

### 2.15 — Recursos REST e tratamento de erros
- **Objetivo:** completar a superfície `/api/v1`.
- **Arquivos:** `adapters/in/web/AuthorizationResource.java`, `AgendaResource.java`, `OperationEventResource.java`, `ApiExceptionHandler.java`.
- **Aceite:** rotas do `ARCHITECTURE.md` §8; erro com `code`, `message`, `locale`, `timestamp`, `path`, `details`; códigos EN estáveis.
- **Verificação:** `@WebMvcTest` por recurso e teste do handler para cada código.
- **Realizado:** a superfície pública ficou completa — `POST /api/v1/authorizations` (`201`
  no opt-in novo, `200` no opt-in repetido), `GET /api/v1/authorizations/{beneficiaryId}`
  (`200`/`404 AUTHORIZATION_NOT_FOUND`), `GET /api/v1/beneficiaries/{beneficiaryId}/agenda`
  (`200`/`403 AUTHORIZATION_REQUIRED`/`404 BENEFICIARY_NOT_FOUND`) e
  `GET /api/v1/portability-operations/{operationId}/events`
  (`200`/`404 OPERATION_NOT_FOUND`). O opt-in novo precisou do adaptador de produção da
  porta `CentralAuthorizationPort`: `CentralHttpClient` responde agora a quarta porta do
  domínio e replica o consentimento em `POST /autorizacoes`, assinado em HMAC como as
  outras chamadas. O `ApiExceptionHandler` traduziu `AUTHORIZATION_NOT_FOUND` (antes
  respondia `500`) e passou a ter um tratador genérico que devolve `500 INTERNAL_ERROR`
  no formato do contrato, com a causa real apenas no log do servidor. A consulta do opt-in
  responde o registro mais recente do beneficiário (`createdAt`, depois `id`), porque o
  contrato público devolve uma autorização e o backend pode ter uma por convênio.
  Arquivos reais: `adapters/in/web/AuthorizationResource.java`, `AgendaResource.java`,
  `OperationEventResource.java`, `adapters/in/web/dto/AuthorizationRequest.java`,
  `AuthorizationResponse.java`, `AgendaResponse.java`, `OperationEventsResponse.java`,
  `adapters/in/web/ApiExceptionHandler.java`, `adapters/out/central/CentralHttpClient.java`,
  `adapters/out/central/dto/CentralAuthorizationRequest.java`,
  `CentralAuthorizationResponse.java`, `application/authorization/RegisterAuthorization.java`,
  `FindAuthorization.java`, `domain/authorization/AuthorizationRepository.java`,
  `domain/shared/DomainError.java`, `adapters/out/persistence/JpaAuthorizationRepository.java`,
  `config/CompositionRoot.java` e os testes `AuthorizationResourceTest.java`,
  `AgendaResourceTest.java`, `OperationEventResourceTest.java`, `ApiExceptionHandlerTest.java`,
  `RegisterAuthorizationTest.java`, `CentralHttpClientTest.java`.

### 2.16 — i18n, Swagger e snapshot OpenAPI
- **Objetivo:** documentação viva e mensagens localizadas.
- **Arquivos:** `backend/src/main/resources/i18n/messages_pt_BR.properties`, `messages_en.properties`, `messages_es.properties`, `config/MessageSourceConfig.java`, `config/OpenApiConfig.java`.
- **Aceite:** `Accept-Language` com fallback `pt-BR`; `code` nunca traduzido; `/v3/api-docs` gera contrato equivalente a `contracts/consigport-api.yaml`.
- **Verificação:** teste de negociação para os três locales e comparação do contrato no CI.
- **Realizado:** as mensagens humanas saem de três catálogos (`messages_pt_BR`, `messages_en`
  e `messages_es`), com a chave do catálogo sendo o próprio código estável do erro: o
  código nunca é traduzido, só a mensagem. O `MessageSourceConfig` publica o
  `MessageSource` (basename `i18n/messages`, UTF-8, sem fallback para o locale da JVM),
  o `AcceptHeaderLocaleResolver` (padrão `pt-BR`, idiomas `pt-BR`/`en`/`es`) e o
  `LocalValidatorFactoryBean` ligado aos mesmos catálogos — assim o detalhe de uma
  falha de validação também vem no idioma do chamador, e não no locale da JVM. O
  `OpenApiConfig` publica a informação da API (título, versão `1.0.0`, contato, licença
  MIT, servidor local, quatro tags) e um `OpenApiCustomizer` que injeta o header
  `Accept-Language` em todas as operações. Os quatro recursos receberam `@Tag`,
  `@Operation` (com o `operationId` do contrato público), `@ApiResponse` por status e o
  header `Idempotent-Replay` no `202`; o `Idempotency-Key` é publicado como obrigatório.
  Os DTOs receberam `@Schema(name=..., requiredProperties=...)` e os dois DTOs da conta de
  recebimento foram colapsados em um só (`ReceivingAccount`), como manda o contrato, que
  publica um único schema. O `GeneratedContractTest` compara o `/v3/api-docs` do
  backend real com o snapshot `contracts/consigport-api.yaml` (rotas, métodos,
  `operationId`, parâmetros, status, corpo obrigatório e shape dos schemas, resolvendo
  `$ref`/`allOf`/`oneOf`); a prova de vida quebrou o `404` de uma rota e o teste ficou
  vermelho com a diferença apontada. Arquivos reais:
  `backend/src/main/resources/i18n/messages_pt_BR.properties`, `messages_en.properties`,
  `messages_es.properties`, `backend/src/main/java/br/com/consigport/config/MessageSourceConfig.java`,
  `OpenApiConfig.java`, `backend/src/main/java/br/com/consigport/adapters/in/web/ApiExceptionHandler.java`,
  `AuthorizationResource.java`, `AgendaResource.java`, `OperationEventResource.java`,
  `PortabilityOperationResource.java`, `backend/src/main/java/br/com/consigport/adapters/in/web/dto/AuthorizationResponse.java`,
  `AgendaResponse.java`, `ErrorResponse.java`, `ErrorDetail.java`, `InstallmentSnapshotResponse.java`,
  `OperationAcceptedResponse.java`, `OperationDetailResponse.java`, `OperationEventsResponse.java`,
  `OperationPageResponse.java`, `OperationSummaryResponse.java`, `PortabilityOperationRequest.java`,
  `ReceivingAccount.java`, `backend/pom.xml` (dependência `jackson-dataformat-yaml`, que o
  springdoc usa para ler o YAML do contrato) e os testes
  `backend/src/test/java/br/com/consigport/adapters/in/web/LocaleNegotiationTest.java`,
  `ApiExceptionHandlerTest.java`, `AuthorizationResourceTest.java`, `AgendaResourceTest.java`,
  `OperationEventResourceTest.java`, `backend/src/test/java/br/com/consigport/contracts/GeneratedContractTest.java`
  e `ContractComparator.java`.

### 2.17 — Gate de cobertura do núcleo
- **Objetivo:** ativar o portão de cobertura do backend sobre o núcleo e fechar as lacunas de branch.
- **Arquivos:** `pom.xml` (propriedades e regras do JaCoCo), `backend/pom.xml` (ativação do plugin), testes dos invariantes do domínio e da aplicação, `.github/workflows/ci.yml` (publicação do relatório).
- **Aceite:** `./mvnw -B -pl backend -am verify` falha quando o domínio ou a aplicação ficam abaixo de **80% de linhas e de branches**; relatório de cobertura anexado como artefato do CI.
- **Verificação:** build verde com o relatório publicado; a redução proposital do limiar deixa o build vermelho com a regra apontada.
- **Realizado:** o JaCoCo, que já estava declarado no `pom.xml` raiz mas nunca ativado em
  nenhum módulo, passou a correr no backend: o plugin entra em `backend/pom.xml` e a
  configuração do `pluginManagement` da raiz restringe a medição ao núcleo
  (`br/com/consigport/domain/**` e `br/com/consigport/application/**`), como manda o
  portão 3.2 do `docs/quality/GATES.md`. O portão cobra **80% de linhas e de branches**,
  em dois limites distintos (`jacoco.line.coverage` e `jacoco.branch.coverage`). A
  cobertura medida antes das correções era de 74,64% de branches: as lacunas estavam nos
  invariantes dos read models (`OperationDetail`, `OperationSummary`, `OperationRequest`),
  da agenda (`Installment`, `BeneficiaryAgenda`), dos status da Central
  (`CentralStatus.Pending/Rejected/NotFound`), do filtro, do backoff e do construtor do
  agregado. Os testes novos exercitam cada invariante recusado pelo domínio, e a
  cobertura subiu para **99,71% de branches e 98,12% de linhas**. A prova de vida elevou
  o limiar de branches para 1,00 e o build ficou vermelho com a regra apontada. O
  relatório de cobertura é publicado pelo CI como o artefato `backend-coverage`.
  Arquivos reais: `pom.xml` (propriedades `jacoco.branch.coverage`, `includes` do
  núcleo e limite de branches), `backend/pom.xml` (ativação do plugin),
  `.github/workflows/ci.yml` (artefato `backend-coverage`),
  `backend/src/main/java/br/com/consigport/domain/portability/OperationRequest.java`
  (correção da ordem da recusa da parcela nula),
  `backend/src/test/java/br/com/consigport/domain/portability/OperationReadModelsTest.java`,
  `OperationRequestTest.java`, `CentralStatusTest.java`, `PortabilityOperationTest.java`,
  `backend/src/test/java/br/com/consigport/domain/agenda/AgendaReadModelsTest.java`,
  `backend/src/test/java/br/com/consigport/domain/shared/SharedValueObjectsTest.java`,
  `backend/src/test/java/br/com/consigport/domain/audit/OperationEventTest.java`,
  `backend/src/test/java/br/com/consigport/application/portability/IntegrationBackoffTest.java`.

### 2.18 — Fluxo completo com o banco real
- **Objetivo:** provar o ciclo de vida inteiro de uma operação contra o PostgreSQL real.
- **Arquivos:** testes de integração com Testcontainers em `backend/src/test/java/br/com/consigport/application/portability/`, fixtures do ciclo de vida.
- **Aceite:** `CREATED` → `PENDING` → `CONFIRMED` → `COMPLETED` com a conta atualizada e a trilha completa; `CREATED` → `PENDING` → `REJECTED` com o motivo e a trilha; rollback quando o registro do evento falha.
- **Verificação:** teste de integração verde com o PostgreSQL 16 do Testcontainers e E2E real com o mock da Central.
- **Realizado:** o teste de integração `OperationLifecyclePersistenceTest` dirige os
  casos de uso — `StartOperation`, `PollOperation` e `CompleteOperation` — contra o
  PostgreSQL 16 do Testcontainers, com os adaptadores JPA e as transações reais: a
  Central é a única peça simulada, porque a regra dos testes proíbe chamar uma API de
  terceiro num teste de integração. O cenário prova `CREATED` → `PENDING` → (204 da
  Central) → `CONFIRMED` → `COMPLETED` com a conta atualizada e a trilha de seis eventos
  na ordem, `CREATED` → `PENDING` → `REJECTED` com o motivo
  `MARGIN_INSUFFICIENT` e o corpo bruto da Central, e o **rollback**: quando a trilha
  falha entre os passos da confirmação, a transação desfaz o estado da operação e o
  evento já escrito, e a operação continua `PENDING`. O duplo que falha no enésimo
  append saiu da classe de teste e virou `FailingOperationEventRepository`, no pacote
  `support`, compartilhado com o `CompletionPersistenceTest`. As duas provas de vida
  fecham a confiança no teste: sem a transação do `SpringTransactionRunner` o rollback
  deixa a operação `COMPLETED` (o teste fica vermelho), e sem o evento `ACCOUNT_UPDATED`
  a trilha fica com cinco eventos em vez de seis (o teste fica vermelho). O E2E real
  repetiu os dois caminhos com o backend, o PostgreSQL e o mock da Central de verdade:
  a operação confirmada levou nove eventos e `account_updated_at` preenchido, e a
  rejeitada levou seis eventos e `rejection_reason = MARGIN_INSUFFICIENT`.
  Arquivos reais:
  `backend/src/test/java/br/com/consigport/application/portability/OperationLifecyclePersistenceTest.java`,
  `backend/src/test/java/br/com/consigport/support/FailingOperationEventRepository.java`,
  `backend/src/test/java/br/com/consigport/adapters/out/persistence/CompletionPersistenceTest.java`.

### Dívidas da Fase 2 (polling e resiliência)
- **Portão de formatação sem implementação:** o portão 3.2 do `docs/quality/GATES.md` exige `./mvnw -q spotless:check` (Google Java Format), mas nenhum `pom.xml` declara o plugin Spotless: o comando falha com "no such goal". Ativar o Spotless exige formatar o código existente, o que produz um diff largo que deve ficar em task própria, com o `spotless:apply` antes do `spotless:check` no CI.
- **Teto de tentativas para `404` da Central:** hoje a operação que a Central não conhece continua em polling a cada 60 s, para sempre (`retry_count` cresce sem limite). Definir um orçamento de tentativas e um destino (alerta operacional ou estado que exija intervenção humana) sem tocar no contrato público.
- **Alinhar "Falhas até `INTEGRATION_ERROR`":** o `ARCHITECTURE.md` §11 e o ADR-0003 dizem `5`, mas a implementação entra em `INTEGRATION_ERROR` assim que o cliente HTTP esgota as 3 tentativas. Decidir se o limiar de 5 é implementado no worker ou se a regra é corrigida.
- **Protocolo repetido pela Central:** o esquema da §6 garante `protocol` único e o mock reinicia a sequência de protocolos a cada `POST /_admin/reset` ou a cada reinício do processo. Quando a Central reemite um protocolo já guardado, a violação da restrição chega como `DataIntegrityViolationException` e a API responde `500` em vez de um erro mapeado. Traduzir a colisão para um erro de domínio (falha de integração, operação retomável) no adaptador de persistência. Evidência: E2E da task 2.12, primeira rodada (`ERROR: duplicate key value violates unique constraint "uk_portability_operations_protocol"`).
- **Central indisponível no opt-in:** `POST /api/v1/authorizations` responde `503 CENTRAL_UNAVAILABLE` quando a Central não registra o consentimento, mas o contrato público lista apenas `400` e `500` para essa rota. Alinhar o contrato (acrescentar `503`) ou o mapeamento (`500 INTERNAL_ERROR`), sem alterar o código estável `CENTRAL_UNAVAILABLE` sem autorização do responsável.

---

## Fase 3 — Frontend RSC (10 tasks)

### 3.1 — Cliente OpenAPI tipado
- **Objetivo:** consumir a API sem duplicar tipos.
- **Arquivos:** `frontend/openapi-ts.config.ts`, `frontend/src/shared/api/client.ts`, `frontend/src/shared/api/errors.ts`.
- **Aceite:** tipos gerados de `contracts/consigport-api.yaml`; `openapi-fetch` com base URL por ambiente; erro mapeado por `code`.
- **Verificação:** `npm run --prefix frontend typecheck` e teste de unidade do parser de erro.
- **Realizado:** o frontend agora tem um cliente tipado do contrato público, gerado e
  versionado. O `openapi-ts.config.ts` é o **gerador** — o `openapi-typescript` v7 não
  lê arquivo de config, então o módulo carrega o caminho da entrada e da saída e chama
  a API programática (`openapiTS` + `astToString`); `npm run --prefix frontend
  generate:types` regenera `src/shared/api/schema.ts` byte a byte a partir de
  `contracts/consigport-api.yaml`. O `client.ts` monta o `openapi-fetch` sobre esses
  tipos, resolve a base URL por ambiente (`VITE_API_BASE_URL`, com o fallback local
  `http://localhost:8080` e sem barra final) e expõe `parseErrorResponse`, que
  transforma uma resposta de erro em `ApiError`. O `errors.ts` é o mapeador por
  `code`: os dez códigos estáveis do contrato viram uma união `ApiErrorCode`, o corpo
  da rede é tratado como `unknown` e um corpo quebrado degrada para `INTERNAL_ERROR`
  em vez de quebrar a tela; a `message` localizada do servidor é preservada e os
  `details` por campo são filtrados. A UI passa a ramificar por `code`, nunca pelo
  status HTTP. Arquivos reais:
  `frontend/openapi-ts.config.ts`,
  `frontend/src/shared/api/client.ts`,
  `frontend/src/shared/api/errors.ts`,
  `frontend/src/shared/api/schema.ts` (gerado),
  `frontend/src/shared/api/client.test.ts`,
  `frontend/src/shared/api/errors.test.ts`,
  `frontend/package.json`,
  `frontend/package-lock.json`,
  `frontend/tsconfig.json`.

### 3.2 — Catálogos i18n
- **Objetivo:** UI em pt-BR com en e es.
- **Arquivos:** `frontend/src/shared/i18n/pt-BR.json`, `en.json`, `es.json`, `provider.tsx`.
- **Aceite:** chaves completas nos três catálogos; fallback pt-BR; troca de idioma persiste; `Accept-Language` enviado.
- **Verificação:** teste que compara o conjunto de chaves entre catálogos.
- **Realizado:** a interface fala três idiomas sem sair do RSC. Os catálogos
  (`pt-BR.json`, `en.json`, `es.json`) têm exatamente o mesmo conjunto de chaves — um
  teste compara as árvores e falha se um idioma ganhar ou perder uma chave — e cada
  código de erro do contrato público tem a sua entrada, com a chave estável `code` e a
  `message` do servidor como fallback. O `locales.ts` resolve qualquer tag de idioma
  (`pt-BR`, `pt`, `es-AR`) para um dos três suportados, com pt-BR como padrão, e lê a
  preferência do cookie `consigport-locale`. O servidor resolve o idioma **antes** de
  renderizar: o `entry.rsc.tsx` lê o cookie e o `Root` monta a página no idioma
  escolhido, com `<html lang>` correto. O `provider.tsx` é a folha cliente que guarda a
  escolha, troca o idioma das folhas interativas sem nova ida ao servidor e persiste o
  cookie por um ano; o `language-switcher.tsx` é um `select` nativo — funciona no
  teclado, no leitor de tela e sem JavaScript — que, depois de gravar o cookie, pede ao
  servidor um novo render (o `entry.browser.tsx` escuta o `replaceState`), para que os
  Server Components também falem o idioma novo. O `Accept-Language` vai em toda
  requisição: o `client.ts` ganhou um middleware do `openapi-fetch` que injeta o
  cabeçalho, e um teste com **MSW** prova o cabeçalho e a leitura do corpo de erro.
  Arquivos reais:
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/shared/i18n/locales.ts`,
  `frontend/src/shared/i18n/i18n.ts`,
  `frontend/src/shared/i18n/provider.tsx`,
  `frontend/src/shared/i18n/language-switcher.tsx`,
  `frontend/src/shared/i18n/server.ts`,
  `frontend/src/shared/i18n/i18n.test.ts`,
  `frontend/src/shared/i18n/server.test.ts`,
  `frontend/src/shared/api/client.ts`,
  `frontend/src/shared/api/client.msw.test.ts`,
  `frontend/src/test/mocks/handlers.ts`,
  `frontend/src/test/mocks/server.ts`,
  `frontend/src/app/navigation.tsx`,
  `frontend/src/app/shell.tsx`,
  `frontend/src/app/router.tsx`,
  `frontend/src/app/router.test.tsx`,
  `frontend/src/app/pages/home.tsx`,
  `frontend/src/app/pages/not-found.tsx`,
  `frontend/src/root.tsx`,
  `frontend/src/framework/entry.rsc.tsx`,
  `frontend/package.json`,
  `frontend/package-lock.json`.

### 3.3 — Primitivas de UI
- **Objetivo:** componentes base fiéis ao design Matrix.
- **Arquivos:** `frontend/src/shared/ui/button.tsx`, `card.tsx`, `field.tsx`, `table.tsx`, `status-badge.tsx`.
- **Aceite:** tokens de `theme.css`; foco visível; contraste AA; variantes sem valores mágicos.
- **Verificação:** testes com Testing Library e checagem de `aria` nos componentes.
- **Realizado:** as primitivas que as fatias seguintes consomem. O `field.tsx` é o
  dono da acessibilidade do formulário: o `Field` liga o rótulo, a dica e o erro ao
  controle (`aria-describedby`), marca a invalidez (`aria-invalid`) e exige o
  asterisco de obrigatório apenas para os olhos — o `required` é do controle; o
  `TextField` monta o `input` por cima dele. O `table.tsx` entrega a tabela como uma
  **região de rolagem nomeada e focável** (`role="region"`, `tabIndex={0}`): no
  celular a tabela rola dentro da própria caixa em vez de empurrar a página, e quem
  navega pelo teclado consegue chegar à rolagem. O `status-badge.tsx` é um conjunto
  **fechado de estados** — neutro, em andamento, sucesso, técnico e erro — com um
  ponto decorativo (`aria-hidden`) ao lado da palavra; o que um estado *significa*
  pertence à feature que o usa, não à primitiva. O `button.tsx` ganhou o anel de
  foco visível em todas as variantes (`focus-visible:outline-primary`, sem
  deslocamento de layout), o estado `disabled` e o estado `loading`, que marca
  `aria-busy` e recusa o segundo clique — nenhuma ação sem resposta. Nenhum
  componente carrega literal de cor: as classes saem dos tokens e o teste de
  contraste lê `src/theme.css` e prova, com o cálculo do WCAG, que **todo par de
  texto que a interface usa** passa dos 4,5:1 do AA — corpo, botões preenchidos e
  badges — com o piso de 3:1 apenas para o token decorativo `text-muted`, que o
  `DESIGN.md` §2 reserva ao placeholder e à chrome desativada. Arquivos reais:
  `frontend/src/shared/ui/button.tsx`,
  `frontend/src/shared/ui/button.test.tsx`,
  `frontend/src/shared/ui/field.tsx`,
  `frontend/src/shared/ui/field.test.tsx`,
  `frontend/src/shared/ui/table.tsx`,
  `frontend/src/shared/ui/table.test.tsx`,
  `frontend/src/shared/ui/status-badge.tsx`,
  `frontend/src/shared/ui/status-badge.test.tsx`,
  `frontend/src/shared/ui/contrast.test.ts`.

### 3.4 — Fluxo de autorização
- **Objetivo:** registrar o opt-in pela interface.
- **Arquivos:** `frontend/src/features/authorization/server/page.tsx`, `ui/authorization-form.tsx`, `actions.ts`.
- **Aceite:** Server Component renderiza; ilha cliente envia `POST /authorizations`; erro localizado; sucesso leva à agenda.
- **Verificação:** teste de componente com `msw` e smoke no build de produção.
- **Realizado:** o opt-in do beneficiário, do começo ao fim. A página
  (`features/authorization/server/page.tsx`) é um **Server Component**: ela desenha o
  cabeçalho da jornada e entrega o formulário para a folha cliente — não busca
  nada e não guarda estado. A **Server Action** (`features/authorization/actions.ts`)
  é a única porta da mutação: o navegador nunca fala com a API. A ação valida com
  **Zod** (o par beneficiário/convênio), chama `POST /api/v1/authorizations` com o
  `Accept-Language` da interface e devolve um estado serializável — nunca uma
  exceção. A folha (`features/authorization/ui/authorization-form.tsx`) usa
  `useActionState`, o que mantém o **formulário funcionando sem JavaScript**: o
  `action` do formulário é a própria ação, e o servidor re-renderiza a página com o
  resultado. O erro é **localizado** pelo `code` estável — a interface procura a chave
  no catálogo (`errors.<CODE>`) e usa a `message` do servidor como fallback — e o
  sucesso leva à **agenda** do beneficiário (`/agenda?beneficiaryId=…`). A API
  fora do ar não derruba a página: a ação devolve `INTERNAL_ERROR` e a tela explica
  em português. Arquivos reais:
  `frontend/src/features/authorization/actions.ts`,
  `frontend/src/features/authorization/actions.test.ts`,
  `frontend/src/features/authorization/ui/authorization-form.tsx`,
  `frontend/src/features/authorization/ui/authorization-form.test.tsx`,
  `frontend/src/features/authorization/server/page.tsx`,
  `frontend/src/app/router.tsx`,
  `frontend/src/app/navigation.tsx`,
  `frontend/src/shared/ui/field.tsx`,
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/test/mocks/handlers.ts`,
  `frontend/package.json`,
  `frontend/package-lock.json`.

### 3.5 — Agenda e seleção de parcelas
- **Objetivo:** listar parcelas elegíveis e permitir seleção.
- **Arquivos:** `frontend/src/features/agenda/server/page.tsx`, `ui/installment-table.tsx`, `ui/selection-summary.tsx`.
- **Aceite:** apenas `OPEN` selecionável; total recalculado; estado vazio e `403` tratados.
- **Verificação:** testes de componente para seleção, total e estado vazio.
- **Realizado:** a agenda do beneficiário, lida no servidor e escolhida no
  navegador. A página (`features/agenda/server/page.tsx`) é um **Server Component
  assíncrono**: durante o render ela pergunta
  `GET /api/v1/beneficiaries/{beneficiaryId}/agenda` com o `Accept-Language` da
  interface, e decide o estado pelo **`code` do erro**, não pelo status HTTP. Os
  estados são honestos e todos nomeados: **vazio** ("Nenhuma parcela elegível na
  agenda."), **sem consentimento** (o `403` vira `AUTHORIZATION_REQUIRED`, com o
  atalho para autorizar), **beneficiário desconhecido** (o `404` vira
  `BENEFICIARY_NOT_FOUND`) e **falha da consulta** (com o link de "Tentar de
  novo"). A mensagem é sempre a do catálogo da interface, indexada pelo código
  estável, com a `message` do servidor como fallback — a regra do ADR-0004.
  A **folha cliente** (`features/agenda/ui/installment-table.tsx`) é a dona da
  seleção: só uma parcela **`OPEN`** pode ser escolhida — a fechada está
  desabilitada, porque oferecer uma parcela que a API recusa seria oferecer uma
  operação impossível —, o total é **recalculado a cada mudança** a partir da
  seleção (em centavos, para o ponto flutuante não mentir), e a seleção viaja
  para a portabilidade pela URL (`/portabilidade?beneficiaryId=…&installments=…`).
  O resumo (`features/agenda/ui/selection-summary.tsx`) anuncia o total com
  `aria-live="polite"`: quem navega pelo teclado ouve o número se mover. O
  **`shared/format/`** — a pasta que o `ARCHITECTURE.md` §5 reserva para
  "moeda, data, status" — nasceu aqui: `money.ts` (reais no formato do leitor e
  soma sem deriva), `date.ts` (a data do calendário no dia que a Central
  queria, montada pelas partes, nunca por `new Date('2026-12-05')`, que seria
  meia-noite UTC) e `status.ts` (o tom da Plataforma: elegível é sucesso,
  concluída é sucesso, recusada é erro). O cabeçalho das páginas virou a
  primitiva `shared/ui/page-header.tsx`, usada pelas três páginas que já existem.
  Arquivos reais:
  `frontend/src/features/agenda/server/page.tsx`,
  `frontend/src/features/agenda/server/page.test.tsx`,
  `frontend/src/features/agenda/ui/installment-table.tsx`,
  `frontend/src/features/agenda/ui/installment-table.test.tsx`,
  `frontend/src/features/agenda/ui/selection-summary.tsx`,
  `frontend/src/shared/format/money.ts`,
  `frontend/src/shared/format/money.test.ts`,
  `frontend/src/shared/format/date.ts`,
  `frontend/src/shared/format/date.test.ts`,
  `frontend/src/shared/format/status.ts`,
  `frontend/src/shared/ui/page-header.tsx`,
  `frontend/src/shared/i18n/server.ts`,
  `frontend/src/app/router.tsx`,
  `frontend/src/app/router.test.tsx`,
  `frontend/src/app/pages/section.tsx`,
  `frontend/src/root.tsx`,
  `frontend/src/features/authorization/server/page.tsx`,
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/test/mocks/handlers.ts`.

### 3.6 — Conta de recebimento e envio
- **Objetivo:** validar a conta e criar a operação.
- **Arquivos:** `frontend/src/features/portability/ui/receiving-account-form.tsx`, `ui/submit-operation.tsx`, `actions.ts`.
- **Aceite:** validação com Zod; `Idempotency-Key` gerada uma vez por tentativa; `409` e `422` exibidos com mensagem do servidor.
- **Verificação:** teste de formulário cobrindo válido, inválido e conflito.
- **Realizado:** a criação da operação de portabilidade, do formulário ao protocolo.
  A **Server Action** (`features/portability/actions.ts`) é a única porta da
  mutação: ela valida a conta de recebimento com **Zod** — as mesmas regras do
  contrato (banco até 64, agência até 32, conta até 64, credor até 255, todas
  não vazias) — envia a **`Idempotency-Key`** e devolve um estado serializável.
  A **`Idempotency-Key` nomeia a tentativa, não o render**: a folha cliente
  (`features/portability/ui/receiving-account-form.tsx`) calcula a impressão
  digital do conteúdo (beneficiário, convênio, parcelas e conta) e guarda a chave
  num `ref`; o mesmo conteúdo **mantém a chave** — uma repetição de um envio
  falho não pode criar uma segunda operação — e outro conteúdo **recebe uma
  chave nova**, porque a API recusa uma chave que chega com outro corpo. Sem
  JavaScript não há chave para enviar, e a ação cria uma: a operação nasce
  mesmo assim, uma vez só. O **resultado** (`features/portability/ui/submit-operation.tsx`)
  fala com duas vozes: o catálogo da interface, indexado pelo `code` estável do
  contrato, e a **mensagem do servidor** — o motivo que a API deu de fato ("a
  parcela PAR-003 não está aberta") — que aparece como detalhe quando diz algo
  que a primeira voz não diz. O `409` e o `422` aparecem assim, com a mensagem do
  servidor visível. A página (`features/portability/server/page.tsx`) é um
  **Server Component**: mostra as parcelas que vieram da agenda e entrega o
  formulário à folha cliente; sem seleção, ela aponta de volta para a agenda.
  A seleção viaja pela URL — `/portabilidade?beneficiaryId=…&agreementId=…&installments=…`
  — e o **convênio** passou a fazer parte dela, porque o pedido do contrato o
  exige. Arquivos reais:
  `frontend/src/features/portability/actions.ts`,
  `frontend/src/features/portability/actions.test.ts`,
  `frontend/src/features/portability/idempotency.ts`,
  `frontend/src/features/portability/idempotency.test.ts`,
  `frontend/src/features/portability/ui/receiving-account-form.tsx`,
  `frontend/src/features/portability/ui/receiving-account-form.test.tsx`,
  `frontend/src/features/portability/ui/submit-operation.tsx`,
  `frontend/src/features/portability/server/page.tsx`,
  `frontend/src/features/agenda/ui/installment-table.tsx`,
  `frontend/src/features/agenda/ui/installment-table.test.tsx`,
  `frontend/src/app/router.tsx`,
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/test/mocks/handlers.ts`.

### 3.7 — Painel de status com polling
- **Objetivo:** acompanhar a operação até o terminal.
- **Arquivos:** `frontend/src/features/operations/server/page.tsx`, `ui/operation-status.tsx`, `ui/use-operation-polling.ts`.
- **Aceite:** TanStack Query com intervalo de 2 s; para em estado terminal; `nextPollAt` respeitado; sem ISR.
- **Verificação:** teste com timers falsos cobrindo `PENDING`, `COMPLETED` e `REJECTED`.
- **Realizado:** o painel que acompanha a operação até o estado terminal.
  A **ilha cliente** (`features/operations/ui/operation-status.tsx`) é a única
  dona do polling; o resto da tela é **Server Component**: o primeiro estado
  da operação é lido **durante o render**, com o `Accept-Language` da cookie, e
  a ilha assume dali em diante — **sem ISR** em lugar nenhum. O **hook**
  (`features/operations/ui/use-operation-polling.ts`) é o único lugar onde o
  **ritmo** mora: `TanStack Query` pergunta a cada **2 s** pelo
  `GET /api/v1/portability-operations/{operationId}`, **para no estado
  terminal** (`COMPLETED` e `REJECTED` — `INTEGRATION_ERROR` é retomável e
  continua sendo perguntado) e **respeita o `nextPollAt`** quando o servidor o
  declara: a espera nunca é menor que a base de 2 s nem menor que o instante
  pedido. O `nextPollAt` pertence ao domínio, mas o contrato público ainda
  não o publica — a leitura é defensiva e o contrato não foi tocado; o mesmo
  vale para o `OPERATION_NOT_FOUND`, que **encerra** o polling em vez de
  repetir uma pergunta sem resposta. O **provedor do cache**
  (`shared/api/query-provider.tsx`) cria o `QueryClient` uma vez por sessão, e
  o **Shell** o embrulha: o navegador guarda o estado do servidor só ali. O
  **`formatDateTime`** (`shared/format/date.ts`) entrou para a hora da última
  atualização — um instante é um momento, não um dia de calendário, então sai
  com a hora ao lado da data. A **operação criada** na 3.6 agora oferece o
  caminho para o painel (`/operacoes?operationId=…`), e a rota `/operacoes`
  entrou no roteador e na navegação. Arquivos reais:
  `frontend/src/shared/api/query-provider.tsx`,
  `frontend/src/features/operations/ui/use-operation-polling.ts`,
  `frontend/src/features/operations/ui/use-operation-polling.test.ts`,
  `frontend/src/features/operations/ui/operation-status.tsx`,
  `frontend/src/features/operations/ui/operation-status.test.tsx`,
  `frontend/src/features/operations/server/page.tsx`,
  `frontend/src/features/operations/server/page.test.tsx`,
  `frontend/src/shared/format/date.ts`,
  `frontend/src/shared/format/date.test.ts`,
  `frontend/src/features/portability/ui/submit-operation.tsx`,
  `frontend/src/app/router.tsx`,
  `frontend/src/app/router.test.tsx`,
  `frontend/src/app/navigation.tsx`,
  `frontend/src/app/shell.tsx`,
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/test/mocks/handlers.ts`.

### 3.8 — Timeline de auditoria
- **Objetivo:** exibir a trilha imutável de eventos.
- **Arquivos:** `frontend/src/features/audit/server/page.tsx`, `ui/event-timeline.tsx`, `ui/event-item.tsx`.
- **Aceite:** ordem cronológica; tipo, status anterior e novo, detalhe e horário; sem ação de edição ou exclusão.
- **Verificação:** teste de componente com eventos mistos e formatação de data.
- **Realizado:** a trilha de auditoria da operação, lida no servidor e mostrada
  como uma lista de fatos. O **Server Component**
  (`features/audit/server/page.tsx`) pergunta o
  `GET /api/v1/portability-operations/{operationId}/events` **durante o render**,
  com o `Accept-Language` da cookie; o navegador nunca busca a trilha. A
  **timeline** (`features/audit/ui/event-timeline.tsx`) ordena os eventos pelo
  instante e, no empate, pelo identificador sequencial — a ordem da tela nunca
  depende da ordem do array — e entrega cada evento ao **item**
  (`features/audit/ui/event-item.tsx`), que mostra o **tipo**, o **status
  anterior**, o **novo status**, o **detalhe** e a **hora**. O evento traz
  também a resposta crua da Central quando existe, num `<details>` nativo:
  nenhuma biblioteca, nenhum JavaScript, e o pasto continua legível. A trilha
  é **append-only** e a tela não oferece botão nenhum — nem editar, nem
  excluir: o teste afirma que não existe um único `button`, `textbox` ou
  `link` na trilha. O **vocabulário do status** subiu para
  `shared/format/status.ts`: o tom e a chave de catálogo de cada estado
  passaram a morar juntos, e o painel da 3.7 usa a mesma tabela — assim a
  operação e a sua trilha falam do mesmo estado com a mesma palavra. O
  **placeholder** `SectionPage` saiu do código: era a página de espera de uma
  feature que agora existe, e a rota `/auditoria` lê a operação da URL.
  Arquivos reais:
  `frontend/src/features/audit/server/page.tsx`,
  `frontend/src/features/audit/server/page.test.tsx`,
  `frontend/src/features/audit/ui/event-timeline.tsx`,
  `frontend/src/features/audit/ui/event-timeline.test.tsx`,
  `frontend/src/features/audit/ui/event-item.tsx`,
  `frontend/src/shared/format/status.ts`,
  `frontend/src/features/operations/ui/operation-status.tsx`,
  `frontend/src/features/operations/ui/operation-status.test.tsx`,
  `frontend/src/app/router.tsx`,
  `frontend/src/app/router.test.tsx`,
  `frontend/src/app/pages/section.tsx` (removido),
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/test/mocks/handlers.ts`.

### 3.9 — Responsividade, acessibilidade e testes finais
- **Objetivo:** fechar a experiência em mobile e teclado.
- **Arquivos:** `frontend/src/styles.css`, `frontend/src/app/shell.tsx`, testes de integração.
- **Aceite:** breakpoints do `DESIGN.md` §4b; navegação por teclado; `prefers-reduced-motion` respeitado; testes de unidade verdes.
- **Verificação:** `npm run --prefix frontend lint && npm run --prefix frontend typecheck && npm run --prefix frontend test:unit -- --run`.
- **Realizado:** a experiência fecha no **mobile** e no **teclado**, com a
  prova no código. O **skip link** (`app/shell.tsx` + `styles.css`) é o
  primeiro alvo do tabulador: ele fica fora da tela (`top: -100px`) e aparece
  quando o teclado o alcança; o alvo é o `<main id="conteudo" tabindex="-1">`,
  e o link existe — o teste afirma que o `id` apontado está no documento. O
  **`prefers-reduced-motion`** foi respeitado na raiz: a regra global corta
  animação, transição e `scroll-behavior` de tudo que se move. Os
  **breakpoints** são os do `DESIGN.md` §4: o `sm` do Tailwind (640px) saiu do
  código — ele partia o layout do mobile antes de a hora — e restaram
  apenas `md` (768px) e `lg` (1024px), os dois limites que o design nomeia; o
  teste do fonte afirma que nenhum outro prefixo aparece. O **teste de
  integração** (`src/test/accessibility.test.tsx`) renderiza o shell de verdade
  e prova o que o olho não alcança: o **ordem do tabulador** (skip link →
  marca → navegação), o **nome acessível** de todo controle, o **piso de
  44px** de todo alvo de toque, o **anel de foco** visível em todo controle e
  os **landmarks** (`banner`, `main`, `contentinfo` e a navegação com nome).
  O **teste responsivo** (`src/test/responsive.test.ts`) prova, do fonte, o
  que a geometria ainda vai medir no navegador: os breakpoints, a ausência
  de conteúdo escondido, o piso de 13px do texto de corpo e a ausência de
  largura fixa que empurre a página de lado. Duas correções de semântica
  vieram junto: os **botões da home** viraram **links** (um botão que muda
  endereço mente para o teclado e para o leitor de tela) e o `<header>` da
  home deixou de ser um segundo *banner* na mesma página. E a **região viva**
  do painel da operação passou a anunciar o **estado** e não o **batimento**:
  antes o texto "Consultando a Central…" / "Aguardando a próxima consulta…"
  alternava dentro de um `aria-live` a cada dois segundos — o leitor de tela
  falava sem parar; agora o badge do estado é a região viva e o batimento
  fica visível e silencioso. Arquivos reais:
  `frontend/src/styles.css`,
  `frontend/src/app/shell.tsx`,
  `frontend/src/app/navigation.tsx`,
  `frontend/src/app/pages/home.tsx`,
  `frontend/src/shared/ui/table.tsx`,
  `frontend/src/shared/ui/button.tsx`,
  `frontend/src/shared/i18n/language-switcher.tsx`,
  `frontend/src/shared/i18n/pt-BR.json`,
  `frontend/src/shared/i18n/en.json`,
  `frontend/src/shared/i18n/es.json`,
  `frontend/src/features/agenda/ui/installment-table.tsx`,
  `frontend/src/features/operations/ui/operation-status.tsx`,
  `frontend/src/features/operations/ui/operation-status.test.tsx`,
  `frontend/src/features/portability/ui/submit-operation.tsx`,
  `frontend/src/test/responsive.test.ts`,
  `frontend/src/test/accessibility.test.tsx`.

### 3.10 — Promoção da release `v0.4.0` e arquivamento da Fase 3
- **Objetivo:** promover, publicar e arquivar a Fase 3.
- **Arquivos:** `SDD/specs/history/phases/phase-3-finished.md`, `SDD/specs/PLAN.md`, `SDD/specs/ROADMAP.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`.
- **Aceite:** `develop` promovido para `staging` e `main` por PR; tag `v0.4.0` criada pelo release-please; release no GitHub com o CHANGELOG; imagens no GHCR com `v0.4.0`; run de staging verde; fase arquivada.
- **Verificação:** `git tag --list "v*"` mostra `v0.4.0`; `gh release view v0.4.0`; run do `cd-staging` verde; `node scripts/check-specs.mjs` com exit 0.

### Dívidas da Fase 3 (frontend e entrega)
- **Base da API presa no build:** a URL da API é resolvida no build (`VITE_API_BASE_URL`,
  com o fallback `http://localhost:8080`), porque o Vite substitui `import.meta.env` na
  hora do bundle. A imagem do frontend publicada no GHCR leva, portanto, o valor do
  build — e o `compose.staging.yaml` só expõe a porta do frontend, sem a variável de
  ambiente que a tornaria configurável. O `compose.staging.yaml` já registra o problema
  ("the typed OpenAPI client arrives with task 3.1"), e a task 3.1 chegou: a dívida
  agora é real. **Consequência:** o stack de staging sobe e as três imagens ficam
  *healthy*, mas as telas que falam com a API (agenda, portabilidade, operações e
  auditoria) não alcançam o backend no ambiente implantado — o navegador do visitante
  resolveria `localhost:8080` na própria máquina. **Caminho:** tornar a URL da API
  configurável em tempo de execução (variável de ambiente do container lida pelo
  servidor, exposta ao navegador por `<meta>` ou por um proxy de `/api` no preview do
  Vite) e provar o fluxo implantado de ponta a ponta. Tarefa própria, na Fase 4, junto
  do E2E no compose (4.3) e da entrega contínua (4.6).
- **Prova geométrica da responsividade:** as quatro regras do `DESIGN.md` §4b.2 têm
  prova estrutural (breakpoints do `DESIGN.md` §4, nome acessível de todo controle,
  alvo de toque de 44px, anel de foco visível) e prova estática (nenhum conteúdo
  escondido, nenhuma largura fixa). A prova **geométrica** — `scrollWidth` real de
  390px, caixa real de cada alvo, tamanho real do texto renderizado — exige um
  navegador e chega com o Playwright da task 4.3.
- **`nextPollAt` fora do contrato publicado:** o domínio e o `ARCHITECTURE.md` §11
  definem o campo, mas o `OperationDetail` do contrato público ainda não o publica. O
  frontend o lê de forma defensiva (`readNextPollAt`), sem tocar no contrato. Alinhar o
  contrato exige autorização do responsável.
- **Fase 3 sem E2E próprio:** a fase entregou 159 testes de unidade e smoke manual
  contra uma API falsa local; nenhum teste de navegador. É a task 4.3 que fecha essa
  lacuna, com o Playwright rodando contra o build de produção e o compose.

---

## Fase 4 — Qualidade e vitrine (7 tasks)

### 4.1 — Gates de contrato — Realizado
- **Objetivo:** impedir drift e quebra de compatibilidade.
- **Arquivos:** `.spectral.yaml`, `.github/workflows/ci.yml`,
  `.github/workflows/nightly.yml`, `scripts/check-contract-drift.mjs`,
  `tests/contracts/breaking-change-base.yaml`, `tests/contracts/breaking-change.yaml`,
  `tests/contracts/springdoc-consigport.json`, `docs/quality/GATES.md`,
  `SDD/specs/PLAN.md`, `SDD/specs/ROADMAP.md`, `SDD/specs/BACKLOG.md`,
  `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`.
- **Aceite:** Spectral sem erro; `oasdiff` compara com a versão de `main` e falha em breaking change; snapshot do springdoc comparado ao contrato.
- **Verificação:** job `contracts` verde e PR proposital com breaking change vermelho.
- **Realizado:** o job `contracts` agora compara o contrato com a versão de `main`
  (antes comparava com a branch base), prova que o portão está armado com um par de
  fixtures e compara o documento publicado pelo springdoc com o contrato versionado.
  O job `smoke` compara o documento publicado pelo backend em execução com o
  contrato e com o snapshot armazenado. O `nightly` usa o mesmo script.
- **Dívidas da Fase 4 (contratos):**
  - **O `oasdiff` não serve para comparar o contrato com o documento publicado:**
    ele acusa diferenças de representação (`format: uuid` que o springdoc não
    publica, `minimum` e `maxLength` dos limites que só o contrato declara) e não
    de contrato. A comparação do documento publicado com o contrato é, portanto,
    estrutural — rotas, métodos, `operationId`, parâmetros, corpo da requisição e
    status das respostas — como no `ContractComparator` do backend, portado para
    Node em `scripts/check-contract-drift.mjs`.
  - **O snapshot do springdoc é um artefato versionado:** `tests/contracts/springdoc-consigport.json`
    é o documento publicado pelo backend. Quando o contrato muda, o snapshot tem de
    ser regerado (`node scripts/check-contract-drift.mjs --live <url> --write-snapshot
    tests/contracts/springdoc-consigport.json`); o job `smoke` publica o documento
    que ele leu como artefato `springdoc-document`, e é esse arquivo que vai para o
    commit.
  - **O contrato da Central não é comparado com o springdoc do mock:** o mock
    publica uma cópia byte a byte de `contracts/central-api.yaml` como recurso
    estático, e o documento gerado a partir das anotações do mock é uma visão
    diferente (o `x-signature`, o `x-timestamp` e os limites são do contrato, não
    do código). A garantia do mock é o teste `ContractTest`, que compara os dois
    bytes.
  - **O trabalho da fase 4 é commit `ci(...)`, e o `release-please` não publica a
    seção `ci` por padrão:** a task 4.6, que já tem no aceite o changelog em
    pt-BR, precisa cadastrar a seção no `release-please-config.json` para que os
    portões de qualidade apareçam na release `v1.0.0`.

### 4.2 — Testes de propriedade com Schemathesis — Realizado
- **Objetivo:** exercitar a API real contra o contrato.
- **Arquivos:** `.github/workflows/ci.yml`, `.github/workflows/nightly.yml`,
  `tests/contract/schemathesis.toml`, `tests/contract/fixtures/drifted-contract.yaml`,
  `scripts/check-schemathesis-gate.mjs`, `.gitignore`, `docs/quality/GATES.md`,
  `SDD/specs/PLAN.md`, `SDD/specs/ROADMAP.md`, `SDD/specs/BACKLOG.md`,
  `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`.
- **Aceite:** Schemathesis roda contra o compose; qualquer resposta fora do contrato falha o job; relatório anexado.
- **Verificação:** job `contracts` com Schemathesis verde e relatório publicado.
- **Realizado:** o job `contracts` sobe a stack real (PostgreSQL 16, Central
  simulada e backend), registra o opt-in do beneficiário e exercita as sete
  operações do contrato contra a API em execução — 94 casos, 94 aprovados. O
  relatório JUnit e JSON é publicado como artefato `schemathesis-report`. O
  portão está armado: o contrato de fixture que esquece o `404` da API reprova
  o teste, e a prova roda a cada PR (`scripts/check-schemathesis-gate.mjs`). O
  nightly usa a mesma configuração, então há uma única verdade.
- **Dívidas da Fase 4 (contrato dinâmico):**
  - **O contrato público não declara todas as respostas que a API dá.** A fase de
    fuzzing (desligada no portão) encontrou cinco divergências reais, com o
    contrato como responsável: o `400 VALIDATION_ERROR` que a API devolve quando o
    identificador do caminho não tem o formato esperado (em
    `GET /authorizations/{beneficiaryId}`,
    `GET /beneficiaries/{beneficiaryId}/agenda`,
    `GET /portability-operations/{operationId}` e
    `GET /portability-operations/{operationId}/events`); o `403
    AUTHORIZATION_REQUIRED` que a criação da operação devolve quando o opt-in do
    beneficiário não existe; e o `503 CENTRAL_UNAVAILABLE` que a consulta da
    agenda devolve quando a Central não está disponível. **Cada correção exige
    autorização para alterar o contrato público** — é a lista de perguntas
    levadas ao responsável.
  - **O contrato é mais frouxo do que a API em dois limites.** O parâmetro `page`
    não declara `maximum` e a API recusa `page` acima de `2147483647`; os
    `installmentIds` não declaram `maxLength` e a API recusa identificadores com
    mais de 64 caracteres. Nenhum dos dois quebra quem consome a API — a API é
    mais estrita —, mas o contrato precisa declarar o limite para que a fase de
    fuzzing possa ser ligada.
  - **A API devolve `500` quando a requisição traz um `Content-Type` não
    declarado.** O certo é `400` ou `415`: um pedido malformado não é erro
    interno. É um defeito do backend, não do contrato, e não depende de
    autorização — fica para uma task própria.
  - **A fase de fuzzing só pode ser ligada depois dessas correções.** Com o
    contrato como está, ela reprova por respostas legítimas da API que o
    contrato não declara. O portão de hoje usa as fases `examples` e `coverage`,
    que medem o contrato.

### 4.3 — E2E com Playwright no CI — Realizado
- **Objetivo:** provar o fluxo completo no navegador.
- **Arquivos:** `.github/workflows/ci.yml`,
  `backend/src/main/java/br/com/consigport/config/CorsConfig.java`,
  `backend/src/main/java/br/com/consigport/adapters/out/central/CentralHttpClient.java`,
  `backend/src/main/resources/application.yml`,
  `backend/src/test/java/br/com/consigport/adapters/out/central/CentralHttpClientTest.java`,
  `frontend/e2e/authorization.spec.ts`, `frontend/e2e/portability.spec.ts`,
  `frontend/e2e/rejection.spec.ts`, `frontend/e2e/support/journey.ts`,
  `frontend/playwright.config.ts`, `frontend/package.json`,
  `frontend/package-lock.json`, `frontend/tsconfig.json`,
  `frontend/src/features/authorization/ui/authorization-form.tsx`,
  `frontend/src/framework/entry.browser.tsx`, `frontend/src/framework/navigation.ts`,
  `docs/quality/GATES.md`, `SDD/specs/PLAN.md`, `SDD/specs/ROADMAP.md`,
  `SDD/specs/BACKLOG.md`, `SDD/docs/EVIDENCE.md`, `docs/ai-harness/AI_USAGE_LOG.md`.
- **Aceite:** três cenários cobrindo caminho feliz, rejeição e idempotência; execução contra build de produção e compose; artefatos de trace e vídeo.
- **Verificação:** job `e2e` verde no GitHub Actions com relatório HTML.
- **Realizado:** a suíte tem cinco testes em três arquivos e roda contra o build de
  produção servido por `vite preview` — o mesmo artefato que o contêiner publica.
  O job `e2e` do CI sobe a stack real (PostgreSQL 16, Central simulada e backend),
  espera as sondas de saúde, aquece as duas JVMs, roda a suíte e publica o
  relatório HTML, os traces, as capturas e os vídeos. Os testes leem os rótulos que
  o leitor lê e clicam nos botões que a tela oferece: nada sabe de classe de CSS.
  A suíte encontrou dois defeitos reais, corrigidos aqui: a API não respondia ao
  navegador da interface (faltava o CORS que a `ARCHITECTURE.md` §13 exige) e o
  circuit breaker contava a recusa legítima da Central como falha, abrindo o
  circuito por dez segundos e derrubando a agenda de quem tinha consentimento.
- **Dívidas da Fase 4 (E2E):**
  - **A troca de payload da navegação perdia a tela.** A agenda era buscada, a
    resposta chegava e o endereço mudava, mas o React descartava a troca do
    payload: a URL apontava para a agenda e a tela continuava no formulário.
    A causa é a troca estar dentro de uma transição (`startTransition`) reagindo
    a um efeito do React — uma transição desse tipo é descartada. A correção é
    a troca urgente do payload, com o motivo escrito no próprio código.
  - **O tempo de leitura da Central (3 s) é curto para o ambiente da suíte.** O
    limite vem da `ARCHITECTURE.md` §11 e continua valendo para a aplicação; o
    ambiente da suíte (navegador e duas JVMs no mesmo runner) dá 20 s ao backend
    por `CENTRAL_TIMEOUT_READ`. É ajuste de ambiente, não de contrato.
  - **A suíte cobre cinco cenários, não a tela de listagem.** Não existe tela de
    listagem de operações na interface — a trilha e o painel são por operação —
    e as operações pendentes e concluídas já são provadas pelo caminho feliz.

### 4.4 — Performance com k6
- **Objetivo:** medir o fluxo sob carga com limites explícitos.
- **Arquivos:** `tests/performance/smoke.js`, `tests/performance/load.js`, `.github/workflows/nightly.yml`.
- **Aceite:** smoke com 5 VUs no PR; load com 50 VUs no nightly e no release; thresholds `http_req_failed < 1%` e `p95 < 800 ms`; resumo HTML.
- **Verificação:** execução no CI com thresholds verdes e resumo publicado.

### 4.5 — Segurança e dependências
- **Objetivo:** bloquear segredo e vulnerabilidade conhecida.
- **Arquivos:** `.github/workflows/ci.yml`, `.github/dependabot.yml`, `.gitleaks.toml`.
- **Aceite:** Gitleaks no diff; `npm audit --audit-level=high`; OWASP dependency check no backend; Dependabot semanal para Maven, npm e Actions.
- **Verificação:** job `security` verde e PR de teste do Dependabot aberto.

### 4.6 — Entrega contínua e release
- **Objetivo:** promover imagens até produção com tag e changelog.
- **Arquivos:** `.github/workflows/cd-staging.yml`, `.github/workflows/release.yml`, `release-please-config.json`.
- **Aceite:** tag dispara deploy em staging; aprovação manual promove produção; imagens com digest fixo; changelog gerado em pt-BR.
- **Verificação:** release `v1.0.0` de teste com deploy em staging e rollback documentado.

### 4.7 — README e vitrine
- **Objetivo:** explicar o sistema, as decisões e os trade-offs.
- **Arquivos:** `README.md`, `docs/demo/ROTEIRO.md`, `docs/quality/GATES.md`.
- **Aceite:** visão, arquitetura, execução com `docker compose up`, variáveis, fluxo demonstrável, decisões, trade-offs e próximos passos; roteiro de demo de 5 minutos.
- **Verificação:** `node scripts/check-specs.mjs` e leitura completa por alguém que nunca viu o projeto.

---

## Ideias registradas (não são compromisso)

- [ ] Storybook dos componentes de `frontend/src/shared/ui`.
- [ ] Contrato versionado em `/api/v2` com adaptador de compatibilidade.
- [ ] Worker de polling horizontal com lock distribuído no PostgreSQL.
- [ ] Painel de métricas do mock com contadores de caos visíveis.
