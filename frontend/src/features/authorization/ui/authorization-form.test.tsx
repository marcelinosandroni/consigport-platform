import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest'

import { AuthorizationForm } from './authorization-form.tsx'
import { I18nProvider } from '../../../shared/i18n/provider.tsx'
import { server } from '../../../test/mocks/server.ts'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => window.history.replaceState(null, '', '/'))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const renderForm = () =>
  render(
    <I18nProvider>
      <AuthorizationForm />
    </I18nProvider>,
  )

describe('AuthorizationForm', () => {
  it('names both fields and asks for the consent', () => {
    renderForm()

    expect(screen.getByLabelText('Beneficiário')).toBeRequired()
    expect(screen.getByLabelText('Convênio')).toBeRequired()
    expect(screen.getByRole('button', { name: 'Autorizar' })).toBeInTheDocument()
  })

  it('registers the consent and confirms it', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Beneficiário'), 'BEN-200')
    await user.type(screen.getByLabelText('Convênio'), 'AGR-200')
    await user.click(screen.getByRole('button', { name: 'Autorizar' }))

    await waitFor(() => {
      expect(screen.getByRole('status')).toHaveTextContent('Convênio autorizado.')
    })
  })

  it('takes the visitor to the agenda of the beneficiary', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Beneficiário'), 'BEN-201')
    await user.type(screen.getByLabelText('Convênio'), 'AGR-201')
    await user.click(screen.getByRole('button', { name: 'Autorizar' }))

    await waitFor(() => {
      expect(window.location.pathname).toBe('/agenda')
      expect(window.location.search).toBe('?beneficiaryId=BEN-201')
    })
  })

  it('shows the localized error of the API', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Beneficiário'), 'BEN-000')
    await user.type(screen.getByLabelText('Convênio'), 'AGR-000')
    await user.click(screen.getByRole('button', { name: 'Autorizar' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Os dados enviados não passaram na validação.',
      )
    })
  })

  it('keeps the visitor on the form when the consent fails', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.type(screen.getByLabelText('Beneficiário'), 'BEN-000')
    await user.type(screen.getByLabelText('Convênio'), 'AGR-000')
    await user.click(screen.getByRole('button', { name: 'Autorizar' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
    })
    expect(window.location.pathname).not.toBe('/agenda')
  })
})
