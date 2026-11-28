import { expect, test } from '@playwright/test'

import {
  authorize,
  HAPPY_AGREEMENT,
  HAPPY_BENEFICIARY,
  UNAUTHORIZED_BENEFICIARY,
} from './support/journey.ts'

/**
 * The consent: the first door of the journey.
 *
 * The screen only exists to register the opt-in of the beneficiary. The test
 * proves that the consent opens the agenda — and that the agenda stays shut
 * for whoever never gave the consent.
 */

test.describe('O consentimento do beneficiário', () => {
  test('registra o opt-in e abre a agenda do beneficiário', async ({ page }) => {
    await authorize(page, HAPPY_BENEFICIARY, HAPPY_AGREEMENT)

    // The agenda of the beneficiary is on the screen, with the three
    // installments the Central knows.
    const agenda = page.getByRole('region', { name: 'Parcelas do beneficiário' })
    await expect(agenda.getByText('PAR-001')).toBeVisible()
    await expect(agenda.getByText('PAR-002')).toBeVisible()
    await expect(agenda.getByText('PAR-003')).toBeVisible()
  })

  test('recusa a agenda de quem não deu consentimento', async ({ page }) => {
    await page.goto(`/agenda?beneficiaryId=${UNAUTHORIZED_BENEFICIARY}`)

    // The screen names the state and offers the way out.
    await expect(page.getByText('Autorize o convênio para continuar.')).toBeVisible()
    await expect(page.getByRole('link', { name: 'Autorizar convênio' })).toBeVisible()
  })
})
