# ADR-0001 — Hexagonal, DDD, ACL e mock isolado

- **Estado:** Aceita
- **Data:** 2026-11-23
- **Contexto do produto:** [`../../SDD/APP.md`](../../SDD/APP.md)
- **Arquitetura:** [`../../SDD/ARCHITECTURE.md`](../../SDD/ARCHITECTURE.md)

## Contexto

O ConsigPort recebe um contrato externo escrito em português (a Central de Convênios,
fiel ao edital) e expõe uma API própria em inglês. O domínio tem invariantes reais:
opt-in único por beneficiário e convênio, snapshot de parcelas, máquina de estados,
trilha imutável. Ao mesmo tempo, o repositório precisa provar o fluxo completo sem
depender de um serviço externo real.

Duas forças se opõem:

1. Um domínio rico precisa ser testável sem Spring, sem banco e sem HTTP.
2. O contrato externo muda por motivos alheios ao domínio — vocabulário, status e
   formatos em português.

## Decisão

1. **DDD tático** com quatro contextos delimitados: `authorization`, `agenda`,
   `portability` e `audit`. O agregado raiz de portabilidade concentra as invariantes e
   a máquina de estados.
2. **Hexagonal estrita** com três camadas: `domain`, `application`, `adapters`.
   - `domain` não importa Spring, JPA, Jackson, HTTP ou logger.
   - `application` importa apenas `domain`.
   - `adapters` implementam portas de `domain`.
   - `config` é o único *composition root*.
3. **Anti-Corruption Layer (ACL)** em `adapters/out/central/acl`. Ela traduz somente
   entre o contrato externo em PT e o domínio em EN. Nenhum DTO externo atravessa a ACL
   e nenhum tipo de domínio é serializado para a Central.
4. **Mock isolado**: `central-convenios-mock/` é um executável separado, sem banco, sem
   Flyway e sem camadas hexagonais. Ele não compartilha código com o backend; os poucos
   DTOs duplicados são intencionais, para que o mock represente um sistema de terceiro.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Camadas em estilo MVC com *transaction script* | As invariantes de parcela, total e transição ficariam espalhadas em serviços; difícil testar sem banco. |
| Módulo compartilhado entre backend e mock | Acoplaria produto e simulador; uma mudança no core quebraria o "terceiro" e esconderia erros de contrato. |
| Um único DTO anotado para contrato externo e domínio | O contrato em PT vazaria para dentro do domínio e a ACL deixaria de existir. |
| Microsserviços por contexto | Custo operacional sem ganho no escopo do desafio; um único deployable é suficiente. |

## Consequências

**Positivas**
- Domínio testável com JUnit puro, rápido e determinístico.
- Trocar a Central por outra implementação mexe apenas no adaptador.
- O contrato em PT não contamina nomes, status ou tabelas.

**Negativas**
- Mapeamento manual gera mais linhas que um `MapStruct`; é deliberado e explícito.
- Duplicação pequena de DTOs no mock.
- Curva de leitura maior para quem chega ao projeto.

**Mitigação**
- ArchUnit impede importações proibidas no primeiro commit da Fase 2.
- Testes de mapeamento da ACL cobrem os dois sentidos.

## Como é verificado

- `HexagonalArchitectureTest` (ArchUnit): `domain` sem framework, `application` sem
  adaptadores, `adapters` sem acesso cruzado.
- Testes de unidade do agregado sem mocks de framework.
- Teste de contrato do mock comparando `openapi/central-api.yaml` com
  `contracts/central-api.yaml`.
