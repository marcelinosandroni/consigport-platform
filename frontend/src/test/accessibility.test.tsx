import { readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { resolveRoute } from '../app/router.tsx'
import { Shell } from '../app/shell.tsx'

/**
 * The keyboard and the screen reader.
 *
 * The `DESIGN.md` §4b asks for a proof; this is the proof that does not need
 * a browser: the shell is rendered, the focus is moved with the keyboard and
 * every control is measured by the rule the design states.
 *
 * The geometric proof — the real box of every element — needs a browser and
 * arrives with the E2E suite (4.3). What is proven here is the structure: the
 * skip link, the landmarks, the name of every control, the floor of the tap
 * target and the visible focus ring.
 */

const renderShell = (pathname: string) => {
  const url = new URL(pathname, 'http://localhost')

  return render(
    <Shell pathname={url.pathname} locale='pt-BR'>
      {resolveRoute(url, 'pt-BR')}
    </Shell>,
  )
}

/** The name a screen reader announces for a control. */
const accessibleName = (element: HTMLElement): string => {
  const ariaLabel = element.getAttribute('aria-label')
  if (ariaLabel !== null && ariaLabel.trim().length > 0) return ariaLabel.trim()

  const labelledBy = element.getAttribute('aria-labelledby')
  if (labelledBy !== null) {
    const text = labelledBy
      .split(' ')
      .map((id) => document.getElementById(id)?.textContent ?? '')
      .join(' ')
      .trim()

    if (text.length > 0) return text
  }

  if (
    element instanceof HTMLInputElement ||
    element instanceof HTMLSelectElement ||
    element instanceof HTMLTextAreaElement
  ) {
    const labels = Array.from(element.labels ?? [])
    const text = labels
      .map((label) => label.textContent ?? '')
      .join(' ')
      .trim()

    if (text.length > 0) return text

    const title = element.getAttribute('title')
    if (title !== null && title.trim().length > 0) return title.trim()

    return ''
  }

  return (element.textContent ?? '').trim()
}

/** The controls a reader can reach: the visible, the interactive ones. */
const interactiveControls = (container: HTMLElement): HTMLElement[] =>
  Array.from(
    container.querySelectorAll<HTMLElement>('a, button, input, select, textarea'),
  ).filter((element) => {
    if (element instanceof HTMLInputElement && element.type === 'hidden') return false
    if (element.getAttribute('aria-hidden') === 'true') return false
    if (element.getAttribute('tabindex') === '-1') return false

    return true
  })

describe('the landmarks of the platform', () => {
  it('names the regions the reader navigates by', () => {
    renderShell('/')

    expect(screen.getByRole('banner')).toBeInTheDocument()
    expect(screen.getByRole('main')).toBeInTheDocument()
    expect(screen.getByRole('contentinfo')).toBeInTheDocument()
    expect(screen.getByRole('navigation', { name: 'Navegação principal' })).toBeInTheDocument()
  })

  it('marks the current page in the navigation', () => {
    renderShell('/autorizacao')

    const navigation = screen.getByRole('navigation')
    const current = within(navigation).getByRole('link', { current: 'page' })

    expect(current).toHaveAttribute('href', '/autorizacao')
  })
})

describe('the skip link', () => {
  it('is the first stop of the keyboard and names a target that exists', async () => {
    const user = userEvent.setup()
    renderShell('/')

    await user.tab()

    const skip = document.activeElement
    expect(skip).toBeInstanceOf(HTMLAnchorElement)
    expect(skip).toHaveTextContent('Pular para o conteúdo')
    expect(skip).toHaveAttribute('href', '#conteudo')

    const target = document.getElementById('conteudo')
    expect(target).not.toBeNull()
    expect(target?.tagName).toBe('MAIN')
  })

  it('is out of the way until the keyboard asks for it', () => {
    const styles = readFileSync(
      path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', 'styles.css'),
      'utf8',
    )

    expect(styles).toContain('.skip-link {')
    expect(styles).toContain('top: -100px')
    expect(styles).toContain('.skip-link:focus {')
  })
})

describe('the tab order', () => {
  it('walks from the skip link to the navigation', async () => {
    const user = userEvent.setup()
    renderShell('/')

    await user.tab()
    expect(document.activeElement).toHaveTextContent('Pular para o conteúdo')

    await user.tab()
    expect(document.activeElement).toHaveTextContent('ConsigPort')

    await user.tab()
    expect(document.activeElement).toHaveAttribute('href', '/')
  })

  it('reaches the form controls of the consent page', async () => {
    const user = userEvent.setup()
    renderShell('/autorizacao')

    for (let stop = 0; stop < 12; stop += 1) {
      await user.tab()

      const active = document.activeElement
      if (active instanceof HTMLInputElement && active.name === 'beneficiaryId') {
        expect(active).toHaveAttribute('id', 'beneficiaryId')
        return
      }
    }

    throw new Error('The beneficiary field was not reachable with the keyboard')
  })
})

describe('the controls of the screen', () => {
  const routes = [
    '/',
    '/autorizacao',
    '/portabilidade?beneficiaryId=BEN-1&agreementId=AGR-1&installments=INS-1',
  ]

  it('gives every control a name', () => {
    for (const route of routes) {
      const { container, unmount } = renderShell(route)

      const unnamed = interactiveControls(container)
        .filter((element) => accessibleName(element).length === 0)
        .map((element) => element.outerHTML.slice(0, 80))

      expect(unnamed, `unnamed controls in ${route}`).toEqual([])

      unmount()
    }
  })

  it('keeps every tap target at 44px', () => {
    for (const route of routes) {
      const { container, unmount } = renderShell(route)

      const small = Array.from(container.querySelectorAll<HTMLElement>('a, button'))
        .filter((element) => {
          // The skip link is drawn by the stylesheet: its height comes from
          // the `min-height` of the rule, not from a Tailwind token.
          if (element.classList.contains('skip-link')) return false

          const token = Array.from(element.classList).find((entry) =>
            /^min-h-(\d+)$/.exec(entry),
          )

          if (token === undefined) return true

          return Number(/^min-h-(\d+)$/.exec(token)?.[1] ?? 0) < 11
        })
        .map((element) => `${element.tagName} "${(element.textContent ?? '').trim().slice(0, 30)}"`)

      expect(small, `small targets in ${route}`).toEqual([])

      unmount()
    }
  })

  it('shows a visible focus ring on every control', () => {
    for (const route of routes) {
      const { container, unmount } = renderShell(route)

      const withoutRing = interactiveControls(container)
        .filter((element) => !element.classList.contains('focus-visible:outline-primary'))
        .map((element) => element.outerHTML.slice(0, 80))

      expect(withoutRing, `controls without a focus ring in ${route}`).toEqual([])

      unmount()
    }
  })

  it('links every field to its label', () => {
    renderShell('/autorizacao')

    const beneficiary = screen.getByLabelText('Beneficiário')

    expect(beneficiary).toHaveAttribute('id', 'beneficiaryId')
    expect(beneficiary).toHaveAttribute('required')
  })
})
