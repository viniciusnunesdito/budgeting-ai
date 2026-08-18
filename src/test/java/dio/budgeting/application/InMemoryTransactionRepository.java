package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simple in-memory test double for {@link TransactionRepository}, used to
 * unit test the use cases without booting Spring or a database.
 */
class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<TransactionId, Transaction> store = new LinkedHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        store.put(transaction.id(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        return store.values().stream().filter(t -> t.type() == type).toList();
    }

    @Override
    public List<Transaction> findByOccurredAtBetween(LocalDate start, LocalDate end) {
        return store.values().stream()
                .filter(t -> start == null || !t.occurredAt().isBefore(start))
                .filter(t -> end == null || !t.occurredAt().isAfter(end))
                .toList();
    }

    @Override
    public void deleteById(TransactionId id) {
        store.remove(id);
    }
}
