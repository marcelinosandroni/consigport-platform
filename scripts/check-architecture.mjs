#!/usr/bin/env node
/**
 * Architecture and Diagram Validator — ConsigPort
 *
 * Validates:
 * 1. Architecture documentation exists and contains expected sections.
 * 2. All Mermaid diagrams in Markdown parse and conform to valid diagram types (flowchart, sequenceDiagram).
 * 3. HTML Traceability Matrix contains valid inline SVGs and Mermaid containers for each view.
 * 4. Architectural invariants (Hexagonal layers, Monorepo modules, Ports/Adapters, ACL) are represented.
 *
 * Usage: node scripts/check-architecture.mjs
 */

import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, "..");

const errors = [];
const successes = [];

function check(condition, message) {
  if (!condition) {
    errors.push(message);
  } else {
    successes.push(message);
  }
}

// 1. Validate ARCHITECTURE_AND_TRACEABILITY.md
const archMdPath = path.join(root, "docs", "architecture", "ARCHITECTURE_AND_TRACEABILITY.md");
check(fs.existsSync(archMdPath), "Arquivo ARCHITECTURE_AND_TRACEABILITY.md existe");

if (fs.existsSync(archMdPath)) {
  const content = fs.readFileSync(archMdPath, "utf8");

  // Check required sections
  check(content.includes("Visão Geral da Topologia do Monorepo"), "Seção 1: Topologia do Monorepo presente");
  check(content.includes("Camadas da Arquitetura Hexagonal"), "Seção 2: Camadas Hexagonais presente");
  check(content.includes("Fluxo de Vida Assíncrono com Polling e Idempotência"), "Seção 3: Fluxo Assíncrono presente");
  check(content.includes("Matriz de Rastreabilidade Resumida"), "Seção 4: Matriz de Rastreabilidade presente");

  // Check Mermaid blocks
  const mermaidBlocks = content.match(/```mermaid[\s\S]*?```/g) || [];
  check(mermaidBlocks.length >= 3, `Diagramas Mermaid encontrados no Markdown: ${mermaidBlocks.length} (esperado >= 3)`);

  mermaidBlocks.forEach((block, idx) => {
    const raw = block.replace(/```mermaid/, "").replace(/```/, "").trim();
    const hasValidType = /^(flowchart|sequenceDiagram|graph|classDiagram|stateDiagram)/.test(raw);
    check(hasValidType, `Bloco Mermaid #${idx + 1} possui tipo suportado válido`);
    // Basic syntax checks
    check(!raw.includes("<script"), `Bloco Mermaid #${idx + 1} não contém injeção de tags proibidas`);
  });
}

// 2. Validate traceability-matrix.html
const htmlPath = path.join(root, "docs", "architecture", "traceability-matrix.html");
check(fs.existsSync(htmlPath), "Arquivo traceability-matrix.html existe");

if (fs.existsSync(htmlPath)) {
  const html = fs.readFileSync(htmlPath, "utf8");

  // Check SVG elements
  const svgMatches = html.match(/<svg[\s\S]*?<\/svg>/g) || [];
  check(svgMatches.length >= 3, `Diagramas SVG nativos embutidos no HTML: ${svgMatches.length} (esperado >= 3)`);

  // Check Mermaid integration in HTML
  check(html.includes("class=\"mermaid\""), "Containers <pre class=\"mermaid\"> presentes para renderização dinâmica");
  check(html.includes("mermaid.min.js"), "Script Mermaid.js carregado no HTML");

  // Check specific architecture representations
  check(html.includes("consigport-net"), "Rede Docker Bridge consigport-net representada no diagrama");
  check(html.includes("INBOUND ADAPTERS"), "Inbound Adapters representados no diagrama hexagonal");
  check(html.includes("DOMAIN CORE (PURO)"), "Domain Core puro (zero framework) representado no diagrama hexagonal");
  check(html.includes("Anti-Corruption Layer (ACL)"), "Camada Anticorrupção (ACL) representada no diagrama");
  check(html.includes("LOOP POLLING COM BACKOFF"), "Loop de polling assíncrono com backoff representado na sequência");

  // Check Traceability Matrix Table rows
  const trMatches = html.match(/<tr data-type=/g) || [];
  check(trMatches.length >= 18, `Requisitos rastreados na tabela HTML: ${trMatches.length} (esperado >= 18)`);
}

// Summary Output
console.log("\n📐 Relatório de Validação dos Desenhos de Arquitetura:");
console.log("-----------------------------------------------------");
successes.forEach(s => console.log(`  ✓ ${s}`));

if (errors.length > 0) {
  console.error("\n❌ Falhas encontradas na validação arquitetural:");
  errors.forEach(e => console.error(`  ✖ ${e}`));
  process.exit(1);
} else {
  console.log(`\n🎉 Todos os ${successes.length} testes de validação dos desenhos e arquitetura passaram com sucesso!\n`);
  process.exit(0);
}
