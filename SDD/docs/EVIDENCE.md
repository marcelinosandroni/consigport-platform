# 📜 EVIDENCE — Registro Central de Evidências

> Registro histórico e auditável das evidências de verificação de cada tarefa concluída no ConsigPort.
> Todas as tarefas `[x]` devem colar aqui a prova de execução (comandos verdes, testes e links).
> Referências: [`../specs/PLAN.md`](../specs/PLAN.md), [`../specs/BACKLOG.md`](../specs/BACKLOG.md) e [`../../docs/quality/GATES.md`](../../docs/quality/GATES.md).

---

## Fase 0 — Fundação

### Task 0.1 — Refinamento SDD completo

- **Data de Conclusão:** 2026-11-23
- **Branch:** `docs/000-sdd-foundation`
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash (High)
- **Objetivo:** Specs em pt-BR, 6 ADRs, harness de IA, gates de qualidade, CI de documentação e correção dos links do `AGENTS.md` raiz.

#### Comandos de Verificação e Saídas:

1. **Validação de Links Relativos em Markdown:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   ```
   *Saída esperada / obtida:*
   ```text
   ✓ 27 documents, every relative link resolves.
   ```

2. **Validação de Invariantes de Especificação (SDD):**
   ```bash
   node scripts/check-specs.mjs
   ```
   *Saída esperada / obtida:*
   ```text
   ✓ SDD specifications valid:
     - Tier 1 and Tier 2 core documents exist.
     - 6 Architecture Decision Records (ADRs) present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN match BACKLOG.md.
   ```

#### Arquivos Tocados:
- `docs/quality/GATES.md`
- `docs/ai-harness/HARNESS_OVERVIEW.md`
- `docs/ai-harness/AI_USAGE_LOG.md`
- `SDD/docs/EVIDENCE.md`
- `AGENTS.md`
- `scripts/check-specs.mjs`
- `.github/workflows/ci-docs.yml`
- `SDD/specs/PLAN.md`


---

### Task 0.2 — Gitflow, PR template e CODEOWNERS

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/002-gitflow` (desenvolvido em worktree `consigport-task-0.2`)
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Objetivo:** Materializar o fluxo de branches, política de revisão obrigatória, template de PR e documentação operacional de Gitflow.

#### Comandos de Verificação e Saídas:

1. **Validação de Links Relativos em Markdown:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   ```
   *Saída obtida:*
   ```text
   ✓ 60 documents, every relative link resolves.
   ```

2. **Validação de Invariantes de Especificação (SDD):**
   ```bash
   node scripts/check-specs.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid:
     - Required core documents and ADRs present.
     - Exactly 1 task in flight in SDD/specs/PLAN.md.
     - Tasks in PLAN.md are cataloged in BACKLOG.md.
   ```

3. **Validação de Desenhos e Arquitetura:**
   ```bash
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso!
   ```

4. **Verificação de Ignoração Git:**
   ```bash
   git check-ignore .github/PULL_REQUEST_TEMPLATE.md .github/CODEOWNERS docs/process/GITFLOW.md
   # Exit code 1 (Arquivos não são ignorados pelo .gitignore)
   ```

#### Arquivos Tocados:
- `.github/PULL_REQUEST_TEMPLATE.md`
- `.github/CODEOWNERS`
- `docs/process/GITFLOW.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`


---

### Task 0.3 — Ambiente local e PostgreSQL

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/003-local-env`
- **Agente / Modelo:** Antigravity — Gemini 3.8 Flash
- **Objetivo:** Estabelecer a infraestrutura base local através de `.gitignore`, `.env.example` e `compose.yaml` com o serviço `postgres:16-alpine`, volume e healthcheck.

#### Comandos de Verificação e Saídas:

1. **Validação de Sintaxe e Estrutura do Docker Compose:**
   ```bash
   docker compose config -q
   # Exit code 0 (Sintaxe e variáveis válidas)
   ```

2. **Verificação das Regras de Ignoração Git (.gitignore):**
   ```bash
   git check-ignore -v .env target/ node_modules/ .idea/ .env.example compose.yaml
   ```
   *Saída obtida:*
   ```text
   .gitignore:8:.env	.env
   .gitignore:18:target/	target/
   .gitignore:35:node_modules/	node_modules/
   .gitignore:52:.idea/	.idea/
   ```
   *(.env.example e compose.yaml rastreados; .env e artefatos ignorados).*

3. **Validação de Invariantes e Documentação:**
   ```bash
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-specs.mjs
   node scripts/check-architecture.mjs
   # Todos com exit code 0.
   ```

