package dio.budgeting.infrastructure.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the transactions REST flow: create → list → summary,
 * plus the validation error path. Runs against the in-memory H2 database
 * configured in {@code application-test.properties}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsListsAndSummarizesTransactions() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Salário","amount":3000,"type":"INCOME","category":"salário","occurredAt":"2026-08-01"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Salário"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Aluguel","amount":1200,"type":"EXPENSE","category":"moradia","occurredAt":"2026-08-05"}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpense").value(1200.00))
                .andExpect(jsonPath("$.balance").value(1800.00));
    }

    @Test
    void rejectsInvalidTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","amount":-5,"type":"EXPENSE","category":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages.length()").value(greaterThan(0)));
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/transactions/" + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
