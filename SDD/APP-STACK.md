# ConsigPort — Stack e Limites Tecnológicos

> Define o que pode ser usado. Fora desta lista, a dependência precisa de ADR.
> Regras de negócio: [`APP.md`](./APP.md). Arquitetura: [`ARCHITECTURE.md`](./ARCHITECTURE.md).

---

## 1. Módulos

| Módulo | Runtime | Build | Porta | Persistência |
|---|---|---|---|---|
| `backend/` | Java 21 + Spring Boot 3.5.x | Maven (reactor) | `8080` | PostgreSQL 16 + Flyway |
| `central-convenios-mock/` | Java 21 + Spring MVC | Maven (reactor) | `8081` | Memória (`ConcurrentHashMap`) |
| `frontend/` | Node 24 + React 19.2 + Vite 7 RSC | npm | `5173` | Nenhuma |
| `postgres` | PostgreSQL 16-alpine | Docker | `5432` | Volume nomeado |

O mock é intencionalmente simples: sem banco, sem camadas hexagonais e sem Flyway.
Ele simula a Central externa, não é produto.

---

## 2. Versões

| Peça | Versão | Observação |
|---|---|---|
| Java | 21 LTS | Records, sealed, pattern matching |
| Spring Boot | 3.5.x | Jakarta, `RestClient`, actuator |
| Maven | 3.10.2 + Wrapper | Wrapper commitado; CI não depende de Maven global |
| PostgreSQL | 16-alpine | Imagem fixa |
| Flyway | Gerenciado pelo Boot | Migrações versionadas |
| Resilience4j | 2.3.x | Retry + CircuitBreaker |
| springdoc-openapi | 2.8.x | Swagger UI + OpenAPI 3.1 |
| ArchUnit | 1.4.x | Regras de dependência |
| Testcontainers | 1.21.x | PostgreSQL real nos testes |
| JUnit / AssertJ | JUnit 5 / 3.x | Testes de domínio sem mocks de framework |
| JaCoCo | 0.8.x | Cobertura com gate |
| Node.js | 24.x | Pinado no CI |
| React / React DOM | 19.2.x | Server Components |
| Vite | 7.x + `@vitejs/plugin-rsc` | Template oficial `rsc`, sem Next.js |
| Tailwind CSS | 4.x | Tokens do `DESIGN.md` |
| i18next / react-i18next | 26.x / 16.x | pt-BR, en, es |
| TanStack Query | 5.x | Polling de status no cliente |
| React Hook Form + Zod | 7.x / 4.x | Formulários e validação |
| openapi-typescript + openapi-fetch | 7.x / 0.13.x | Cliente tipado gerado do contrato |
| shadcn/ui (Radix) | Componentes copiados | Sem dependência de runtime do shadcn |
| Vitest + Testing Library | 4.x / 16.x | Unitários e componentes |
| Playwright | 1.5x | E2E exclusivamente no CI/CD |
| k6 | 1.x | Performance com thresholds e HTML |
| release-please | v4 action | Versão, changelog e tag automáticos |

Versões de patch são resolvidas por `package-lock.json` e pelo Maven no primeiro build;
os arquivos de lock entram nos commits da Fase 0.

---

## 3. Variáveis de ambiente

`.env.example` é a fonte; `.env` nunca é commitado.

| Variável | Padrão | Uso |
|---|---|---|
| `POSTGRES_DB` | `consigport` | Banco |
| `POSTGRES_USER` | `consigport` | Usuário |
| `POSTGRES_PASSWORD` | `consigport` | Senha local |
| `POSTGRES_PORT` | `5432` | Porta publicada |
| `DATABASE_URL` | `jdbc:postgresql://postgres:5432/consigport` | Backend em Docker |
| `BACKEND_PORT` | `8080` | API |
| `MOCK_PORT` | `8081` | Central |
| `FRONTEND_PORT` | `5173` | UI |
| `HMAC_SECRET` | `change-me-in-local` | Segredo compartilhado backend/mock |
| `CENTRAL_BASE_URL` | `http://central-convenios-mock:8081` | URL interna da Central |
| `MOCK_CONFIRMATION_DELAY_MS` | `4000` | Delay até `CONFIRMADA`/`REJEITADA` |
| `MOCK_TIMEOUT_DELAY_MS` | `6000` | Atraso do cenário `-TIMEOUT` |
| `POLLING_INTERVAL_MS` | `2000` | Intervalo do worker |
| `POLLING_BATCH_SIZE` | `20` | Lote por ciclo |
| `VITE_API_BASE_URL` | `http://localhost:8080` | API no browser |

