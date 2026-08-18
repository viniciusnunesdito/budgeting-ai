package dio.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the {@link ChatClient} used by the voice assistant: a system prompt
 * describing the assistant's role plus the {@code @Tool} methods it is
 * allowed to call to actually create/query transactions.
 */
@Configuration
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
            Você é o assistente financeiro por voz do app de orçamento (budgeting).
            Sua função é interpretar comandos transcritos de áudio sobre transações
            financeiras e usar as ferramentas disponíveis para registrar despesas,
            registrar receitas, listar transações ou calcular o resumo financeiro.

            Regras importantes:
            - Nunca invente valores, datas ou categorias: se a informação não estiver
              clara no comando, faça a melhor inferência razoável ou peça esclarecimento
              na resposta em vez de chamar uma ferramenta com dados inventados.
            - Sempre use uma ferramenta quando a intenção for criar ou consultar uma
              transação; não responda de memória sobre saldos ou lançamentos.
            - Depois de usar uma ferramenta, responda de forma curta, natural e em
              português do Brasil, confirmando o que foi feito ou apresentando o
              resultado da consulta (ex: valores de receita, despesa e saldo).
            - Datas devem ser convertidas para o formato ISO yyyy-MM-dd antes de
              chamar uma ferramenta.
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, TransactionTools transactionTools) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(transactionTools)
                .build();
    }
}
