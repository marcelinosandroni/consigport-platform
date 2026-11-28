import { expect, type Page } from '@playwright/test'

/**
 * The steps of the journey, shared by the specs.
 *
 * The suite drives the interface the way a person does: it reads the labels
 * the reader sees, it clicks the buttons the screen offers, and it waits for
 * the state the screen announces. Nothing here reaches for a class name — a
 * test that knows the markup breaks the day the design changes.
 */

/** The beneficiary of the happy path: three eligible installments. */
export const HAPPY_BENEFICIARY = 'BEN-001'

/** The agreement of the happy path. */
export const HAPPY_AGREEMENT = 'AGR-001'

/** The beneficiary the Central rejects: the identifier ends in `-REJ`. */
export const REJECTED_BENEFICIARY = 'BEN-REJ'

/** The agreement of the rejected path. */
export const REJECTED_AGREEMENT = 'AGR-003'

/** The beneficiary nobody granted a consent to. */
export const UNAUTHORIZED_BENEFICIARY = 'BEN-MARGIN'

/** The protocol the Central gives to an operation: `PRT-yyyyMMdd-NNNN`. */
export const PROTOCOL = /PRT-\d{8}-\d{4}/

/**
 * Registers the opt-in and waits for the agenda to take its place.
 *
 * @param page the page of the test
 * @param beneficiaryId the beneficiary who grants the consent
 * @param agreementId the agreement covered by the consent
 */
export async function authorize(
  page: Page,
  beneficiaryId: string,
  agreementId: string,
): Promise<void> {
  await page.goto('/autorizacao')

  await page.getByLabel('Beneficiário').fill(beneficiaryId)
  await page.getByLabel('Convênio').fill(agreementId)
  await page.getByRole('button', { name: 'Autorizar' }).click()

  // The consent carries the reader to the agenda of the beneficiary.
  await expect(page).toHaveURL(new RegExp(`/agenda\\?beneficiaryId=${beneficiaryId}`))
  await expect(page.getByRole('region', { name: 'Parcelas do beneficiário' })).toBeVisible()
}

/**
 * Chooses the installments and carries the reader to the portability screen.
 *
 * @param page the page of the test
 * @param installmentIds the installments the reader chooses
 */
export async function selectInstallments(
  page: Page,
  installmentIds: readonly string[],
): Promise<void> {
  for (const installmentId of installmentIds) {
    await page.getByLabel(`Selecionar a parcela ${installmentId}`).check()
  }

  await page.getByRole('button', { name: 'Continuar para a portabilidade' }).click()
  await expect(page).toHaveURL(/\/portabilidade\?/)
  await expect(page.getByLabel('Banco')).toBeVisible()
}

/**
 * Sends the operation and waits for the answer of the server.
 *
 * The action answers through the RSC channel, and the answer of a replayed
 * attempt is fast enough to slip between two observations of the screen: the
 * test waits for the request itself, not for a sign of the pending state.
 *
 * @param page the page of the test
 */
export async function sendOperation(page: Page): Promise<void> {
  const submit = page.getByRole('button', { name: 'Enviar operação' })
  const answered = page.waitForResponse(
    (response) =>
      response.request().method() === 'POST' && response.url().includes('/portabilidade_.rsc'),
  )

  await submit.click()
  await answered
}

/**
 * Fills the receiving account and sends the operation.
 *
 * @param page the page of the test
 * @returns the protocol the Central gave to the operation
 */
export async function submitOperation(page: Page): Promise<string> {
  await page.getByLabel('Banco').fill('001')
  await page.getByLabel('Agência').fill('1234')
  await page.getByLabel('Conta').fill('56789-0')
  await page.getByLabel('Credor').fill('ConsigPort')

  await sendOperation(page)

  const answer = page.getByRole('status')
  await expect(answer).toContainText('Operação criada', { timeout: 30_000 })

  const protocol = PROTOCOL.exec((await answer.textContent()) ?? '')?.[0]
  expect(protocol, 'the operation must come back with a protocol').toBeDefined()

  return protocol ?? ''
}
