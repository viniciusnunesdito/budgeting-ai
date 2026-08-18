package dio.budgeting.application;

import dio.budgeting.domain.Money;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * Registers a new financial transaction (income or expense).
 * <p>
 * Single business capability, callable both from {@code TransactionController}
 * (REST) and from {@code TransactionTools} (AI tool calling) — the AI layer
 * never touches the repository or the entity directly, it always goes
 * through this use case, so every validation rule applies regardless of who
 * is asking.
 */
@Component
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionView execute(CreateTransactionCommand command) {
        Transaction transaction = Transaction.create(
                command.description(),
                Money.of(command.amount()),
                command.type(),
                command.category(),
                command.occurredAt()
        );
        Transaction saved = transactionRepository.save(transaction);
        return TransactionView.from(saved);
    }
}
