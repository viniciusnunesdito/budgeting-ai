package dio.budgeting.infrastructure.persistence.jpa;

import dio.budgeting.domain.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA representation of a transaction. Kept separate from the domain
 * {@code Transaction} class so persistence annotations never leak into the
 * domain model (see {@link TransactionRepositoryJpaAdapter} for the mapping).
 */
@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 140)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false)
    private LocalDate occurredAt;

    @Column(nullable = false)
    private Instant createdAt;
}
