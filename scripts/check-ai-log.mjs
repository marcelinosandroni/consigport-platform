#!/usr/bin/env node
/**
 * AI Usage Log gate.
 *
 *   node scripts/check-ai-log.mjs           # structure only
 *   node scripts/check-ai-log.mjs --staged  # pre-commit: work staged without the log
 *
 * In a pull request, AI_LOG_BASE_SHA enables the coverage rule: if the PR changes
 * project files, it must also change the log and add at least one new session.
 */
import { execFileSync } from "node:child_process";
import fs from "node:fs";

const LOG_PATH = "docs/ai-harness/AI_USAGE_LOG.md";
const STAGED = process.argv.includes("--staged");
const MAX_SESSIONS_REPORTED = 3;

const WORK_PATHS = [
  /^backend\//,
  /^central-convenios-mock\//,
  /^frontend\//,
  /^contracts\//,
  /^scripts\//,
  /^\.github\//,
  /^SDD\//,
  /^\.mvn\//,
  /^pom\.xml$/,
  /^mvnw(\.cmd)?$/,
  /^compose\.ya?ml$/,
  /^\.env\.example$/,
  /^\.gitignore$/,
  /^\.gitattributes$/,
  /^package\.json$/,
];

function git(args, { allowFail = false } = {}) {
  try {
    return execFileSync("git", args, {
      encoding: "utf8",
      stdio: ["ignore", "pipe", "pipe"],
    }).trim();
  } catch {
    if (allowFail) return "";
    throw new Error(`git ${args.join(" ")} failed.`);
  }
}

function fail(message) {
  console.error(`✗ AI usage log: ${message}`);
  process.exit(1);
}

function pass(message) {
  console.log(`✓ AI usage log: ${message}`);
  process.exit(0);
}

function isWorkPath(file) {
  return WORK_PATHS.some((pattern) => pattern.test(file));
}

function splitLines(output) {
  return output ? output.split("\n").filter(Boolean) : [];
}

