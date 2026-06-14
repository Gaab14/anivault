# AniVault — Contexto del Proyecto para Claude Code

## Descripción general

Aplicación fullstack de tipo watchlist personal de animes, similar a IMDb pero completamente local (sin autenticación ni cuentas). El usuario puede gestionar su colección personal de animes: agregar títulos, llevar seguimiento de su progreso, dejar reseñas privadas, reaccionar y ver estadísticas.

**No hay Spring Security. No hay multi-usuario. Todo es local.**

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.5 |
| Base de datos | H2 en modo archivo (persiste localmente) |
| ORM | Spring Data JPA + Hibernate |
| Build | Maven |
| Java | 21 |
| Frontend | Angular |
| Anime data | Jikan API (MyAnimeList, gratuita, sin API key) |
| Puerto backend | 8080 |
| Puerto frontend | 4200 |

---

## application.properties

```properties
spring.application.name=anivault

spring.datasource.url=jdbc:h2:file:./data/animewatchlist
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8080

spring.web.cors.allowed-origins=http://localhost:4200
```

> Consola H2: `http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:file:./data/animewatchlist` — User: `sa` — Password: (vacío)

---

## Estructura de paquetes — Backend

```
src/main/java/com/gaab/anivault/
├── domain/
│   ├── dto/
│   │   ├── AnimeRequestDto.java       # POST — datos de Jikan + status inicial
│   │   ├── AnimeUpdateDto.java        # PUT — solo datos personales del usuario
│   │   └── AnimeResponseDto.java      # Respuesta siempre
│   ├── enums/
│   │   ├── WatchStatus.java           # PENDING, WATCHING, COMPLETED, DROPPED
│   │   └── Reaction.java              # LIKED, LOVED, DISAPPOINTED
│   ├── exception/
│   │   ├── AnimeAlreadyExistsException.java
│   │   ├── AnimeDoesNotExistException.java
│   │   └── EpisodeDoesNotExistException.java
│   ├── repository/
│   │   └── AnimeRepository.java       # Interfaz de dominio
│   └── service/
│       └── AnimeService.java
├── persistence/
│   ├── crud/
│   │   └── CrudAnimeEntity.java       # Extiende JpaRepository
│   ├── entity/
│   │   └── AnimeEntity.java
│   ├── mapper/
│   │   └── AnimeMapper.java           # MapStruct
│   └── AnimeEntityRepository.java     # Implementa AnimeRepository
├── web/
│   ├── controller/
│   │   └── AnimeController.java
│   └── exception/
│       ├── Error.java                 # Record: type + message
│       └── RestExceptionHandler.java  # @RestControllerAdvice
└── AnivaultApplication.java
```

---

## Entidad — `AnimeEntity.java`

```java
@Entity
public class AnimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos de Jikan (cacheados localmente al agregar)
    private Long malId;           // identificador único, evita duplicados
    private String title;
    private String titleJapanese;
    private String imageUrl;
    private String synopsis;
    private String genre;
    private String studio;
    @Column(name = "release_year") private Integer year;   // "year" es reservado en H2
    @Column(name = "anime_type")   private String type;    // "type" es reservado en H2
    private Integer totalEpisodes;
    private Integer episodeDuration;
    private Double malScore;
    private String airedStatus;

    // Datos personales del usuario
    @Enumerated(EnumType.STRING) private WatchStatus status;
    @Enumerated(EnumType.STRING) private Reaction reaction;
    private Integer userRating;
    private Integer currentEpisode;
    private String userReview;
    private Boolean wouldRecommend;
    private LocalDate startedDate;
    private LocalDate completedDate;
    private LocalDateTime addedAt;

    @ElementCollection
    private List<String> tags;
}
```

---

## DTOs

### AnimeRequestDto — POST

```java
public record AnimeRequestDto(
    @NotNull  Long malId,
    @NotBlank String title,
    String titleJapanese, String imageUrl, String synopsis,
    String genre, String studio, Integer year, Integer totalEpisodes,
    Integer episodeDuration, Double malScore, String type,
    String airedStatus, WatchStatus status
) {}
```

### AnimeUpdateDto — PUT

```java
public record AnimeUpdateDto(
    WatchStatus status,
    Reaction reaction,
    @Min(1) @Max(10) Integer userRating,
    Integer currentEpisode,          // validado en repo contra totalEpisodes
    String userReview,
    Boolean wouldRecommend,
    List<String> tags,
    LocalDate startedDate,
    @PastOrPresent LocalDate completedDate
) {}
```

### AnimeResponseDto — respuesta siempre

Contiene todos los campos de la entity: id, malId, title, titleJapanese, imageUrl, synopsis, genre, studio, year, totalEpisodes, episodeDuration, malScore, type, airedStatus, status, reaction, userRating, currentEpisode, userReview, wouldRecommend, tags, startedDate, completedDate, addedAt.

---

## Endpoints REST

### AnimeController — `/animes`

