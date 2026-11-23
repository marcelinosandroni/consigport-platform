# Desafio Técnico — Desenvolvedor(a) Fullstack (React + Java)
## Plataforma de Portabilidade de Convênios de Desconto em Folha

---

### Contexto
Você foi contratado(a) para desenvolver uma plataforma que permite a uma empresa atuar como nova credora de convênios de desconto em folha, assumindo o direito de receber parcelas que hoje pertencem a outro credor — processo conhecido como **portabilidade de convênio**.

Para isso, a plataforma precisa se conectar a uma **Central de Convênios** (sistema externo que concentra as autorizações dos beneficiários e a agenda de descontos futuros), capturar a autorização do beneficiário, consultar essa agenda, registrar a operação de portabilidade e acompanhar sua confirmação até a atualização da conta de recebimento.

Após a entrega, haverá uma sessão de *code review* e apresentação para explicar decisões técnicas, *trade-offs* e possíveis evoluções.

---

### Objetivo
Construir uma solução fullstack (backend em Java + frontend em React) que simule o fluxo de portabilidade de convênio de desconto em folha, incluindo a integração com uma Central de Convênios externa que você também vai simular, já que não existe um sistema real disponível para este desafio.

---

### Funcionalidades Esperadas
* Captura da autorização (*opt-in*) do beneficiário para consulta de sua agenda de descontos
* Consulta e visualização da agenda de descontos futuros de um beneficiário autorizado
* Seleção das parcelas elegíveis para portabilidade
* Registro da operação de portabilidade junto à Central de Convênios, com acompanhamento assíncrono da confirmação
* Atualização da conta de recebimento após confirmação da portabilidade
* Painel com o histórico/trilha de auditoria de cada operação realizada

---

### Requisitos Funcionais
Implemente ao menos os seguintes fluxos:

1. **Registrar a autorização (*opt-in*)** de um beneficiário, vinculando-a a um identificador único.
2. **Consultar a agenda de descontos futuros** de um beneficiário autorizado, exibindo parcelas com valor, data de desconto, convênio de origem e status.
3. **Selecionar uma ou mais parcelas elegíveis** e iniciar uma operação de portabilidade, informando a conta de recebimento de destino.
4. **Acompanhar o status da operação** (pendente, confirmada, rejeitada), considerando que a confirmação da Central é assíncrona.
5. **Exibir, por operação, uma trilha de eventos** (quando foi solicitada, quando mudou de status, qual foi o retorno da Central).
6. **Tratar e exibir ao usuário cenários de rejeição ou falha** de integração com a Central.

---

### Requisitos Não-Funcionais
* A integração com a Central de Convênios deve prever **assinatura/verificação de integridade** das mensagens trocadas (pode ser implementada de forma simplificada, ex.: HMAC; não é necessário certificado digital real).
* O fluxo de registro de operação deve ser **idempotente**: reenvios da mesma solicitação (por falha de rede, duplo clique, retry) não podem gerar operações duplicadas na Central nem no seu sistema.
* A confirmação da operação deve seguir um **padrão assíncrono** (a Central responde imediatamente com um protocolo, e a confirmação chega depois, por consulta de status). Implemente esse padrão com **retry e backoff adequados**.
* **Tratamento explícito de erros de integração** (indisponibilidade da Central, timeout, resposta inesperada).
* Toda mudança de estado relevante de uma operação deve ser **registrada de forma auditável e não sobrescrita** (trilha de eventos).
* O código deve ser organizado, testável e de fácil manutenção.
* O projeto deve possuir **testes automatizados para os cenários críticos** (idempotência, falha de integração, transições de estado da operação).
* Fazer uso de ferramenta(s) de IA como suporte à implementação é esperado e **será avaliado o "como" a IA foi usada** para construir a solução.

---

### Restrições Técnicas
* **Backend:** Java (framework livre; Spring Boot é o mais comum, mas não obrigatório).
* **Frontend:** React (TypeScript é recomendado, mas não obrigatório).
* **Banco de dados:** Livre.
* **Simulação da Central de Convênios:** Não há um sistema externo real disponível para este desafio — você deve implementar um mock/stub da Central (pode ser um serviço separado, endpoints no próprio backend, ou uma ferramenta de mock HTTP), seguindo o contrato mínimo abaixo e simulando: resposta assíncrona (202 + protocolo), tempo de confirmação (ex.: alguns segundos de atraso) e falhas ocasionais (timeout, rejeição).
* O projeto deve ser executável com instruções claras no README.
* **Disponibilize a documentação do harness do agente de IA utilizado**, incluindo specs, decisões arquiteturais, uso da IA (prompts e restrições) e os resultados que orientaram a implementação da solução.
* É permitido (e recomendado) uso de bibliotecas externas.

---

### Contrato Mínimo Sugerido da Central de Convênios
Você tem liberdade para desenhar os endpoints como preferir; o contrato abaixo é um ponto de partida, não uma especificação fechada.

#### 1. Registrar autorização (opt-in)
* **Endpoint:** `POST /autorizacoes`
* **Body:**
  ```json
  {
    "beneficiarioId": "string",
    "convenioId": "string"
  }
  ```
* **Resposta:** `201 Created` com um identificador de autorização.

#### 2. Consultar agenda de descontos
* **Endpoint:** `GET /beneficiarios/{beneficiarioId}/agenda`
* **Resposta:** Lista de parcelas:
  ```json
  [
    {
      "parcelaId": "string",
      "convenioId": "string",
      "valor": 0.0,
      "dataDesconto": "AAAA-MM-DD",
      "status": "ABERTA"
    }
  ]
  ```

#### 3. Registrar operação de portabilidade
* **Endpoint:** `POST /operacoes-portabilidade`
* **Body:**
  ```json
  {
    "beneficiarioId": "string",
    "parcelas": ["parcelaId", "..."],
    "credorCessionario": "string",
    "contaRecebimento": {
      "banco": "string",
      "agencia": "string",
      "conta": "string"
    },
    "assinatura": "string"
  }
  ```
* **Resposta:** `202 Accepted` com `{ "protocolo": "string" }`.

#### 4. Consultar status da operação
* **Endpoint:** `GET /operacoes-portabilidade/{protocolo}`
* **Resposta:**
  ```json
  {
    "status": "PENDENTE" | "CONFIRMADA" | "REJEITADA",
    "motivoRejeicao": "string ou null"
  }
  ```
  *(Enquanto `PENDENTE`, pode retornar `204 No Content` com header `Retry-After`).*

---

### Entrega
* Crie um repositório público no GitHub com a solução.
* Envie o link no prazo combinado.
* Inclua no `README.md`:
  * Instruções de execução (backend, frontend e mock da Central)
  * Decisões arquiteturais
  * Bibliotecas externas utilizadas e justificativas
  * Como foi feita a simulação/integração com a Central de Convênios
  * Trade-offs considerados
  * O que faria com mais tempo