# Event Gateway

Event Gateway is a Spring Boot REST service that accepts account events, stores them in an H2 database, and synchronously forwards the corresponding transaction to an Account Service.

## Architecture

```text
HTTP client
    |
    v
EventGatewayController (/events)
    |
    v
EventGatewayService / EventServiceImpl
    |                         |
    v                         v
EventGatewayRepo         EventIngestionService
    |                         |
    v                         v
H2 database              AccountClient (OpenFeign)
                              |
                              v
                         Account Service
```

- **Controller:** Exposes the REST endpoints and validates incoming `EventDto` request bodies.
- **Service:** `EventServiceImpl` converts DTOs to entities, stores events, and maps stored events back to response DTOs.
- **Persistence:** `EventGatewayRepo` is a Spring Data JPA repository. Events are stored in the `events` table; optional metadata is stored as an element collection.
- **Downstream integration:** `EventIngestionService` sends each accepted event to the Account Service using OpenFeign.
- **Resilience:** The downstream call uses a Resilience4j circuit breaker and semaphore bulkhead. OpenFeign circuit-breaker integration is enabled, and its fallback reports downstream unavailability. These failures are returned as HTTP 503.
- **Error responses:** `GlobalExceptionHandler` maps request, controller, database, and unexpected errors to HTTP statuses with Spring `ProblemDetail` responses. Database internals are not included in client-facing error details.

Event creation is synchronous: the event is saved before the Account Service call is made. If forwarding fails, the API returns 503; the event may already have been saved. A client retry using the same event ID may encounter a database conflict.

## Requirements

- Java 21
- Network access on first run to download Maven and dependencies
- Account Service available at `http://localhost:8080` for successful event creation and forwarding

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Run locally

From the project directory (`EventGateway`):

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on port **8800** by default. Configure a different Account Service URL before starting the application with Spring's environment-variable override:

```powershell
$env:ACCOUNT_SERVICE_URL = "http://localhost:8081"
.\mvnw.cmd spring-boot:run
```

To package and run the executable JAR:

```powershell
.\mvnw.cmd package
java -jar .\target\EventGateway-0.0.1-SNAPSHOT.jar
```

### Configuration

Defaults are in `src/main/resources/application.yaml`.

| Setting | Default | Purpose |
|---|---|---|
| `server.port` | `8800` | Event Gateway HTTP port |
| `account-service.url` | `http://localhost:8080` | Base URL for the Account Service |
| `spring.datasource.url` | `jdbc:h2:mem:eventgateway` | In-memory H2 database |
| `spring.jpa.hibernate.ddl-auto` | `update` | Update the database schema on startup |

The H2 console is enabled at `http://localhost:8800/h2-console`. Use JDBC URL `jdbc:h2:mem:eventgateway`, username `sa`, and an empty password. The database is in memory and its contents are lost when the application stops.

The YAML also contains settings for Resilience4j retry, rate limiting, and time limiting. The current service call is annotated with the circuit breaker and semaphore bulkhead; retry, rate limiter, and time limiter behavior is not applied unless corresponding annotations or programmatic decorators are added.

## API

### Health check

`GET /health`

```powershell
curl.exe -i http://localhost:8800/health
```

Returns **200 OK** with `{"status":"UP"}` when the Event Gateway process is serving requests. This is a lightweight liveness endpoint; it does not check the database or Account Service.

### Create and forward an event

`POST /events`

```powershell
curl.exe -i -X POST http://localhost:8800/events `
  -H "Content-Type: application/json" `
  -d '{
    "eventId": "evt-1001",
    "accountId": "acct-42",
    "type": "CREDIT",
    "amount": 25.50,
    "currency": "USD",
    "eventTimestamp": "2025-01-02T03:04:05Z",
    "metadata": {
      "source": "web"
    }
  }'
```

Required fields:

- `eventId`, `accountId`, `currency`: non-blank strings.
- `type`: `CREDIT` or `DEBIT`.
- `amount`: a positive number.
- `eventTimestamp`: a parseable ISO-8601 instant with a timezone, for example `2025-01-02T03:04:05Z`. The current bean validation checks its string format; an impossible calendar date can pass that check and fail during mapping.
- `metadata`: optional object; values are persisted as strings.

Success returns **201 Created** and `Event created successfully`. Malformed JSON and fields that fail bean validation return **400 Bad Request**. If the Account Service cannot be reached, the endpoint returns **503 Service Unavailable**.

The gateway forwards a `POST` to:

```text
{account-service.url}/accounts/{accountId}/transactions
```

The forwarded body contains `eventId`, `amount`, `type`, and `eventTimestamp`.

### Get an event by ID

`GET /events/{id}`

```powershell
curl.exe -i http://localhost:8800/events/evt-1001
```

Returns **200 OK** with the event, or **404 Not Found** if the ID does not exist.

### List events for an account

`GET /events?account={accountId}`

```powershell
curl.exe -i "http://localhost:8800/events?account=acct-42"
```

Returns matching events ordered by event timestamp ascending, or **404 Not Found** when there are no events for the account. The `account` query parameter is required.

## Tests and coverage

Run the test suite:

```powershell
.\mvnw.cmd test
```

Run tests and the configured JaCoCo coverage checks:

```powershell
.\mvnw.cmd verify
```

Tests cover the REST endpoints, request validation, service and mapper behavior, downstream fallback, exception handling, and DTO methods. The JaCoCo verification rule enforces 100% instruction and branch coverage for included application code, and 100% instruction coverage for DTO classes. The bundle rule excludes bootstrap, entity, interface-only, repository, and DTO classes; DTOs have their own instruction-coverage rule.

## Operational notes

- Start the Account Service separately and ensure its transaction endpoint matches the Feign mapping before testing event creation.
- A failed Account Service call is surfaced to the API caller as 503; this application does not currently queue failed transactions for later delivery.
- The `/health` route is a custom liveness endpoint. Actuator endpoint exposure is configured in `application.yaml`, but an Actuator dependency is not currently declared in `pom.xml`; add it if detailed health components or metrics endpoints are needed.
