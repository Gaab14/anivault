# AniVault — Backend

API REST para la gestion de colecciones personales de anime con autenticacion JWT.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Security (JWT stateless con jjwt 0.12.6)
- Spring Data JPA / Hibernate
- PostgreSQL 17 (Docker)
- MapStruct 1.6.3
- Lombok
- JUnit 5 + Mockito (70 tests)

## Requisitos

- Java JDK 21+
- Docker Desktop

No se requiere instalar Maven; el proyecto incluye Maven Wrapper (`mvnw`).

## Inicio rapido

```bash
# 1. Levantar PostgreSQL
docker compose up -d

# 2. Ejecutar la aplicacion
.\mvnw spring-boot:run
```

La API estara disponible en `http://localhost:8080`.

## Tests

```bash
.\mvnw test
```

Resultado esperado: `Tests run: 70, Failures: 0, Errors: 0 — BUILD SUCCESS`

Las pruebas usan H2 en memoria (perfil `test`), independiente del contenedor de PostgreSQL.

## API Endpoints

### Autenticacion (publicos)

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/auth/register` | Registro de usuario |
| POST | `/auth/login` | Inicio de sesion (retorna JWT) |

### Anime (requieren header `Authorization: Bearer <token>`)

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/animes` | Listar animes del usuario |
| GET | `/animes/{id}` | Obtener anime por ID |
| POST | `/animes` | Agregar anime a la coleccion |
| PUT | `/animes/{id}` | Actualizar seguimiento |
| DELETE | `/animes/{id}` | Eliminar anime |

## Arquitectura

```
src/main/java/com/gaab/anivault/
├── domain/
│   ├── dto/              # Request/Response DTOs
│   ├── enums/            # WatchStatus, Reaction, Role
│   ├── exception/        # Excepciones de negocio
│   ├── repository/       # Interface del repositorio
│   └── service/          # AnimeService, AuthService, JwtService
├── persistence/
│   ├── crud/             # Repositorios JPA (CrudAnimeEntity, CrudUserEntity)
│   ├── entity/           # Entidades JPA (UserEntity, AnimeEntity)
│   ├── mapper/           # MapStruct mapper
│   └── AnimeEntityRepository.java
└── web/
    ├── controller/       # AnimeController, AuthController
    ├── exception/        # RestExceptionHandler
    ├── security/         # SecurityConfig, JwtFilter, EntryPoints
    └── CorsConfig.java
```

## Configuracion

**application.properties:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/anivault
spring.datasource.username=anivault
spring.datasource.password=anivault
spring.jpa.hibernate.ddl-auto=update
jwt.secret=tu-clave-secreta-de-al-menos-32-caracteres
jwt.expiration=86400000
```

## Base de datos

Las tablas se crean automaticamente via Hibernate (`ddl-auto=update`):

- `users` — Usuarios con username, email, password (BCrypt) y role
- `anime_entity` — Animes del usuario con seguimiento, resenias y puntuaciones
- `anime_entity_tags` — Etiquetas personalizadas por anime

## Frontend

El frontend (Angular 21) se encuentra en un repositorio separado: [anivault-frontend](https://github.com/Gaab14/anivault-frontend)
