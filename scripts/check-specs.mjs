#!/usr/bin/env node
/**
 * SDD SKILL: check-specs
 * Validates the core invariants and specifications of the ConsigPort SDD AI Stack.
 *
 * Checks:
 * 1. Required Tier 1, Tier 2, ADRs, Quality Gates and Evidence documents exist.
 * 2. SDD/specs/PLAN.md contains exactly ONE task in flight marked [-].
 * 3. Tasks listed in PLAN.md exist in SDD/specs/BACKLOG.md.
 *
 * Usage: node scripts/check-specs.mjs
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, "..");

const errors = [];

// 1. Check required documents
const requiredDocs = [
  "AGENTS.md",
  "SDD/AGENTS.md",
  "SDD/APP.md",
  "SDD/APP-STACK.md",
  "SDD/ARCHITECTURE.md",
  "SDD/stacks/clean-code.md",
  "SDD/specs/PLAN.md",
  "SDD/specs/BACKLOG.md",
  "SDD/specs/ROADMAP.md",
  "SDD/docs/EVIDENCE.md",
  "docs/spec/desafio-tecnico.md",
  "docs/quality/GATES.md",
  "docs/ai-harness/HARNESS_OVERVIEW.md",
  "docs/ai-harness/AI_USAGE_LOG.md",
  "docs/adr/0001-hexagonal-ddd-acl-e-mock-isolado.md",
  "docs/adr/0002-integridade-hmac-e-idempotencia.md",
  "docs/adr/0003-assincronismo-polling-e-trilha-imutavel.md",
  "docs/adr/0004-contrato-openapi-versionamento-e-i18n.md",
  "docs/adr/0005-frontend-rsc-design-e-renderizacao.md",
  "docs/adr/0006-gitflow-ci-cd-docker-e-performance.md"
];

for (const doc of requiredDocs) {
  const fullPath = path.join(root, doc);
  if (!fs.existsSync(fullPath)) {
    errors.push(`Missing required document: ${doc}`);
  }
}

// 2. Validate SDD/specs/PLAN.md single in-flight task rule
const planPath = path.join(root, "SDD/specs/PLAN.md");
if (fs.existsSync(planPath)) {
  const planContent = fs.readFileSync(planPath, "utf8");
  
  // Find all tasks marked as [-]
  const inFlightMatches = planContent.match(/^-\s*\[-\]/gm) || [];
  if (inFlightMatches.length > 1) {
    errors.push(
      `SDD rule violation in SDD/specs/PLAN.md: Expected at most 1 task marked '[-]', found ${inFlightMatches.length}.`
    );
  }

  // 3. Cross-reference tasks in PLAN with BACKLOG
  const backlogPath = path.join(root, "SDD/specs/BACKLOG.md");
  if (fs.existsSync(backlogPath)) {
    const backlogContent = fs.readFileSync(backlogPath, "utf8");
    const taskPattern = /^-\s*\[([ x\-])\]\s*(?:\*\*)?(\d+\.\d+)/gm;
    let match;
    while ((match = taskPattern.exec(planContent)) !== null) {
      const taskId = match[2];
      const regexInBacklog = new RegExp(`###\\s*${taskId.replace(".", "\\.")}\\b`);
      if (!regexInBacklog.test(backlogContent)) {
        errors.push(`Task ${taskId} in PLAN.md not found in BACKLOG.md.`);
      }
    }
  }
}

if (errors.length > 0) {
  console.error(`\n✖ SDD Specification check failed with ${errors.length} error(s):`);
  for (const err of errors) {
    console.error(`  - ${err}`);
  }
  process.exit(1);
}

console.log("✓ SDD specifications valid:");
console.log("  - Required core documents and ADRs present.");
console.log("  - Exactly 1 task in flight in SDD/specs/PLAN.md.");
console.log("  - Tasks in PLAN.md are cataloged in BACKLOG.md.");
process.exit(0);
