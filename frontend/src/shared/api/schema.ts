export interface paths {
    "/api/v1/authorizations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Registra a autorização (opt-in) do beneficiário
         * @description Registra o consentimento do beneficiário para um convênio. O par
         *     `(beneficiaryId, agreementId)` é único: repetir a chamada devolve `200` com
         *     a autorização existente em vez de criar outra.
         */
        post: operations["registerAuthorization"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/authorizations/{beneficiaryId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Consulta a autorização de um beneficiário
         * @description Devolve a autorização registrada para o beneficiário.
         */
        get: operations["findAuthorization"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/beneficiaries/{beneficiaryId}/agenda": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Consulta a agenda de descontos futuros
         * @description Devolve as parcelas futuras do beneficiário na Central. Exige autorização
         *     registrada; apenas parcelas `OPEN` são elegíveis para portabilidade.
         */
        get: operations["getBeneficiaryAgenda"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/portability-operations": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Lista operações de portabilidade
         * @description Lista paginada, opcionalmente filtrada por status.
         */
        get: operations["listPortabilityOperations"];
        put?: never;
        /**
         * Cria uma operação de portabilidade
         * @description Cria a operação com snapshot das parcelas e envia a solicitação assinada à
         *     Central. A resposta é `202` porque a confirmação é assíncrona. A mesma
         *     `Idempotency-Key` com o mesmo payload devolve a operação existente e o
         *     header `Idempotent-Replay: true`; com payload diferente devolve `409`.
         */
        post: operations["startPortabilityOperation"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/portability-operations/{operationId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Consulta uma operação de portabilidade
         * @description Devolve o detalhe completo, incluindo snapshot, conta e motivo de rejeição.
         */
        get: operations["findPortabilityOperation"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/portability-operations/{operationId}/events": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Lista a trilha de eventos da operação
         * @description Devolve os eventos em ordem cronológica. A trilha é append-only.
         */
        get: operations["listOperationEvents"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        AuthorizationRequest: {
            /**
             * @description Identificador do beneficiário.
             * @example BEN-001
             */
            beneficiaryId: string;
            /**
             * @description Identificador do convênio de origem.
             * @example AGR-001
             */
            agreementId: string;
        };
        Authorization: {
            /**
             * Format: uuid
             * @description Identificador da autorização.
             */
            authorizationId: string;
            /** @description Identificador do beneficiário. */
            beneficiaryId: string;
            /** @description Identificador do convênio. */
            agreementId: string;
            /**
             * Format: date-time
             * @description Instante do opt-in.
             */
            createdAt: string;
        };
        AgendaResponse: {
            /** @description Identificador do beneficiário. */
            beneficiaryId: string;
            /** @description Parcelas futuras do beneficiário. */
            installments: components["schemas"]["Installment"][];
        };
        Installment: {
            /**
             * @description Identificador da parcela.
             * @example PAR-001
             */
            installmentId: string;
            /**
             * @description Convênio de origem.
             * @example AGR-001
             */
            agreementId: string;
            /**
             * Format: double
             * @description Valor da parcela.
             * @example 450
             */
            amount: number;
            /**
             * Format: date
             * @description Data do desconto futuro.
             * @example 2026-12-05
             */
            discountDate: string;
            status: components["schemas"]["InstallmentStatus"];
        };
        /**
         * @description Elegibilidade da parcela.
         * @enum {string}
         */
        InstallmentStatus: "OPEN" | "CLOSED";
        ReceivingAccount: {
            /**
             * @description Banco de destino.
             * @example 001
             */
            bank: string;
            /**
             * @description Agência de destino.
             * @example 1234
             */
            branch: string;
            /**
             * @description Conta de destino.
             * @example 56789-0
             */
            account: string;
        };
        PortabilityOperationRequest: {
            /**
             * @description Beneficiário titular das parcelas.
             * @example BEN-001
             */
            beneficiaryId: string;
            /**
             * @description Convênio de origem.
             * @example AGR-001
             */
            agreementId: string;
            /**
             * @description Parcelas elegíveis selecionadas.
             * @example [
             *       "PAR-001",
             *       "PAR-002"
             *     ]
             */
            installmentIds: string[];
            /**
             * @description Credor cessionário que passa a receber as parcelas.
             * @example ConsigPort
             */
            assigneeCreditor: string;
            receivingAccount: components["schemas"]["ReceivingAccount"];
        };
        /**
         * @description Estado da operação. `COMPLETED` e `REJECTED` são terminais;
         *     `INTEGRATION_ERROR` é retomável e nunca duplica a submissão.
         * @enum {string}
         */
        OperationStatus: "CREATED" | "PENDING" | "CONFIRMED" | "COMPLETED" | "REJECTED" | "INTEGRATION_ERROR";
        OperationAcceptedResponse: {
            /**
             * Format: uuid
             * @description Identificador da operação.
             */
            operationId: string;
            /**
             * @description Protocolo devolvido pela Central; nulo enquanto não aceita.
             * @example PRT-20261123-0001
             */
            protocol: string | null;
            status: components["schemas"]["OperationStatus"];
            /**
             * Format: double
             * @description Soma das parcelas.
             * @example 750
             */
            totalAmount: number;
            /**
             * Format: date-time
             * @description Instante da criação.
             */
            createdAt: string;
        };
        OperationSummary: {
            /**
             * Format: uuid
             * @description Identificador da operação.
             */
            operationId: string;
            /** @description Protocolo da Central. */
            protocol: string | null;
            /** @description Beneficiário titular. */
            beneficiaryId: string;
            status: components["schemas"]["OperationStatus"];
            /**
             * Format: double
             * @description Soma das parcelas.
             */
            totalAmount: number;
            /**
             * Format: date-time
             * @description Instante da criação.
             */
            createdAt: string;
            /**
             * Format: date-time
             * @description Instante da última atualização.
             */
            updatedAt: string;
        };
        OperationDetail: components["schemas"]["OperationSummary"] & {
            /** @description Convênio de origem. */
            agreementId: string;
            /** @description Snapshot das parcelas no momento da solicitação. */
            installments: components["schemas"]["InstallmentSnapshot"][];
            /** @description Credor cessionário. */
            assigneeCreditor: string;
            receivingAccount: components["schemas"]["ReceivingAccount"];
            /**
             * @description Motivo devolvido pela Central quando rejeitada.
             * @example MARGIN_INSUFFICIENT
             */
            rejectionReason: string | null;
            /** @description Tentativas de integração já realizadas. */
            retryCount: number;
            /**
             * Format: date-time
             * @description Instante da atualização da conta de recebimento.
             */
            accountUpdatedAt: string | null;
        };
        InstallmentSnapshot: {
            /** @description Identificador da parcela. */
            installmentId: string;
            /** @description Convênio de origem. */
            agreementId: string;
            /**
             * Format: double
             * @description Valor congelado da parcela.
             */
            amount: number;
            /**
             * Format: date
             * @description Data do desconto congelada.
             */
            discountDate: string;
        };
        OperationPage: {
            /** @description Operações da página. */
            items: components["schemas"]["OperationSummary"][];
            /** @description Página atual. */
            page: number;
            /** @description Itens por página. */
            size: number;
            /** @description Total de operações. */
            totalElements: number;
            /** @description Total de páginas. */
            totalPages: number;
        };
        OperationEvent: {
            /**
             * Format: int64
             * @description Identificador sequencial do evento.
             */
            eventId: number;
            /** @description Status anterior; nulo na criação. */
            previousStatus: components["schemas"]["OperationStatus"] | null;
            newStatus: components["schemas"]["OperationStatus"];
            eventType: components["schemas"]["EventType"];
            /** @description Descrição legível do evento. */
            details: string;
            /** @description Payload externo relevante, quando existir. */
            rawPayload: {
                [key: string]: unknown;
            } | null;
            /**
             * Format: date-time
             * @description Instante do evento.
             */
            createdAt: string;
        };
        /**
         * @description Tipo estável do evento.
         * @enum {string}
         */
        EventType: "OPERATION_CREATED" | "OPERATION_SUBMITTED" | "POLL_ATTEMPT" | "OPERATION_CONFIRMED" | "OPERATION_REJECTED" | "ACCOUNT_UPDATED" | "OPERATION_COMPLETED" | "INTEGRATION_FAILED";
        OperationEventsResponse: {
            /**
             * Format: uuid
             * @description Identificador da operação.
             */
            operationId: string;
            /** @description Eventos em ordem cronológica. */
            events: components["schemas"]["OperationEvent"][];
        };
        ErrorResponse: {
            /**
             * @description Código estável e não traduzido. Valores possíveis: `VALIDATION_ERROR`,
             *     `AUTHORIZATION_NOT_FOUND`, `AUTHORIZATION_REQUIRED`,
             *     `BENEFICIARY_NOT_FOUND`, `OPERATION_NOT_FOUND`,
             *     `IDEMPOTENCY_KEY_REQUIRED`, `IDEMPOTENCY_CONFLICT`,
             *     `INSTALLMENT_NOT_ELIGIBLE`, `CENTRAL_UNAVAILABLE`, `INTERNAL_ERROR`.
             * @example IDEMPOTENCY_CONFLICT
             */
            code: string;
            /** @description Mensagem localizada conforme `Accept-Language`. */
            message: string;
            /**
             * @description Locale negociado.
             * @example pt-BR
             */
            locale: string;
            /**
             * Format: date-time
             * @description Instante do erro.
             */
            timestamp: string;
            /** @description Caminho da requisição. */
            path: string;
            /** @description Detalhes de validação por campo. */
            details: components["schemas"]["ErrorDetail"][];
        };
        ErrorDetail: {
            /** @description Campo ou caminho relacionado ao erro. */
            field: string;
            /** @description Explicação localizada do problema. */
            message: string;
        };
    };
    responses: {
        /** @description Requisição inválida. */
        BadRequest: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description Beneficiário sem autorização registrada. */
        Forbidden: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description Recurso não encontrado. */
        NotFound: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description A chave de idempotência já foi usada com outro conteúdo. */
        Conflict: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description Parcela inelegível para portabilidade. */
        UnprocessableEntity: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description Central de Convênios indisponível. */
        ServiceUnavailable: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
        /** @description Erro interno inesperado. */
        InternalError: {
            headers: {
                [name: string]: unknown;
            };
            content: {
                "application/json": components["schemas"]["ErrorResponse"];
            };
        };
    };
    parameters: {
        /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
        AcceptLanguage: "pt-BR" | "en" | "es";
        /** @description UUID que identifica a tentativa de criação da operação. */
        IdempotencyKey: string;
        /** @description Identificador do beneficiário na Central. */
        BeneficiaryIdPath: string;
        /** @description Identificador da operação no ConsigPort. */
        OperationIdPath: string;
        /** @description Filtra a listagem por status. */
        StatusFilter: components["schemas"]["OperationStatus"];
        /** @description Página solicitada, começando em zero. */
        Page: number;
        /** @description Itens por página. */
        Size: number;
    };
    requestBodies: never;
    headers: {
        /** @description `true` quando a operação devolvida já existia para a chave enviada. */
        IdempotentReplay: boolean;
    };
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    registerAuthorization: {
        parameters: {
            query?: never;
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AuthorizationRequest"];
            };
        };
        responses: {
            /** @description Autorização já existente para o par beneficiário/convênio. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["Authorization"];
                };
            };
            /** @description Autorização criada. */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["Authorization"];
                };
            };
            400: components["responses"]["BadRequest"];
            500: components["responses"]["InternalError"];
        };
    };
    findAuthorization: {
        parameters: {
            query?: never;
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path: {
                /** @description Identificador do beneficiário na Central. */
                beneficiaryId: components["parameters"]["BeneficiaryIdPath"];
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Autorização encontrada. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["Authorization"];
                };
            };
            404: components["responses"]["NotFound"];
            500: components["responses"]["InternalError"];
        };
    };
    getBeneficiaryAgenda: {
        parameters: {
            query?: never;
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path: {
                /** @description Identificador do beneficiário na Central. */
                beneficiaryId: components["parameters"]["BeneficiaryIdPath"];
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Agenda do beneficiário. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["AgendaResponse"];
                };
            };
            403: components["responses"]["Forbidden"];
            404: components["responses"]["NotFound"];
            500: components["responses"]["InternalError"];
        };
    };
    listPortabilityOperations: {
        parameters: {
            query?: {
                /** @description Filtra a listagem por status. */
                status?: components["parameters"]["StatusFilter"];
                /** @description Página solicitada, começando em zero. */
                page?: components["parameters"]["Page"];
                /** @description Itens por página. */
                size?: components["parameters"]["Size"];
            };
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Página de operações. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["OperationPage"];
                };
            };
            400: components["responses"]["BadRequest"];
            500: components["responses"]["InternalError"];
        };
    };
    startPortabilityOperation: {
        parameters: {
            query?: never;
            header: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
                /** @description UUID que identifica a tentativa de criação da operação. */
                "Idempotency-Key": components["parameters"]["IdempotencyKey"];
            };
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PortabilityOperationRequest"];
            };
        };
        responses: {
            /** @description Operação aceita e enviada à Central. */
            202: {
                headers: {
                    "Idempotent-Replay": components["headers"]["IdempotentReplay"];
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["OperationAcceptedResponse"];
                };
            };
            400: components["responses"]["BadRequest"];
            409: components["responses"]["Conflict"];
            422: components["responses"]["UnprocessableEntity"];
            500: components["responses"]["InternalError"];
            503: components["responses"]["ServiceUnavailable"];
        };
    };
    findPortabilityOperation: {
        parameters: {
            query?: never;
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path: {
                /** @description Identificador da operação no ConsigPort. */
                operationId: components["parameters"]["OperationIdPath"];
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Operação encontrada. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["OperationDetail"];
                };
            };
            404: components["responses"]["NotFound"];
            500: components["responses"]["InternalError"];
        };
    };
    listOperationEvents: {
        parameters: {
            query?: never;
            header?: {
                /** @description Idioma das mensagens humanas. Códigos de erro nunca são traduzidos. */
                "Accept-Language"?: components["parameters"]["AcceptLanguage"];
            };
            path: {
                /** @description Identificador da operação no ConsigPort. */
                operationId: components["parameters"]["OperationIdPath"];
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Eventos da operação. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["OperationEventsResponse"];
                };
            };
            404: components["responses"]["NotFound"];
            500: components["responses"]["InternalError"];
        };
    };
}
