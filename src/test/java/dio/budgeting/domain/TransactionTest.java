package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void createsAValidTransaction() {
        Transaction transaction = Transaction.create(
                "Salary", Money.of(BigDecimal.valueOf(5000)), TransactionType.INCOME, "salary", LocalDate.now());

        assertEquals("Salary", transaction.description());
        assertEquals(TransactionType.INCOME, transaction.type());
        assertTrue(transaction.id() != null);
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(InvalidTransactionException.class, () -> Transaction.create(
                "  ", Money.of(BigDecimal.TEN), TransactionType.EXPENSE, "food", LocalDate.now()));
    }

    @Test
    void rejectsFutureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThrows(InvalidTransactionException.class, () -> Transaction.create(
                "Rent", Money.of(BigDecimal.TEN), TransactionType.EXPENSE, "housing", tomorrow));
    }

    @Test
    void defaultsOccurredAtToTodayWhenNull() {
        Transaction transaction = Transaction.create(
                "Coffee", Money.of(BigDecimal.valueOf(9.9)), TransactionType.EXPENSE, "food", null);
        assertEquals(LocalDate.now(), transaction.occurredAt());
    }

    @Test
    void signedAmountIsNegativeForExpenseAndPositiveForIncome() {
        Transaction expense = Transaction.create(
                "Groceries", Money.of(BigDecimal.valueOf(100)), TransactionType.EXPENSE, "food", LocalDate.now());
        Transaction income = Transaction.create(
                "Freelance", Money.of(BigDecimal.valueOf(100)), TransactionType.INCOME, "work", LocalDate.now());

        assertEquals(BigDecimal.valueOf(100).negate().setScale(2), expense.signedAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(100).setScale(2), income.signedAmount().setScale(2));
    }
}
