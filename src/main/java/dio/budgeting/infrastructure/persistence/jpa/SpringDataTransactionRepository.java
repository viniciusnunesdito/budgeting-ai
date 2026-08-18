package dio.budgeting.infrastructure.persistence.jpa;

import dio.budgeting.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Spring Data JPA repository — the low-level Spring Data contract. */
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    List<TransactionJpaEntity> findByType(TransactionType type);

    List<TransactionJpaEntity> findByOccurredAtBetween(LocalDate start, LocalDate end);

    List<TransactionJpaEntity> findByOccurredAtGreaterThanEqual(LocalDate start);

    List<TransactionJpaEntity> findByOccurredAtLessThanEqual(LocalDate end);
}
