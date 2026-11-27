# Instant System — Parking API

REST API built with Spring Boot exposing real-time parking availability near a given geographic position.

**Time spent:** ~8 hours

---

## Structure

```
parking-core/     → shared domain (entities, repositories, DTOs, ParkingSyncService)
parking-poitiers/ → Poitiers integration (Feign, mapper, service) — depends on parking-core
parking-app/      → Spring Boot application (REST, cron, config, Liquibase) — depends on both
```

## Quick start

**Prerequisites:** Java 17+ (the Maven wrapper is included).

```bash
./mvnw install -DskipTests && ./mvnw spring-boot:run -pl parking-app
```

The application starts on port **8080** with an embedded PostgreSQL database (Zonky) on port **5432** (fixed so that a
SQL client can connect to it: user `postgres`, database `postgres`). Parkings are synchronised right at startup.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Example: `curl "http://localhost:8080/v1/parkings?lat=46.58&lon=0.34&radiusKm=1"`

**Tests:** `./mvnw test` — unit tests, MockMvc tests and a sync integration test on the embedded database (random port,
scheduling disabled, Poitiers API mocked: the tests never call the real source).

---

## API

```
GET /v1/parkings?lat=&lon=&radiusKm=
```

| Parameter  | Required | Constraint      | Description                 |
|------------|----------|-----------------|-----------------------------|
| `lat`      | yes      | [-90, 90]       | User latitude               |
| `lon`      | yes      | [-180, 180]     | User longitude              |
| `radiusKm` | yes      | ]0, 10]         | Search radius in kilometres |

The maximum radius of **10 km** is arbitrary: it matches a "parkings near me" use case and bounds the response size.

Results are sorted by ascending distance. Each parking entry includes:

- `distanceMeters` — distance from the requested position;
- `lastUpdated` — when the source last measured the availability, so that clients can detect stale data (e.g. if the
  source is down and the last known data is served).

**Example response:**

```json
{
  "parkings": [
    {
      "name": "THEATRE",
      "capacity": 320,
      "availableSpaces": 50,
      "occupancyRate": 84.3,
      "latitude": 46.58,
      "longitude": 0.34,
      "city": "POITIERS",
      "distanceMeters": 243.7,
      "lastUpdated": "2026-11-27T09:52:40Z"
    }
  ]
}
```

**Status codes:**

- `200` — parkings within the radius (empty `parkings` list if none)
- `400` — missing, malformed or out-of-range parameter (ProblemDetail RFC 9457, `detail` names the faulty parameter)

**Contract stability** (the API must not change when cities are added):

- versioned route (`/v1/`);
- response wrapped in an object rather than a bare array, so metadata (pagination, search area...) can be added later
  without breaking clients;
- `city` exposed as a plain string: adding a city never changes the published schema (an enum would);
- the exposed `ParkingDto` is independent from any source format — each city maps its own format onto the common
  `Parking` entity.

---

### Technical choices

- **Embedded PostgreSQL (Zonky)**: zero configuration, the application is self-contained. A real PostgreSQL instance can
  be enabled via `embedded.postgres.enabled=false` + `spring.datasource.*` in `application.properties`. ⚠️ Intentionally
  simplified for this test project — not suitable for production.
- **Cron-based synchronisation**: data is cached in the database every 5 minutes (completely arbitrary, no thought given
  to this). The mobile API never depends on the availability of the external source — if the Poitiers API is down, the
  last known data continues to be served, and `lastUpdated` tells clients how old it is.
- **Sync semantics**: the source is the reference. Parkings are upserted on the source's stable business key
  (Poitiers `Id`, unique per city) — not on the data-fair `_id`, which is regenerated on every dataset refresh.
  Parkings missing from the payload are deleted; an empty payload is treated as a source failure and the last known
  data is kept. Invalid records (missing id/name, malformed coordinates) are skipped or stored without coordinates
  individually, and each city is synced independently, so one failure never blocks the rest.
- **No DB connection held during the remote call**: `PoitiersParkingSyncService` fetches the source outside of any
  transaction, then delegates to `PoitiersParkingWriter`, whose `@Transactional` method updates the database. Two beans
  rather than two methods of the same class, because Spring's proxy-based `@Transactional` is ignored on self-invocation.
