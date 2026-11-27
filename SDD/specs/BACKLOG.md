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

## Fase 2 — Backend core (17 tasks)

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

### 2.17 — Fechamento do backend
- **Objetivo:** provar domínio, persistência e resiliência juntos.
- **Arquivos:** testes de integração com Testcontainers, `backend/pom.xml` (JaCoCo).
- **Aceite:** cobertura de linhas ≥ 80% no domínio e aplicação; fluxo completo `CREATED` → `COMPLETED` e `REJECTED`; ArchUnit verde.
- **Verificação:** `./mvnw -q -pl backend -am verify` com relatório JaCoCo anexado.

### Dívidas da Fase 2 (polling e resiliência)
- **Teto de tentativas para `404` da Central:** hoje a operação que a Central não conhece continua em polling a cada 60 s, para sempre (`retry_count` cresce sem limite). Definir um orçamento de tentativas e um destino (alerta operacional ou estado que exija intervenção humana) sem tocar no contrato público.
- **Alinhar "Falhas até `INTEGRATION_ERROR`":** o `ARCHITECTURE.md` §11 e o ADR-0003 dizem `5`, mas a implementação entra em `INTEGRATION_ERROR` assim que o cliente HTTP esgota as 3 tentativas. Decidir se o limiar de 5 é implementado no worker ou se a regra é corrigida.
- **Protocolo repetido pela Central:** o esquema da §6 garante `protocol` único e o mock reinicia a sequência de protocolos a cada `POST /_admin/reset` ou a cada reinício do processo. Quando a Central reemite um protocolo já guardado, a violação da restrição chega como `DataIntegrityViolationException` e a API responde `500` em vez de um erro mapeado. Traduzir a colisão para um erro de domínio (falha de integração, operação retomável) no adaptador de persistência. Evidência: E2E da task 2.12, primeira rodada (`ERROR: duplicate key value violates unique constraint "uk_portability_operations_protocol"`).
- **Central indisponível no opt-in:** `POST /api/v1/authorizations` responde `503 CENTRAL_UNAVAILABLE` quando a Central não registra o consentimento, mas o contrato público lista apenas `400` e `500` para essa rota. Alinhar o contrato (acrescentar `503`) ou o mapeamento (`500 INTERNAL_ERROR`), sem alterar o código estável `CENTRAL_UNAVAILABLE` sem autorização do responsável.

---

## Fase 3 — Frontend RSC (9 tasks)

### 3.1 — Cliente OpenAPI tipado
- **Objetivo:** consumir a API sem duplicar tipos.
- **Arquivos:** `frontend/openapi-ts.config.ts`, `frontend/src/shared/api/client.ts`, `frontend/src/shared/api/errors.ts`.
- **Aceite:** tipos gerados de `contracts/consigport-api.yaml`; `openapi-fetch` com base URL por ambiente; erro mapeado por `code`.
- **Verificação:** `npm run --prefix frontend typecheck` e teste de unidade do parser de erro.

### 3.2 — Catálogos i18n
- **Objetivo:** UI em pt-BR com en e es.
- **Arquivos:** `frontend/src/shared/i18n/pt-BR.json`, `en.json`, `es.json`, `provider.tsx`.
- **Aceite:** chaves completas nos três catálogos; fallback pt-BR; troca de idioma persiste; `Accept-Language` enviado.
- **Verificação:** teste que compara o conjunto de chaves entre catálogos.

### 3.3 — Primitivas de UI
- **Objetivo:** componentes base fiéis ao design Matrix.
- **Arquivos:** `frontend/src/shared/ui/button.tsx`, `card.tsx`, `field.tsx`, `table.tsx`, `status-badge.tsx`.
- **Aceite:** tokens de `theme.css`; foco visível; contraste AA; variantes sem valores mágicos.
- **Verificação:** testes com Testing Library e checagem de `aria` nos componentes.

### 3.4 — Fluxo de autorização
- **Objetivo:** registrar o opt-in pela interface.
- **Arquivos:** `frontend/src/features/authorization/server/page.tsx`, `ui/authorization-form.tsx`, `actions.ts`.
- **Aceite:** Server Component renderiza; ilha cliente envia `POST /authorizations`; erro localizado; sucesso leva à agenda.
- **Verificação:** teste de componente com `msw` e smoke no build de produção.

