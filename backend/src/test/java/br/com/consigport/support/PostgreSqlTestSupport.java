package br.com.consigport.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL 16 shared by the integration tests of the backend.
 *
 * <p>The container is the database of the stack: Flyway applies the migrations and
 * Hibernate validates the schema against the entities, so the tests prove the DDL and
 * the mapping agree.
 *
 * <p>One container serves the whole test run. A container per test class would break
 * the Spring contexts: they capture the connection details when they are created, and a
 * container that restarts on another port leaves the context pointing to a dead
 * database.
 */
public abstract class PostgreSqlTestSupport {

  /** The image of the stack: PostgreSQL 16 on Alpine. */
  protected static final DockerImageName POSTGRES_IMAGE =
      DockerImageName.parse("postgres:16-alpine");

  /** The database of the integration tests, started once for the whole run. */
  private static final PostgreSQLContainer<?> POSTGRES = startPostgres();

  /**
   * Points the datasource of the test at the shared container.
   *
   * <p>The worker of the polling is switched off: it would race with the assertions of
   * the tests, which drive the use case themselves.
   *
   * @param registry the properties of the context under construction
   */
  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("central.poll.enabled", () -> "false");
  }

  private static PostgreSQLContainer<?> startPostgres() {
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    container.start();
    return container;
  }
}
