package dio.budgeting.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated view of the financial health for a period: total income, total
 * expense, resulting balance and how many transactions were considered.
 * <p>
 * This is the read model behind the "financial summary" evolution: a new
 * query type on top of the base project's create/list transactions flow,
 * exposed both as a REST endpoint and as a new AI tool.
 */
public record FinancialSummaryView(
        LocalDate start,
        LocalDate end,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount
) {
}
