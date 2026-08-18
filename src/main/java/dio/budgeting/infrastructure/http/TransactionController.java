package dio.budgeting.infrastructure.http;

import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.DeleteTransactionUseCase;
import dio.budgeting.application.FinancialSummaryView;
import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.GetTransactionUseCase;
import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.application.TransactionView;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST adapter for the transaction use cases. Every handler is a thin
 * translation from HTTP to a use case call — no business logic lives here,
 * matching the same use cases the AI tool-calling layer invokes.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final GetFinancialSummaryUseCase getFinancialSummaryUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                  ListTransactionsUseCase listTransactionsUseCase,
                                  GetTransactionUseCase getTransactionUseCase,
                                  DeleteTransactionUseCase deleteTransactionUseCase,
                                  GetFinancialSummaryUseCase getFinancialSummaryUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
        this.getFinancialSummaryUseCase = getFinancialSummaryUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionView> create(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionView created = createTransactionUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<TransactionView> list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return listTransactionsUseCase.execute(type, start, end);
    }

    @GetMapping("/{id}")
    public TransactionView getOne(@PathVariable String id) {
        return getTransactionUseCase.execute(TransactionId.of(id));
    }

    @GetMapping("/summary")
    public FinancialSummaryView summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return getFinancialSummaryUseCase.execute(start, end);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteTransactionUseCase.execute(TransactionId.of(id));
        return ResponseEntity.noContent().build();
    }
}
