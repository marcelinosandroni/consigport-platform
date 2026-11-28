#!/usr/bin/env node
/**
 * The contract drift gate.
 *
 *   node scripts/check-contract-drift.mjs                 # every local gate
 *   node scripts/check-contract-drift.mjs --base main      # breaking changes against main
 *   node scripts/check-contract-drift.mjs --self-test     # proof that the gate is armed
 *   node scripts/check-contract-drift.mjs --document tests/contracts/springdoc-consigport.json
 *   node scripts/check-contract-drift.mjs --live http://localhost:8080/v3/api-docs
 *   node scripts/check-contract-drift.mjs --live http://localhost:8080/v3/api-docs \
 *     --write-snapshot tests/contracts/springdoc-consigport.json
 *
 * The gate answers three questions:
 *
 *   1. Did the public contract break? `oasdiff breaking` compares the contract
 *      of the baseline ref (`main`, by default) with the contract of the
 *      working tree. A breaking change fails the gate.
 *   2. Does the published document still honour the contract? The OpenAPI
 *      document published by the springdoc (`/v3/api-docs`) is compared with the
 *      versioned contract: the routes, the methods, the operationIds, the
 *      parameters, the request bodies and the answers must agree.
 *   3. Is the gate armed? The self-test runs `oasdiff` against a fixture pair
 *      where the revision removes an answer. The gate must fail. A gate that
 *      never fails is not a gate.
 *
 * The comparison of the documents ignores what does not change the contract of
 * the consumer — the descriptions, the examples, the formats and the numeric
 * bounds — the same way the Java comparator of the backend does.
 */
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const CONTRACTS = ["contracts/consigport-api.yaml", "contracts/central-api.yaml"];
const DEFAULT_CONTRACT = "contracts/consigport-api.yaml";
const DEFAULT_SNAPSHOT = "tests/contracts/springdoc-consigport.json";
const FIXTURE_BASE = "tests/contracts/breaking-change-base.yaml";
const FIXTURE_BREAKING = "tests/contracts/breaking-change.yaml";
const DEFAULT_OASDIFF_VERSION = "v1.39.0";

let failures = 0;

function abort(message) {
  console.error(`✗ Contract drift: ${message}`);
  process.exit(1);
}

function verdict(ok, passed, failed, details) {
  if (ok) {
    console.log(`✓ ${passed}`);
    return;
  }
  failures += 1;
  console.error(`✗ ${failed}`);
  if (details) console.error(details);
}

function usage() {
  console.log(
    [
      "Usage: node scripts/check-contract-drift.mjs [options]",
      "",
      "  --base <ref>            compare the contracts with the baseline ref (default: main)",
      "  --self-test             prove that the gate catches a breaking change",
      "  --document <path>       compare a published document with the contract",
      "  --live <url>            fetch the published document and compare it with the contract",
      "  --snapshot <path>       the stored snapshot of the published document",
      "  --write-snapshot <path> write the fetched document as the snapshot",
      "  --contract <path>       the contract of the comparison (default: contracts/consigport-api.yaml)",
      "  --oasdiff <path>        the oasdiff binary (default: the one in PATH)",
    ].join("\n"),
  );
}

function parseArguments(argv) {
  const options = {
    base: null,
    selfTest: false,
    document: null,
    live: null,
    snapshot: null,
    writeSnapshot: null,
    contract: DEFAULT_CONTRACT,
    oasdiff: null,
  };

  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index];
    const value = () => {
      index += 1;
      if (index >= argv.length) abort(`the option ${argument} needs a value.`);
      return argv[index];
    };

    switch (argument) {
      case "--base":
        options.base = value();
        break;
      case "--self-test":
        options.selfTest = true;
        break;
      case "--document":
        options.document = value();
        break;
      case "--live":
        options.live = value();
        break;
      case "--snapshot":
        options.snapshot = value();
        break;
      case "--write-snapshot":
        options.writeSnapshot = value();
        break;
      case "--contract":
        options.contract = value();
        break;
      case "--oasdiff":
        options.oasdiff = value();
        break;
      case "--help":
      case "-h":
        usage();
        process.exit(0);
        break;
      default:
        abort(`unknown option: ${argument}.`);
    }
  }

  return options;
}

