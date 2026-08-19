# Budgeting AI — Projeto Final (Bootcamp Java Santander / DIO — Trilha Spring)

API de orçamento pessoal que entende **comandos de voz** para registrar e consultar
transações financeiras, usando **Spring Boot + Spring AI**. É a minha entrega do
desafio de projeto final do módulo `05-spring-ai` da trilha
[dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track),
construída do zero a partir do enunciado do módulo, seguindo a mesma arquitetura em
camadas (domain / application / infrastructure) usada no restante da trilha.

## O que o projeto faz

Fluxo principal (voz → ação → voz):

1. A pessoa usuária envia um arquivo de áudio (`POST /api/assistant/voice`) dizendo,
   por exemplo, *"Registra uma despesa de 45 reais com almoço, categoria alimentação"*.
2. O áudio é transcrito para texto pelo `TranscriptionModel` (Whisper, via OpenAI).
3. O texto transcrito é enviado a um `ChatClient` do Spring AI, que decide qual
   **ferramenta** (`@Tool`) da aplicação chamar para atender o pedido.
4. A ferramenta escolhida executa um **use case real** da aplicação — o mesmo que a
   API REST usa — que valida e persiste (ou consulta) a transação no banco de dados.
5. O modelo compõe uma resposta final em português, que é convertida de volta em
   áudio (MP3) pelo `TextToSpeechModel` e devolvida junto com o texto e a transcrição.

Além do fluxo de voz, a API expõe endpoints REST tradicionais para criar, listar,
consultar, apagar transações e obter um **resumo financeiro** (receitas, despesas e
saldo em um período).

## Arquitetura

Mesma organização em camadas usada na trilha (DDD simplificado):

```
src/main/java/dio/budgeting/
├── domain/            → modelo de negócio, invariantes e contrato de repositório
│   ├── Transaction, TransactionId, Money, TransactionType
│   ├── TransactionRepository (interface)
│   └── InvalidTransactionException, TransactionNotFoundException
├── application/        → use cases (regra de aplicação), usados pelo REST e pela IA
│   ├── CreateTransactionUseCase, ListTransactionsUseCase, GetTransactionUseCase,
│   │   DeleteTransactionUseCase, GetFinancialSummaryUseCase
│   └── CreateTransactionCommand, TransactionView, FinancialSummaryView (DTOs)
└── infrastructure/     → adapters e integração externa
    ├── http/            → controllers REST, DTOs de request/response, exception handler
    ├── persistence/jpa/ → entidade JPA + adapter que implementa TransactionRepository
    └── ai/              → ChatClient, ferramentas (@Tool) e orquestração de voz
```

Regra seguida em todo o projeto: **a camada de IA nunca acessa o repositório ou a
entidade de domínio diretamente.** As ferramentas (`TransactionTools`) só chamam os
mesmos use cases que o `TransactionController` chama. Isso garante que um comando de
voz nunca consiga pular uma validação que uma requisição HTTP normal teria que
respeitar.

## Tecnologias usadas

- **Java 25** (toolchain do Gradle) + **Spring Boot 4.0.5**
- **Spring AI 2.0.0-M4** (`spring-ai-starter-model-openai`)
  - `ChatClient` + Tool Calling (`@Tool` / `@ToolParam`) para interpretar intenção e
    executar funções reais
  - `TranscriptionModel` (Whisper) para transcrever áudio em texto
  - `TextToSpeechModel` para gerar a resposta em áudio (MP3)
- **Spring Data JPA** + **MySQL** (via Docker Compose) para persistência
- **Bean Validation** (`spring-boot-starter-validation`) para validar requisições
- **H2** em memória, apenas no escopo de teste
- **JUnit 5** para os testes automatizados
- **Lombok**, para reduzir boilerplate na entidade JPA

## Como executar a aplicação

### Pré-requisitos

- Java 25 (o Gradle wrapper baixa o toolchain automaticamente se ele não existir)
- Docker (para subir o MySQL via `compose.yml`) — ou ajuste `application.properties`
  para outro banco de sua preferência
- Uma chave de API da OpenAI

### Passo a passo

```bash
# 1. Configure sua chave da OpenAI
export OPENAI_API_KEY="sua_chave_aqui"

# 2. Suba o banco de dados (o Spring Boot também sobe isso sozinho via
#    spring-boot-docker-compose, se o Docker estiver disponível)
docker compose up -d

# 3. Rode a aplicação
./gradlew bootRun

# 4. Rode os testes
./gradlew test
```

A API sobe em `http://localhost:8080`.

### Sem Docker (perfil `local`, H2)

Se não tiver Docker instalado, dá pra rodar sem MySQL usando um perfil alternativo
com H2 (banco em arquivo, sem precisar de container):

```bash
export OPENAI_API_KEY="sua_chave_aqui"
./gradlew bootRun --args='--spring.profiles.active=local'
```

Os dados ficam em `./data/budgeting.mv.db`. O `OPENAI_API_KEY` também tem um valor
padrão de placeholder, então a aplicação sobe mesmo sem ele — só os endpoints de IA
(`/api/assistant/*`) exigem uma chave real para funcionar de verdade.

