import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { Button, buttonClasses } from './button.tsx'

describe('Button', () => {
  it('renders a real button that does not submit by accident', () => {
    render(<Button>Authorize</Button>)

    const button = screen.getByRole('button', { name: 'Authorize' })
    expect(button).toHaveAttribute('type', 'button')
  })

  it('marks the busy state and refuses a second click', async () => {
    const user = userEvent.setup()
    let clicks = 0

    render(
      <Button loading onClick={() => (clicks += 1)}>
        Sending
      </Button>,
    )

    const button = screen.getByRole('button', { name: 'Sending' })
    expect(button).toHaveAttribute('aria-busy', 'true')
    expect(button).toBeDisabled()

    await user.click(button)
    expect(clicks).toBe(0)
  })

  it('keeps a visible focus ring on every variant', () => {
    const { rerender } = render(<Button variant="primary">Primary</Button>)
    expect(screen.getByRole('button')).toHaveClass('focus-visible:outline-primary')

    for (const variant of ['primary', 'secondary', 'ghost'] as const) {
      rerender(<Button variant={variant}>{variant}</Button>)
      expect(screen.getByRole('button')).toHaveClass('focus-visible:outline-2')
    }
  })
})

describe('buttonClasses', () => {
  it('keeps the tap target at 44px', () => {
    expect(buttonClasses()).toContain('min-h-11')
  })

  it('never carries a literal colour', () => {
    const classes = buttonClasses({ variant: 'primary' })

    expect(classes).not.toMatch(/#[0-9a-fA-F]{3,8}/)
    expect(classes).not.toMatch(/rgba?\(/)
  })
})
