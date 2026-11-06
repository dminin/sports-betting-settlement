# AGENTS.md

Bet settlement service for a backend home assignment. The spec is `doc/Home_Assignment_Backend_Engineer_-_Kafka___RocketMQ.pdf`. The graders assess OO modelling, Java code and code structure, so clean design matters more than features. `README.md` is the human guide: run steps, API, design and accepted trade-offs.

Flow: `POST /api/event-outcomes` -> Kafka `event-outcomes` -> `MatchBetsService` -> `BetSettlementPublisher` -> RocketMQ `bet-settlements` -> `SettleBetService` (WON if the bet's `eventWinnerId` equals the outcome winner, else LOST).

## Commands

- Build and run all tests (the same command as CI in `.github/workflows/ci.yml`): `./mvnw -B -ntp verify`. Needs JDK 25.
- One test class or method: `./mvnw test -Dtest=BetTest` or `./mvnw test -Dtest='BetTest#betOnTheWinnerIsWon'`.
- The end-to-end tests need Docker: `RocketMqBetSettlementFlowTest` (Testcontainers Kafka and real RocketMQ) and `BetSettlementFlowTest` (Testcontainers Kafka, `mock` profile). Without Docker they are skipped, so a green build without Docker does not prove the flow.
- Run with RocketMQ mocked (needs only Kafka): `docker compose up -d kafka`, then `./mvnw spring-boot:run -Dspring-boot.run.profiles=mock`.
- Run with real RocketMQ: `docker compose up -d`, wait until `docker compose ps -a` shows `rocketmq-init` as `Exited (0)`, then `./mvnw spring-boot:run`.
- Manual check: run the requests in `http/bet-settlement.http` from top to bottom. Each one has a comment with the expected result for the seeded data. Restart the app to reset the H2 data.
- Stop the brokers: `docker compose down`.

## Stack facts that differ from older Spring

- Spring Boot 4.1 uses Jackson 3: the packages are `tools.jackson.*` (`JsonMapper`, `JacksonException`), not `com.fasterxml.jackson.databind`. The Kafka serde is `JacksonJsonSerializer` / `JacksonJsonDeserializer`.
- Boot 4 test APIs: `@WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`, and mocks use `@MockitoBean`.
- Testcontainers 2: `org.testcontainers.kafka.KafkaContainer`. RocketMQ has no Testcontainers module, so `RocketMqBetSettlementFlowTest` uses a `GenericContainer`.
- RocketMQ uses the plain gRPC client `rocketmq-client-java` 5.x through the proxy on `localhost:8081`, with the beans in `config/RocketMqConfig`. Keep this client: the RocketMQ Spring starter targets Boot 2.7 and Jackson 2. The gRPC client cannot create topics, so the `rocketmq-init` container creates the topic (and the test container creates it in its start script).
- In `RocketMqBetSettlementFlowTest` the proxy runs in local mode, and topic routes carry the broker IP and the proxy port. So the broker advertises `brokerIP1=127.0.0.1`, and the host port equals the container port. Without this, the client cannot connect on macOS.

## Architecture rules

Layered packages under `com.sporty.settlement`. The package map is in `README.md`, section "Design".

- The REST controllers (`web`) and the message listeners (`messaging`) call services. Services call `persistence` and the message publishers. `domain` has no Spring or JPA imports.
- Use concrete classes. Add an interface only when there are two or more implementations. Today `BetSettlementPublisher` is the only interface: RocketMQ in the default profile and in-process in the `mock` profile.
- Put each DTO in the `dto` subpackage of the layer that owns it: `web/dto` (REST bodies with Bean Validation), `messaging/dto` (wire contracts of the topics), `service/dto` (service input). Put the mapping methods on the DTO record (`from`, `toDomain`, `toCommand`).
- The domain `Bet` and `BetJpaEntity` are separate. The JPA types are package-private in `persistence`, and only `BetRepository` is public. Change the bet state only through `Bet.place`, `Bet.restore` and `Bet.settle`.
- Make classes package-private by default. Make a class public only when another package uses it.

## Invariants

- Both brokers deliver at least once, so every handler must be idempotent. Matching selects only `PENDING` bets. Settlement skips settled bets. `@Version` optimistic locking stops concurrent duplicates.
- `MatchBetsService` has no transaction on purpose: no database transaction stays open during broker I/O.
- Three places classify errors, and they must agree: `web/GlobalExceptionHandler` (HTTP status), `config/KafkaConfig` (exceptions that are not retried) and `messaging/rocketmq/BetSettlementRocketMqConsumer` (acknowledge or redeliver). If you add an exception type, decide how each of the three handles it.
- Kafka records use `eventId` as the key, so the outcomes of one event stay in order.
- Get the time from the injected `Clock` (`config/ClockConfig`). Tests use `Clock.fixed`.

## Change together

- If you move or rename `messaging/dto/EventOutcomeMessage`, update `spring.json.value.default.type` in `application.yml`. The compiler does not check it, so only the app at runtime or the end-to-end tests show the error.
- If you change the RocketMQ topic or consumer group in `application.yml`, update `docker-compose.yml` (`rocketmq-init`) and the start script in `RocketMqBetSettlementFlowTest`.
- If the REST API changes, update the API table in `README.md` and `http/bet-settlement.http`.
- If you add or move a package, update the package map in the "Design" section of `README.md`.
- If the seed bets in `config/SeedDataConfig` change, update the seed table in `README.md` and the expected results in `http/bet-settlement.http`.

## Code style

No formatter is configured, so match the code around you.

- Use records for DTOs and value objects. Inject dependencies through package-private constructors: `@RequiredArgsConstructor(access = AccessLevel.PACKAGE)`, or a hand-written constructor when it does more than assign fields. Main code does not use `var` or field `@Autowired`.
- Lombok: `@Slf4j` for loggers, `@RequiredArgsConstructor` for injection, and `@Getter` / `@NoArgsConstructor` / `@AllArgsConstructor` on JPA entities. Do not use `@Data`, `@Builder`, `@Value`, `@Setter` or experimental features. The domain `Bet` keeps hand-written record-style accessors.
- Use 4-space indents and lines of up to 120 characters. Put static imports first, then the other imports in alphabetical order, with no wildcards.
- Log with SLF4J `{}` placeholders, and include the ids (`betId`, `eventId`).
- Write Javadoc only for contracts: null semantics, thrown exceptions, idempotency. Write a comment only for a constraint that the code cannot show.
- Put input validation on `web/dto` with Bean Validation. Put domain invariants in constructors (`Preconditions.requireNonBlank`), which throw `IllegalArgumentException` and give HTTP 400.

## Tests

- Unit tests: Mockito (`mock()` or `@Mock`), BDDMockito `given` / `willThrow`, AssertJ. Build a bet in a given state with `Bet.restore(...)`.
- Web slice tests: `@WebMvcTest(SomeController.class)` and `@MockitoBean` for the services that the controller uses.
- End-to-end tests: extend `AbstractBetSettlementFlowTest` (`@SpringBootTest`, Testcontainers Kafka, Awaitility for asynchronous settlement). One subclass uses the `mock` profile, and the other uses the default profile with a real RocketMQ container.
- Put a test in the same package as the class it tests. Name test methods as behaviour sentences, for example `settlesPendingBetAndSavesIt`.

## Git and boundaries

- Commit subjects are imperative and capitalized, with no prefix and no final period. Make one logical change per commit (see `git log`).
- `target/`, `.idea/`, `.cursor/` and `_task/` are generated or local, and git ignores them. `doc/` holds the assignment spec: read it, and keep it unchanged.
- Ask before you add a dependency, change Docker images or ports, change the CI workflow, or change a message contract (topic names, JSON fields, message key).
- The "Assumptions and trade-offs" section of `README.md` holds decisions that are settled. Raise one again only with new evidence.
