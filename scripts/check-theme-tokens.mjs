#!/usr/bin/env node
/**
 * Guards the design tokens against drift.
 *
 *   node scripts/check-theme-tokens.mjs
 *
 * `frontend/src/theme.css` must be a verbatim copy of `SDD/themes/matrix/tokens.css`.
 * A second copy with edited values is a second design system wearing the same name.
 */
import fs from "node:fs";

const SOURCE = "SDD/themes/matrix/tokens.css";
const COPY = "frontend/src/theme.css";

function fail(message) {
  console.error(`✗ Theme tokens: ${message}`);
  process.exit(1);
}

if (!fs.existsSync(SOURCE)) fail(`${SOURCE} not found.`);
if (!fs.existsSync(COPY)) fail(`${COPY} not found.`);

const source = fs.readFileSync(SOURCE, "utf8");
const copy = fs.readFileSync(COPY, "utf8");

// The repository stores both files with LF (see .gitattributes), but a Windows
// working tree may still hold CRLF. Line endings are not design drift.
const normalize = (text) => text.replace(/\r\n/g, "\n");

if (normalize(source) !== normalize(copy)) {
  fail(`${COPY} diverged from ${SOURCE}; copy the source again.`);
}

if (!source.includes("@theme")) {
  fail(`${SOURCE} does not declare a Tailwind @theme block.`);
}

console.log(`✓ Theme tokens: ${COPY} matches ${SOURCE} (line endings normalized).`);
