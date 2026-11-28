'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, type ReactNode } from 'react'

/**
 * The remote cache of the browser.
 *
 * TanStack Query is the only place where the state of the server lives in the
 * browser: the islands ask it, and it decides when to ask the API again. The
 * client is created once per session — a new one on every render would throw
 * the cache away and restart the polling of every island.
 *
 * There is no ISR anywhere: the data of a transaction is read when the page is
 * rendered and refreshed by the island that owns it, never by a cache that
 * outlives the request.
 */
export function QueryProvider({ children }: { readonly children: ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            // One retry: a poll that fails once is a hiccup, not an outage.
            retry: 1,
            // The panel is the source of truth while it is open; a focus on
            // another window must not restart the schedule.
            refetchOnWindowFocus: false,
          },
        },
      }),
  )

  return <QueryClientProvider client={client}>{children}</QueryClientProvider>
}
