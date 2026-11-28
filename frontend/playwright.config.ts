import { defineConfig, devices } from '@playwright/test'

/**
 * The end-to-end configuration.
 *
 * The suite runs against the production build served by `vite preview`: the
 * same artifact the container publishes. The API, the Central and the
 * PostgreSQL are started by the CI job — the suite only owns the browser and
 * the server of the interface.
 *
 * The stack is shared, so the tests run one at a time: the Central keeps the
 * state of the operation and a parallel flow would change what the other one
 * sees.
 */
export default defineConfig({
  testDir: './e2e',
  // The mock of the Central keeps the state of the operations in memory: two
  // flows at the same time would compete for the same beneficiary.
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  // The stack is cold when the suite starts: the first call to the Central
  // takes seconds while the JVM warms up.
  timeout: 120_000,
  expect: {
    timeout: 30_000,
  },
  reporter: process.env.CI
    ? [
        ['html', { open: 'never' }],
        ['list'],
      ]
    : [['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://127.0.0.1:5173',
    locale: 'pt-BR',
    // The proof of a failure is the trace, the screenshot and the video: the
    // three say what the browser saw when the test broke.
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  // The interface is the production build: the suite starts the preview server
  // and waits for it to answer.
  webServer: {
    command: 'npm run preview -- --port 5173 --host 127.0.0.1 --strictPort',
    url: 'http://127.0.0.1:5173/',
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
})
