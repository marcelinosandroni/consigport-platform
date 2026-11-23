# ADR-0005 — Frontend RSC, design e renderização

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Design:** [`../../SDD/DESIGN.md`](../../SDD/DESIGN.md)
- **Arquitetura:** [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md) §5

## Contexto

O frontend precisa entregar um fluxo transacional — opt-in, agenda, seleção, envio,
status e auditoria — com boa primeira pintura e sem baixar JavaScript de dados para o
cliente. O projeto tem duas restrições explícitas:

1. **Sem Next.js.** O framework é React 19.2 com Vite e o plugin oficial `rsc`.
2. **Design Matrix, dark-only**, com tokens próprios e fontes self-hosted.

Dados de portabilidade são transacionais: cache de página com ISR não é aceitável, porque
o status muda por polling e uma resposta velha seria um erro de negócio.

## Decisão

### Base técnica

- React 19.2 + React DOM 19.2, Vite 7 e `@vitejs/plugin-rsc`, a partir do template
  oficial `rsc`.
- TypeScript estrito; Tailwind 4; shadcn/ui sobre Radix.
- TanStack Query apenas nas ilhas cliente que fazem polling.
- `openapi-fetch` com tipos gerados do contrato; sem Axios.
- Fontes via `@fontsource` (Manrope, JetBrains Mono, Playfair Display).

### Modelo de renderização

| Rota | Estratégia |
|---|---|
| Shell e páginas estáticas | Pré-renderizadas no build (SSG estático) |
| Páginas com dado de operação | Streaming SSR sob demanda |
| Formulários e polling | Ilhas cliente com hidratação seletiva |

- **Server Components por padrão**; `"use client"` só em folhas interativas
  (formulário, tabela selecionável, painel com polling, troca de idioma).
- **Sem ISR.** Dado transacional nunca é servido de cache de página.
- O shell estático é o primeiro entregável, para que o RSC não bloqueie a Fase 0.

### Design

- Dark-only, tokens de `SDD/themes/matrix/tokens.css` copiados verbatim para
  `frontend/src/theme.css`.
- Tipografia, espaçamento, raios e estados definidos no `DESIGN.md`.
- Responsivo conforme `DESIGN.md` §4b; foco visível e contraste AA.
- `prefers-reduced-motion` respeitado.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Next.js 16 | Proibido pelo requisito do projeto; traria App Router, cache e deployment próprios. |
| SPA pura com Vite | Perde RSC e streaming; a primeira pintura dependeria de JS. |
| Remix / React Router framework | Também embute o modelo de servidor que o requisito exclui. |
| ISR para o painel de status | Serviria status velho em um fluxo transacional. |
| Fontes via CDN | Viola self-hosted e adiciona dependência externa em runtime. |
| Redux/Zustand para dados de servidor | Duplicaria cache; TanStack Query resolve o polling. |

## Consequências

**Positivas**
- Primeira pintura útil sem depender de JS de dados.
- Menos JavaScript no cliente nas rotas de leitura.
- Design consistente e offline-friendly nas fontes.

**Negativas**
- RSC em Vite ainda é fronteira móvel; o plugin pode exigir ajustes entre versões.
- RSC manual custa mais que o App Router do Next.
- Streaming SSR exige cuidado com erros e *fallback*.

**Mitigação**
- Versão do template congelada no `package-lock.json` na task 0.8.
- Shell estático entregue antes das rotas dinâmicas.
- Testes de componente e E2E cobrem o fluxo real, não apenas o build.

## Como é verificado

- `npm run --prefix frontend build` e `typecheck` no CI.
- E2E Playwright no CI cobrindo caminho feliz, rejeição e idempotência.
- Teste de polling com timers falsos parando em estado terminal.
- Inspeção de responsividade nos breakpoints do `DESIGN.md`.
