# Copilot Instructions — accounts-user-api

## Build, test, and lint commands

```bash
# Build (skip tests)
mvn package -DskipTests=true
# or
make build

# Run all tests (unit + integration)
make test                                  # runs mvn verify

# Run unit tests only
make test-unit                             # excludes "integration-test" group

# Run integration tests only
make test-integration                      # requires Docker (Testcontainers)

# Run a single test class
mvn test -Dtest=UsersServiceTest -DexcludedGroups="integration-test"

# Run a single test method
mvn test -Dtest=UsersServiceTest#fetchUser -DexcludedGroups="integration-test"

# SonarQube analysis
make sonar
```

## Architecture

Spring Boot 4 / Java 21 REST API. Owner of the `account.user` MongoDB resource.

**Layered structure:**
```
Controller → Service → Repository (MongoRepository<Users, String>)
```

**Controller interfaces** are generated from the API spec and live in `private-api-sdk-java` (`uk.gov.companieshouse.api.accounts.user.api.*Interface`). Controllers implement these interfaces — do not define `@RequestMapping` directly on controller methods unless the interface doesn't cover the route (e.g. `GET /user/profile`).

**DTO ↔ DAO mapping** is done with MapStruct. Mappers live in `mapper/`. The DAO model (`models/Users`) maps to the SDK DTO (`uk.gov.companieshouse.api.accounts.user.model.User`) via `UsersDtoDaoMapper`. Always use the mapper; never manually copy fields between DAO and DTO.

**ERIC gateway:** All inbound requests pass through ERIC, which sets auth headers. The interceptor chain (configured in `InterceptorConfig`) enforces:
- `/users/**` — `EricAuthorisedKeyPrivilegesInterceptor`: requires API key with both `internal-app` **and** `user-data` privileges, OR OAuth2 token with `/admin/user/roles` role
- `/internal/users/**` — `RolePermissionInterceptor` requiring `/admin/user/search`
- `/internal/admin/users/**` — `AdminUserRolePermissionInterceptor`: GET requires `/admin/user/search`, PATCH requires `/admin/user/unlinkonelogin`
- `/internal/admin/roles/**` — `RolePermissionInterceptor` requiring `/admin/roles`
- `/internal/admin/permissions/**` — `RolePermissionInterceptor` requiring `/admin/permissions`
- `/user/profile` — `TokenPermissionsInterceptor`

**Environment variables required at runtime:**
- `MONGODB_URL` — MongoDB connection URI
- `MONGODB_DATABASE` — database name
- `DATABASE_LIMIT` — max results for partial email search (default: 50)
- `OTEL_LOG_ENABLED` — enables OpenTelemetry log/trace/metric export (default: `false`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` — OTLP collector base URL; required only when `OTEL_LOG_ENABLED=true`

## Key conventions

### Logging
Use `StaticPropertyUtil.APPLICATION_NAMESPACE` as the logger namespace — it is populated from `spring.application.name` at startup:
```java
private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
```
Always pass a `Map<String, Object>` context to structured log calls rather than string concatenation.

### Exception handling
Throw one of the three custom runtime exceptions; `ControllerAdvice` maps them to HTTP responses returning a CH `Errors` object:
- `NotFoundRuntimeException` → 404
- `BadRequestRuntimeException` → 400
- `InternalServerErrorRuntimeException` → 500

### No Lombok
Write explicit constructors, getters, and setters. Constructor injection only.

### MongoDB updates
Use `org.springframework.data.mongodb.core.query.Update` with `UsersRepository.updateUser(userId, update)` for partial updates rather than saving the full document.

### Testing — unit tests
- Use `@WebMvcTest(ControllerClass.class)` with `MockMvc`
- `@MockBean` the service, `InterceptorConfig`, and `StaticPropertyUtil` to bypass ERIC interceptors
- Tag with `@Tag("unit-test")`

### Testing — integration tests
- Extend `BaseMongoIntegration` — spins up `mongo:7.0.17-jammy` via Testcontainers; requires Docker
- Tag with `@Tag("integration-test")`
- Use `TestDataManager.getInstance()` for canonical test fixtures (DAO and DTO flavours)

### BOM dependencies
This service imports `ch-dependency-bom` and `ch-test-bom`. Do not redeclare versions for dependencies already managed by those BOMs. Do not add a `pom.xml` dependency that is never imported in the codebase.