/* -------------------------------------------------------------------------- */
/* The oasdiff binary                                                         */
/* -------------------------------------------------------------------------- */

function cacheRoot() {
  const base = process.env.XDG_CACHE_HOME ?? path.join(os.homedir(), ".cache");
  return path.join(base, "consigport-platform");
}

function downloadOasdiff() {
  const version = process.env.OASDIFF_VERSION ?? DEFAULT_OASDIFF_VERSION;
  const platform = process.platform === "darwin" ? "darwin" : "linux";
  const architecture = process.arch === "arm64" ? "arm64" : "amd64";
  const directory = path.join(cacheRoot(), "oasdiff", version);
  const binary = path.join(directory, "oasdiff");

  if (fs.existsSync(binary)) return binary;

  const url =
    `https://github.com/oasdiff/oasdiff/releases/download/${version}` +
    `/oasdiff_${version.replace(/^v/, "")}_${platform}_${architecture}.tar.gz`;

  return (async () => {
    console.log(`· downloading ${url}`);
    const response = await fetch(url);
    if (!response.ok) abort(`the download of oasdiff ${version} failed with HTTP ${response.status}.`);

    fs.mkdirSync(directory, { recursive: true });
    const archive = path.join(directory, "oasdiff.tar.gz");
    fs.writeFileSync(archive, Buffer.from(await response.arrayBuffer()));

    const extracted = spawnSync("tar", ["-xzf", archive, "-C", directory], { encoding: "utf8" });
    if (extracted.status !== 0) {
      abort(`the extraction of oasdiff failed: ${(extracted.stderr ?? "").trim()}`);
    }
    fs.chmodSync(binary, 0o755);
    fs.rmSync(archive, { force: true });
    return binary;
  })();
}

async function resolveOasdiff(explicit) {
  const asked = explicit ?? process.env.OASDIFF_BIN;
  if (asked) {
    if (!fs.existsSync(asked)) abort(`the oasdiff binary ${asked} does not exist.`);
    return asked;
  }

  const inPath = spawnSync("which", ["oasdiff"], { encoding: "utf8" });
  if (inPath.status === 0) return inPath.stdout.trim();

  return downloadOasdiff();
}

function runOasdiff(binary, args) {
  const result = spawnSync(binary, args, { encoding: "utf8" });
  if (result.error) abort(`the oasdiff binary could not run: ${result.error.message}`);
  return {
    status: result.status,
    stdout: result.stdout ?? "",
    stderr: result.stderr ?? "",
  };
}

function describe(result) {
  return `${result.stdout}${result.stderr}`.trim();
}

/* -------------------------------------------------------------------------- */
/* The baseline of the breaking-change diff                                   */
/* -------------------------------------------------------------------------- */

function readBaseline(ref, contract, workDirectory) {
  const candidates = [ref, `origin/${ref}`];

  for (const candidate of candidates) {
    const shown = spawnSync("git", ["show", `${candidate}:${contract}`], {
      cwd: ROOT,
      encoding: "utf8",
    });
    if (shown.status === 0) return writeBaseline(shown.stdout, contract, workDirectory);
  }

  const fetched = spawnSync("git", ["fetch", "--quiet", "origin", ref], { cwd: ROOT, encoding: "utf8" });
  if (fetched.status === 0) {
    const shown = spawnSync("git", ["show", `FETCH_HEAD:${contract}`], {
      cwd: ROOT,
      encoding: "utf8",
    });
    if (shown.status === 0) return writeBaseline(shown.stdout, contract, workDirectory);
  }

  return null;
}