#### Arquivos Tocados:
- `.gitignore`
- `.env.example`
- `compose.yaml`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`

---

### Task 0.4 — Maven Wrapper e reactor raiz

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/004-maven-reactor`
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Objetivo:** Build Java reprodutível sem Maven global. Wrapper oficial 3.3.4
  (`only-script`) apontando para Maven 3.10.2, reactor raiz em Java 21 + Spring Boot
  3.5.16 e módulos mínimos `backend` e `central-convenios-mock`.

#### Comandos de Verificação e Saídas:

1. **Wrapper resolve o Maven distribuído e encontra o Java 21:**
   ```powershell
   .\mvnw.cmd -v
   ```
   *Saída obtida:*
   ```text
   Apache Maven 3.10.2 (61b8bc355f4c4325463c22aa9afe4e7811a290f8)
   Maven home: C:\Users\marcelino\.m2\wrapper\dists\apache-maven-3.10.2\...
   Java version: 21.0.12, vendor: Oracle Corporation
   exit=0
   ```

2. **Reactor válido na raiz (não recursivo):**
   ```powershell
   .\mvnw.cmd -N validate
   ```
   *Saída obtida:*
   ```text
   [INFO] BUILD SUCCESS
   exit=0
   ```

3. **Build completo dos dois módulos sem testes:**
   ```powershell
   .\mvnw.cmd -q -DskipTests package
   ```
   *Saída obtida:*
   ```text
   exit=0
   ```

4. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid: ... (exit 0)
   ✓ 60 documents, every relative link resolves. (exit 0)
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso! (exit 0)
   ```

5. **Portão do AI Usage Log (hook e estrutura):**
   ```powershell
   node scripts/check-ai-log.mjs
   node scripts/check-ai-log.mjs --staged   # com trabalho staged e sem o log
   ```
   *Saída obtida:*
   ```text
   ✓ AI usage log: 7 sessions, structure valid. (exit 0)
   ✗ AI usage log: staging 1 project file(s) requires staging
     docs/ai-harness/AI_USAGE_LOG.md with a new session. (exit 1, esperado)
   ```

#### Arquivos Tocados:
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/maven-wrapper.properties`
- `.mvn/jvm.config`
- `.gitattributes`
- `backend/pom.xml`
- `central-convenios-mock/pom.xml`
- `scripts/check-ai-log.mjs`
- `.githooks/pre-commit`
- `.github/workflows/ci-docs.yml`
- `docs/ai-harness/AI_USAGE_LOG.md`
- `docs/ai-harness/HARNESS_OVERVIEW.md`
- `docs/quality/GATES.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`

---

### Task 0.5 — Contratos OpenAPI e Vetor HMAC

- **Data de Conclusão:** 2026-11-24
- **Branch:** `feat/005-contracts`
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Objetivo:** Contrato primeiro. Superfície pública `/api/v1` em inglês, contrato da
  Central fiel ao edital em português, vetor HMAC compartilhado e verificação
  automatizada de ambos.

#### Comandos de Verificação e Saídas:

1. **Contratos validados pelo Spectral (zero erros e zero avisos):**
   ```powershell
   npx @stoplight/spectral-cli@6 lint contracts/consigport-api.yaml contracts/central-api.yaml --fail-severity=warn
   ```
   *Saída obtida:*
   ```text
   No results with a severity of 'warn' or higher found!
   exit=0
   ```

2. **Vetor HMAC reproduzido deterministicamente:**
   ```powershell
   node scripts/hmac-vector.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ HMAC vector: 3 vectors reproduced with HMAC-SHA256.
   exit=0
   ```

3. **Invariantes de especificação, links e arquitetura:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:*
   ```text
   ✓ SDD specifications valid: ... (exit 0)
   ✓ 60 documents, every relative link resolves. (exit 0)
   🎉 Todos os 22 testes de validação dos desenhos e arquitetura passaram com sucesso! (exit 0)
   ```

4. **Portão de contratos no CI (Spectral, HMAC e oasdiff):**
   ```text
   Workflow: CI Contracts (.github/workflows/ci-contracts.yml)
   - npx --yes @stoplight/spectral-cli@6 lint contracts/*.yaml --fail-severity=warn
   - node scripts/hmac-vector.mjs
   - node scripts/check-ai-log.mjs          (AI_LOG_BASE_SHA no pull request)
   - oasdiff breaking <contrato da base> <contrato atual> --fail-on ERR
   Baseline ausente na branch base → diff pulado com aviso, sem quebrar o pipeline.
   ```

#### Conteúdo Entregue:
- **API pública:** autorizações, agenda, criação/listagem/detalhe de operações e trilha
  de eventos, com `Idempotency-Key`, `Idempotent-Replay`, paginação e erros localizados.
