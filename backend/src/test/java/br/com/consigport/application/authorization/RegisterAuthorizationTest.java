package br.com.consigport.application.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.AuthorizationAlreadyExistsException;
import br.com.consigport.domain.authorization.AuthorizationRepository;
import br.com.consigport.domain.authorization.InMemoryAuthorizationRepository;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the opt-in registration.
 *
 * <p>The use case is idempotent (BR-02), dates the opt-in with the injected clock and
 * replicates the consent in the Central before storing the local record.
 */
class RegisterAuthorizationTest {

  private static final Instant NOW = Instant.parse("2026-11-26T10:15:30Z");

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");

  private InMemoryAuthorizationRepository repository;
  private InMemoryCentralAuthorizationPort central;
  private RegisterAuthorization useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryAuthorizationRepository();
    central = new InMemoryCentralAuthorizationPort();
    useCase = new RegisterAuthorization(repository, central, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void storesTheAuthorizationWithTheInstantOfTheClock() {
    Authorization authorization = useCase.execute(BENEFICIARY, AGREEMENT);

    assertThat(authorization.beneficiaryId()).isEqualTo(BENEFICIARY);
    assertThat(authorization.agreementId()).isEqualTo(AGREEMENT);
    assertThat(authorization.createdAt()).isEqualTo(NOW);
    assertThat(repository.findByBeneficiaryAndAgreement(BENEFICIARY, AGREEMENT))
        .contains(authorization);
  }

  @Test
  void replicatesTheConsentInTheCentral() {
    useCase.execute(BENEFICIARY, AGREEMENT);

    assertThat(central.replications()).containsExactly("BEN-001:AGR-001");
  }

  @Test
  void returnsTheExistingAuthorizationWhenTheOptInIsRepeated() {
    Authorization first = useCase.execute(BENEFICIARY, AGREEMENT);

    Authorization second = useCase.execute(BENEFICIARY, AGREEMENT);

    assertThat(second).isEqualTo(first);
    assertThat(repository.size()).isEqualTo(1);
  }

  @Test
  void doesNotTalkToTheCentralAgainWhenTheOptInIsRepeated() {
    useCase.execute(BENEFICIARY, AGREEMENT);
    useCase.execute(BENEFICIARY, AGREEMENT);

    assertThat(central.replicationCount()).isEqualTo(1);
  }

  @Test
  void keepsTheOptInsOfDifferentAgreementsApart() {
    useCase.execute(BENEFICIARY, AGREEMENT);
    useCase.execute(BENEFICIARY, new AgreementId("AGR-002"));

    assertThat(repository.size()).isEqualTo(2);
    assertThat(central.replicationCount()).isEqualTo(2);
  }

  @Test
  void returnsTheWinnerWhenTheSaveLosesTheRace() {
    RacingAuthorizationRepository racingRepository = new RacingAuthorizationRepository();
    Authorization winner = Authorization.register(BENEFICIARY, AGREEMENT, NOW);
    racingRepository.storeConcurrently(winner);
    RegisterAuthorization racingUseCase =
        new RegisterAuthorization(
            racingRepository, central, Clock.fixed(NOW, ZoneOffset.UTC));

    Authorization result = racingUseCase.execute(BENEFICIARY, AGREEMENT);

    assertThat(result).isEqualTo(winner);
    assertThat(central.replicationCount()).isEqualTo(1);
  }

  /**
   * Repository that loses the race: the first lookup sees nothing, another request
   * commits the pair in the meantime and the save is refused by the unique constraint.
   */
  private static final class RacingAuthorizationRepository implements AuthorizationRepository {

    private final InMemoryAuthorizationRepository delegate = new InMemoryAuthorizationRepository();
    private boolean firstLookup = true;

    void storeConcurrently(Authorization authorization) {
      delegate.save(authorization);
    }

    @Override
    public Optional<Authorization> findByBeneficiaryAndAgreement(
        BeneficiaryId beneficiaryId, AgreementId agreementId) {
      if (firstLookup) {
        firstLookup = false;
        return Optional.empty();
      }
      return delegate.findByBeneficiaryAndAgreement(beneficiaryId, agreementId);
    }

    @Override
    public void save(Authorization authorization) {
      if (delegate
          .findByBeneficiaryAndAgreement(
              authorization.beneficiaryId(), authorization.agreementId())
          .isPresent()) {
        throw new AuthorizationAlreadyExistsException(
            authorization.beneficiaryId(), authorization.agreementId());
      }
      delegate.save(authorization);
    }
  }
}
