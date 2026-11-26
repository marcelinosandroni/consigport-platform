package br.com.consigport.adapters.out.central.acl;

import br.com.consigport.adapters.out.central.dto.AgendaResponse;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.InstallmentId;
import java.util.List;

/**
 * Anti-corruption layer between the contract of the Central and the domain.
 *
 * <p>The Central speaks Portuguese; the domain speaks English. This class is the only
 * place that knows the translation, in both directions: {@code ABERTA} is
 * {@code OPEN} and {@code FECHADA} is {@code CLOSED}, and the answers of the Central
 * become domain errors instead of HTTP statuses.
 */
public final class CentralAcl {

  private static final String CENTRAL_OPEN = "ABERTA";
  private static final String CENTRAL_CLOSED = "FECHADA";

  /**
   * Translates the agenda answered by the Central into the domain.
   *
   * @param beneficiaryId the beneficiary whose agenda was read
   * @param parcelas the installments as the Central answered them
   * @return the agenda in the domain
   */
  public BeneficiaryAgenda toAgenda(BeneficiaryId beneficiaryId, List<AgendaResponse> parcelas) {
    return new BeneficiaryAgenda(
        beneficiaryId,
        parcelas.stream()
            .map(parcela -> toInstallment(parcela))
            .toList());
  }

  /**
   * Translates one parcela answered by the Central into the domain.
   *
   * @param parcela the installment as the Central answered it
   * @return the installment in the domain
   */
  public Installment toInstallment(AgendaResponse parcela) {
    return new Installment(
        new InstallmentId(parcela.installmentId()),
        new AgreementId(parcela.agreementId()),
        new Money(parcela.amount()),
        parcela.discountDate(),
        toDomainStatus(parcela.status()));
  }

  /**
   * Translates the situation of a parcela into the domain.
   *
   * @param centralStatus the situation as the Central writes it
   * @return the situation in the domain
   * @throws IllegalArgumentException when the Central answers an unknown situation
   */
  public InstallmentStatus toDomainStatus(String centralStatus) {
    if (centralStatus == null || centralStatus.isBlank()) {
      throw new IllegalArgumentException("installment status must not be blank");
    }
    return switch (centralStatus.trim()) {
      case CENTRAL_OPEN -> InstallmentStatus.OPEN;
      case CENTRAL_CLOSED -> InstallmentStatus.CLOSED;
      default -> throw new IllegalArgumentException("unknown installment status: " + centralStatus);
    };
  }

  /**
   * Translates the situation of a parcela into the contract of the Central.
   *
   * @param status the situation in the domain
   * @return the situation as the Central writes it
   */
  public String toCentralStatus(InstallmentStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("installment status must not be null");
    }
    return switch (status) {
      case OPEN -> CENTRAL_OPEN;
      case CLOSED -> CENTRAL_CLOSED;
    };
  }

  /**
   * Translates the refusal of the Central into a domain error.
   *
   * @param status the HTTP status answered by the Central
   * @param beneficiaryId the beneficiary whose agenda was read
   * @return the domain error of the refusal
   * @throws IllegalArgumentException when the Central answers an unexpected status
   */
  public DomainError toDomainError(int status, BeneficiaryId beneficiaryId) {
    return switch (status) {
      case 403 -> new DomainError.AuthorizationRequired();
      case 404 -> new DomainError.BeneficiaryNotFound(beneficiaryId);
      default -> throw new IllegalArgumentException("unexpected Central status: " + status);
    };
  }
}