## Endpoints

### Transações (REST tradicional)

| Método | Caminho                        | Descrição                                   |
|--------|---------------------------------|----------------------------------------------|
| POST   | `/api/transactions`             | Cria uma transação                            |
| GET    | `/api/transactions`              | Lista transações (`?type=`, `?start=`, `?end=`) |
| GET    | `/api/transactions/{id}`         | Busca uma transação por id                    |
| DELETE | `/api/transactions/{id}`         | Remove uma transação                          |
| GET    | `/api/transactions/summary`      | Resumo financeiro (`?start=`, `?end=`)        |

### Assistente de voz (IA)

| Método | Caminho               | Descrição                                                        |
|--------|------------------------|--------------------------------------------------------------------|
| POST   | `/api/assistant/voice` | `multipart/form-data`, campo `audio`. Retorna transcrição, resposta em texto e resposta em áudio (base64). |
| POST   | `/api/assistant/text`  | `{"message": "..."}`. Mesmo pipeline de IA, sem precisar de áudio. |

## Melhoria implementada (evolução sobre o projeto base)

O enunciado descreve um assistente que só cria e consulta transações "cruas". A
principal evolução que implementei foi:

**1. Resumo financeiro inteligente (`GetFinancialSummaryUseCase` + nova ferramenta de IA)**
Um novo tipo de consulta financeira — receitas, despesas e saldo, filtráveis por
período — exposto tanto em `GET /api/transactions/summary` quanto como uma nova
`@Tool` (`obterResumoFinanceiro`), para responder perguntas como *"como estão minhas
finanças este mês?"* diretamente por voz, e não só criar/listar lançamentos crus.

Junto com essa evolução principal, adicionei três melhorias menores que sustentam a
qualidade da entrega:

**2. Validações consistentes antes de salvar** — a mesma regra de negócio (valor
positivo, descrição/categoria não vazias, data não futura) é aplicada tanto na
entidade de domínio (`Transaction.create`) quanto na camada HTTP (`Bean Validation`
em `CreateTransactionRequest`), então uma transação inválida nunca é salva,
independente de vir da API REST ou de um comando de voz.

**3. Endpoint de texto (`POST /api/assistant/text`)** — permite testar todo o
pipeline de tool calling sem precisar gravar áudio, o que tornou o desenvolvimento e
os testes automatizados muito mais rápidos.

**4. Testes automatizados dos principais fluxos** — regras de domínio (`Money`,
`Transaction`, `TransactionType`), use cases (`CreateTransactionUseCase`,
`GetFinancialSummaryUseCase`) e o fluxo HTTP de transações, cobrindo tanto o caminho
feliz quanto os casos de validação.

## Como testar o fluxo principal

### 1. Testes automatizados

```bash
./gradlew test
```

### 2. Fluxo de texto (mais rápido para validar o tool calling)

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Registra uma despesa de 45 reais com almoço, categoria alimentação"}'
```

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Como estão minhas finanças este mês?"}'
```

### 3. Fluxo de voz completo

```bash
curl -X POST http://localhost:8080/api/assistant/voice \
  -F "audio=@meu-comando.mp3"
```

A resposta traz `transcript` (o que foi entendido), `reply` (a resposta em texto) e
`audioBase64` (a resposta falada, em MP3 codificado em base64). Mais exemplos,
incluindo como decodificar o áudio de volta para um arquivo `.mp3`, estão em
[`docs/EXAMPLES.md`](docs/EXAMPLES.md).

### 4. REST puro

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Salário","amount":5000,"type":"INCOME","category":"salário","occurredAt":"2026-08-01"}'

curl http://localhost:8080/api/transactions/summary
```

## O que eu aprendi

- Como estruturar uma aplicação Spring AI **sem deixar a IA vazar para o domínio**:
  o `ChatClient` e as ferramentas (`@Tool`) são só mais um adapter de entrada, no
  mesmo nível que um `@RestController` — ambos falam com os mesmos use cases.
- A diferença prática entre `TranscriptionModel` (áudio → texto), `ChatClient` com
  Tool Calling (texto → intenção → ação) e `TextToSpeechModel` (texto → áudio), e
  como encadear os três em um único fluxo síncrono.
- Que descrições boas em `@Tool`/`@ToolParam` — inclusive em português, no meu caso —
  são o que realmente faz o modelo escolher a ferramenta certa e preencher os
  parâmetros corretamente; é basicamente "documentação para a IA".
- Que validar tanto no DTO da camada HTTP quanto na entidade de domínio é o que
  garante que um comando de voz (que não passa pelo DTO) não consiga contornar as
  mesmas regras que uma requisição REST precisa seguir.
- Que vale a pena expor um caminho de teste mais barato (endpoint de texto) para uma
  funcionalidade cara/lenta de testar (voz), sem duplicar lógica.

## Créditos

Baseado no desafio de projeto final do módulo
[`05-spring-ai`](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/blob/main/05-spring-ai/README.md)
da trilha DIO Spring Boot Learning Track, ministrada pelo expert Poiani.
