package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTypeTest {

    @Test
    void parsesEnglishAndPortugueseSynonyms() {
        assertEquals(TransactionType.INCOME, TransactionType.parse("income"));
        assertEquals(TransactionType.INCOME, TransactionType.parse("Receita"));
        assertEquals(TransactionType.EXPENSE, TransactionType.parse("expense"));
        assertEquals(TransactionType.EXPENSE, TransactionType.parse("despesa"));
        assertEquals(TransactionType.EXPENSE, TransactionType.parse("gasto"));
    }

    @Test
    void rejectsUnknownType() {
        assertThrows(InvalidTransactionException.class, () -> TransactionType.parse("invalid"));
        assertThrows(InvalidTransactionException.class, () -> TransactionType.parse(""));
    }
}