function writeBaseline(content, contract, workDirectory) {
  const file = path.join(workDirectory, path.basename(contract));
  fs.writeFileSync(file, content, "utf8");
  return file;
}

/* -------------------------------------------------------------------------- */
/* The comparison of the documents                                            */
/* -------------------------------------------------------------------------- */

const asMap = (value) =>
  value !== null && typeof value === "object" && !Array.isArray(value) ? value : {};

const asArray = (value) => (Array.isArray(value) ? value : []);

const asText = (value) => (value === null || value === undefined ? "" : String(value));

const asBool = (value) => value === true;

const asTexts = (value) => asArray(value).map((item) => String(item));

/** Follows a local `$ref` inside the document. */
const resolveReference = (node, document) => {
  const reference = asMap(node).$ref;
  if (typeof reference !== "string" || !reference.startsWith("#/")) return asMap(node);

  let current = document;
  for (const step of reference.slice(2).split("/")) {
    if (current === null || typeof current !== "object") return asMap(node);
    current = current[step];
  }
  return asMap(current);
};

/** Merges the branches of an `allOf` into one schema. */
const flattenAllOf = (schema, document) => {
  const source = asMap(schema);
  if (!source.allOf) return source;

  const properties = {};
  const required = new Set();

  for (const branch of asArray(source.allOf)) {
    const resolved = flattenAllOf(resolveReference(asMap(branch), document), document);
    Object.assign(properties, asMap(resolved.properties));
    for (const name of asTexts(resolved.required)) required.add(name);
  }

  return { type: "object", properties, required: [...required].sort() };
};

const canonicalType = (schema, document) => {
  const source = asMap(schema);
  const type = source.type;

  if (typeof type === "string") return type;
  if (Array.isArray(type)) {
    const name = type.find((item) => item !== "null");
    return name === undefined ? "null" : String(name);
  }
  if (source.oneOf) {
    for (const branch of asArray(source.oneOf)) {
      const resolved = flattenAllOf(resolveReference(asMap(branch), document), document);
      const name = canonicalType(resolved, document);
      if (name !== "null") return name;
    }
  }
  if (source.properties) return "object";
  if (source.items) return "array";
  return "unknown";
};

const canonicalProperty = (raw, document) => {
  const schema = resolveReference(asMap(raw), document);

  if (schema.oneOf) {
    for (const branch of asArray(schema.oneOf)) {
      const resolved = flattenAllOf(resolveReference(asMap(branch), document), document);
      if (canonicalType(resolved, document) !== "null") return canonicalProperty(resolved, document);
    }
  }

  const property = { type: canonicalType(schema, document) };
  const values = asTexts(schema.enum);
  if (values.length > 0) property.enum = [...values].sort();
  return property;
};

const canonicalSchema = (raw, document) => {
  const schema = flattenAllOf(resolveReference(asMap(raw), document), document);
  const properties = {};

  for (const [name, value] of Object.entries(asMap(schema.properties))) {
    properties[name] = canonicalProperty(value, document);
  }

  return {
    type: canonicalType(schema, document),
    required: asTexts(schema.required).sort(),
    properties,
  };
};

const canonicalParameters = (parameters, document) => {
  const canonical = new Set();

  for (const parameter of asArray(parameters)) {
    const resolved = resolveReference(asMap(parameter), document);
    canonical.add(
      `${asText(resolved.name)} (${asText(resolved.in)}, required: ${asBool(resolved.required)})`,
    );
  }

  return canonical;
};

/** The schema of the first media type of a body. */
const schemaOfBody = (body) => {
  const content = asMap(asMap(body).content);
  for (const media of Object.values(content)) {
    return asMap(media).schema;
  }
  return null;
};

const compareDocuments = (contract, document) => {
  const differences = [];
  comparePaths(asMap(contract.paths), asMap(document.paths), contract, document, differences);
  return differences;
};

