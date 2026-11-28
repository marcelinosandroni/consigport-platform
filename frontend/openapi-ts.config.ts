import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import openapiTS, { astToString } from "openapi-typescript";

/**
 * Generates the typed client contract from the public OpenAPI file.
 *
 * The source of truth is `contracts/consigport-api.yaml` — the same file the CI
 * compares the springdoc snapshot against. The generated file is committed, so a
 * broken contract breaks the build and not only the next generation.
 *
 * `openapi-typescript` v7 has no configuration file of its own, so this module is
 * both the configuration and the generator: `npm run --prefix frontend generate:types`.
 */
export const openApiTsConfig = {
  input: pathToFileURL(resolve(dirname(fileURLToPath(import.meta.url)), "../contracts/consigport-api.yaml")),
  output: resolve(dirname(fileURLToPath(import.meta.url)), "src/shared/api/schema.ts"),
} as const;

async function generateTypes(): Promise<void> {
  const ast = await openapiTS(openApiTsConfig.input);
  await mkdir(dirname(openApiTsConfig.output), { recursive: true });
  await writeFile(openApiTsConfig.output, astToString(ast), "utf8");
  console.log(`generated ${openApiTsConfig.output}`);
}

await generateTypes();
