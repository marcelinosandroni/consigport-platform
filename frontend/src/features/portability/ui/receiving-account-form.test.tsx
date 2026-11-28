import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'

import { ReceivingAccountForm } from './receiving-account-form.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const renderForm = () =>
  render(
    <I18nProvider>
      <ReceivingAccountForm
        beneficiaryId='BEN-001'
        agreementId='AGR-001'
        installmentIds={['PAR-001', 'PAR-002']}
      />
    </I18nProvider>,
  )

const fillAccount = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(screen.getByLabelText('Banco'), '001')
  await user.type(screen.getByLabelText('Agência'), '1234')
  await user.type(screen.getByLabelText('Conta'), '56789-0')
}

describe('ReceivingAccountForm', () => {
  it('names every field of the receiving account', () => {
    renderForm()

    expect(screen.getByLabelText('Banco')).toBeRequired()
    expect(screen.getByLabelText('Agência')).toBeRequired()
    expect(screen.getByLabelText('Conta')).toBeRequired()
    expect(screen.getByLabelText('Credor')).toHaveValue('ConsigPort')
  })

  it('creates the operation and shows the protocol', async () => {
    const user = userEvent.setup()
    renderForm()

    await fillAccount(user)
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))

    await waitFor(() => {
      expect(screen.getByText('PRT-20261128-0001')).toBeInTheDocument()
    })
    expect(screen.getByText(/R\$\s*750,00/)).toBeInTheDocument()
  })

  it('names the attempt with a key and keeps it for the same content', async () => {
    const keys: (string | null)[] = []

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        keys.push(request.headers.get('idempotency-key'))
      }
    })

    const user = userEvent.setup()
    renderForm()

    await fillAccount(user)
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))
    await waitFor(() => expect(screen.getByText('PRT-20261128-0001')).toBeInTheDocument())

    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))
    await waitFor(() => expect(keys.length).toBe(2))

    expect(keys[0]).toMatch(/^[0-9a-f-]{36}$/i)
    expect(keys[1]).toBe(keys[0])
  })

  it('names another attempt when the content changes', async () => {
    const keys: (string | null)[] = []

    server.events.on('request:start', ({ request }) => {
      if (request.method === 'POST') {
        keys.push(request.headers.get('idempotency-key'))
      }
    })

    const user = userEvent.setup()
    renderForm()

    await fillAccount(user)
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))
    await waitFor(() => expect(keys.length).toBe(1))

    await user.type(screen.getByLabelText('Conta'), '9')
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))
    await waitFor(() => expect(keys.length).toBe(2))

    expect(keys[1]).not.toBe(keys[0])
  })

  it('shows the conflict with the message of the server', async () => {
    server.use(
      http.post('http://localhost:8080/api/v1/portability-operations', () =>
        HttpResponse.json(
          {
            code: 'IDEMPOTENCY_CONFLICT',
            message: 'A chave de idempotência já foi usada com outro conteúdo.',
            locale: 'pt-BR',
            timestamp: '2026-11-28T10:00:06Z',
            path: '/api/v1/portability-operations',
            details: [],
          },
          { status: 409 },
        ),
      ),
    )

    const user = userEvent.setup()
    renderForm()

    await fillAccount(user)
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Esta operação já foi enviada com outro conteúdo.',
      )
    })
    expect(
      screen.getByText('A chave de idempotência já foi usada com outro conteúdo.'),
    ).toBeInTheDocument()
  })

  it('shows the ineligible installment with the message of the server', async () => {
    server.use(
      http.post('http://localhost:8080/api/v1/portability-operations', () =>
        HttpResponse.json(
          {
            code: 'INSTALLMENT_NOT_ELIGIBLE',
            message: 'A parcela PAR-003 não está aberta.',
            locale: 'pt-BR',
            timestamp: '2026-11-28T10:00:06Z',
            path: '/api/v1/portability-operations',
            details: [{ field: 'installmentIds', message: 'PAR-003 está CLOSED' }],
          },
          { status: 422 },
        ),
      ),
    )

    const user = userEvent.setup()
    renderForm()

    await fillAccount(user)
    await user.click(screen.getByRole('button', { name: 'Enviar operação' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Uma das parcelas não é elegível.')
    })
    expect(screen.getByText('A parcela PAR-003 não está aberta.')).toBeInTheDocument()
  })
})
