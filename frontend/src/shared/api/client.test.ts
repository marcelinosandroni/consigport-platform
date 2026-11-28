import { describe, expect, it } from 'vitest'

import { DEFAULT_API_BASE_URL, resolveApiBaseUrl } from './client.ts'

describe('resolveApiBaseUrl', () => {
  it('uses the configured URL of the environment', () => {
    expect(resolveApiBaseUrl({ VITE_API_BASE_URL: 'https://api.consigport.example' })).toBe(
      'https://api.consigport.example',
    )
  })

  it('falls back to the local API when the environment says nothing', () => {
    expect(resolveApiBaseUrl({})).toBe(DEFAULT_API_BASE_URL)
  })

  it('falls back to the local API when the environment is blank', () => {
    expect(resolveApiBaseUrl({ VITE_API_BASE_URL: '   ' })).toBe(DEFAULT_API_BASE_URL)
  })

  it('removes the trailing slash so the paths never double it', () => {
    expect(resolveApiBaseUrl({ VITE_API_BASE_URL: 'https://api.consigport.example/' })).toBe(
      'https://api.consigport.example',
    )
  })
})
