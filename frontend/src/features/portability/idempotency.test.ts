import { describe, expect, it } from 'vitest'

import { createIdempotencyKey, isUuid } from './idempotency.ts'

describe('createIdempotencyKey', () => {
  it('creates a UUID the API accepts', () => {
    const key = createIdempotencyKey()

    expect(isUuid(key)).toBe(true)
    expect(key).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i)
  })

  it('creates a different key every time', () => {
    const keys = new Set(Array.from({ length: 20 }, () => createIdempotencyKey()))

    expect(keys.size).toBe(20)
  })
})

describe('isUuid', () => {
  it('accepts a UUID', () => {
    expect(isUuid('11111111-1111-4111-8111-111111111111')).toBe(true)
  })

  it('refuses anything else', () => {
    expect(isUuid('')).toBe(false)
    expect(isUuid('not-a-uuid')).toBe(false)
    expect(isUuid('11111111-1111-4111-8111-11111111111')).toBe(false)
  })
})
