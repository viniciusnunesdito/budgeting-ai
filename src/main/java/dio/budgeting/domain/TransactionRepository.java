package dio.budgeting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Domain contract for persisting and querying {@link Transaction}s.
 * <p>
 * The implementation (JPA, in-memory, etc.) lives in the infrastructure
 * layer, so the domain and application layers never depend on a persistence
 * framework directly.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    List<Transaction> findAll();

    List<Transaction> findByType(TransactionType type);

    /** Inclusive date range. Either bound may be {@code null} to mean "no limit". */
    List<Transaction> findByOccurredAtBetween(LocalDate start, LocalDate end);

    void deleteById(TransactionId id);
}