const comparePaths = (contractPaths, documentPaths, contract, document, differences) => {
  for (const [route, value] of Object.entries(contractPaths)) {
    const contractPath = asMap(value);
    const documentPath = asMap(documentPaths[route]);

    if (Object.keys(documentPath).length === 0) {
      differences.push(`the route ${route} is missing in the published document`);
      continue;
    }

    for (const [method, operation] of Object.entries(contractPath)) {
      const documentOperation = asMap(documentPath[method]);

      if (Object.keys(documentOperation).length === 0) {
        differences.push(`the operation ${method.toUpperCase()} ${route} is missing`);
        continue;
      }

      compareOperation(
        `${method.toUpperCase()} ${route}`,
        asMap(operation),
        documentOperation,
        contract,
        document,
        differences,
      );
    }

    for (const method of Object.keys(documentPath)) {
      if (!Object.hasOwn(contractPath, method)) {
        differences.push(
          `the published document declares ${method.toUpperCase()} ${route}, and the contract does not`,
        );
      }
    }
  }

  for (const route of Object.keys(documentPaths)) {
    if (!Object.hasOwn(contractPaths, route)) {
      differences.push(`the published document declares the route ${route}, and the contract does not`);
    }
  }
};

const compareOperation = (label, contractOperation, documentOperation, contract, document, differences) => {
  const contractId = asText(contractOperation.operationId);
  const documentId = asText(documentOperation.operationId);

  if (contractId !== documentId) {
    differences.push(
      `${label}: the operationId is ${documentId} and the contract declares ${contractId}`,
    );
  }

  compareParameters(
    label,
    asArray(contractOperation.parameters),
    asArray(documentOperation.parameters),
    contract,
    document,
    differences,
  );
  compareRequestBody(
    label,
    contractOperation.requestBody,
    documentOperation.requestBody,
    contract,
    document,
    differences,
  );
  compareResponses(
    label,
    asMap(contractOperation.responses),
    asMap(documentOperation.responses),
    contract,
    document,
    differences,
  );
};

const compareParameters = (label, contractParameters, documentParameters, contract, document, differences) => {
  const expected = canonicalParameters(contractParameters, contract);
  const actual = canonicalParameters(documentParameters, document);

  for (const parameter of expected) {
    if (!actual.has(parameter)) differences.push(`${label}: the parameter ${parameter} is missing`);
  }
  for (const parameter of actual) {
    if (!expected.has(parameter)) {
      differences.push(`${label}: the parameter ${parameter} is not in the contract`);
    }
  }
};

const compareRequestBody = (label, contractBody, documentBody, contract, document, differences) => {
  if (contractBody === undefined || contractBody === null) {
    if (documentBody !== undefined && documentBody !== null) {
      differences.push(`${label}: the published document declares a request body`);
    }
    return;
  }
  if (documentBody === undefined || documentBody === null) {
    differences.push(`${label}: the request body is missing`);
    return;
  }

  const contractRequest = resolveReference(asMap(contractBody), contract);
  const documentRequest = asMap(documentBody);

  if (asBool(contractRequest.required) !== asBool(documentRequest.required)) {
    differences.push(
      `${label}: the request body is required: ${asBool(documentRequest.required)}`,
    );
  }

  compareSchemas(
    `${label} request body`,
    schemaOfBody(contractRequest),
    schemaOfBody(documentRequest),
    contract,
    document,
    differences,
  );
};

const compareResponses = (label, contractResponses, documentResponses, contract, document, differences) => {
  for (const status of Object.keys(contractResponses)) {
    const documentResponse = asMap(documentResponses[status]);

    if (Object.keys(documentResponse).length === 0) {
      differences.push(`${label}: the answer ${status} is missing`);
      continue;
    }

    compareSchemas(
      `${label} answer ${status}`,
      schemaOfBody(resolveReference(asMap(contractResponses[status]), contract)),
      schemaOfBody(documentResponse),
      contract,
      document,
      differences,
    );
  }

  for (const status of Object.keys(documentResponses)) {
    if (!Object.hasOwn(contractResponses, status)) {
      differences.push(
        `${label}: the published document answers ${status}, and the contract does not`,
      );
    }
  }
};

