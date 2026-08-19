# Exemplos de uso da API

Todos os exemplos assumem a aplicação rodando em `http://localhost:8080` e a
variável `OPENAI_API_KEY` configurada antes do `./gradlew bootRun`.

## 0. Evidência real de execução (validada localmente)

Antes de considerar o projeto pronto, rodei a aplicação de verdade (perfil `local`,
H2, sem Docker) e capturei as respostas reais abaixo — não são exemplos ilustrativos.

**Aplicação sobe normalmente:**
```
2026-08-19T00:11:47.970-03:00  INFO 18384 --- [budgeting] [main] dio.budgeting.BudgetingApplication :
Started BudgetingApplication in 6.521 seconds (process running for 7.433)
```

**Criar receita e despesa (com acentuação, `alimentação`) — `201 Created`:**
```json
{"id":"84b9904c-385b-4cfa-9fee-80c9b520728f","description":"Salario","amount":5000.00,"type":"INCOME","category":"salario","occurredAt":"2026-08-01"}
{"id":"638a36ce-7c43-4673-8ab0-ae9c9781b516","description":"Supermercado","amount":320.50,"type":"EXPENSE","category":"alimentação","occurredAt":"2026-08-05"}
```

**Resumo financeiro (`GET /api/transactions/summary`) — cálculo real do saldo:**
```json
{"start":null,"end":null,"totalIncome":5000.00,"totalExpense":320.50,"balance":4679.50,"transactionCount":2}
```

**Validação rejeitando dados inválidos — `400 Bad Request`:**
```json
{"timestamp":"2026-08-19T03:12:40.908428900Z","status":400,"error":"VALIDATION_ERROR",
 "messages":["amount: amount must be greater than zero","description: description must not be blank","category: category must not be blank"]}
```

**Id inexistente — `404 Not Found`** (confirmado, corpo omitido).

**Ferramenta de IA chegando de verdade na OpenAI:** com uma chave placeholder
(`demo-key-not-set`), `POST /api/assistant/text` retornou `500`, e o log mostrou que
a chamada realmente saiu para a API da OpenAI e voltou um erro de autenticação
esperado — prova de que o `ChatClient`, o registro das `@Tool` e a propriedade
`spring.ai.openai.api-key` estão todos corretos; só falta uma chave real:
```
org.springframework.ai.retry.NonTransientAiException: HTTP 401 - {
  "error": { "message": "Incorrect API key provided: demo-key****-set. ..." }
}
```

Com uma `OPENAI_API_KEY` real, esse mesmo caminho retorna a resposta gerada pelo
modelo em vez do erro 401 (veja os exemplos ilustrativos nas seções 2 e 3 abaixo).

## 1. Transações (REST)

### Criar uma receita

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
        "description": "Salário",
        "amount": 5000,
        "type": "INCOME",
        "category": "salário",
        "occurredAt": "2026-08-01"
      }'
```

### Criar uma despesa

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
        "description": "Supermercado",
        "amount": 320.50,
        "type": "EXPENSE",
        "category": "alimentação",
        "occurredAt": "2026-08-05"
      }'
```

### Listar transações

```bash
curl "http://localhost:8080/api/transactions"
curl "http://localhost:8080/api/transactions?type=EXPENSE"
curl "http://localhost:8080/api/transactions?start=2026-08-01&end=2026-08-31"
```

### Resumo financeiro

```bash
curl "http://localhost:8080/api/transactions/summary"
curl "http://localhost:8080/api/transactions/summary?start=2026-08-01&end=2026-08-31"
```

Resposta esperada:

```json
{
  "start": "2026-08-01",
  "end": "2026-08-31",
  "totalIncome": 5000.00,
  "totalExpense": 320.50,
  "balance": 4679.50,
  "transactionCount": 2
}
```

### Requisição inválida (validação)

```bash
curl -i -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "", "amount": -10, "type": "EXPENSE", "category": ""}'
```

Retorna `400 Bad Request` com a lista de campos inválidos.

## 2. Assistente por texto (evolução — sem precisar de áudio)

> As respostas de `reply` abaixo são **ilustrativas** (o texto exato depende do
> modelo de IA na hora); o restante do fluxo — transcrição, escolha da ferramenta e
> persistência — é o mesmo já comprovado com dados reais na seção 0.

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Registra uma despesa de 45 reais com almoço, categoria alimentação"}'
```

```json
{
  "reply": "Prontinho! Registrei uma despesa de R$ 45,00 em alimentação (almoço) hoje."
}
```

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Como estão minhas finanças este mês?"}'
```

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Registra uma receita de 200 reais de venda de um produto, categoria vendas"}'
```

## 3. Assistente por voz (fluxo completo)

> Resposta abaixo **ilustrativa** — não gravei áudio real para este teste, mas a
> transcrição, o tool calling e a persistência seguem o mesmo caminho de código
> validado na seção 0; só a etapa de fala (Whisper/TTS) não foi exercitada aqui.

Grave um áudio curto (ex: `comando.mp3` ou `.wav`) dizendo algo como *"Registra uma
despesa de 60 reais com transporte, categoria transporte"* e envie:

```bash
curl -X POST http://localhost:8080/api/assistant/voice \
  -F "audio=@comando.mp3"
```

Resposta:

```json
{
  "transcript": "Registra uma despesa de 60 reais com transporte, categoria transporte",
  "reply": "Prontinho! Registrei uma despesa de R$ 60,00 em transporte hoje.",
  "audioBase64": "SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjYxLjcuMTAwAAAAAAAAAAAAAAD/+5DEAAAI...",
  "audioMimeType": "audio/mpeg"
}
```

Para ouvir a resposta, decodifique o base64 de volta para um arquivo `.mp3`:

```bash
# Linux/macOS/Git Bash
echo "$AUDIO_BASE64" | base64 -d > resposta.mp3

# PowerShell
[IO.File]::WriteAllBytes("resposta.mp3", [Convert]::FromBase64String($audioBase64))
```

Ou, com um pequeno script (salvando a resposta inteira em `resposta.json` primeiro):

```bash
curl -s -X POST http://localhost:8080/api/assistant/voice -F "audio=@comando.mp3" \
  | python3 -c "import json,base64,sys; d=json.load(sys.stdin); open('resposta.mp3','wb').write(base64.b64decode(d['audioBase64'])); print(d['transcript']); print(d['reply'])"
```

## 4. Erros comuns

| Situação | Resposta |
|---|---|
| Áudio vazio ou ausente | `400 Bad Request`, `error: INVALID_AUDIO` |
| Transação com valor <= 0 | `400 Bad Request`, `error: INVALID_TRANSACTION` |
| Id de transação inexistente | `404 Not Found`, `error: NOT_FOUND` |
| `OPENAI_API_KEY` não configurada | A aplicação sobe, mas qualquer chamada à IA falha ao contatar a OpenAI |
