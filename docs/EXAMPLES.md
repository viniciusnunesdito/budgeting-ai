# Exemplos de uso da API

Todos os exemplos assumem a aplicação rodando em `http://localhost:8080` e a
variável `OPENAI_API_KEY` configurada antes do `./gradlew bootRun`.

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
