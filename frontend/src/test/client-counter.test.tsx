import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { ClientCounter } from '../client.tsx'

describe('ClientCounter', () => {
  it('increments the counter on click', async () => {
    const user = userEvent.setup()
    render(<ClientCounter />)

    const button = screen.getByRole('button', { name: /client counter/i })
    expect(button).toHaveTextContent('Client Counter: 0')

    await user.click(button)

    expect(button).toHaveTextContent('Client Counter: 1')
  })
})
