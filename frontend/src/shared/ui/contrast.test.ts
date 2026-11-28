import { readFileSync } from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

/**
 * The contrast of the pairs the primitives use.
 *
 * The rule says "contrast that passes"; the tokens say which colours exist.
 * This test reads the tokens of `src/theme.css` — the copy of the Matrix
 * theme, guarded by `scripts/check-theme-tokens.mjs` — and proves that every
 * pair a component puts on the screen clears the WCAG AA ratio of 4.5:1.
 */

const tokens = readFileSync(path.resolve(process.cwd(), 'src/theme.css'), 'utf8')

const readToken = (name: string): string => {
  const match = tokens.match(new RegExp(`--color-${name}:\\s*(#[0-9a-fA-F]{6})`))
  if (match === null) {
    throw new Error(`The token --color-${name} is not defined in src/theme.css`)
  }
  return match[1] as string
}

const channels = (hex: string): [number, number, number] => {
  const value = hex.replace('#', '')
  return [
    parseInt(value.slice(0, 2), 16),
    parseInt(value.slice(2, 4), 16),
    parseInt(value.slice(4, 6), 16),
  ]
}

const luminance = (hex: string): number => {
  const [r = 0, g = 0, b = 0] = channels(hex).map((channel) => {
    const value = channel / 255
    return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

const contrast = (foreground: string, background: string): number => {
  const first = luminance(foreground)
  const second = luminance(background)
  return (Math.max(first, second) + 0.05) / (Math.min(first, second) + 0.05)
}

/** The background of a tinted badge: the accent over the surface, at 8%. */
const tinted = (accent: string, surface: string, alpha = 0.08): string => {
  const [ar = 0, ag = 0, ab = 0] = channels(accent)
  const [sr = 0, sg = 0, sb = 0] = channels(surface)
  const mix = (a: number, s: number) => Math.round(a * alpha + s * (1 - alpha))
  return `#${[mix(ar, sr), mix(ag, sg), mix(ab, sb)]
    .map((channel) => channel.toString(16).padStart(2, '0'))
    .join('')}`
}

const AA = 4.5

describe('the contrast of the Matrix tokens', () => {
  it('keeps the body text readable on every surface', () => {
    const surfaces = ['surface-base', 'surface-raised', 'surface-overlay', 'surface-container']

    for (const surface of surfaces) {
      expect(contrast(readToken('text-primary'), readToken(surface))).toBeGreaterThanOrEqual(AA)
      expect(contrast(readToken('text-secondary'), readToken(surface))).toBeGreaterThanOrEqual(AA)
    }
  })

  it('keeps the filled button readable', () => {
    expect(contrast(readToken('on-primary'), readToken('primary'))).toBeGreaterThanOrEqual(AA)
    expect(contrast(readToken('on-secondary'), readToken('secondary'))).toBeGreaterThanOrEqual(AA)
    expect(contrast(readToken('on-error'), readToken('error'))).toBeGreaterThanOrEqual(AA)
  })

  it('keeps the badges readable on the raised surface', () => {
    const surface = readToken('surface-raised')
    const accents = ['primary', 'secondary', 'tertiary', 'error']

    for (const accent of accents) {
      const colour = readToken(accent)
      expect(contrast(colour, surface)).toBeGreaterThanOrEqual(AA)
      expect(contrast(colour, tinted(colour, surface))).toBeGreaterThanOrEqual(AA)
    }
  })

  it('keeps the decorative token visible, as decoration', () => {
    // DESIGN §2: `text-muted` is tertiary/decorative — the placeholder and the
    // de-emphasised chrome, never a sentence. Its floor is the 3:1 of the
    // large-text and UI-contrast threshold, not the 4.5:1 of body copy.
    expect(contrast(readToken('text-muted'), readToken('surface-base'))).toBeGreaterThanOrEqual(3)
  })
})