| Método | Ruta | Descripción | Status |
|---|---|---|---|
| `GET` | `/animes` | Listar todos | 200 |
| `GET` | `/animes/{id}` | Detalle | 200 / 404 |
| `POST` | `/animes` | Agregar al vault | 201 / 409 |
| `PUT` | `/animes/{id}` | Actualizar datos personales | 200 / 404 / 400 |
| `DELETE` | `/animes/{id}` | Eliminar | 204 / 404 |

### Excepciones manejadas

| Excepción | HTTP |
|---|---|
| `AnimeDoesNotExistException` | 404 |
| `AnimeAlreadyExistsException` | 409 |
| `EpisodeDoesNotExistException` | 400 |
| `MethodArgumentNotValidException` (@Valid) | 400 |

Formato de error:
```json
{ "type": "anime-does-not-exist", "message": "El anime con ID: 5 no existe." }
```

---

## Flujo de datos — integración Jikan

```
1. Usuario busca en el frontend → Angular llama Jikan directamente
   GET https://api.jikan.moe/v4/anime?q=Naruto&limit=10

2. Usuario hace clic "Agregar al vault"
   → Angular hace POST /animes con datos del anime + status inicial
   → Backend verifica que malId no exista (existsByMalId)
   → Guarda en H2

3. Para ver la colección → GET /animes (desde H2, sin llamar a Jikan)
4. Para editar progreso → PUT /animes/{id}
```

> Jikan tiene rate limit de ~3 req/s. La metadata se cachea en H2 al agregar — nunca se vuelve a llamar a Jikan para ese anime.

---

## Estructura de carpetas — Frontend Angular

```
src/app/
├── core/
│   ├── services/
│   │   ├── anime.service.ts       # Habla con Spring Boot (/animes)
│   │   └── jikan.service.ts       # Habla con Jikan API directamente
│   └── interceptors/
│       └── error.interceptor.ts   # Maneja errores del RestExceptionHandler
│
├── shared/
│   ├── components/
│   │   ├── anime-card/            # Tarjeta reutilizable
│   │   └── status-badge/          # Badge de WATCHING, PENDING, etc.
│   └── models/
│       ├── anime-request.model.ts
│       ├── anime-response.model.ts
│       └── anime-update.model.ts
│
└── features/
    ├── vault/                     # Ruta: /  — colección completa con filtros
    ├── search/                    # Ruta: /search — búsqueda en Jikan
    ├── detail/                    # Ruta: /vault/:id — detalle + editar progreso
    └── stats/                     # Ruta: /stats — estadísticas personales
```

### Rutas

```
/              → vault (colección completa)
/search        → buscar en Jikan y agregar al vault
/vault/:id     → detalle de un anime + editar progreso
/stats         → estadísticas personales
```

### Modelos (espejo de los DTOs del backend)

```typescript
export type WatchStatus = 'PENDING' | 'WATCHING' | 'COMPLETED' | 'DROPPED';
export type Reaction = 'LIKED' | 'LOVED' | 'DISAPPOINTED';

export interface AnimeResponse {
  id: number;
  malId: number;
  title: string;
  titleJapanese: string;
  imageUrl: string;
  synopsis: string;
  genre: string;
  studio: string;
  year: number;
  totalEpisodes: number;
  episodeDuration: number;
  malScore: number;
  type: string;
  airedStatus: string;
  status: WatchStatus;
  reaction: Reaction | null;
  userRating: number | null;
  currentEpisode: number | null;
  userReview: string | null;
  wouldRecommend: boolean | null;
  tags: string[];
  startedDate: string | null;
  completedDate: string | null;
  addedAt: string;
}

export interface AnimeRequest {
  malId: number;
  title: string;
  titleJapanese: string;
  imageUrl: string;
  synopsis: string;
  genre: string;
  studio: string;
  year: number;
  totalEpisodes: number;
  episodeDuration: number;
  malScore: number;
  type: string;
  airedStatus: string;
  status: WatchStatus;
}

export interface AnimeUpdate {
  status?: WatchStatus;
  reaction?: Reaction;
  userRating?: number;
  currentEpisode?: number;
  userReview?: string;
  wouldRecommend?: boolean;
  tags?: string[];
  startedDate?: string;
  completedDate?: string;
}
```

---

## Decisiones de diseño tomadas

1. **Sin autenticación** — app 100% local, un solo usuario
2. **H2 en modo archivo** — persiste entre reinicios sin instalar nada
3. **Records para DTOs** — inmutables, Java 21
4. **Jikan en el frontend** — el backend no llama a APIs externas
5. **malId como identificador único** — evita duplicados en el vault
6. **Tags como @ElementCollection** — lista de strings sin entidad separada
7. **`year` y `type` con @Column** — son palabras reservadas en H2 2.x
8. **Arquitectura de capas en backend**: `domain` (DTOs, interfaces, service) separado de `persistence` (JPA, mappers) y `web` (controllers, handlers)
