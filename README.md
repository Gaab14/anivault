# AniVault — Backend

A personal anime collection manager REST API built with Java 21 and Spring Boot 3.5, featuring stateless JWT authentication and a clean layered architecture.

> **Frontend (Angular):** [anivault-frontend](https://github.com/Gaab14/anivault-frontend)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 17 (Docker) |
| Mapping | MapStruct 1.6.3 |
| Utilities | Lombok |
| Testing | JUnit 5 + Mockito (70 tests) |

---

## Architecture

The project follows a clean 3-layer architecture with clear separation of concerns:

```
src/main/java/com/gaab/anivault/
├── domain/
│   ├── dto/              # Request / Response / Update DTOs
│   ├── enums/            # WatchStatus, Reaction, Role
│   ├── exception/        # Business exceptions
│   ├── repository/       # Repository interface (domain contract)
│   └── service/          # AnimeService, AuthService, JwtService
├── persistence/
│   ├── crud/             # Spring Data JPA repositories
│   ├── entity/           # JPA entities (UserEntity, AnimeEntity)
│   ├── mapper/           # MapStruct mapper (Entity ↔ DTO)
│   └── AnimeEntityRepository.java  # Interface implementation
└── web/
    ├── controller/       # AnimeController, AuthController
    ├── exception/        # Global exception handler (RestExceptionHandler)
    ├── security/         # SecurityConfig, JwtFilter, EntryPoints
    └── CorsConfig.java
```

**Key design decisions:**
- Domain layer is completely decoupled from persistence (depends on interfaces, not JPA)
- All dependencies injected via constructor (no `@Autowired`)
- Stateless sessions — no server-side session state whatsoever
- DTOs separated by intent: `RequestDto`, `ResponseDto`, `UpdateDto`

---

## API Endpoints

### Authentication (public)

| Method | Route | Description |
|---|---|---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login and receive a JWT token |

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Anime Collection (requires `Authorization: Bearer <token>`)

| Method | Route | Description |
|---|---|---|
| `GET` | `/animes` | Get all animes for the authenticated user |
| `GET` | `/animes/{id}` | Get a specific anime by ID |
| `POST` | `/animes` | Add an anime to the collection |
| `PUT` | `/animes/{id}` | Update watch status, rating, progress |
| `DELETE` | `/animes/{id}` | Remove an anime from the collection |

---

## Security

- Passwords hashed with **BCrypt**
- JWT validated on every request via `JwtAuthenticationFilter` (`OncePerRequestFilter`)
- Unauthenticated requests return `401`, unauthorized access returns `403`
- CORS configured for frontend integration

---

## Getting Started

### Requirements

- Java JDK 21+
- Docker Desktop

> Maven is not required — the project includes Maven Wrapper (`mvnw`).

### Run locally

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Start the application
./mvnw spring-boot:run       # macOS / Linux
.\mvnw spring-boot:run       # Windows
```

API available at: `http://localhost:8080`

### Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/anivault
spring.datasource.username=anivault
spring.datasource.password=anivault
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your-secret-key-at-least-32-characters
jwt.expiration=86400000
```

Tables are created automatically by Hibernate on startup:
- `users` — username, email, BCrypt password, role
- `anime_entity` — anime data with watch tracking and ratings
- `anime_entity_tags` — custom tags per anime

---

## Running Tests

```bash
./mvnw test
```

Expected output: `Tests run: 70, Failures: 0, Errors: 0 — BUILD SUCCESS`

Tests use an **in-memory H2 database** — no Docker required to run them.

**Test coverage includes:**
- Unit tests for all service methods (Mockito)
- Integration tests for JPA repositories
- Integration tests for REST controllers (`@SpringBootTest`)
- Security configuration tests
