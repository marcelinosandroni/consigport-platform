import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { StatCard } from './stat-card.tsx'

describe('StatCard', () => {
  it('shows the label, the number and the annotation', () => {
    render(<StatCard label="Operações ativas" value="12" annotation="janela de 24 h" />)

    expect(screen.getByText('Operações ativas')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('janela de 24 h')).toBeInTheDocument()
  })

  it('marks a live status for assistive technology', () => {
    const { container } = render(<StatCard label="Operações ativas" value="12" status="live" />)

    expect(container.querySelector('[aria-hidden="true"]')).not.toBeNull()
  })
})
