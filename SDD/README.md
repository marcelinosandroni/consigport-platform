# 🧠 SDD — the rules

> Spec-Driven Development. The specification is the nervous system; you only need
> to know **where to look right now**.

## Start here

| Read | When |
| --- | --- |
| [`AGENTS.md`](./AGENTS.md) | Always, before coding. The laws and the delivery flow. |
| [`specs/PLAN.md`](./specs/PLAN.md) | To find the one task in flight. |
| [`PREFLIGHT.md`](./PREFLIGHT.md) | Before committing. The gates and what each one proves. |
| [`APP.md`](./APP.md) | To remember what this project is. |
| [`APP-STACK.md`](./APP-STACK.md) | To know which stack file applies. |

## The rule map

Every document opens with a router, so you never read one that does not apply.

| Folder | Scope |
| --- | --- |
| [`stacks/`](./stacks) | One file per stack — language, framework, tooling. Start at [`stacks/README.md`](./stacks/README.md). |
| [`specs/`](./specs) | The PLAN, the task in flight, and one file per closed phase. |
| [`SKILLS/`](./SKILLS) | Automations. If a pattern repeats, it becomes a skill. |
| [`docs/`](./docs) | [What this template delivers](./docs/PRODUCT.md) and [how to plan](./docs/PLANNING.md). |

## The one rule that matters most

**Read the minimum.** An agent that reads forty files to answer one question is an
agent that starts guessing at file thirty. If a document does not tell you when to
open it, that document is the problem.

> This index is generated on every `create-sdd-ai-stack`. It never mentions the
> template's own repository, its releases, or its npm package — those are not
> your project's history.
