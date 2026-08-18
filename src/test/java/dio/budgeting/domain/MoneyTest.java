package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void rejectsNullAmount() {
        assertThrows(InvalidTransactionException.class, () -> new Money(null));
    }

    @Test
    void rejectsZeroOrNegativeAmount() {
        assertThrows(InvalidTransactionException.class, () -> Money.of(BigDecimal.ZERO));
        assertThrows(InvalidTransactionException.class, () -> Money.of(BigDecimal.valueOf(-10)));
    }

    @Test
    void normalizesScaleToTwoDecimals() {
        Money money = Money.of(new BigDecimal("10.5"));
        assertEquals(new BigDecimal("10.50"), money.amount());
    }
}