- **Configurable source**: the full Poitiers endpoint URL (`parking.poitiers.url`) and the Feign timeouts (connect 5 s,
  read 10 s) are set in `application.properties`. If the source moves but keeps its format (dataset renamed, API
  version bump, proxy...), only the configuration changes; a different format still requires a new DTO/mapper.
- **Plugin registry (ParkingSyncService)**: each data source implements `ParkingSyncService`. The generic cron iterates
  over all implementations detected by Spring. Adding a new city means creating a new module/package with its own Feign
  client, mapper and service, and adding a value to the `CityName` enum — the REST layer is untouched.
- **Read/write repository segregation**: `ParkingReadRepository` (extends `Repository`) only exposes queries;
  `ParkingWriteRepository` (extends `JpaRepository`) is the only way to modify parkings and is used by the
  synchronisation only. Same table, two distinct access contracts.
- **Haversine distance calculation**: performed in memory on the Java side. Justified by the low volume (a few dozen
  parkings). For larger volumes, PostGIS would be more appropriate.
- **MapStruct**: compile-time code generation, no reflection-based mapping at runtime.
- **OpenAPI (springdoc)**: the spec is generated from the code (code-first) and exposed through Swagger UI, including the
  parameter constraints.

---

## Known issues not addressed

- **Parkings without coordinates**: 2 of the 9 Poitiers parkings (GARE EFFIA, CORDELIERS) have no geolocation in the
  real-time dataset. They are stored (availability is known) but can never appear in a proximity search; the sync logs
  how many are affected. A fix would be a static fallback (coordinates per source `Id`, e.g. from another Grand Poitiers
  dataset or a manual reference table).
- **Authentication / security**: the API is public. In production, an authentication mechanism would be required (API
  key, OAuth2), as well as rate limiting.
- **Pagination**: for dense areas, pagination would be necessary. Since the Haversine filter is applied in Java memory
  after a `SELECT *`, pagination cannot reduce the volume of data read from the database — the geographic filter would
  need to be pushed to PostgreSQL (via PostGIS or a SQL bounding box) for pagination to be truly effective. The response
  envelope already allows adding pagination metadata without breaking clients.
- **CityName as configuration**: today adding a city requires extending the enum and writing code. A config-driven
  approach (sources declared in `application.yml`) was explored but not retained. The fundamental constraint remains
  unchanged: each city has a different source API format, so each requires its own mapper and client — these are code
  decisions, not configuration. The only real gain would be if multiple cities shared the same format (e.g. all on
  OpenDataSoft): a generic adapter per format could be written, and new cities in that format would only need a yml
  entry. Without this format convergence, config-driven adds complexity without removing the obligation to deliver code
  for each new city.
- **Monitoring**: Spring Actuator is present, but there is no health indicator or alert on sync failures / data
  freshness.
- **Race condition on the cron**: in a multi-instance deployment, two nodes could synchronise in parallel. ShedLock
  (`@SchedulerLock`) would address this.
- **API contract governance**: the OpenAPI spec is generated from the code. On a real multi-team project it would be
  contract-first: the spec becomes the shared contract between producers and consumers and codegen prevents drift. The
  proper layering would also be `DTO ↔ VO ↔ Entity` — decoupling the API contract from internal value objects so
  changing a VO never accidentally changes what's exposed.
- **Industrialisation**: on a real project I would add a Dockerfile / docker-compose (real PostgreSQL instead of the
  embedded one), a CI pipeline (build, tests, coverage, Sonar), and externalised configuration per environment.

---

## Using Claude

This project was developed in collaboration with **Claude Code** (Anthropic) via the `claude` CLI.

**What Claude generated:**

- Initial Spring Boot project scaffolding (multi-module pom.xml, Liquibase config, Feign/Zonky integration)
- Core implementation: domain, sync service, REST controller, Haversine calculation

**What I did manually:**

- Restructuring into Maven modules (`parking-core` / `parking-poitiers` / `parking-app`)
- Dependency cleanup
- Writing the technical choices and known issues sections in this README

**Approach:** Claude was used step by step, with verification and review between each action — no blind acceptance of
generated output. Every architectural decision (module separation, plugin registry pattern, lightweight CQRS), every
technical trade-off, and every line that made it into the final codebase was understood, validated, and owned by me. I
am the full owner of this result.
