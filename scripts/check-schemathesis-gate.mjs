#!/usr/bin/env node
/**
 * The armed-proof of the dynamic contract gate.
 *
 *   node scripts/check-schemathesis-gate.mjs
 *
 * A gate that never fails is not a gate. The proof runs the Schemathesis
 * against a fixture contract that describes the real operation of the API but
 * forgets one answer of it: the API answers `404` for an unknown operation and
 * the fixture declares only the `200`. The gate has to fail with the
 * "undocumented status code" violation — and it has to fail for that reason,
 * not because the tool is broken.
 *
 * The fixture lives in `tests/contract/fixtures/drifted-contract.yaml` and the
 * configuration is the one of the gate, `tests/contract/schemathesis.toml`, so
 * the proof runs with the same checks as the real test.
 */
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const CONFIG = "tests/contract/schemathesis.toml";
const FIXTURE = "tests/contract/fixtures/drifted-contract.yaml";
const EXPECTED = "Undocumented status code";

function abort(message) {
  console.error(`✗ Dynamic contract gate: ${message}`);
  process.exit(1);
}

function main() {
  const binary = process.env.SCHEMATHESIS_BIN ?? "schemathesis";
  if (!fs.existsSync(path.join(ROOT, CONFIG))) {
    abort(`the configuration ${CONFIG} is missing.`);
  }
  if (!fs.existsSync(path.join(ROOT, FIXTURE))) {
    abort(`the fixture ${FIXTURE} is missing.`);
  }

  const reportDirectory = fs.mkdtempSync(path.join(os.tmpdir(), "consigport-schemathesis-"));
  try {
    const run = spawnSync(
      binary,
      [
        "--config-file",
        path.join(ROOT, CONFIG),
        "run",
        path.join(ROOT, FIXTURE),
        "--no-color",
        "--report-dir",
        reportDirectory,
      ],
      { cwd: ROOT, encoding: "utf-8" },
    );

    if (run.error) {
      abort(
        `the Schemathesis binary is not available (${binary}). ` +
          "Install it with `pip install schemathesis` or point SCHEMATHESIS_BIN at it.",
      );
    }

    const output = `${run.stdout ?? ""}${run.stderr ?? ""}`;
    if (run.status === 0) {
      abort(
        `the gate passed against a contract that forgets the 404 of the API. ` +
          "A gate that never fails is not a gate.",
      );
    }
    if (!output.includes(EXPECTED)) {
      abort(
        `the gate failed, but not for the expected reason (${EXPECTED}). ` +
          `The output of the tool was:\n${output.trim()}`,
      );
    }

    console.log(`✓ the gate is armed: the undocumented answer of the API fails the test`);
    console.log(`✓ the failure is the expected one: ${EXPECTED}`);
  } finally {
    fs.rmSync(reportDirectory, { recursive: true, force: true });
  }
}

main();
