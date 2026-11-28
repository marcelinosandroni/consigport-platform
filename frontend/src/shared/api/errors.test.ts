import { describe, expect, it } from "vitest";

import { API_ERROR_CODES, ApiError, isApiError, isApiErrorCode, parseApiError } from "./errors.ts";

const payload = {
  code: "IDEMPOTENCY_CONFLICT",
  message: "A chave de idempotência já foi usada com outro conteúdo.",
  locale: "pt-BR",
  timestamp: "2026-11-28T10:00:00Z",
  path: "/api/v1/portability-operations",
  details: [{ field: "installmentIds", message: "A parcela PAR-101 não é elegível." }],
};

describe("parseApiError", () => {
  it("keeps the stable code, the localized message and the details", () => {
    const error = parseApiError(payload);

    expect(error.code).toBe("IDEMPOTENCY_CONFLICT");
    expect(error.message).toBe("A chave de idempotência já foi usada com outro conteúdo.");
    expect(error.locale).toBe("pt-BR");
    expect(error.timestamp).toBe("2026-11-28T10:00:00Z");
    expect(error.path).toBe("/api/v1/portability-operations");
    expect(error.details).toEqual([
      { field: "installmentIds", message: "A parcela PAR-101 não é elegível." },
    ]);
  });

  it("degrades an unknown code to INTERNAL_ERROR", () => {
    const error = parseApiError({ ...payload, code: "SOMETHING_NEW" });

    expect(error.code).toBe("INTERNAL_ERROR");
  });

  it("degrades a body that is not an object to INTERNAL_ERROR", () => {
    const error = parseApiError("gateway timeout");

    expect(error.code).toBe("INTERNAL_ERROR");
    expect(error.message.length).toBeGreaterThan(0);
    expect(error.details).toEqual([]);
  });

  it("keeps the empty details when the field is missing", () => {
    expect(parseApiError({ ...payload, details: undefined }).details).toEqual([]);
  });

  it("ignores malformed detail entries", () => {
    const error = parseApiError({ ...payload, details: ["installmentIds", { field: "amount" }] });

    expect(error.details).toEqual([{ field: "amount", message: "" }]);
  });
});

describe("isApiErrorCode", () => {
  it("accepts every code of the public contract", () => {
    for (const code of API_ERROR_CODES) {
      expect(isApiErrorCode(code)).toBe(true);
    }
  });

  it("rejects a code that is not part of the contract", () => {
    expect(isApiErrorCode("TEAPOT")).toBe(false);
    expect(isApiErrorCode(42)).toBe(false);
  });
});

describe("isApiError", () => {
  it("recognises a parsed error", () => {
    expect(isApiError(parseApiError(payload))).toBe(true);
    expect(isApiError(new Error("boom"))).toBe(false);
  });

  it("carries the code on the error instance", () => {
    const error = new ApiError({
      code: "VALIDATION_ERROR",
      message: "Payload inválido.",
      locale: "pt-BR",
      timestamp: "",
      path: "",
      details: [],
    });

    expect(error).toBeInstanceOf(Error);
    expect(error.code).toBe("VALIDATION_ERROR");
  });
});
