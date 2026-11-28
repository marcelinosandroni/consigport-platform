/**
 * The key of the attempt.
 *
 * The API identifies an attempt by a UUID: the same key with the same content
 * returns the same operation, and the same key with another content is a
 * conflict. The key therefore belongs to the *attempt* — not to the render, not
 * to the click — because a retry of a failed submission must not create a
 * second operation.
 */

/** The shape of a UUID, in the version the API accepts. */
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

/**
 * Creates a UUID for the attempt.
 *
 * `crypto.randomUUID` needs a secure context; the fallback keeps the key a
 * valid UUID even when the page is served over plain HTTP.
 */
export const createIdempotencyKey = (): string => {
  if (typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  const bytes = crypto.getRandomValues(new Uint8Array(16))
  bytes[6] = (bytes[6] ?? 0) & 0x0f | 0x40
  bytes[8] = (bytes[8] ?? 0) & 0x3f | 0x80
  const hex = [...bytes].map((byte) => byte.toString(16).padStart(2, '0')).join('')

  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
}

/** Whether a value is a UUID the API accepts as an idempotency key. */
export const isUuid = (value: string): boolean => UUID.test(value)
