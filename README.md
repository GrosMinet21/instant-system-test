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

```bash
./mvnw install -DskipTests && ./mvnw spring-boot:run -pl parking-app
```

The application starts on port **8080** with an embedded PostgreSQL database (Zonky).  
Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`

---

## API

```
GET /parkings?lat=&lon=&radiusKm=
```

| Parameter  | Required | Description                 |
|------------|----------|-----------------------------|
| `lat`      | yes      | User latitude               |
| `lon`      | yes      | User longitude              |
| `radiusKm` | yes      | Search radius in kilometres |

Results are sorted by ascending distance. Each parking entry includes a `distanceMeters` field.

**Example response:**

```json
[
  {
    "name": "THEATRE",
    "capacity": 320,
    "availableSpaces": 50,
    "occupancyRate": 84.3,
    "latitude": 46.58,
    "longitude": 0.34,
    "city": "POITIERS",
    "distanceMeters": 243.7
  }
]
```

**Status codes:**

- `200` — list of parkings (empty if none within the radius)
- `400` — missing or invalid parameter (ProblemDetail RFC 9457)

---

### Technical choices

- **Embedded PostgreSQL (Zonky)**: zero configuration, the application is self-contained. A real PostgreSQL instance can
  be enabled via `embedded.postgres.enabled=false` + `spring.datasource.*` in `application.properties`. ⚠️ Intentionally
  simplified for this test project — not suitable for production.
- **Cron-based synchronisation**: data is cached in the database every 5 minutes (completely arbitrary, no thought given
  to this). The mobile API never depends on the availability of the external source — if the Poitiers API is down, the
  last known data continues to be served.
- **Plugin registry (ParkingSyncService)**: each data source implements `ParkingSyncService`. The generic cron iterates
  over all implementations detected by Spring. Adding a new city means creating a new package with its own Feign client,
  mapper, and service, without touching anything else.
- **Lightweight CQRS**: `ParkingReadRepository` (extends `Repository`, read-only) is used by the controller;
  `ParkingWriteRepository` (extends `JpaRepository`) is reserved for synchronisation. Same table, two distinct access
  contracts.
- **Haversine distance calculation**: performed in memory on the Java side. Justified by the low volume (a few dozen
  parkings). For larger volumes, PostGIS would be more appropriate.
- **MapStruct**: compile-time code generation, no reflection at runtime.

---

## Known issues not addressed

- **Authentication / security**: the API is public. In production, an authentication mechanism would be required (API
  key, OAuth2).
- **Pagination**: for dense areas, pagination would be necessary. Since the Haversine filter is applied in Java memory
  after a `SELECT *`, pagination cannot reduce the volume of data read from the database — the geographic filter would
  need to be pushed to PostgreSQL (via PostGIS or a SQL bounding box) for pagination to be truly effective.
- **CityName as configuration**: today adding a city requires extending the enum and writing code. A config-driven
  approach (sources declared in `application.yml`) was explored but not retained. The fundamental constraint remains
  unchanged: each city has a different source API format, so each requires its own mapper and client — these are code
  decisions, not configuration. The only real gain would be if multiple cities shared the same format (e.g. all on
  OpenDataSoft): a generic adapter per format could be written, and new cities in that format would only need a yml
  entry. Without this format convergence, config-driven adds complexity without removing the obligation to deliver code
  for each new city.
- **Monitoring**: Spring Actuator is present but no alerts are configured for sync failures.
- **Race condition on the cron**: in a multi-instance deployment, two nodes could synchronise in parallel. ShedLock
  (`@ScheduledLock`) would address this.
- **Swagger / OpenAPI**: skipped here (single route, no versioning). On a real multi-team project: the OpenAPI spec
  becomes the shared contract between producers and consumers, codegen prevents drift, and `/v1/` versioning lets the
  API evolve without breaking clients. The proper layering would also be `DTO ↔ VO ↔ Entity` — decoupling the API
  contract from internal value objects so changing a VO never accidentally changes what's exposed.

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