- **API da Central:** `POST /autorizacoes`, `GET /beneficiarios/{id}/agenda`,
  `POST /operacoes-portabilidade`, `GET /operacoes-portabilidade/{protocolo}` com
  `202`/`204` + `Retry-After`, HMAC, idempotência, caos e administração.
- **Vetor HMAC:** três requisições canônicas (POST de portabilidade, GET de status e
  POST de autorização) com segredo de teste e assinatura verificável por script.
- **Portão de CI:** workflow dedicado que reprova contrato inválido, vetor HMAC divergente
  e breaking change contra a branch base.
- **Catálogo de erros:** códigos `BENEFICIARY_NOT_FOUND` no core e
  `REQUISICAO_INVALIDA`, `SEM_AUTORIZACAO`, `BENEFICIARIO_NAO_ENCONTRADO` e
  `PARCELA_INVALIDA` na Central, refletidos no `ARCHITECTURE.md`.

#### Arquivos Tocados:
- `contracts/consigport-api.yaml`
- `contracts/central-api.yaml`
- `contracts/hmac-vector.json`
- `scripts/hmac-vector.mjs`
- `.spectral.yaml`
- `.github/workflows/ci-contracts.yml`
- `docs/quality/GATES.md`
- `SDD/ARCHITECTURE.md`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`

---

### Task 0.6 — Scaffold do Backend

- **Data de Conclusão:** 2026-11-25
- **Branch:** `feat/006-backend-scaffold`
- **Agente / Modelo:** OpenCode — DeepSeek V4.1 Flash
- **Objetivo:** Aplicação Spring Boot vazia, compilando, com health respondendo `UP` e
  configuração única (`application.yml`) dirigida por `DATABASE_URL`.

#### Comandos de Verificação e Saídas:

1. **Build do módulo no reactor (compilação + testes):**
   ```powershell
   .\mvnw.cmd -q -pl backend -am verify
   ```
   *Saída obtida:*
   ```text
   Tomcat started on port 54011 (http) with context path '/'
   Init duration for springdoc-openapi is: 180 ms
   Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in br.com.consigport.ConsigPortApplicationTests
   verify_exit=0
   ```

2. **Health e contrato expostos por HTTP (dentro do teste, servidor real):**
   ```text
   GET /actuator/health → 200 {"status":"UP"}
   GET /v3/api-docs     → 200 {"openapi":"3.1.0",...}
   ```

3. **Reactor válido (critério de saída da fase):**
   ```powershell
   .\mvnw.cmd -N validate
   ```
   *Saída obtida:*
   ```text
   [INFO] BUILD SUCCESS
   reactor_validate_exit=0
   ```

4. **Invariantes de documentação e especificação:**
   ```powershell
   node scripts/check-specs.mjs
   node SDD/SKILLS/check-docs/check-docs.mjs .
   node scripts/check-architecture.mjs
   ```
   *Saída obtida:* exit 0 nos três.

#### Conteúdo Entregue:
- **Dependências:** `web`, `validation`, `data-jpa`, `actuator`, `flyway-core`,
  `flyway-database-postgresql`, driver PostgreSQL (runtime), springdoc e
  `spring-boot-starter-test`; H2 apenas em escopo de teste.
- **Configuração:** porta `8080` (`SERVER_PORT`), `DATABASE_URL`/`POSTGRES_*` vindos do
  `.env`, Hibernate em `ddl-auto: validate` (o schema é do Flyway), `open-in-view: false`,
  timezone UTC, `shutdown: graceful`, actuator expondo `health`/`info` com probes e
  Swagger em `/swagger-ui.html`.
- **Contrato alinhado:** springdoc configurado com `openapi_3_1`, então `/v3/api-docs`
  publica OpenAPI `3.1.0`, a mesma versão dos contratos versionados.
- **Perfil `local`:** mesmo bloco YAML apontando para o PostgreSQL do `compose.yaml` em
  `localhost:5432`, com log `DEBUG` do pacote `br.com.consigport`.
- **Teste de fumaça:** sobe o contexto completo em porta aleatória e prova
  `GET /actuator/health` → `UP` e `GET /v3/api-docs` → 200.

#### Débito Registrado:
- O teste do scaffold usa H2 em memória para não depender de Docker; a task de
  persistência troca por Testcontainers PostgreSQL 16 e reativa o Flyway, cobrindo as
  migrações de verdade.

#### Arquivos Tocados:
- `backend/pom.xml`
- `backend/src/main/java/br/com/consigport/ConsigPortApplication.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/com/consigport/ConsigPortApplicationTests.java`
- `SDD/specs/PLAN.md`
- `SDD/docs/EVIDENCE.md`
- `docs/ai-harness/AI_USAGE_LOG.md`
