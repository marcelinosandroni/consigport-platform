# ConsigPort — Frontend

React 19.2 + Vite 7 RSC + TypeScript + Tailwind CSS 4. Sem Next.js.

## Scripts

| Script | O que faz |
| --- | --- |
| `npm run dev` | Servidor de desenvolvimento (porta 5173). |
| `npm run build` | Build dos três ambientes RSC (`rsc`, `ssr`, `client`). |
| `npm run preview` | Serve o build de produção. |
| `npm run lint` | ESLint (flat config) em todo o projeto. |
| `npm run typecheck` | `tsc --noEmit` em modo estrito. |
| `npm run test:unit` | Vitest + Testing Library. |

## Estrutura

- `src/framework/` — entradas RSC do template oficial (`entry.rsc`, `entry.ssr`, `entry.browser`).
- `src/root.tsx` — documento HTML do servidor.
- `src/styles.css` — Tailwind CSS 4 e estilos base.
- `src/theme.css` — cópia verbatim de `SDD/themes/matrix/tokens.css` (guardada por
  `node scripts/check-theme-tokens.mjs`).

## Instalação

```bash
npm ci --prefix frontend
```
