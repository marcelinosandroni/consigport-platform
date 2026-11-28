import { render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { Table, TableBody, TableCell, TableHeader, TableHead, TableRow } from './table.tsx'

describe('Table', () => {
  it('names the scroll region', () => {
    render(
      <Table label="Installments">
        <TableHead>
          <TableRow>
            <TableHeader>Installment</TableHeader>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            <TableCell>PAR-101</TableCell>
          </TableRow>
        </TableBody>
      </Table>,
    )

    const region = screen.getByRole('region', { name: 'Installments' })
    expect(region).toHaveAttribute('tabindex', '0')
  })

  it('reads the headers as column headers', () => {
    render(
      <Table label="Installments">
        <TableHead>
          <TableRow>
            <TableHeader>Installment</TableHeader>
            <TableHeader>Amount</TableHeader>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            <TableCell>PAR-101</TableCell>
            <TableCell>R$ 120,00</TableCell>
          </TableRow>
        </TableBody>
      </Table>,
    )

    const row = screen.getByRole('row', { name: /Installment/ })
    expect(within(row).getAllByRole('columnheader')).toHaveLength(2)
  })
})
