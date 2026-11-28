import type { StatusTone } from '../ui/status-badge.ts'

/**
 * The status vocabulary of the platform.
 *
 * The tone is a cross-cutting visual decision — what a state *means* belongs
 * to the feature that owns it, but how the platform paints "finished" or
 * "refused" is the same everywhere. The label, on the other hand, stays in the
 * catalog of the feature: the agenda says "Elegível", the operations say
 * "Concluída".
 */

export type PlatformStatus =
  | 'OPEN'
  | 'CLOSED'
  | 'CREATED'
  | 'PENDING'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'REJECTED'
  | 'INTEGRATION_ERROR'

const TONES: Record<PlatformStatus, StatusTone> = {
  OPEN: 'success',
  CLOSED: 'neutral',
  CREATED: 'progress',
  PENDING: 'progress',
  CONFIRMED: 'progress',
  COMPLETED: 'success',
  REJECTED: 'error',
  INTEGRATION_ERROR: 'error',
}

/**
 * The catalog key of each status.
 *
 * The label belongs to the catalog of the feature that owns the meaning of the
 * state — the agenda says "Elegível", the operations say "Concluída" — but the
 * vocabulary is the same everywhere, so the key lives with the tone. A status
 * the platform does not know has no key: the screen shows the raw state
 * instead of inventing a label for it.
 */
const LABEL_KEYS: Record<PlatformStatus, string> = {
  OPEN: 'agenda.statusOpen',
  CLOSED: 'agenda.statusClosed',
  CREATED: 'operations.created',
  PENDING: 'operations.pending',
  CONFIRMED: 'operations.confirmed',
  COMPLETED: 'operations.completed',
  REJECTED: 'operations.rejected',
  INTEGRATION_ERROR: 'operations.integrationError',
}

/**
 * The tone of a status.
 *
 * A status the platform does not know is `neutral`: the word still shows up,
 * the badge stays quiet instead of crying wolf.
 */
export const statusTone = (status: string): StatusTone => TONES[status as PlatformStatus] ?? 'neutral'

/** The catalog key of a status, or `undefined` when the platform ignores it. */
export const statusLabelKey = (status: string): string | undefined =>
  LABEL_KEYS[status as PlatformStatus]
