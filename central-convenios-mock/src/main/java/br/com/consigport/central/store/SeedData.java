package br.com.consigport.central.store;

import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Installment.InstallmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic fixtures from {@code SDD/ARCHITECTURE.md} §4.
 *
 * <p>Every call builds a fresh, independent copy, so a reset never shares mutable state
 * with a previous run. The scenarios are fixed on purpose: the same state must always
 * produce the same sequence.
 */
final class SeedData {

  private SeedData() {
    // Utility class.
  }

  /** Returns the agenda fixtures keyed by beneficiary identifier. */
  static Map<String, List<Installment>> fixtures() {
    Map<String, List<Installment>> fixtures = new LinkedHashMap<>();

    // Happy path: three open installments, confirmed after the delay.
    fixtures.put(
        "BEN-001",
        List.of(
            parcela("PAR-001", "AGR-001", "450.00", "2026-12-05"),
            parcela("PAR-002", "AGR-001", "300.00", "2027-01-05"),
            parcela("PAR-003", "AGR-002", "1200.00", "2027-02-05")));

    // Rejected because the beneficiary identifier ends in -REJ.
    fixtures.put(
        "BEN-REJ", List.of(parcela("PAR-101", "AGR-003", "250.00", "2026-12-15")));

    // Rejected because the sum of the installments exceeds 5000.00.
    fixtures.put(
        "BEN-MARGIN", List.of(parcela("PAR-301", "AGR-004", "6000.00", "2026-12-20")));

    // The first two status reads sleep before answering.
    fixtures.put(
        "BEN-TIMEOUT", List.of(parcela("PAR-201", "AGR-005", "500.00", "2026-12-25")));

    return fixtures;
  }

  private static Installment parcela(
      String parcelaId, String convenioId, String valor, String dataDesconto) {
    return new Installment(
        parcelaId,
        convenioId,
        new BigDecimal(valor),
        LocalDate.parse(dataDesconto),
        InstallmentStatus.ABERTA);
  }
}