Segredos reais vêm de GitHub Secrets em CI/CD. Nunca em workflow, log ou commit.

---

## 4. Bibliotecas aprovadas

### Backend
- `spring-boot-starter-web`, `validation`, `data-jpa`, `actuator`, `flyway-core`, `postgresql`.
- `resilience4j-spring-boot3` (retry e circuit breaker).
- `springdoc-openapi-starter-webmvc-ui` (Swagger).
- `archunit-junit5`, `testcontainers-postgresql`, `spring-boot-starter-test`.
- `jacoco-maven-plugin`.
- Mapeamento explícito em records; sem Lombok, sem MapStruct, sem reflection.

### Mock
- `spring-boot-starter-web`, `validation`, `actuator`, `springdoc-openapi-starter-webmvc-ui`.
- Apenas `ScheduledExecutorService` e coleções concorrentes.

### Frontend
- `react`, `react-dom`, `@vitejs/plugin-rsc`, `vite`, `typescript`, `tailwindcss`.
- `i18next`, `react-i18next`.
- `@tanstack/react-query`.
- `react-hook-form`, `zod`, `@hookform/resolvers`.
- `openapi-fetch`, `openapi-typescript`.
- `shadcn/ui` (Radix), `lucide-react`, `sonner`, `class-variance-authority`, `tailwind-merge`, `clsx`.
- `@fontsource/manrope`, `@fontsource/jetbrains-mono`, `@fontsource/playfair-display` (fontes locais, sem `next/font`).
- `vitest`, `@testing-library/react`, `@testing-library/user-event`, `@playwright/test`.

### Proibido
- Next.js, Remix, Gatsby ou qualquer framework de React Router com RSC embutido.
- Redux, Zustand e MobX para estado de servidor.
- Axios; o cliente é `openapi-fetch`.
- ORM ou Spring no domínio.
- CSS-in-JS; o estilo usa Tailwind + tokens do `DESIGN.md`.
- `npm install` em CI; usar `npm ci` com lockfile.

---

## 5. Contratos e idioma

| Artefato | Caminho | Idioma |
|---|---|---|
| API ConsigPort | `contracts/consigport-api.yaml` | Campos/status EN; descrições PT |
| API Central (mock) | `contracts/central-api.yaml` | PT, fiel ao edital |
| Cliente do front | gerado de `consigport-api.yaml` | Tipos EN |
| Mensagens da API | `backend/src/main/resources/i18n/messages_*.properties` | pt-BR (padrão), en, es |
| UI | `frontend/src/shared/i18n/*.json` | pt-BR (padrão), en, es |

Negociação: `Accept-Language` com fallback `pt-BR`. Códigos de erro são em inglês e
estáveis; `message` é localizada. A ACL traduz o contrato PT da Central para o domínio EN.

> **Nota de versão:** o `i18next` foi fixado em `26.x` (e não `25.x`) porque a série 25
> carrega o advisory **GHSA-6jcc-5g8w-32mx** (`deepExtend` recursivo sobre objetos
> embutidos, DoS), corrigido apenas a partir da `26.3.4`. O `react-i18next@16` aceita
> `i18next >= 25.10.9`, então a combinação segue suportada.

Documentação do projeto (specs, ADRs, README) é em pt-BR por decisão explícita do
responsável. Código, identificadores, commits e regras do template `SDD/` permanecem em
inglês. Ver [ADR-0004](../docs/adr/0004-contrato-openapi-versionamento-e-i18n.md).

---

## 6. Comandos

```bash
# Java
./mvnw -q -pl backend -am verify
./mvnw -q -pl central-convenios-mock -am verify

# Frontend
npm ci --prefix frontend
npm run --prefix frontend lint
npm run --prefix frontend typecheck
npm run --prefix frontend test:unit -- --run
npm run --prefix frontend build

# Documentação
node scripts/check-specs.mjs
node SDD/SKILLS/check-docs/check-docs.mjs
```

E2E, performance e testes de contrato rodam apenas no GitHub Actions. Ver
[`GATES.md`](../docs/quality/GATES.md).
