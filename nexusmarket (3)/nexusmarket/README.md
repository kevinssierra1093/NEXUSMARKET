# NexusMarket

Marketplace platform (Construcción de Software 2). Spring Boot 4.1 / Java 17, Hexagonal Architecture (Ports & Adapters) + DDD.

```
src/main/java/application
├── NexusMarketApplication.java
├── domain                          <- implemented (framework-free except @Service / Lombok)
│   ├── enums/                      ApprovalDecision
│   ├── exceptions/                 DomainException and business exceptions
│   ├── models/                     Person, Buyer, Seller, User, Warehouse, Product, Order, ...
│   ├── valueobjects/               DomainCatalog + business catalogs (OrderStatus, SystemRole, ...)
│   ├── ports/out/                  repository ports (one per aggregate) + JWT / password ports
│   └── services/                   one use case per class, organized by subdomain
├── adapters
│   ├── shared/                     ValueObjectResolver (catalog codes), TraceableEntityReferences
│   ├── out/persistence/mysql       <- implemented: JPA entities, Spring Data repositories, mappers,
│   │                                  one *MySqlAdapter per repository port
│   ├── out/persistence/mongodb     <- implemented: AuditLogDocument + AuditLogMongoAdapter
│   └── in/rest                     <- implemented: 14 controllers, request/response DTOs (records
│                                      with Bean Validation), mappers, GlobalExceptionHandler
└── infrastructure
    ├── database/                   <- implemented: repository scanning, initial administrator
    └── security/                   <- implemented: JWT (HS256), BCrypt, filter, SecurityConfig,
                                       AuthenticatedUserResolver
```

## Running locally

Requirements: JDK 17+ and MySQL + MongoDB (`docker compose up -d` starts both with `compose.yaml`).

```bash
./mvnw clean compile                 # Windows: .\mvnw.cmd clean compile
./mvnw spring-boot:run               # needs MySQL and MongoDB running
```

Configuration lives in `src/main/resources/application.properties` and can be overridden with
environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `MONGO_URI`, `JWT_SECRET`, `ADMIN_PASSWORD`, ...).
On the first start an `ADMINISTRATOR` user is created (`admin` / `Admin123*` by default).

`api-requests.http` contains the complete business flow (VS Code *REST Client* extension).
`mvn package` should use `-DskipTests` unless both databases are running (the `contextLoads`
test starts the application).

## Design documents (SDD)

```
SDD
├── Software Architecture/Software Architecture.md   layers, packages, REST endpoints, tables, security, transactions
└── domain
    ├── Domain Model.md
    ├── Domain Value Objects.md
    ├── Domain Services.md                           service catalog by subdomain + business flow
    ├── Output-ports.md                              port contracts, adapter mapping, hydration rules
    └── services/                                    one detailed file per subdomain (14 files)
```
