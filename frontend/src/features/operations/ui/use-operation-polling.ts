'use client'

import { useQuery } from '@tanstack/react-query'

import { createApiClient, resolveApiBaseUrl } from '../../../shared/api/client.ts'
import { ApiError, parseApiError } from '../../../shared/api/errors.ts'
import type { components } from '../../../shared/api/schema.ts'
import type { SupportedLocale } from '../../../shared/i18n/locales.ts'

/**
 * The status of the operation, followed by the browser.
 *
 * The Central answers asynchronously: the operation is created, the protocol
 * comes back, and the confirmation arrives later. The screen therefore polls
 * the public API until the operation reaches a terminal state — and this hook
 * is the only place where that schedule lives.
 */

/** The operation as the public contract publishes it. */
export type OperationDetail = components['schemas']['OperationDetail']

/** How often the platform asks the API about the operation. */
export const POLL_INTERVAL_MS = 2000

/**
 * The states that end the operation.
 *
 * `COMPLETED` and `REJECTED` are terminal; `INTEGRATION_ERROR` is resumable
 * and keeps polling, because the platform retries the integration by itself.
 */
export const TERMINAL_STATUSES = ['COMPLETED', 'REJECTED'] as const

export const isTerminalStatus = (status: string): boolean =>
  (TERMINAL_STATUSES as readonly string[]).includes(status)

/**
 * The instant the server asks for the next poll.
 *
 * `nextPollAt` belongs to the domain — the aggregate schedules its own
 * polling — but the public contract does not publish it yet. The hook reads
 * the field when the server sends it and never assumes it is there: the
 * contract stays untouched and the polling still respects the schedule the
 * server declares.
 */
export const readNextPollAt = (operation: OperationDetail): string | undefined => {
  const value = (operation as { readonly nextPollAt?: unknown }).nextPollAt
  return typeof value === 'string' && value.length > 0 ? value : undefined
}

/**
 * The delay until the next poll.
 *
 * `false` stops the polling: the operation is terminal, or the API says it
 * does not exist. Otherwise the wait is the base interval — two seconds — or
 * longer when the server asks for a later moment.
 */
export const pollDelay = (input: {
  readonly operation: OperationDetail | undefined
  readonly failure: ApiError | undefined
  readonly now: number
}): number | false => {
  const { operation, failure, now } = input

  if (failure?.code === 'OPERATION_NOT_FOUND') {
    return false
  }

  if (operation !== undefined && isTerminalStatus(operation.status)) {
    return false
  }

  const nextPollAt = operation === undefined ? undefined : readNextPollAt(operation)
  const earliest = nextPollAt === undefined ? Number.NaN : Date.parse(nextPollAt)

  if (Number.isNaN(earliest)) {
    return POLL_INTERVAL_MS
  }

  return Math.max(POLL_INTERVAL_MS, earliest - now)
}

export type OperationPolling = {
  readonly operation: OperationDetail | undefined
  readonly failure: ApiError | undefined
  readonly isFetching: boolean
  readonly refresh: () => void
}

/**
 * Follows the operation until it ends.
 *
 * The island asks the API through the generated client — the browser speaks
 * the contract, never a hand-written URL — and TanStack Query owns the
 * schedule: it polls every two seconds while the operation lives, stops at a
 * terminal state, and waits longer when the server declares `nextPollAt`.
 */
export function useOperationPolling({
  operationId,
  locale,
  initialOperation,
}: {
  readonly operationId: string
  readonly locale: SupportedLocale
  readonly initialOperation?: OperationDetail
}): OperationPolling {
  const query = useQuery({
    queryKey: ['operation', operationId, locale],
    queryFn: async () => {
      const client = createApiClient(resolveApiBaseUrl(), locale)
      const { data, error } = await client.GET('/api/v1/portability-operations/{operationId}', {
        params: { path: { operationId } },
      })

      if (data === undefined) {
        throw parseApiError(error)
      }

      return data
    },
    // The server already delivered the first state; it stays fresh for one
    // interval, and the schedule takes over from there.
    initialData: initialOperation,
    staleTime: POLL_INTERVAL_MS,
    retry: 1,
    refetchInterval: (current) =>
      pollDelay({
        operation: current.state.data,
        failure: current.state.error instanceof ApiError ? current.state.error : undefined,
        now: Date.now(),
      }),
  })

  return {
    operation: query.data,
    failure: query.error instanceof ApiError ? query.error : undefined,
    isFetching: query.isFetching,
    refresh: () => {
      void query.refetch()
    },
  }
}
