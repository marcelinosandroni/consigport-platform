import { describe, expect, it } from 'vitest'

import { ApiError } from '../../../shared/api/errors.ts'
import {
  POLL_INTERVAL_MS,
  isTerminalStatus,
  pollDelay,
  readNextPollAt,
  type OperationDetail,
} from './use-operation-polling.ts'

const operation = (status: string, extra: Record<string, unknown> = {}): OperationDetail =>
  ({
    operationId: 'OP-1',
    protocol: 'PRT-20261128-0001',
    beneficiaryId: 'BEN-001',
    agreementId: 'AGR-001',
    status,
    totalAmount: 750,
    installments: [],
    assigneeCreditor: 'ConsigPort',
    receivingAccount: { bank: '001', branch: '1234', account: '56789-0' },
    rejectionReason: null,
    retryCount: 0,
    accountUpdatedAt: null,
    createdAt: '2026-11-28T10:00:05Z',
    updatedAt: '2026-11-28T10:00:05Z',
    ...extra,
  }) as OperationDetail

const failure = (code: string) =>
  new ApiError({
    code: code as ApiError['code'],
    message: 'Falha.',
    locale: 'pt-BR',
    timestamp: '2026-11-28T10:00:05Z',
    path: '/api/v1/portability-operations/OP-1',
    details: [],
  })

describe('isTerminalStatus', () => {
  it('ends the operation only on COMPLETED and REJECTED', () => {
    expect(isTerminalStatus('COMPLETED')).toBe(true)
    expect(isTerminalStatus('REJECTED')).toBe(true)
    expect(isTerminalStatus('PENDING')).toBe(false)
    expect(isTerminalStatus('CREATED')).toBe(false)
    expect(isTerminalStatus('CONFIRMED')).toBe(false)
  })

  it('keeps polling a resumable integration error', () => {
    expect(isTerminalStatus('INTEGRATION_ERROR')).toBe(false)
  })
})

describe('readNextPollAt', () => {
  it('reads the instant the server declares', () => {
    expect(readNextPollAt(operation('PENDING', { nextPollAt: '2026-11-28T10:00:15Z' }))).toBe(
      '2026-11-28T10:00:15Z',
    )
  })

  it('is absent when the contract does not publish it', () => {
    expect(readNextPollAt(operation('PENDING'))).toBeUndefined()
  })
})

describe('pollDelay', () => {
  it('waits two seconds between two polls', () => {
    expect(
      pollDelay({ operation: operation('PENDING'), failure: undefined, now: Date.now() }),
    ).toBe(POLL_INTERVAL_MS)
  })

  it('waits two seconds when there is nothing yet', () => {
    expect(pollDelay({ operation: undefined, failure: undefined, now: Date.now() })).toBe(
      POLL_INTERVAL_MS,
    )
  })

  it('stops on a terminal state', () => {
    expect(pollDelay({ operation: operation('COMPLETED'), failure: undefined, now: Date.now() })).toBe(
      false,
    )
    expect(pollDelay({ operation: operation('REJECTED'), failure: undefined, now: Date.now() })).toBe(
      false,
    )
  })

  it('stops when the API says the operation does not exist', () => {
    expect(
      pollDelay({
        operation: undefined,
        failure: failure('OPERATION_NOT_FOUND'),
        now: Date.now(),
      }),
    ).toBe(false)
  })

  it('keeps polling after a failure that is not a missing operation', () => {
    expect(
      pollDelay({ operation: undefined, failure: failure('INTERNAL_ERROR'), now: Date.now() }),
    ).toBe(POLL_INTERVAL_MS)
  })

  it('waits for the instant the server declares', () => {
    const now = Date.parse('2026-11-28T10:00:05Z')

    expect(
      pollDelay({
        operation: operation('PENDING', { nextPollAt: '2026-11-28T10:00:15Z' }),
        failure: undefined,
        now,
      }),
    ).toBe(10000)
  })

  it('never polls earlier than the base interval', () => {
    const now = Date.parse('2026-11-28T10:00:05Z')

    expect(
      pollDelay({
        operation: operation('PENDING', { nextPollAt: '2026-11-28T10:00:06Z' }),
        failure: undefined,
        now,
      }),
    ).toBe(POLL_INTERVAL_MS)
  })

  it('ignores an instant the server cannot mean', () => {
    const now = Date.parse('2026-11-28T10:00:05Z')

    expect(
      pollDelay({
        operation: operation('PENDING', { nextPollAt: 'not-an-instant' }),
        failure: undefined,
        now,
      }),
    ).toBe(POLL_INTERVAL_MS)
  })
})
