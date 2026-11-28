import { readFileSync, readdirSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

/**
 * The responsive audit, as code.
 *
 * The `DESIGN.md` §4b is the law: three viewports, four hard rules, and a
 * proof instead of a hope. The geometric proof — the real box of every
 * element — needs a browser, and the browser suite arrives with the E2E task
 * (4.3). What can be proven now, from the source, is proven now: the
 * breakpoints the layout uses, the content that is hidden, the floor of the
 * text and the rule that honours `prefers-reduced-motion`.
 */

/** The `src` of the app, resolved from this file and not from the shell. */
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

/** Every source file of the app, the tests aside. */
const sourceFiles = (): string[] => {
  const files: string[] = []

  const walk = (directory: string) => {
    for (const entry of readdirSync(directory, { withFileTypes: true })) {
      const full = path.join(directory, entry.name)

      if (entry.isDirectory()) {
        walk(full)
        continue
      }

      if (full.endsWith('.tsx') || full.endsWith('.css')) {
        files.push(full)
      }
    }
  }

  walk(root)

  return files
}

/** The source of the app without the tests: the tests may talk about rules. */
const applicationFiles = (): string[] =>
  sourceFiles().filter(
    (file) => !file.includes(`${path.sep}test${path.sep}`) && !file.endsWith('.test.tsx'),
  )

const readAll = (files: string[]): string =>
  files.map((file) => readFileSync(file, 'utf8')).join('\n')

describe('the breakpoints of the layout', () => {
  it('uses only the breakpoints the design names', () => {
    // DESIGN §4: mobile is below 768px, tablet is 768–1024px and desktop is
    // above 1024px. The `sm` breakpoint (640px) would split the mobile layout
    // in half before the design says so.
    const source = readAll(applicationFiles())
    const used = new Set(source.match(/(?:^|[\s'"])(sm|md|lg|xl|2xl):/g) ?? [])

    expect([...used].map((entry) => entry.trim()).sort()).toEqual(['lg:', 'md:'])
  })

  it('hides nothing', () => {
    // DESIGN §4b rule 4: no content that only exists above 768px. A hidden
    // element is a blank screen on the majority of the devices.
    const source = readAll(applicationFiles())
      // The hidden inputs and the decorative dots are not layout: they are
      // fields the form carries and marks the eye skips.
      .replace(/type=['"]hidden['"]/g, '')
      .replace(/aria-hidden=['"]true['"]/g, '')

    const hidden = source.match(/(?:^|[\s'"])hidden(?:[\s'"]|$)/g) ?? []

    expect(hidden).toEqual([])
  })

  it('keeps the wide content inside its own scroll box', () => {
    // DESIGN §4b rule 1: nothing pushes the page sideways. The two wide
    // things — the table of the agenda and the raw payload of the event —
    // scroll inside their own box, and no element asks for a fixed width.
    const source = readAll(applicationFiles())
    const fixed = source.match(/(?:^|[\s'"])(?:min-)?w-\[\d+px\](?:[\s'"]|$)/g) ?? []

    expect(fixed).toEqual([])
  })
})

describe('the floor of the text', () => {
  it('keeps every body text at 13px or more', () => {
    const tokens = readFileSync(path.resolve(root, 'theme.css'), 'utf8')
    const sizes = [...tokens.matchAll(/--text-([a-z0-9-]+):\s*(\d+)px/g)]
    const below = sizes
      .filter(([, , size]) => Number(size) < 13)
      // The chrome of the platform — the eyebrows, the table headers, the
      // tags — is 11px by design and is never body copy.
      .filter(([, name]) => name !== 'label-mono')
      .map(([, name, size]) => `${name}: ${size}px`)

    expect(sizes.length).toBeGreaterThan(0)
    expect(below).toEqual([])
  })
})

describe('the motion', () => {
  it('honours the reader who asks for less of it', () => {
    const styles = readFileSync(path.resolve(root, 'styles.css'), 'utf8')

    expect(styles).toContain('@media (prefers-reduced-motion: reduce)')
    expect(styles).toContain('transition-duration: 0.01ms')
  })
})
