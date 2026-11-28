import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { StatusBadge } from './status-badge.tsx'

describe('StatusBadge', () => {
  it('names the state with its word', () => {
    render(<StatusBadge tone="success">Completed</StatusBadge>)

    expect(screen.getByText('Completed')).toBeInTheDocument()
  })

  it('hides the dot from the assistive technology', () => {
    const { container } = render(<StatusBadge tone="progress">In progress</StatusBadge>)

    const dot = container.querySelector('[aria-hidden="true"]')
    expect(dot).not.toBeNull()
    expect(dot?.tagName).toBe('SPAN')
  })

  it('can render without the dot', () => {
    const { container } = render(
      <StatusBadge tone="neutral" withDot={false}>
        Created
      </StatusBadge>,
    )

    expect(container.querySelector('[aria-hidden="true"]')).toBeNull()
  })

  it('never carries a literal colour', () => {
    const { container } = render(<StatusBadge tone="error">Rejected</StatusBadge>)

    expect(container.innerHTML).not.toMatch(/#[0-9a-fA-F]{3,8}/)
    expect(container.innerHTML).not.toMatch(/rgba?\(/)
  })
})
