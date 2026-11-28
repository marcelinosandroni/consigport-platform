package br.com.consigport;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Boots the whole application and proves the health probe answers UP.
 *
 * <p>The application runs against the real PostgreSQL 16 of the stack, with the
 * migration applied by Flyway and the schema validated by Hibernate: the context only
 * starts when the DDL and the entities agree.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsigPortApplicationTests extends PostgreSqlTestSupport {

  @Autowired private TestRestTemplate rest;

  @Test
  void healthEndpointReportsUp() {
    ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"status\":\"UP\"");
  }

  @Test
  void openApiDocumentIsPublishedAsThirtyOne() {
    ResponseEntity<String> response = rest.getForEntity("/v3/api-docs", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"openapi\":\"3.1.0\"");
  }
}
