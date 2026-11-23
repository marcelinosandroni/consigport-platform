# ADR-0006 — Gitflow, CI/CD, Docker e performance

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Roadmap:** [`../../SDD/specs/ROADMAP.md`](../../SDD/specs/ROADMAP.md)
- **Gates:** [`../quality/GATES.md`](../quality/GATES.md)

## Contexto

O projeto é individual e de curta duração, mas precisa provar disciplina de entrega:
histórico legível, nenhum commit direto na branch principal, pipeline que roda de fato,
e desempenho medido sem transformar a máquina do desenvolvedor em laboratório de carga.

O desafio pede execução com um comando, e a avaliação tende a olhar o repositório antes
de olhar a aplicação. Portanto o processo também é entregável.

## Decisão

### Branches

| Branch | Papel |
|---|---|
| `main` | Produção; protegida; recebe apenas merge de `staging` |
| `staging` | Homologação; recebe `develop` por PR |
| `develop` | Integração; recebe branches de task por PR |
| `docs/000-*`, `feat/*`, `fix/*`, `chore/*` | Trabalho; vida curta |

- Nenhum commit direto em `main`, `staging` ou `develop`.
- Merge por squash, histórico linear na branch de destino.
- Proteção aplicada via `gh api` e documentada em `docs/process/GITFLOW.md`.

### Commits e versão

- Conventional Commits em inglês:
  `type(scope): descrição. (Agent: OpenCode - DeepSeek V4.1 Flash)`.
- SemVer com tag por fase: `v0.1.0` … `v1.0.0`.
- `release-please` gera changelog e tag a partir dos commits.

### CI/CD

- GitHub Actions, jobs paralelos: `docs`, `backend`, `mock`, `frontend`, `contracts`,
  `e2e`, `k6-smoke`, `security`.
- Tudo empacotado em Docker; `docker compose up` sobe banco, backend, mock e frontend.
- Imagens publicadas em GHCR com tag SemVer e `latest`.
- `staging` por tag; produção por *manual approval*.
- Segredos somente em GitHub Secrets; nunca em workflow, log ou commit.

### Performance

- **k6**, não Gatling/JMeter: script em JavaScript, thresholds nativos, relatório HTML e
  imagem oficial pequena.
- `smoke.js` com 5 VUs no PR; `load.js` com 50 VUs no nightly e no release.
- Limites: `http_req_failed < 1%` e `p95 < 800 ms`.

### Onde os testes pesados rodam

- E2E (Playwright) e performance (k6) rodam **exclusivamente no CI/CD**.
- Localmente ficam unidade, integração e build — rápidos o suficiente para o ciclo de
  edição.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Trunk-based com feature flags | Menos cerimônia, mas o edital e a avaliação valorizam fluxo de branches explícito. |
| GitLab CI | O repositório está no GitHub; Actions reduz atrito. |
| Jenkins | Infraestrutura extra para um projeto individual. |
| Gatling ou JMeter | JVM/XML e relatórios mais pesados; k6 cobre o requisito com menos atrito. |
| E2E local obrigatório | Lento e dependente de compose; o CI é o ambiente reprodutível. |
| Deploy por push em `main` | Sem portão humano; risco de publicar quebrado. |

## Consequências

**Positivas**
- Histórico auditável e releases automáticos.
- Ambiente único e reprodutível em Docker.
- Performance medida com limites objetivos.

**Negativas**
- Fluxo de branches adiciona passos para um desenvolvedor solo.
- CI completo é caro em minutos; jobs pesados foram para nightly/release.
- k6 exige um runner com Docker e compose saudável.

**Mitigação**
- Jobs paralelos e cache de Maven/npm.
- `nightly.yml` separado para carga e Schemathesis prolongado.
- Healthchecks no compose antes de E2E e k6.

## Como é verificado

- PR só entra com todos os jobs verdes e uma revisão.
- `docker compose config -q` no job `docs`/`backend`.
- Tag de teste gera release e deploy em staging.
- Resumo k6 anexado ao run com thresholds verdes.
