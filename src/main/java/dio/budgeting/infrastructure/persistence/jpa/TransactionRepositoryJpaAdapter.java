package dio.budgeting.infrastructure.persistence.jpa;

import dio.budgeting.domain.Money;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the domain {@link TransactionRepository} contract on
 * top of Spring Data JPA. This is the only class that knows how to translate
 * between the domain aggregate and the JPA entity.
 */
@Repository
public class TransactionRepositoryJpaAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepository;

    public TransactionRepositoryJpaAdapter(SpringDataTransactionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        springDataRepository.save(toEntity(transaction));
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return springDataRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Transaction> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        return springDataRepository.findByType(type).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Transaction> findByOccurredAtBetween(LocalDate start, LocalDate end) {
        List<TransactionJpaEntity> entities;
        if (start != null && end != null) {
            entities = springDataRepository.findByOccurredAtBetween(start, end);
        } else if (start != null) {
            entities = springDataRepository.findByOccurredAtGreaterThanEqual(start);
        } else if (end != null) {
            entities = springDataRepository.findByOccurredAtLessThanEqual(end);
        } else {
            entities = springDataRepository.findAll();
        }
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(TransactionId id) {
        springDataRepository.deleteById(id.value());
    }

    private TransactionJpaEntity toEntity(Transaction transaction) {
        return new TransactionJpaEntity(
                transaction.id().value(),
                transaction.description(),
                transaction.amount().amount(),
                transaction.type(),
                transaction.category(),
                transaction.occurredAt(),
                transaction.createdAt()
        );
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.restore(
                new TransactionId(entity.getId()),
                entity.getDescription(),
                Money.of(entity.getAmount()),
                entity.getType(),
                entity.getCategory(),
                entity.getOccurredAt(),
                entity.getCreatedAt()
        );
    }
}
