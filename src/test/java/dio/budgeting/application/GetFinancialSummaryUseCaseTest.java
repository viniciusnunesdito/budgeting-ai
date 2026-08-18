package dio.budgeting.application;

import dio.budgeting.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetFinancialSummaryUseCaseTest {

    private InMemoryTransactionRepository repository;
    private CreateTransactionUseCase createUseCase;
    private GetFinancialSummaryUseCase summaryUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        createUseCase = new CreateTransactionUseCase(repository);
        summaryUseCase = new GetFinancialSummaryUseCase(repository);
    }

    @Test
    void computesTotalsAndBalance() {
        createUseCase.execute(new CreateTransactionCommand(
                "Salary", BigDecimal.valueOf(3000), TransactionType.INCOME, "salary", LocalDate.now()));
        createUseCase.execute(new CreateTransactionCommand(
                "Rent", BigDecimal.valueOf(1200), TransactionType.EXPENSE, "housing", LocalDate.now()));
        createUseCase.execute(new CreateTransactionCommand(
                "Groceries", BigDecimal.valueOf(300), TransactionType.EXPENSE, "food", LocalDate.now()));

        FinancialSummaryView summary = summaryUseCase.execute(null, null);

        assertEquals(0, new BigDecimal("3000.00").compareTo(summary.totalIncome()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(summary.totalExpense()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(summary.balance()));
        assertEquals(3, summary.transactionCount());
    }

    @Test
    void excludesTransactionsOutsideTheRequestedPeriod() {
        createUseCase.execute(new CreateTransactionCommand(
                "Old salary", BigDecimal.valueOf(1000), TransactionType.INCOME, "salary",
                LocalDate.now().minusMonths(2)));
        createUseCase.execute(new CreateTransactionCommand(
                "Current salary", BigDecimal.valueOf(2000), TransactionType.INCOME, "salary", LocalDate.now()));

        FinancialSummaryView summary = summaryUseCase.execute(LocalDate.now().minusDays(1), LocalDate.now());

        assertEquals(0, new BigDecimal("2000.00").compareTo(summary.totalIncome()));
        assertEquals(1, summary.transactionCount());
    }
}
