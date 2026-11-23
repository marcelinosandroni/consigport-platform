# AI Agent Guidance & Execution Protocol

Welcome to **ConsigPort**! This project follows the **SDD (Spec-Driven Development) AI Stack** protocol. All AI agents interacting with this repository MUST strictly follow the governance rules, tiered context hierarchy, and architectural patterns defined in the `SDD/` directory.

---

## 🧭 Entry Point & Tiered Read Order

Before reading code or executing any task, agents **MUST** read files in the following order:

### Tier 1: Core System & Guidelines (Always Loaded)
1. [`SDD/AGENTS.md`](SDD/AGENTS.md) — Primary rules, system prompts, and operational constraints.
2. [`SDD/APP.md`](SDD/APP.md) — Product domain, business objectives, and functional goals.
3. [`SDD/APP-STACK.md`](SDD/APP-STACK.md) — Technology stack specifications and boundaries.
4. [`SDD/clean-code.md`](SDD/clean-code.md) — General software engineering and clean code standards.

### Tier 2: Specs & Active Work (Task-Specific)
1. [`SDD/specs/PLAN.md`](SDD/specs/PLAN.md) — Active roadmap, task checklists, and immediate steps.
2. [`SDD/specs/ARCHITECTURE.md`](SDD/specs/ARCHITECTURE.md) — System architecture, hexagonal layers, and boundaries.
3. [`docs/spec/desafio-tecnico.md`](docs/spec/desafio-tecnico.md) — Core business challenge requirement specification.

### Tier 3: Stacks & Reference Specs (On-Demand)
* [`SDD/stacks/java.md`](SDD/stacks/java.md) — Spring Boot, Java 21, and Backend standards.
* [`SDD/stacks/react.md`](SDD/stacks/react.md) (or `spa.md`) — React 19, TypeScript, and Frontend standards.
* [`SDD/stacks/language.md`](SDD/stacks/language.md) — Bilingual domain/code convention rules.

---

## 🏗️ Monorepo Architecture Overview

This project is structured as a monorepo divided into three main execution modules:

* **`backend/`**: Java 21 + Spring Boot (Hexagonal Architecture). Responsible for idempotency, HMAC validation, event logging, and polling backoff orchestrations.
* **`frontend/`**: React + TypeScript + Vite + Tailwind CSS. Handles opt-in flows, payroll schedule visualizer, portability status tracking, and audit log viewer.
* **`central-convenios-mock/`**: Mocked external Central service simulating asynchronous responses (`202 Accepted`), delay/polling, HMAC signatures, and failure scenarios.

---

## ⚠️ Key Operational Constraints for AI Agents

1. **Strict Context Adherence:** Do not assume missing business requirements without consulting `docs/spec/desafio-tecnico.md` and `SDD/APP.md`.
2. **Hexagonal Boundaries:** In the Java backend, keep Domain logic pure and uncoupled from Spring/persistence infrastructure (`inbound -> application -> domain <- outbound`).
3. **Evidence Logging:** All non-trivial implementation steps must update `SDD/docs/EVIDENCE.md` with test output or verification logs.
4. **Idempotency & Auditability:** Every status change and portability request must maintain an immutable event trail as required by the technical challenge spec.

---

> For detailed agent setup and instructions, refer directly to [**`SDD/AGENTS.md`**](SDD/AGENTS.md).