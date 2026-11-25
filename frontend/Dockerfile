# syntax=docker/dockerfile:1

# ── Build ───────────────────────────────────────────────────────────────────
# React 19 + Vite 7 RSC: the build produces the three environments (rsc, ssr
# and client) under frontend/dist.
FROM node:24-alpine AS build
WORKDIR /workspace

# The package manager is pinned exactly like in CI: the allowScripts field in
# package.json only exists from npm 12 on.
RUN npm install --global npm@12.0.2

# The manifest is copied first: the install layer survives every source change.
COPY frontend/package.json frontend/package-lock.json frontend/
RUN npm ci --prefix frontend

COPY frontend frontend
RUN npm run --prefix frontend build

# ── Runtime ─────────────────────────────────────────────────────────────────
FROM node:24-alpine AS runtime
WORKDIR /workspace

# vite preview serves the built RSC app, so the toolchain has to ship with it.
# The Vite config is part of the runtime: without it the preview server has no
# RSC plugin and every route answers 404.
COPY --from=build /workspace/frontend/package.json /workspace/frontend/package-lock.json frontend/
COPY --from=build /workspace/frontend/vite.config.ts frontend/vite.config.ts
COPY --from=build /workspace/frontend/node_modules frontend/node_modules
COPY --from=build /workspace/frontend/dist frontend/dist

# Vite bundles the config file into node_modules/.vite-temp before serving, so
# the unprivileged user needs to own the tree it writes to.
RUN chown -R node:node /workspace/frontend

USER node
EXPOSE 5173

# 127.0.0.1, not localhost: the server binds IPv4 only and busybox resolves
# localhost to ::1 first.
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://127.0.0.1:5173/ || exit 1

CMD ["npm", "run", "--prefix", "frontend", "preview", "--", "--host", "0.0.0.0", "--port", "5173"]
