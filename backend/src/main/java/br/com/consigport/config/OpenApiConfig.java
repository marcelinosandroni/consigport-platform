package br.com.consigport.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The published description of the public API.
 *
 * <p>The document of {@code /v3/api-docs} is the runtime version of the snapshot
 * {@code contracts/consigport-api.yaml}: the information, the server, the tags and the
 * header of the language come from here, and the comparison of the task 2.16 proves that
 * the two do not drift apart.
 *
 * <p>The header {@code Accept-Language} is added to every operation, because the
 * negotiation of the locale belongs to the whole API and not to one route.
 */
@Configuration
public class OpenApiConfig {

  /**
   * The description of the API.
   *
   * @return the document that springdoc publishes
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("ConsigPort API")
                .version("1.0.0")
                .description(
                    """
                    API pública do ConsigPort para portabilidade de convênios de desconto em folha.

                    Identificadores, status e códigos de erro são estáveis e em inglês. Mensagens
                    humanas são localizadas por `Accept-Language` (padrão `pt-BR`). A criação de
                    operação exige `Idempotency-Key`; o replay devolve a operação existente com o
                    header `Idempotent-Replay: true`.
                    """)
                .contact(
                    new Contact()
                        .name("ConsigPort")
                        .url("https://github.com/marcelinosandroni/consigport-platform"))
                .license(new License().name("MIT").identifier("MIT")))
        .servers(List.of(new Server().url("http://localhost:8080").description("Desenvolvimento local")))
        .tags(
            List.of(
                new Tag()
                    .name("Authorizations")
                    .description("Opt-in do beneficiário para consulta da agenda."),
                new Tag()
                    .name("Agenda")
                    .description("Agenda de descontos futuros de um beneficiário autorizado."),
                new Tag()
                    .name("Portability Operations")
                    .description("Criação, consulta e acompanhamento de operações de portabilidade."),
                new Tag().name("Audit").description("Trilha imutável de eventos de uma operação.")));
  }

  /**
   * The header of the language, added to every operation.
   *
   * @return the customizer that publishes the header
   */
  @Bean
  public OpenApiCustomizer acceptLanguageCustomizer() {
    return openApi ->
        openApi.getPaths().values().stream()
            .flatMap(path -> path.readOperations().stream())
            .forEach(
                operation ->
                    operation.addParametersItem(
                        new HeaderParameter()
                            .name("Accept-Language")
                            .description(
                                "Idioma das mensagens humanas. Códigos de erro nunca são traduzidos.")
                            .required(false)
                            .schema(languageSchema())));
  }

  private static StringSchema languageSchema() {
    StringSchema schema = new StringSchema();
    schema.setEnum(List.of("pt-BR", "en", "es"));
    schema.setDefault("pt-BR");
    return schema;
  }
}
