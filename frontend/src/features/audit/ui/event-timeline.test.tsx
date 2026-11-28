import { render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { EventTimeline } from './event-timeline.tsx'
import type { OperationEvent } from './event-item.tsx'
import { createTranslator } from '../../../shared/i18n/server.ts'

const t = createTranslator('pt-BR')

const event = (init: Partial<OperationEvent> & { eventId: number }): OperationEvent => ({
  previousStatus: null,
  newStatus: 'CREATED',
  eventType: 'OPERATION_CREATED',
  details: 'Operação criada com 2 parcelas',
  rawPayload: null,
  createdAt: '2026-11-28T10:00:05Z',
  ...init,
})

describe('EventTimeline', () => {
  it('shows every event in chronological order', () => {
    render(
      <EventTimeline
        locale='pt-BR'
        t={t}
        events={[
          event({ eventId: 3, newStatus: 'COMPLETED', eventType: 'OPERATION_COMPLETED', details: 'A Central fechou a operação', createdAt: '2026-11-28T10:00:11Z' }),
          event({ eventId: 1, createdAt: '2026-11-28T10:00:05Z' }),
          event({
            eventId: 2,
            previousStatus: 'CREATED',
            newStatus: 'PENDING',
            eventType: 'OPERATION_SUBMITTED',
            details: 'Central aceitou a operação',
            createdAt: '2026-11-28T10:00:06Z',
          }),
        ]}
      />,
    )

    const items = screen.getAllByRole('listitem')

    expect(items).toHaveLength(3)
    expect(within(items[0] as HTMLElement).getByText('Operação criada')).toBeInTheDocument()
    expect(within(items[1] as HTMLElement).getByText('Operação enviada')).toBeInTheDocument()
    expect(within(items[2] as HTMLElement).getByText('Operação concluída')).toBeInTheDocument()
    expect(within(items[2] as HTMLElement).getByText('A Central fechou a operação')).toBeInTheDocument()
  })

  it('shows the type, the statuses, the detail and the time of an event', () => {
    render(
      <EventTimeline
        locale='pt-BR'
        t={t}
        events={[
          event({
            eventId: 1,
            previousStatus: 'PENDING',
            newStatus: 'REJECTED',
            eventType: 'OPERATION_REJECTED',
            details: 'A Central rejeitou a operação por margem insuficiente',
            createdAt: '2026-11-28T10:00:05Z',
          }),
        ]}
      />,
    )

    expect(screen.getByText('Operação rejeitada')).toBeInTheDocument()
    expect(
      screen.getByText('A Central rejeitou a operação por margem insuficiente'),
    ).toBeInTheDocument()
    expect(screen.getByText(/Status anterior:.*Em andamento/)).toBeInTheDocument()
    expect(screen.getByText(/Novo status:.*Rejeitada/)).toBeInTheDocument()
    expect(screen.getByText('28 de nov. de 2026, 10:00')).toBeInTheDocument()
  })

  it('says that the trail starts when there is no previous status', () => {
    render(
      <EventTimeline
        locale='pt-BR'
        t={t}
        events={[event({ eventId: 1, previousStatus: null, newStatus: 'CREATED' })]}
      />,
    )

    expect(screen.getByText(/Status anterior:.*Início/)).toBeInTheDocument()
  })

  it('shows the answer of the Central when the event carries one', () => {
    render(
      <EventTimeline
        locale='pt-BR'
        t={t}
        events={[
          event({
            eventId: 2,
            previousStatus: 'CREATED',
            newStatus: 'PENDING',
            eventType: 'OPERATION_SUBMITTED',
            rawPayload: { protocolo: 'PRT-20261128-0001' },
          }),
        ]}
      />,
    )

    expect(screen.getByText('Resposta da Central')).toBeInTheDocument()
    expect(screen.getByText(/"protocolo": "PRT-20261128-0001"/)).toBeInTheDocument()
  })

  it('offers no way to edit or delete an event', () => {
    render(
      <EventTimeline locale='pt-BR' t={t} events={[event({ eventId: 1 })]} />,
    )

    expect(screen.queryByRole('button')).not.toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })

  it('formats the time in the language of the reader', () => {
    render(
      <EventTimeline
        locale='en'
        t={createTranslator('en')}
        events={[event({ eventId: 1 })]}
      />,
    )

    expect(screen.getByText('Nov 28, 2026, 10:00 AM')).toBeInTheDocument()
    expect(screen.getByText('Operation created')).toBeInTheDocument()
  })
})
