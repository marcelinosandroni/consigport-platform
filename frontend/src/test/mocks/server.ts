import { setupServer } from 'msw/node'

import { handlers } from './handlers.ts'

/** The fake API used by the tests: the request never leaves the process. */
export const server = setupServer(...handlers)
