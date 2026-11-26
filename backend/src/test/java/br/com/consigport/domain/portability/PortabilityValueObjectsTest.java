package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the portability value objects.
 *
 * <p>Money carries two decimal places and never goes negative; the receiving account
 * carries the three mandatory fields of BR-06.
 */
class PortabilityValueObjectsTest {

  @Nested
  class MoneyTest {

    @Test
    void normalizesTheScaleToTwoDecimals() {
      assertThat(new Money(new BigDecimal("10")).amount()).isEqualTo(new BigDecimal("10.00"));
      assertThat(new Money(new BigDecimal("10.5")).amount()).isEqualTo(new BigDecimal("10.50"));
    }

    @Test
    void comparesByValue() {
      assertThat(new Money(new BigDecimal("10"))).isEqualTo(new Money(new BigDecimal("10.00")));
      assertThat(new Money(new BigDecimal("10.00"))).isNotEqualTo(new Money(new BigDecimal("11.00")));
    }

    @Test
    void acceptsZero() {
      assertThat(new Money(BigDecimal.ZERO).amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejectsNullAmounts() {
      assertThatThrownBy(() -> new Money(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("amount must not be null");
    }

    @Test
    void rejectsNegativeAmounts() {
      assertThatThrownBy(() -> new Money(new BigDecimal("-0.01")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("amount must not be negative");
    }

    @Test
    void rejectsAmountsWithMoreThanTwoDecimals() {
      assertThatThrownBy(() -> new Money(new BigDecimal("10.123")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("amount must have at most 2 decimal places");
    }
  }

  @Nested
  class ReceivingAccountTest {

    @Test
    void keepsEveryFieldTrimmed() {
      ReceivingAccount account = new ReceivingAccount(" 001 ", " 1234 ", " 56789-0 ");

      assertThat(account.bank()).isEqualTo("001");
      assertThat(account.branch()).isEqualTo("1234");
      assertThat(account.account()).isEqualTo("56789-0");
    }

    @Test
    void comparesByValue() {
      ReceivingAccount first = new ReceivingAccount("001", "1234", "56789-0");
      ReceivingAccount second = new ReceivingAccount("001", "1234", "56789-0");

      assertThat(first).isEqualTo(second);
      assertThat(first).isNotEqualTo(new ReceivingAccount("001", "1234", "99999-9"));
    }

    @Test
    void rejectsBlankFields() {
      assertThatThrownBy(() -> new ReceivingAccount(null, "1234", "56789-0"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("bank must not be blank");
      assertThatThrownBy(() -> new ReceivingAccount("001", "  ", "56789-0"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("branch must not be blank");
      assertThatThrownBy(() -> new ReceivingAccount("001", "1234", ""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("account must not be blank");
    }

    @Test
    void rejectsFieldsLongerThanTheContractAllows() {
      assertThatThrownBy(() -> new ReceivingAccount("B".repeat(65), "1234", "56789-0"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("bank must have at most 64 characters");
      assertThatThrownBy(() -> new ReceivingAccount("001", "A".repeat(33), "56789-0"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("branch must have at most 32 characters");
      assertThatThrownBy(() -> new ReceivingAccount("001", "1234", "C".repeat(65)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("account must have at most 64 characters");
    }
  }
}
