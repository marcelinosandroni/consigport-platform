package br.com.consigport.central.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Proves the administrative surface and the determinism of the in-memory store.
 *
 * <p>The acceptance criterion of task 1.1 is a reset performed twice that yields the
 * same state, so the comparison is made on the raw JSON body: key order included.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InMemoryCentralStoreTests {

  @Autowired private TestRestTemplate rest;

  @Test
  void resetReturnsNoContent() {
    ResponseEntity<Void> response = rest.postForEntity("/_admin/reset", null, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void stateExposesEveryFixtureFromTheArchitecture() {
    reset();
    ResponseEntity<String> response = rest.getForEntity("/_admin/state", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains(
            "BEN-001", "PAR-001", "PAR-002", "PAR-003",
            "BEN-REJ", "PAR-101",
            "BEN-MARGIN", "PAR-301",
            "BEN-TIMEOUT", "PAR-201",
            "\"valor\":450.00",
            "\"valor\":6000.00",
            "\"status\":\"ABERTA\"");
  }

  @Test
  void twoResetsProduceTheSameState() {
    ResponseEntity<String> first = resetAndReadState();
    ResponseEntity<String> second = resetAndReadState();

    assertThat(second.getBody()).isEqualTo(first.getBody());
  }

  private ResponseEntity<String> resetAndReadState() {
    reset();
    return rest.getForEntity("/_admin/state", String.class);
  }

  private void reset() {
    rest.postForEntity("/_admin/reset", null, Void.class);
  }
}
