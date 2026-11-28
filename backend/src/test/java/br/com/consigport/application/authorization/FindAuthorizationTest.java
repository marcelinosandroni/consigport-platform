package br.com.consigport.application.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.InMemoryAuthorizationRepository;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the opt-in query.
 *
 * <p>The use case answers whether the agenda of the beneficiary may be consulted
 * (BR-01).
 */
class FindAuthorizationTest {

  private static final Instant NOW = Instant.parse("2026-11-26T10:15:30Z");

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");

  private InMemoryAuthorizationRepository repository;
  private FindAuthorization useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryAuthorizationRepository();
    useCase = new FindAuthorization(repository);
  }

  @Test
  void findsTheAuthorizationOfThePair() {
    Authorization authorization = Authorization.register(BENEFICIARY, AGREEMENT, NOW);
    repository.save(authorization);

    assertThat(useCase.execute(BENEFICIARY, AGREEMENT)).contains(authorization);
  }

  @Test
  void returnsEmptyWhenThePairWasNeverAuthorized() {
    assertThat(useCase.execute(BENEFICIARY, AGREEMENT)).isEmpty();
  }

  @Test
  void doesNotFindTheAuthorizationOfAnotherAgreement() {
    repository.save(Authorization.register(BENEFICIARY, AGREEMENT, NOW));

    assertThat(useCase.execute(BENEFICIARY, new AgreementId("AGR-002"))).isEmpty();
  }
}
