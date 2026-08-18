package dio.budgeting.application;

import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionNotFoundException;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Component;

/** Removes a transaction, e.g. to correct a mistaken voice command. */
@Component
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(TransactionId id) {
        if (transactionRepository.findById(id).isEmpty()) {
            throw new TransactionNotFoundException(id);
        }
        transactionRepository.deleteById(id);
    }
}
