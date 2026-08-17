# Rate Limiting Service

API de rate limiting com Redis e Token Bucket. Basicamente você manda um IP e a gente controla quantas requisições ele pode fazer.

## O que precisa

- Java 21
- Maven
- Redis rodando (no Windows usa Memurai, no Mac/Linux instala normalmente)

## Como rodar

### 1. Redis

Se não tem:

Windows:

choco install memurai
memurai


Linux/Mac:

brew install redis
redis-server


### 2. Aplicação

./mvnw spring-boot:run


Pronto, vai rodar em http://localhost:8080

## Como usar

Manda um POST pra `/api/v1/check-limit` com isso:

```json
{
  "ip": "192.168.1.100",
  "limite": 100,
  "windowSeconds": 60
}
```

Se passou no limite:
```json
{
  "allowed": true,
  "remaining": 99,
  "reset_at": 1706963578,
  "limite": 100,
  "window_seconds": 60,
  "retry_after": null
}
```

Se bloqueou (HTTP 429):
```json
{
  "allowed": false,
  "remaining": 0,
  "reset_at": 1706963579,
  "limite": 5,
  "window_seconds": 60,
  "retry_after": 45
}
```

## Parâmetros

- `ip`: IPv4 ou IPv6 (obrigatório)
- `limite`: Entre 1 e 1.000.000
- `windowSeconds`: Entre 1 e 86400 segundos

## Como funciona

Token Bucket basicamente:
- IP recebe N tokens (igual ao limite)
- Cada requisição consome 1 token
- Quando acaba token, bloqueia com 429
- Depois de X segundos, reseta

Exemplo com limite 5:

Req 1: tem 5 tokens, usa 1, sobra 4 ✓
Req 2: tem 4, usa 1, sobra 3 ✓
Req 3: tem 3, usa 1, sobra 2 ✓
Req 4: tem 2, usa 1, sobra 1 ✓
Req 5: tem 1, usa 1, sobra 0 ✓
Req 6: tem 0, não usa... ✗ (429 Too Many Requests)


## Testando com curl

```bash
curl -X POST http://localhost:8080/api/v1/check-limit \
  -H "Content-Type: application/json" \
  -d '{"ip":"192.168.1.100","limite":5,"windowSeconds":60}'
```

## Erros

Pode vir 400 se:
- IP inválido
- Limite <= 0
- WindowSeconds muito grande

Vem 503 se Redis cair.

Vem 429 se exceder o limite.

## Testes

./mvnw test


Tem 5 testes unitários, todos passando.

## Estrutura

src/main/java/com/paccanaro/ratelimit/
 config/ (configuração do Redis)

 controller/ (endpoint HTTP)

 dto/ (modelos de request/response)

 exception/ (exceções customizadas)

 service/ (lógica do Token Bucket)

---
 Artur Paccanaro