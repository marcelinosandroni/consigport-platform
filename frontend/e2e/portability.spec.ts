import { expect, test } from '@playwright/test'

import {
  authorize,
  HAPPY_AGREEMENT,
  HAPPY_BENEFICIARY,
  PROTOCOL,
  selectInstallments,
  sendOperation,
  submitOperation,
} from './support/journey.ts'

/**
 * The happy path of the portability.
 *
 * The reader chooses the installments, sends the operation and follows it
 * until the Central answers. The second test proves the idempotence the
 * contract promises: the same attempt is one operation, never two.
 */

test.describe('A portabilidade', () => {
  test('leva a operação até a confirmação da Central', async ({ page }) => {
    await authorize(page, HAPPY_BENEFICIARY, HAPPY_AGREEMENT)
    await selectInstallments(page, ['PAR-001', 'PAR-002'])

    const protocol = await submitOperation(page)
    expect(protocol).toMatch(PROTOCOL)

    // The panel of the operation follows the state of the operation.
    await page.getByRole('link', { name: 'Acompanhar operação' }).click()
    await expect(page).toHaveURL(/\/operacoes\?operationId=/)

    // The Central confirms after a couple of seconds and the screen polls
    // until the operation ends.
    await expect(page.getByText('Concluída', { exact: true })).toBeVisible({ timeout: 60_000 })
    await expect(page.getByText('Operação encerrada.', { exact: true })).toBeVisible()

    // The trail of the operation is append-only: it shows every state the
    // operation went through, in the order the events were written.
    await page.getByRole('link', { name: 'Ver a trilha de auditoria' }).click()
    await expect(page).toHaveURL(/\/auditoria\?operationId=/)

    const trail = page.getByRole('list').filter({ hasText: 'Operação criada' })
    await expect(trail.getByText('Operação criada')).toBeVisible()
    await expect(trail.getByText('Operação concluída')).toBeVisible()
  })

  test('não cria uma segunda operação quando a tentativa se repete', async ({ page }) => {
    await authorize(page, HAPPY_BENEFICIARY, HAPPY_AGREEMENT)
    await selectInstallments(page, ['PAR-003'])

    const first = await submitOperation(page)

    // The same payload carries the same key: the API replays the operation
    // instead of creating another one.
    await sendOperation(page)

    await expect(page.getByRole('status')).toContainText(first)
  })
})
