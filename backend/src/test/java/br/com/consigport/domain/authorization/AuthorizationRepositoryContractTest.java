package br.com.consigport.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contract of the {@link AuthorizationRepository} port.
 *
 * <p>The fake in memory plays the role of the adapter: the test proves the behaviour the
 * application layer relies on — one authorization per {@code (beneficiaryId,
 * agreementId)} (BR-02).
 */
class AuthorizationRepositoryContractTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-26T10:15:30Z");

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");

  private InMemoryAuthorizationRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryAuthorizationRepository();
  }

  @Test
  void findsTheAuthorizationSavedForThePair() {
    Authorization authorization = Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT);
    repository.save(authorization);

    assertThat(repository.findByBeneficiaryAndAgreement(BENEFICIARY, AGREEMENT))
        .contains(authorization);
  }

  @Test
  void returnsEmptyWhenThePairWasNeverAuthorized() {
    assertThat(repository.findByBeneficiaryAndAgreement(BENEFICIARY, AGREEMENT)).isEmpty();
  }

  @Test
  void rejectsASecondOptInForTheSamePair() {
    repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT));

    assertThatThrownBy(
            () -> repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT)))
        .isInstanceOf(AuthorizationAlreadyExistsException.class);
  }

  @Test
  void carriesTheOffendingPairInTheException() {
    repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT));

    assertThatThrownBy(
            () -> repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT)))
        .isInstanceOfSatisfying(
            AuthorizationAlreadyExistsException.class,
            exception -> {
              assertThat(exception.beneficiaryId()).isEqualTo(BENEFICIARY);
              assertThat(exception.agreementId()).isEqualTo(AGREEMENT);
              assertThat(exception)
                  .hasMessage(
                      "authorization already exists for beneficiary BEN-001 and agreement AGR-001");
            });
  }

  @Test
  void keepsTheOptInsOfDifferentAgreementsApart() {
    repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT));
    repository.save(
        Authorization.register(BENEFICIARY, new AgreementId("AGR-002"), CREATED_AT));

    assertThat(repository.size()).isEqualTo(2);
  }

  @Test
  void keepsTheOptInsOfDifferentBeneficiariesApart() {
    repository.save(Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT));
    repository.save(
        Authorization.register(new BeneficiaryId("BEN-002"), AGREEMENT, CREATED_AT));

    assertThat(repository.size()).isEqualTo(2);
  }
}
