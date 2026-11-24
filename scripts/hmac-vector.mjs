#!/usr/bin/env node
/**
 * Reproduces the shared HMAC vectors used by the backend and the Central mock.
 *
 *   node scripts/hmac-vector.mjs          # verify every vector
 *   node scripts/hmac-vector.mjs --write  # recompute and persist signatures
 */
import { createHash, createHmac } from "node:crypto";
import fs from "node:fs";
import { fileURLToPath } from "node:url";

const FILE = fileURLToPath(new URL("../contracts/hmac-vector.json", import.meta.url));
const WRITE = process.argv.includes("--write");

function fail(message) {
  console.error(`✗ HMAC vector: ${message}`);
  process.exit(1);
}

if (!fs.existsSync(FILE)) fail("contracts/hmac-vector.json not found.");

const data = JSON.parse(fs.readFileSync(FILE, "utf8"));
const { algorithm, secret, vectors } = data;

if (algorithm !== "HMAC-SHA256") fail(`unsupported algorithm: ${algorithm}.`);
if (typeof secret !== "string" || secret.length === 0) fail("secret is empty.");
if (!Array.isArray(vectors) || vectors.length === 0) fail("no vectors found.");

function sha256Hex(value) {
  return createHash("sha256").update(value, "utf8").digest("hex");
}

function canonical(vector) {
  return [
    vector.method,
    vector.path,
    String(vector.timestamp),
    sha256Hex(vector.body ?? ""),
  ].join("\n");
}

function signature(vector) {
  return createHmac("sha256", secret).update(canonical(vector), "utf8").digest("hex");
}

const seen = new Set();
let mismatch = false;

for (const vector of vectors) {
  if (!vector.name || seen.has(vector.name)) fail(`duplicate or empty vector name: ${vector.name}.`);
  seen.add(vector.name);

  if (!["GET", "POST", "PUT", "PATCH", "DELETE"].includes(vector.method)) {
    fail(`${vector.name}: unsupported method ${vector.method}.`);
  }
  if (typeof vector.path !== "string" || !vector.path.startsWith("/")) {
    fail(`${vector.name}: path must start with '/'.`);
  }
  if (!Number.isInteger(vector.timestamp) || vector.timestamp <= 0) {
    fail(`${vector.name}: timestamp must be a positive epoch in seconds.`);
  }

  const expectedCanonical = canonical(vector);
  const expectedSignature = signature(vector);

  if (WRITE) {
    vector.canonical = expectedCanonical;
    vector.signature = expectedSignature;
    continue;
  }

  if (vector.canonical !== expectedCanonical) {
    console.error(`✗ HMAC vector: ${vector.name}: canonical string diverges.`);
    mismatch = true;
  }
  if (vector.signature !== expectedSignature) {
    console.error(
      `✗ HMAC vector: ${vector.name}: expected ${expectedSignature}, found ${vector.signature}.`,
    );
    mismatch = true;
  }
}

if (WRITE) {
  fs.writeFileSync(FILE, `${JSON.stringify(data, null, 2)}\n`, "utf8");
  console.log(`✓ HMAC vector: ${vectors.length} signatures written to contracts/hmac-vector.json.`);
  process.exit(0);
}

if (mismatch) process.exit(1);
console.log(`✓ HMAC vector: ${vectors.length} vectors reproduced with ${algorithm}.`);
