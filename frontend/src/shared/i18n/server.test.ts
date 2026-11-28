import { describe, expect, it } from 'vitest'

import { createTranslator, translate } from './server.ts'

describe('translate', () => {
  it('translates a key for each supported locale', () => {
    expect(translate('pt-BR', 'nav.home')).toBe('Início')
    expect(translate('en', 'nav.home')).toBe('Home')
    expect(translate('es', 'nav.home')).toBe('Inicio')
  })

  it('reads a nested key', () => {
    expect(translate('pt-BR', 'errors.IDEMPOTENCY_CONFLICT')).toBe(
      'Esta operação já foi enviada com outro conteúdo.',
    )
  })

  it('returns the key when it is unknown everywhere', () => {
    expect(translate('pt-BR', 'does.not.exist')).toBe('does.not.exist')
  })
})

describe('createTranslator', () => {
  it('binds the locale to the translator', () => {
    const t = createTranslator('es')
    expect(t('common.retry')).toBe('Intentar de nuevo')
  })
})