const compareSchemas = (label, contractSchema, documentSchema, contract, document, differences) => {
  if (contractSchema === undefined || contractSchema === null) return;
  if (documentSchema === undefined || documentSchema === null) {
    differences.push(`${label}: the schema of the body is missing`);
    return;
  }

  const expected = canonicalSchema(contractSchema, contract);
  const actual = canonicalSchema(documentSchema, document);

  if (expected.type !== actual.type) {
    differences.push(`${label}: the type is ${actual.type} and the contract declares ${expected.type}`);
  }

  if (expected.required.join(",") !== actual.required.join(",")) {
    differences.push(
      `${label}: the fields always present are [${actual.required.join(", ")}] and the contract declares [${expected.required.join(", ")}]`,
    );
  }

  for (const [name, property] of Object.entries(expected.properties)) {
    const counterpart = actual.properties[name];
    if (counterpart === undefined) {
      differences.push(`${label}: the field ${name} is missing`);
      continue;
    }
    if (property.type !== counterpart.type) {
      differences.push(
        `${label}: the field ${name} is ${counterpart.type} and the contract declares ${property.type}`,
      );
    }
    const expectedEnum = property.enum ?? [];
    const actualEnum = counterpart.enum ?? [];
    if (expectedEnum.join(",") !== actualEnum.join(",")) {
      differences.push(
        `${label}: the field ${name} accepts [${actualEnum.join(", ")}] and the contract declares [${expectedEnum.join(", ")}]`,
      );
    }
  }

  for (const name of Object.keys(actual.properties)) {
    if (!Object.hasOwn(expected.properties, name)) {
      differences.push(`${label}: the field ${name} is not in the contract`);
    }
  }
};

/* -------------------------------------------------------------------------- */
/* The gates                                                                  */
/* -------------------------------------------------------------------------- */

function compareWithBaseline(binary, ref, contract, workDirectory) {
  const baseline = readBaseline(ref, contract, workDirectory);

  if (baseline === null) {
    console.log(
      `· ${contract}: the ref ${ref} has no contract yet; the breaking-change diff is skipped.`,
    );
    return;
  }

  const result = runOasdiff(binary, [
    "breaking",
    baseline,
    path.resolve(ROOT, contract),
    "--fail-on",
    "ERR",
  ]);

  verdict(
    result.status === 0,
    `${contract}: no breaking change against ${ref}`,
    `${contract}: breaking change against ${ref}`,
    result.status === 0 ? null : describe(result),
  );
}

function readContract(binary, contract) {
  const absolute = path.resolve(ROOT, contract);
  if (!fs.existsSync(absolute)) abort(`the contract ${contract} does not exist.`);

  const result = runOasdiff(binary, ["flatten", "--format", "json", absolute]);
  if (result.status !== 0) abort(`the contract ${contract} could not be read: ${describe(result)}`);

  try {
    return JSON.parse(result.stdout);
  } catch (error) {
    abort(`the contract ${contract} could not be parsed: ${error.message}`);
  }
}

function compareDocumentWithContract(binary, documentPath, contract, document) {
  const published = document ?? JSON.parse(fs.readFileSync(path.resolve(ROOT, documentPath), "utf8"));
  const expected = readContract(binary, contract);
  const differences = compareDocuments(expected, published);

  verdict(
    differences.length === 0,
    `${documentPath} honours ${contract}`,
    `${documentPath} does not honour ${contract}`,
    differences.length === 0
      ? null
      : differences.map((difference) => `  - ${difference}`).join("\n"),
  );
}

