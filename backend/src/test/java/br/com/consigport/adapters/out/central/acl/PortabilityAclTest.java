package br.com.consigport.adapters.out.central.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.RejectionReason;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Translation of the status answered by the Central into the domain.
 *
 * <p>The Central speaks Portuguese; the domain speaks English. A rejection whose reason
 * the contract does not declare still closes the operation, with the code {@code
 * UNMAPPED_REJECTION}: the reason the Central wrote stays in the raw payload.
 */
class PortabilityAclTest {

  private PortabilityAcl acl;

  @BeforeEach
  void setUp() {
    acl = new PortabilityAcl();
  }

  @Test
  void translatesTheStatusThatIsStillPending() {
    CentralStatus answer = acl.toCentralStatus("{\"status\":\"PENDENTE\"}", Duration.ofSeconds(2));

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Pending.class,
        pending -> assertThat(pending.retryAfter()).isEqualTo(Duration.ofSeconds(2)));
  }

  @Test
  void translatesTheConfirmation() {
    CentralStatus answer =
        acl.toCentralStatus("{\"status\":\"CONFIRMADA\",\"motivoRejeicao\":null}", Duration.ofSeconds(2));

    assertThat(answer).isInstanceOf(CentralStatus.Confirmed.class);
  }

  @Test
  void translatesTheRejectionWithTheReasonTheContractDeclares() {
    CentralStatus answer =
        acl.toCentralStatus(
            "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"MARGEM_CONSIGNADA_INSUFICIENTE\"}",
            Duration.ofSeconds(2));

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Rejected.class,
        rejected -> assertThat(rejected.reason()).isEqualTo(new RejectionReason.MarginInsufficient()));
  }

  @Test
  void translatesTheRejectionWithAReasonTheContractDoesNotDeclare() {
    CentralStatus answer =
        acl.toCentralStatus(
            "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"LIMITE_DE_PARCELAS\"}",
            Duration.ofSeconds(2));

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Rejected.class,
        rejected -> {
          assertThat(rejected.reason()).isEqualTo(new RejectionReason.Unmapped());
          assertThat(rejected.rawPayload()).contains("LIMITE_DE_PARCELAS");
        });
  }

  @Test
  void refusesTheRejectionWithoutAReason() {
    assertThatThrownBy(
            () ->
                acl.toCentralStatus("{\"status\":\"REJEITADA\"}", Duration.ofSeconds(2)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("the Central rejected the operation without a reason");
  }

  @Test
  void refusesTheStatusTheContractDoesNotDeclare() {
    assertThatThrownBy(
            () -> acl.toCentralStatus("{\"status\":\"AGUARDANDO\"}", Duration.ofSeconds(2)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("unknown Central status: AGUARDANDO");
  }

  @Test
  void refusesTheBodyThatIsNotTheContract() {
    assertThatThrownBy(() -> acl.toCentralStatus("[\"not-the-contract\"]", Duration.ofSeconds(2)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("the Central answered a body that is not the contract");
  }

  @Test
  void refusesTheAnswerWithoutABody() {
    assertThatThrownBy(() -> acl.toCentralStatus(" ", Duration.ofSeconds(2)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("the Central answered the status without a body");
  }
}
