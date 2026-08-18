package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.CreateTransactionCommand;
import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.FinancialSummaryView;
import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.application.TransactionView;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.TransactionType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Business capabilities exposed to the language model as {@code @Tool}
 * methods. Every method is a thin wrapper around an application use case —
 * the exact same use case the REST controller calls — so a voice command
 * can never do anything a regular HTTP request could not, and every
 * validation rule from the domain layer still applies.
 */
@Component
public class TransactionTools {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetFinancialSummaryUseCase getFinancialSummaryUseCase;

    public TransactionTools(CreateTransactionUseCase createTransactionUseCase,
                             ListTransactionsUseCase listTransactionsUseCase,
                             GetFinancialSummaryUseCase getFinancialSummaryUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.getFinancialSummaryUseCase = getFinancialSummaryUseCase;
    }

    @Tool(description = "Registra uma nova transação financeira (uma receita ou uma despesa) informada pela pessoa usuária.")
    public TransactionView registrarTransacao(
            @ToolParam(description = "Descrição curta da transação, ex: 'almoço no restaurante'") String descricao,
            @ToolParam(description = "Valor da transação em reais, sempre um número positivo") BigDecimal valor,
            @ToolParam(description = "Tipo da transação: RECEITA para dinheiro que entra, DESPESA para dinheiro que sai") String tipo,
            @ToolParam(description = "Categoria da transação, ex: alimentação, transporte, salário, lazer") String categoria,
            @ToolParam(description = "Data em que ocorreu, no formato ISO yyyy-MM-dd. Se não informada, usa a data de hoje", required = false) String data
    ) {
        return createTransactionUseCase.execute(new CreateTransactionCommand(
                descricao,
                valor,
                TransactionType.parse(tipo),
                categoria,
                parseDate(data)
        ));
    }

    @Tool(description = "Lista as transações financeiras já registradas, podendo filtrar por tipo (RECEITA ou DESPESA).")
    public List<TransactionView> listarTransacoes(
            @ToolParam(description = "Tipo para filtrar: RECEITA, DESPESA, ou vazio para listar todas", required = false) String tipo
    ) {
        TransactionType type = (tipo == null || tipo.isBlank()) ? null : TransactionType.parse(tipo);
        return listTransactionsUseCase.execute(type, null, null);
    }

    @Tool(description = "Calcula o resumo financeiro (total de receitas, total de despesas e saldo) em um período. "
            + "Use para responder perguntas como 'como estão minhas finanças este mês?' ou 'qual meu saldo?'.")
    public FinancialSummaryView obterResumoFinanceiro(
            @ToolParam(description = "Data inicial do período, formato ISO yyyy-MM-dd. Vazio significa sem limite inicial", required = false) String dataInicio,
            @ToolParam(description = "Data final do período, formato ISO yyyy-MM-dd. Vazio significa sem limite final", required = false) String dataFim
    ) {
        return getFinancialSummaryUseCase.execute(parseDate(dataInicio), parseDate(dataFim));
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidTransactionException("Invalid date, expected ISO yyyy-MM-dd: " + raw);
        }
    }
}
