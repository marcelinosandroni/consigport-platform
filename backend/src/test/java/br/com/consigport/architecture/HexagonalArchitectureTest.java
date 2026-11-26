package br.com.consigport.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Hexagonal boundary of the ConsigPort backend.
 *
 * <p>The rules encode the dependency table of {@code SDD/ARCHITECTURE.md} §3: the domain
 * is pure, the application talks to the domain only, the adapters implement the ports and
 * the composition root is the only place that knows the concrete adapters.
 *
 * <p>The rules are evaluated here instead of through the ArchUnit JUnit 5 engine: under
 * JUnit Platform 1.12.2 the engine does not discover the {@code @ArchTest} fields, so a
 * broken rule would stay green without ever running. Every rule is checked against the
 * production classes and, in the last test, against deliberately broken fixtures that
 * prove the rule really rejects the violation it describes.
 */
class HexagonalArchitectureTest {

  private static final JavaClasses MAIN_CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("br.com.consigport");

  private static final JavaClasses FIXTURE_CLASSES =
      new ClassFileImporter()
          .importPackages("br.com.consigport.architecture.fixture", "org.springframework.stereotype");

  private static final ArchRule DOMAIN_IS_FREE_OF_FRAMEWORKS =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "jakarta.validation..",
              "com.fasterxml..",
              "org.hibernate..",
              "org.slf4j..",
              "java.sql..",
              "java.net..")
          .because("the domain is pure: no framework, no ORM, no HTTP, no logger");

  private static final ArchRule APPLICATION_IMPORTS_THE_DOMAIN_ONLY =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideOutsideOfPackages("br.com.consigport.domain..", "java..")
          .because("the application layer talks to the domain and to the JDK only");

  private static final ArchRule DOMAIN_AND_APPLICATION_NEVER_REACH_ADAPTERS =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapters..")
          .because("only the composition root wires the concrete adapters");

  private static final ArchRule INBOUND_ADAPTERS_NEVER_REACH_OUTBOUND_ADAPTERS =
      noClasses()
          .that()
          .resideInAPackage("..adapters.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapters.out..")
          .because("the HTTP edge talks to use cases, never to outbound adapters")
          // The HTTP edge is born in task 2.9. The rule is empty until then and bites
          // from the first inbound adapter on; the fixture below proves it rejects a
          // violation as soon as a subject exists.
          .allowEmptyShould(true);

  private static final ArchRule ONLY_THE_COMPOSITION_ROOT_IS_SHARED =
      noClasses()
          .that()
          .resideOutsideOfPackages("br.com.consigport", "..config..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..config..")
          .because("configuration is private to the composition root");

  private static final ArchRule LAYERS_HAVE_NO_CYCLES =
      slices().matching("br.com.consigport.(*)..").should().beFreeOfCycles();

  private static final ArchRule EXTERNAL_DTOS_STAY_IN_THE_OUTBOUND_ADAPTER =
      noClasses()
          .that()
          .resideOutsideOfPackage("..adapters.out.central..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapters.out.central.dto..")
          .because("the DTOs of the Central never leave the outbound adapter");

  @Test
  void the_layers_are_declared() {
    Path javaRoot = moduleRoot().resolve("src/main/java/br/com/consigport");

    assertThat(javaRoot.resolve("domain")).isDirectory();
    assertThat(javaRoot.resolve("application")).isDirectory();
    assertThat(javaRoot.resolve("adapters")).isDirectory();
    assertThat(javaRoot.resolve("config")).isDirectory();
  }

  @Test
  void the_composition_root_is_compiled_in_the_config_package() {
    assertThat(MAIN_CLASSES.stream().map(JavaClass::getFullName))
        .contains("br.com.consigport.config.CompositionRoot");
  }

  @Test
  void the_domain_is_free_of_frameworks() {
    assertHolds(DOMAIN_IS_FREE_OF_FRAMEWORKS);
  }

  @Test
  void the_application_imports_the_domain_only() {
    assertHolds(APPLICATION_IMPORTS_THE_DOMAIN_ONLY);
  }

  @Test
  void the_domain_and_the_application_never_reach_the_adapters() {
    assertHolds(DOMAIN_AND_APPLICATION_NEVER_REACH_ADAPTERS);
  }

  @Test
  void the_inbound_adapters_never_reach_the_outbound_adapters() {
    assertHolds(INBOUND_ADAPTERS_NEVER_REACH_OUTBOUND_ADAPTERS);
  }

  @Test
  void only_the_composition_root_is_shared() {
    assertHolds(ONLY_THE_COMPOSITION_ROOT_IS_SHARED);
  }

  @Test
  void the_layers_have_no_cycles() {
    assertHolds(LAYERS_HAVE_NO_CYCLES);
  }

  @Test
  void the_external_dtos_stay_in_the_outbound_adapter() {
    assertHolds(EXTERNAL_DTOS_STAY_IN_THE_OUTBOUND_ADAPTER);
  }

  @Test
  void the_rules_reject_the_broken_fixtures() {
    assertRejects(DOMAIN_IS_FREE_OF_FRAMEWORKS);
    assertRejects(APPLICATION_IMPORTS_THE_DOMAIN_ONLY);
    assertRejects(DOMAIN_AND_APPLICATION_NEVER_REACH_ADAPTERS);
    assertRejects(INBOUND_ADAPTERS_NEVER_REACH_OUTBOUND_ADAPTERS);
    assertRejects(EXTERNAL_DTOS_STAY_IN_THE_OUTBOUND_ADAPTER);
  }

  private static void assertHolds(ArchRule rule) {
    EvaluationResult result = rule.evaluate(MAIN_CLASSES);

    assertThat(result.hasViolation())
        .as("%s%n%s", rule.getDescription(), result.getFailureReport())
        .isFalse();
  }

  private static void assertRejects(ArchRule rule) {
    EvaluationResult result = rule.evaluate(FIXTURE_CLASSES);

    assertThat(result.hasViolation())
        .as("the rule must reject the broken fixture: %s", rule.getDescription())
        .isTrue();
  }

  private static Path moduleRoot() {
    Path current = Path.of("").toAbsolutePath();
    while (current != null && !Files.isDirectory(current.resolve("src/main/java/br/com/consigport"))) {
      current = current.getParent();
    }
    if (current == null) {
      throw new IllegalStateException(
          "Backend module root not found from " + Path.of("").toAbsolutePath());
    }
    return current;
  }
}