### 3.5 — Agenda e seleção de parcelas
- **Objetivo:** listar parcelas elegíveis e permitir seleção.
- **Arquivos:** `frontend/src/features/agenda/server/page.tsx`, `ui/installment-table.tsx`, `ui/selection-summary.tsx`.
- **Aceite:** apenas `OPEN` selecionável; total recalculado; estado vazio e `403` tratados.
- **Verificação:** testes de componente para seleção, total e estado vazio.

### 3.6 — Conta de recebimento e envio
- **Objetivo:** validar a conta e criar a operação.
- **Arquivos:** `frontend/src/features/portability/ui/receiving-account-form.tsx`, `ui/submit-operation.tsx`, `actions.ts`.
- **Aceite:** validação com Zod; `Idempotency-Key` gerada uma vez por tentativa; `409` e `422` exibidos com mensagem do servidor.
- **Verificação:** teste de formulário cobrindo válido, inválido e conflito.

### 3.7 — Painel de status com polling
- **Objetivo:** acompanhar a operação até o terminal.
- **Arquivos:** `frontend/src/features/operations/server/page.tsx`, `ui/operation-status.tsx`, `ui/use-operation-polling.ts`.
- **Aceite:** TanStack Query com intervalo de 2 s; para em estado terminal; `nextPollAt` respeitado; sem ISR.
- **Verificação:** teste com timers falsos cobrindo `PENDING`, `COMPLETED` e `REJECTED`.

### 3.8 — Timeline de auditoria
- **Objetivo:** exibir a trilha imutável de eventos.
- **Arquivos:** `frontend/src/features/audit/server/page.tsx`, `ui/event-timeline.tsx`, `ui/event-item.tsx`.
- **Aceite:** ordem cronológica; tipo, status anterior e novo, detalhe e horário; sem ação de edição ou exclusão.
- **Verificação:** teste de componente com eventos mistos e formatação de data.

### 3.9 — Responsividade, acessibilidade e testes finais
- **Objetivo:** fechar a experiência em mobile e teclado.
- **Arquivos:** `frontend/src/styles.css`, `frontend/src/app/shell.tsx`, testes de integração.
- **Aceite:** breakpoints do `DESIGN.md` §4b; navegação por teclado; `prefers-reduced-motion` respeitado; testes de unidade verdes.
- **Verificação:** `npm run --prefix frontend lint && npm run --prefix frontend typecheck && npm run --prefix frontend test:unit -- --run`.

---

## Fase 4 — Qualidade e vitrine (7 tasks)

### 4.1 — Gates de contrato
- **Objetivo:** impedir drift e quebra de compatibilidade.
- **Arquivos:** `.spectral.yaml`, `.github/workflows/ci.yml`, `scripts/check-contract-drift.mjs`.
- **Aceite:** Spectral sem erro; `oasdiff` compara com a versão de `main` e falha em breaking change; snapshot do springdoc comparado ao contrato.
- **Verificação:** job `contracts` verde e PR proposital com breaking change vermelho.

### 4.2 — Testes de propriedade com Schemathesis
- **Objetivo:** exercitar a API real contra o contrato.
- **Arquivos:** `.github/workflows/ci.yml`, `tests/contract/schemathesis.toml`.
- **Aceite:** Schemathesis roda contra o compose; qualquer resposta fora do contrato falha o job; relatório anexado.
- **Verificação:** job `contracts` com Schemathesis verde e relatório publicado.

### 4.3 — E2E com Playwright no CI
- **Objetivo:** provar o fluxo completo no navegador.
- **Arquivos:** `frontend/e2e/authorization.spec.ts`, `frontend/e2e/portability.spec.ts`, `frontend/e2e/rejection.spec.ts`, `frontend/playwright.config.ts`.
- **Aceite:** três cenários cobrindo caminho feliz, rejeição e idempotência; execução contra build de produção e compose; artefatos de trace e vídeo.
- **Verificação:** job `e2e` verde no GitHub Actions com relatório HTML.

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
