package dio.budgeting.application;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateTransactionUseCaseTest {

    private InMemoryTransactionRepository repository;
    private CreateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        useCase = new CreateTransactionUseCase(repository);
    }

    @Test
    void createsAndPersistsATransaction() {
        TransactionView view = useCase.execute(new CreateTransactionCommand(
                "Groceries", BigDecimal.valueOf(150), TransactionType.EXPENSE, "food", LocalDate.now()));

        assertEquals("Groceries", view.description());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rejectsInvalidAmount() {
        assertThrows(InvalidTransactionException.class, () -> useCase.execute(new CreateTransactionCommand(
                "Groceries", BigDecimal.ZERO, TransactionType.EXPENSE, "food", LocalDate.now())));
    }
}
