import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { Field, TextField } from './field.tsx'

describe('Field', () => {
  it('names the control with its label', () => {
    render(
      <Field id="beneficiary" label="Beneficiary">
        {(control) => <input {...control} />}
      </Field>,
    )

    expect(screen.getByLabelText('Beneficiary')).toBeInTheDocument()
  })

  it('links the hint and the error to the control', () => {
    render(
      <Field id="beneficiary" label="Beneficiary" hint="As printed on the card" error="Required">
        {(control) => <input {...control} />}
      </Field>,
    )

    const input = screen.getByLabelText('Beneficiary')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input.getAttribute('aria-describedby')).toContain('beneficiary-hint')
    expect(input.getAttribute('aria-describedby')).toContain('beneficiary-error')
    expect(screen.getByRole('alert')).toHaveTextContent('Required')
  })

  it('marks the required control', () => {
    render(
      <Field id="beneficiary" label="Beneficiary" required>
        {(control) => <input {...control} />}
      </Field>,
    )

    expect(screen.getByLabelText(/Beneficiary/)).toBeRequired()
  })
})

describe('TextField', () => {
  it('renders a labelled input with the hint', () => {
    render(<TextField id="agreement" label="Agreement" hint="The agreement number" />)

    const input = screen.getByLabelText('Agreement')
    expect(input).toHaveAttribute('id', 'agreement')
    expect(input.getAttribute('aria-describedby')).toBe('agreement-hint')
  })

  it('announces the validation error', () => {
    render(<TextField id="agreement" label="Agreement" error="The agreement is required" />)

    const input = screen.getByLabelText('Agreement')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByRole('alert')).toHaveTextContent('The agreement is required')
  })
})
