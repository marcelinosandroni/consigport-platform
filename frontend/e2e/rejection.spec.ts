import { expect, test } from '@playwright/test'

import {
  authorize,
  PROTOCOL,
  REJECTED_AGREEMENT,
  REJECTED_BENEFICIARY,
  selectInstallments,
  submitOperation,
} from './support/journey.ts'

/**
 * The rejection of the Central.
 *
 * The rejection is a decision of the Central, not a failure of the platform:
 * the operation is created, the answer arrives later, and the screen says
 * what happened and why.
 */

test.describe('A rejeição da Central', () => {
  test('mostra o motivo quando a Central recusa a operação', async ({ page }) => {
    await authorize(page, REJECTED_BENEFICIARY, REJECTED_AGREEMENT)
    await selectInstallments(page, ['PAR-101'])

    const protocol = await submitOperation(page)
    expect(protocol).toMatch(PROTOCOL)

    await page.getByRole('link', { name: 'Acompanhar operação' }).click()
    await expect(page).toHaveURL(/\/operacoes\?operationId=/)

    // The screen names the state, the reason the Central gave and the end of
    // the operation.
    await expect(page.getByText('Rejeitada', { exact: true })).toBeVisible({ timeout: 60_000 })
    await expect(page.getByText('MARGIN_INSUFFICIENT', { exact: true })).toBeVisible()
    await expect(page.getByText('Operação encerrada.', { exact: true })).toBeVisible()
  })
})