function compareDocumentsWithEachOther(firstPath, secondPath, first, second) {
  const published = first ?? JSON.parse(fs.readFileSync(path.resolve(ROOT, firstPath), "utf8"));
  const expected = second ?? JSON.parse(fs.readFileSync(path.resolve(ROOT, secondPath), "utf8"));
  const differences = compareDocuments(expected, published);

  verdict(
    differences.length === 0,
    `${firstPath} honours ${secondPath}`,
    `${firstPath} does not honour ${secondPath}`,
    differences.length === 0
      ? null
      : differences.map((difference) => `  - ${difference}`).join("\n"),
  );
}

async function fetchDocument(url) {
  let response;
  try {
    response = await fetch(url);
  } catch (error) {
    abort(`the document ${url} could not be fetched: ${error.message}`);
  }
  if (!response.ok) abort(`the document ${url} answered HTTP ${response.status}.`);
  return response.json();
}

function proveTheGateIsArmed(binary) {
  const base = path.join(ROOT, FIXTURE_BASE);
  const breaking = path.join(ROOT, FIXTURE_BREAKING);

  if (!fs.existsSync(base) || !fs.existsSync(breaking)) {
    abort(`the fixtures ${FIXTURE_BASE} and ${FIXTURE_BREAKING} are missing.`);
  }

  const caught = runOasdiff(binary, ["breaking", base, breaking, "--fail-on", "ERR"]);
  verdict(
    caught.status !== 0,
    "the gate is armed: the fixture with the removed answer fails the comparison",
    "the gate is not armed: the fixture with the removed answer passed the comparison",
    caught.status !== 0 ? null : describe(caught),
  );

  const quiet = runOasdiff(binary, ["breaking", base, base, "--fail-on", "ERR"]);
  verdict(
    quiet.status === 0,
    "the gate is quiet: the fixture compared with itself passes",
    "the gate is noisy: the fixture compared with itself failed",
    quiet.status === 0 ? null : describe(quiet),
  );
}

/* -------------------------------------------------------------------------- */
/* The entry point                                                            */
/* -------------------------------------------------------------------------- */

async function main() {
  const options = parseArguments(process.argv.slice(2));
  const asked =
    options.base !== null ||
    options.selfTest ||
    options.document !== null ||
    options.live !== null ||
    options.snapshot !== null ||
    options.writeSnapshot !== null;

  const binary = await resolveOasdiff(options.oasdiff);
  const workDirectory = fs.mkdtempSync(path.join(os.tmpdir(), "consigport-contract-"));

  try {
    if (!asked) {
      options.base = "main";
      options.selfTest = true;
      if (fs.existsSync(path.resolve(ROOT, DEFAULT_SNAPSHOT))) options.document = DEFAULT_SNAPSHOT;
    }

    if (options.base !== null) {
      for (const contract of CONTRACTS) {
        compareWithBaseline(binary, options.base, contract, workDirectory);
      }
    }

    if (options.selfTest) proveTheGateIsArmed(binary);

    if (options.document !== null) {
      compareDocumentWithContract(binary, options.document, options.contract);
    }

    if (options.live !== null) {
      const published = await fetchDocument(options.live);

      if (options.writeSnapshot !== null) {
        fs.mkdirSync(path.dirname(path.resolve(ROOT, options.writeSnapshot)), { recursive: true });
        fs.writeFileSync(
          path.resolve(ROOT, options.writeSnapshot),
          `${JSON.stringify(published, null, 2)}\n`,
          "utf8",
        );
        console.log(`· the snapshot ${options.writeSnapshot} was written.`);
      }

      compareDocumentWithContract(binary, options.live, options.contract, published);

      if (options.snapshot !== null) {
        compareDocumentsWithEachOther(options.live, options.snapshot, published);
      }
    }
  } finally {
    fs.rmSync(workDirectory, { recursive: true, force: true });
  }

  if (failures > 0) {
    console.error(`✗ Contract drift: ${failures} check(s) failed.`);
    process.exit(1);
  }

  console.log("✓ Contract drift: every check passed.");
}

await main();
