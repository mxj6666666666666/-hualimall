# AGENTS.md

## Project snapshot
- Spring Boot + MyBatis product service under `src/main/java/com/xinjiema/hualimall`.
- Main flow: `controll/*Controller` -> `service/ProductService` -> `mapper/ProductMapper` -> `resources/com/xinjiema/hualimall/mapper/ProductMapper.xml`.
- API responses are wrapped with `pojo/Result<T>`; paged lists use `pojo/PageResult<T>`.

## Key code paths
- `ProductController` exposes read endpoints under `/products`.
- `SuProductController` exposes admin CRUD endpoints under `/admin/products`.
- `ProductServiceImpl` uses `PageHelper.startPage(page, pageSize)` before `selectProductPage(...)`.
- `ProductMapper.xml` must stay aligned with `ProductMapper.java` method names and parameter names.

## Conventions to preserve
- Keep the existing package name `controll` even though it looks misspelled; it is used by the current source tree.
- Controllers log requests with `Slf4j` and return `Result.success(...)` directly.
- Batch mapper methods depend on named collections, e.g. `updateBatch(@Param("products") ...)` matches `<foreach collection="products">`.
- `selectProductPage(ProQueryParams)` currently orders by `create_time DESC` and does not yet apply the commented filter fields.

## Build / run workflow
- Maven build: `mvn test` or `mvn package` from the repo root.
- Local run: `mvn spring-boot:run`.
- Java baseline in `pom.xml` is `25`; Spring Boot parent is `4.0.5`.

## Environment assumptions
- `src/main/resources/application.yml` points to local MySQL `jdbc:mysql://localhost:3306/hualimall` with user `root`.
- JPA is present in `pom.xml`, but the visible application flow is MyBatis/PageHelper-based.

## Editing rules
- When changing mapper methods, update the interface and XML together, including parameter annotations and `<foreach>` collection names.
- When changing paging behavior, keep `PageResult(total, rows)` construction in `ProductServiceImpl` consistent with `PageHelper`.
- Avoid editing build outputs under `target/`.