function countSessions(text) {
  return (text.match(/^## Sessão #\d{2} — .+$/gm) || []).length;
}

function checkStructure(text) {
  if (!text.includes("# Diário de Uso de IA")) {
    fail("missing the 'Diário de Uso de IA' title.");
  }

  const placeholders = [/\[Nome/i, /\bTODO\b/, /\bTBD\b/, /preencher/i, /exemplo@/i];
  for (const placeholder of placeholders) {
    if (placeholder.test(text)) {
      fail(`placeholder found (${placeholder}); write the real value.`);
    }
  }

  const sessions = [];
  const heading = /^## Sessão #(\d{2}) — (.+)$/gm;
  let match;
  while ((match = heading.exec(text)) !== null) {
    sessions.push({
      number: Number(match[1]),
      title: match[2].trim(),
      start: match.index,
    });
  }

  if (sessions.length === 0) {
    fail("no '## Sessão #NN — Título' block found.");
  }

  sessions.forEach((session, index) => {
    const expected = index + 1;
    if (session.number !== expected) {
      fail(
        `session numbering must be contiguous: expected #${String(expected).padStart(2, "0")}, found #${String(session.number).padStart(2, "0")}.`,
      );
    }
    if (!session.title) {
      fail(`session #${session.number} has an empty title.`);
    }

    const end = sessions[index + 1]?.start ?? text.length;
    const block = text.slice(session.start, end);

    const date = block.match(/\*\*Data:\*\*\s*(\d{4}-\d{2}-\d{2})/)?.[1];
    if (!date) fail(`session #${session.number} is missing '- **Data:** YYYY-MM-DD'.`);
    if (Number.isNaN(Date.parse(`${date}T00:00:00Z`))) {
      fail(`session #${session.number} has an invalid date: ${date}.`);
    }
    session.date = date;

    const required = [
      ["Agente / Modelo", /\*\*Agente \/ Modelo:\*\*\s*\S+/],
      ["Branch", /\*\*Branch:\*\*\s*\S+/],
      ["Objetivo", /\*\*Objetivo:\*\*\s*\S+/],
    ];
    for (const [label, pattern] of required) {
      if (!pattern.test(block)) {
        fail(`session #${session.number} is missing '- **${label}:** ...'.`);
      }
    }

    if (!/^### Atividades Realizadas:/m.test(block)) {
      fail(`session #${session.number} is missing '### Atividades Realizadas:'.`);
    }
    if (!/^\d+\.\s+\S/m.test(block)) {
      fail(`session #${session.number} has no numbered activity.`);
    }
  });

  // Dates must not go backwards: the log is chronological.
  const dates = sessions.map((session) => session.date);
  for (let index = 1; index < dates.length; index += 1) {
    if (dates[index] < dates[index - 1]) {
      fail(
        `sessions are out of order: #${String(index + 1).padStart(2, "0")} (${dates[index]}) is before #${String(index).padStart(2, "0")} (${dates[index - 1]}).`,
      );
    }
  }

  return { sessions, dates };
}

function checkStaged() {
  const log = fs.existsSync(LOG_PATH) ? fs.readFileSync(LOG_PATH, "utf8") : "";
  const { sessions } = checkStructure(log);

  if (fs.existsSync(git(["rev-parse", "--git-path", "MERGE_HEAD"]))) {
    pass("merge commit, log update not required.");
  }

  const staged = splitLines(
    git(["diff", "--cached", "--name-only", "--diff-filter=ACMR"]),
  );
  const work = staged.filter(isWorkPath);
  if (work.length === 0) {
    pass("no project files staged.");
  }
  if (staged.includes(LOG_PATH)) {
    pass(`${work.length} project file(s) staged with the log (${sessions.length} sessions).`);
  }

  const baseRef = ["origin/develop", "origin/main"].find(
    (ref) => git(["rev-parse", "--verify", ref], { allowFail: true }) !== "",
  );
  if (!baseRef) {
    fail("cannot find origin/develop or origin/main; fetch before committing.");
  }
  const base = git(["merge-base", baseRef, "HEAD"], { allowFail: true });
  if (base) {
    const branch = splitLines(git(["diff", "--name-only", `${base}...HEAD`], { allowFail: true }));
    if (branch.includes(LOG_PATH)) {
      pass("log already updated in this branch.");
    }
  }

  fail(
    `staging ${work.length} project file(s) requires staging ${LOG_PATH} with a new session.`,
  );
}

function checkPullRequest(text, { sessions }) {
  const baseSha = (process.env.AI_LOG_BASE_SHA || "").trim();
  if (!baseSha) {
    pass(`${sessions.length} sessions, structure valid.`);
  }
  if (!/^[0-9a-f]{7,40}$/i.test(baseSha)) {
    fail(`AI_LOG_BASE_SHA is not a commit SHA: ${baseSha}.`);
  }

  const changed = splitLines(
    git(["diff", "--name-only", `${baseSha}...HEAD`], { allowFail: true }),
  );
  if (changed.length === 0) {
    pass(`${sessions.length} sessions, no diff against the base commit.`);
  }

  const work = changed.filter(isWorkPath);
  if (work.length === 0) {
    pass(`${sessions.length} sessions, no project files changed.`);
  }
  if (!changed.includes(LOG_PATH)) {
    fail(`this PR changes ${work.length} project file(s) but not ${LOG_PATH}.`);
  }

  const baseLog = git(["show", `${baseSha}:${LOG_PATH}`], { allowFail: true });
  const baseSessions = countSessions(baseLog);
  if (sessions.length <= baseSessions) {
    fail(
      `the log changed but no session was added (base: ${baseSessions}, head: ${sessions.length}).`,
    );
  }

  pass(
    `${sessions.length} sessions (+${sessions.length - baseSessions} in this PR), structure valid.`,
  );
}

const log = fs.existsSync(LOG_PATH) ? fs.readFileSync(LOG_PATH, "utf8") : "";
if (!log) fail(`${LOG_PATH} not found.`);

const structure = checkStructure(log);
if (STAGED) {
  checkStaged();
} else {
  checkPullRequest(log, structure);
}
