import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'

import { InstallmentTable, type AgendaInstallment } from './installment-table.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'

const installments: AgendaInstallment[] = [
  {
    installmentId: 'PAR-001',
    agreementId: 'AGR-001',
    amount: 450,
    discountDate: '2026-12-05',
    status: 'OPEN',
  },
  {
    installmentId: 'PAR-002',
    agreementId: 'AGR-001',
    amount: 300,
    discountDate: '2027-01-05',
    status: 'OPEN',
  },
  {
    installmentId: 'PAR-003',
    agreementId: 'AGR-001',
    amount: 120.5,
    discountDate: '2026-11-20',
    status: 'CLOSED',
  },
]

const renderTable = () =>
  render(
    <I18nProvider>
      <InstallmentTable installments={installments} beneficiaryId='BEN-001' />
    </I18nProvider>,
  )

/** The total is the polite region: it announces itself when it moves. */
const total = (): HTMLElement => {
  const element = document.querySelector('[aria-live="polite"]')
  if (element === null) {
    throw new Error('The total of the selection was not found')
  }
  return element as HTMLElement
}

const checkbox = (installmentId: string) =>
  screen.getByRole('checkbox', { name: `Selecionar a parcela ${installmentId}` })

beforeEach(() => {
  window.history.replaceState(null, '', '/')
})

describe('InstallmentTable', () => {
  it('lists the installments of the beneficiary', () => {
    renderTable()

    expect(screen.getByText('PAR-001')).toBeInTheDocument()
    expect(screen.getByText('PAR-003')).toBeInTheDocument()
    expect(screen.getAllByText('Elegível')).toHaveLength(2)
    expect(screen.getByText('Fechada')).toBeInTheDocument()
  })

  it('only lets an open installment be chosen', () => {
    renderTable()

    expect(checkbox('PAR-001')).toBeEnabled()
    expect(checkbox('PAR-002')).toBeEnabled()
    expect(checkbox('PAR-003')).toBeDisabled()
  })

  it('recalculates the total with the selection', async () => {
    const user = userEvent.setup()
    renderTable()

    expect(total()).toHaveTextContent(/R\$\s*0,00/)

    await user.click(checkbox('PAR-001'))
    expect(total()).toHaveTextContent(/R\$\s*450,00/)

    await user.click(checkbox('PAR-002'))
    expect(total()).toHaveTextContent(/R\$\s*750,00/)

    await user.click(checkbox('PAR-001'))
    expect(total()).toHaveTextContent(/R\$\s*300,00/)
  })

  it('counts the selected installments', async () => {
    const user = userEvent.setup()
    renderTable()

    expect(screen.getByText('Nenhuma parcela selecionada.')).toBeInTheDocument()

    await user.click(checkbox('PAR-001'))
    expect(screen.getByText('1 parcela selecionada')).toBeInTheDocument()

    await user.click(checkbox('PAR-002'))
    expect(screen.getByText('2 parcelas selecionadas')).toBeInTheDocument()
  })

  it('clears the selection', async () => {
    const user = userEvent.setup()
    renderTable()

    await user.click(checkbox('PAR-001'))
    await user.click(screen.getByRole('button', { name: 'Limpar seleção' }))

    expect(total()).toHaveTextContent(/R\$\s*0,00/)
    expect(checkbox('PAR-001')).not.toBeChecked()
  })

  it('carries the selection to the portability step', async () => {
    const user = userEvent.setup()
    renderTable()

    await user.click(checkbox('PAR-001'))
    await user.click(checkbox('PAR-002'))
    await user.click(screen.getByRole('button', { name: 'Continuar para a portabilidade' }))

    expect(window.location.pathname).toBe('/portabilidade')
    expect(window.location.search).toContain('beneficiaryId=BEN-001')
    expect(window.location.search).toContain('agreementId=AGR-001')
    expect(window.location.search).toContain('installments=PAR-001%2CPAR-002')
  })

  it('cannot continue without a selection', () => {
    renderTable()

    expect(screen.getByRole('button', { name: 'Continuar para a portabilidade' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Limpar seleção' })).toBeDisabled()
  })
})
