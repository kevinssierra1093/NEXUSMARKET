# Software Architecture

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles, the same architecture used by the organization's reference project.

The primary objective of this architecture is to isolate the business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols and infrastructure concerns.

Technology stack:

| Concern            | Technology |
| ------------------ | ---------- |
| Language / runtime | Java 17 |
| Framework          | Spring Boot 4.1 (Web MVC, Security, Data JPA, Data MongoDB) |
| Relational storage | MySQL (business data and operations) |
| Document storage   | MongoDB (audit logs) |
| Authentication     | JWT (stateless) |
| Boilerplate        | Lombok |

---

# Architectural Principles

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.

The domain contains all business rules and never depends on external technologies. The only framework elements tolerated in the domain are `@Service` (component scanning) and Lombok annotations, as in the reference project.

---

# Architecture Layers

```text
Application
│
├── Adapters          (in: REST  ·  out: MySQL, MongoDB)
│
├── Domain            (models, value objects, services, ports, exceptions)   ← implemented
│
└── Infrastructure    (config, database, security)
```

---

# Package Structure

```text
src/
└── main/
    └── java/
        └── application/
            │
            ├── NexusMarketApplication.java
            │
            ├── adapters/
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       ├── mappers/
            │   │       └── handlers/          (GlobalExceptionHandler)
            │   │
            │   └── out/
            │       └── persistence/
            │           ├── mysql/
            │           │   ├── adapters/
            │           │   ├── entities/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           └── mongodb/
            │               ├── adapters/
            │               ├── documents/
            │               ├── repositories/
            │               └── mappers/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   ├── exceptions/
            │   └── ports/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
```

---

# Layer Responsibilities

## Application

The `application` package is the root of the project. `NexusMarketApplication.java` is the entry point: it boots Spring, loads the infrastructure, performs dependency injection and starts the REST server.

---

# Adapters

Adapters connect external technologies with the business domain. They translate external requests into domain operations and transform domain objects into technology-specific representations. The domain never communicates directly with external systems.

---

## Input Adapters — `adapters/in/rest`

### Responsibilities

- Receive HTTP requests.
- Validate the format of incoming data (Bean Validation on Request DTOs).
- Resolve the authenticated `User` (`requestingUser`) from the security context.
- Convert Request DTOs into Domain Models.
- Execute the domain services (use cases) inside a transaction.
- Convert domain results into Response DTOs.

Controllers must never implement business rules.

### Controllers

One controller per subdomain. Base path: `/api`.

| Controller                | Base path            | Domain services |
| ------------------------- | -------------------- | --------------- |
| `AuthController`          | `/api/auth`          | `LoginService`, `RegisterBuyerService` + `RegisterParticipantUserService` |
| `UserController`          | `/api/users`         | `RegisterStaffUserService`, `RegisterParticipantUserService`, `ConsultUserService`, `ChangeUserStatusService`, `ChangeUserPasswordService` |
| `BuyerController`         | `/api/buyers`        | `ConsultBuyerService`, `UpdateBuyerService`, `ChangeBuyerStatusService` |
| `SellerController`        | `/api/sellers`       | `RegisterSellerService`, `ConsultSellerService`, `UpdateSellerService`, `ChangeSellerStatusService` |
| `WarehouseController`     | `/api/warehouses`    | `RegisterWarehouseService`, `ConsultWarehouseService`, `UpdateWarehouseService`, `ChangeWarehouseStatusService` |
| `ProductController`       | `/api/products`      | Catalog services |
| `InventoryController`     | `/api/inventory`     | `RegisterInventoryInboundService`, `AdjustInventoryService`, `ChangeInventoryItemStatusService`, `ConsultInventoryService`, `ConsultInventoryMovementsService` |
| `CartController`          | `/api/cart`          | `AddItemToCartService`, `UpdateCartItemQuantityService`, `RemoveItemFromCartService`, `ConsultCartService` |
| `OrderController`         | `/api/orders`        | `ConfirmOrderService`, `ConfirmOrderPaymentService`, `DispatchOrderService`, `ConsultOrderService`, `ConsultInvoiceService`, `ConsultShipmentService` |
| `ShipmentController`      | `/api/shipments`     | `UpdateShipmentStatusService`, `ConfirmShipmentDeliveryService` |
| `ReturnController`        | `/api/returns`       | Return services, `RequestRefundService`, `ConsultRefundService.executeByReturnRequest` |
| `RefundController`        | `/api/refunds`       | `ApproveRefundService`, `RejectRefundService`, `ProcessRefundService`, `ConsultRefundService.executeAll` |
| `ReportController`        | `/api/reports`       | Report services |
| `AuditController`         | `/api/audit`         | `ConsultOperationsService`, `ConsultAuditLogsService` |

Internal domain services (`ReserveInventoryService`, `RegisterSaleOutboundService`, `RegisterInventoryReturnService`, `RecordInventoryMovementService`, `IssueInvoiceService`, `AnnulInvoiceService`, `CreateShipmentsService`, `FinalizeOrderService`, `ResolveBuyerCartService`, `CalculateOrderTotalService`, `RegisterOperationService`, `RegisterAuditLogService`, `RegisterOperationAndAuditService` and the authorization services) are **never** exposed through a controller.

### Endpoints

| Method | Path | Service |
| ------ | ---- | ------- |
| POST   | `/api/auth/login` | `LoginService` *(public)* |
| POST   | `/api/auth/register-buyer` | `RegisterBuyerService` + `RegisterParticipantUserService` *(public, one transaction)* |
| POST   | `/api/users/staff` | `RegisterStaffUserService` |
| POST   | `/api/users/participants` | `RegisterParticipantUserService` (seller access) |
| GET    | `/api/users` · `/api/users/{userId}` · `/api/users/me` | `ConsultUserService` |
| PATCH  | `/api/users/{userId}/status` | `ChangeUserStatusService` |
| PATCH  | `/api/users/{userId}/password` | `ChangeUserPasswordService` |
| GET    | `/api/buyers` · `/api/buyers/{identification}` | `ConsultBuyerService` |
| PUT    | `/api/buyers/{identification}` | `UpdateBuyerService` |
| PATCH  | `/api/buyers/{identification}/status` | `ChangeBuyerStatusService` |
| GET    | `/api/buyers/{identification}/orders` | `ConsultOrderService.executeByBuyer` |
| GET    | `/api/buyers/{identification}/returns` | `ConsultReturnService.executeByBuyer` |
| POST   | `/api/sellers` | `RegisterSellerService` (body: seller + first warehouse) |
| GET    | `/api/sellers` · `/api/sellers/{identification}` | `ConsultSellerService` |
| PUT    | `/api/sellers/{identification}` | `UpdateSellerService` |
| PATCH  | `/api/sellers/{identification}/status` | `ChangeSellerStatusService` |
| GET    | `/api/sellers/{identification}/warehouses` | `ConsultWarehouseService.executeBySeller` |
| GET    | `/api/sellers/{identification}/products` | `ConsultPublishedCatalogService.executeBySeller` |
| POST   | `/api/warehouses` | `RegisterWarehouseService` |
| GET    | `/api/warehouses` · `/api/warehouses/{id}` | `ConsultWarehouseService` |
| PUT    | `/api/warehouses/{id}` | `UpdateWarehouseService` |
| PATCH  | `/api/warehouses/{id}/status` | `ChangeWarehouseStatusService` |
| GET    | `/api/warehouses/{id}/inventory` | `ConsultInventoryService.executeByWarehouse` |
| POST   | `/api/products` | `RegisterProductService` |
| GET    | `/api/products` | `ConsultPublishedCatalogService.execute` |
| GET    | `/api/products/{id}` | `ConsultProductService` |
| PUT    | `/api/products/{id}` | `UpdateProductService` |
| POST   | `/api/products/{id}/variants` | `AddProductVariantService` |
| POST   | `/api/products/{id}/publish` · `/suspend` · `/discontinue` | `PublishProductService` · `SuspendProductService` · `DiscontinueProductService` |
| GET    | `/api/products/variants/{variantId}/inventory` | `ConsultInventoryService.executeByVariant` |
| POST   | `/api/inventory/inbound` | `RegisterInventoryInboundService` |
| PATCH  | `/api/inventory/{id}/adjustment` | `AdjustInventoryService` |
| PATCH  | `/api/inventory/{id}/status` | `ChangeInventoryItemStatusService` |
| GET    | `/api/inventory/{id}/movements` | `ConsultInventoryMovementsService` |
| GET    | `/api/cart` · `/api/cart/total` | `ConsultCartService` |
| POST   | `/api/cart/items` | `AddItemToCartService` |
| PATCH  | `/api/cart/items/{variantId}` | `UpdateCartItemQuantityService` |
| DELETE | `/api/cart/items/{variantId}` | `RemoveItemFromCartService` |
| POST   | `/api/orders/{id}/confirm` | `ConfirmOrderService` |
| POST   | `/api/orders/{id}/payment` | `ConfirmOrderPaymentService` |
| POST   | `/api/orders/{id}/dispatch` | `DispatchOrderService` |
| GET    | `/api/orders/{id}` · `/api/orders?status=PAID` | `ConsultOrderService` |
| GET    | `/api/orders/{id}/invoice` | `ConsultInvoiceService` |
| GET    | `/api/orders/{id}/shipments` | `ConsultShipmentService` |
| GET    | `/api/orders/{id}/returns` | `ConsultReturnService.executeByOrder` (also used by `GET /api/orders/{id}` to show the `returnStatus` of each line) |
| PATCH  | `/api/shipments/{id}/status` | `UpdateShipmentStatusService` |
| POST   | `/api/shipments/{id}/delivery` | `ConfirmShipmentDeliveryService` |
| POST   | `/api/returns` | `RequestReturnService` (body: `orderIdentifier`, `variantIdentifier`, `reason` — one product per return) |
| GET    | `/api/returns/{id}` · `/api/returns?status=REQUESTED` | `ConsultReturnService` |
| POST   | `/api/returns/{id}/approve` · `/reject` · `/reception` | `ApproveReturnService` · `RejectReturnService` · `ConfirmReturnReceptionService` |
| POST   | `/api/returns/{id}/refund` | `RequestRefundService` |
| GET    | `/api/returns/{id}/refund` | `ConsultRefundService.executeByReturnRequest` |
| GET    | `/api/refunds` | `ConsultRefundService.executeAll` |
| POST   | `/api/refunds/{id}/approve` · `/reject` · `/process` | `ApproveRefundService` · `RejectRefundService` · `ProcessRefundService` |
| GET    | `/api/reports/orders` · `/inventory` · `/returns` | Global report services |
| GET    | `/api/reports/sellers` · `/api/reports/sellers/{identification}` | `ConsultSellerReportService` (all sellers · one seller) |
| GET    | `/api/audit/operations?user=…\|type=…\|entityType=…&entityId=…` | `ConsultOperationsService` |
| GET    | `/api/audit/logs?user=…\|type=…\|entityType=…&entityId=…` | `ConsultAuditLogsService` |

### Transactions in the controllers

Every controller is annotated with `@Transactional` (class level; `readOnly = true` for reports and audit), so each HTTP request executes its use case in one MySQL transaction. A domain exception rolls the whole use case back.

### Trying the API

`api-requests.http` (project root) contains the complete business flow — administrator, seller, product, stock, buyer, cart, payment, dispatch, delivery, return, refund, reports and audit — ready to run with the VS Code *REST Client* extension. Tokens and identifiers are captured automatically from previous responses.

### Requests and Responses

* Request DTOs carry identifiers and plain values (`String`, `Integer`, `BigDecimal`) and are validated with Jakarta Bean Validation (`@NotBlank`, `@Positive`, ...). They never enter the domain.
* Catalog values travel as their `code` (e.g. `"PAID"`, `"SELLER"`). The REST mappers convert codes to Value Objects through a `ValueObjectResolver` (see *Catalog codes* below).
* Response DTOs never expose the password hash nor persistence entities.

### Mappers

Convert Request DTO → Domain Model and Domain Model → Response DTO. When a request references an existing entity by id, the mapper builds a **reference model** containing only the identifier (e.g. `new Order()` with `identifier`); the domain service loads the full model through its port.

### Exception handling — `handlers/GlobalExceptionHandler`

| Domain exception                    | HTTP status |
| ----------------------------------- | ----------- |
| `InvalidCredentialsException`       | 401 Unauthorized |
| `UnauthorizedOperationException`    | 403 Forbidden |
| `EntityNotFoundException`           | 404 Not Found |
| `InvalidStatusTransitionException`  | 409 Conflict |
| `DomainException` (any other)       | 422 Unprocessable Content |
| `MethodArgumentNotValidException`   | 400 Bad Request (with `fieldErrors`) |
| `HttpMessageNotReadableException`, type mismatch, missing parameter | 400 Bad Request |
| `DataIntegrityViolationException` (unique or foreign key violated in MySQL) | 409 Conflict |
| Any other exception                 | 500 Internal Server Error (logged) |

Every error uses the same body (`ErrorResponse`): `timestamp`, `status`, `error`, `message`, `path` and, for validation errors, `fieldErrors`.

Because every domain exception extends `DomainException`, the handler must declare the specific handlers first (Spring picks the most specific type).

---

## Output Adapters — `adapters/out/persistence`

Every Output Port of the domain is implemented by one adapter (see *Output-ports.md*, "Mapping to Adapters").

### MySQL Adapter

Responsible for relational persistence of business data and operations.

| Component     | Responsibility |
| ------------- | -------------- |
| `entities/`   | JPA entities (`@Entity`) — never leave the adapter. |
| `repositories/` | Spring Data JPA interfaces (`JpaRepository`). |
| `mappers/`    | Entity ↔ Domain Model conversion, including Value Object codes. |
| `adapters/`   | `@Component` classes implementing the Output Ports (`XxxMySqlAdapter implements XxxRepositoryPort`). |

#### Tables

| Table                        | Main columns | Notes |
| ---------------------------- | ------------ | ----- |
| `buyer`                      | `identification` (PK), `full_name`, `email` (UQ), `phone_number`, `address`, `role`, `primary_address`, `status` | |
| `buyer_additional_address`   | `buyer_identification` (FK), `address` | `@ElementCollection` |
| `seller`                     | `identification` (PK), `full_name`, `email` (UQ), `phone_number`, `address`, `role`, `business_name`, `registration_date`, `status` | |
| `users`                      | `user_id` (PK, auto), `username` (UQ), `password`, `identification` (UQ), `full_name`, `email` (UQ), `phone_number`, `address`, `role`, `status`, `buyer_identification` (FK, nullable), `seller_identification` (FK, nullable) | `user` is reserved in some engines; staff users have both FKs in null |
| `warehouse`                  | `identifier` (PK), `name`, `address`, `type`, `owner_identification` (FK seller, nullable), `status` | |
| `product`                    | `identifier` (PK), `seller_identification` (FK), `name`, `description`, `type`, `status` | |
| `product_variant`            | `identifier` (PK), `product_identifier` (FK), `price` | |
| `product_variant_attribute`  | `variant_identifier` (FK), `attr_key`, `attr_value` | `@ElementCollection` map |
| `inventory_item`             | `identifier` (PK), `variant_identifier` (FK), `warehouse_identifier` (FK), `quantity_available`, `quantity_reserved`, `status` | UQ (variant, warehouse); CHECK ≥ 0 |
| `inventory_movement`         | `movement_id` (PK, auto), `inventory_item_identifier` (FK), `movement_type`, `quantity`, `movement_date`, `performed_by` (FK users), `related_order_identifier` (FK, nullable) | |
| `orders`                     | `identifier` (PK), `buyer_identification` (FK), `order_status`, `creation_date`, `payment_date`, `dispatch_date`, `delivery_date` | `order` is reserved |
| `order_item`                 | `id` (PK, auto), `order_identifier` (FK), `variant_identifier` (FK), `quantity`, `unit_price`, `warehouse_identifier` (FK, nullable) | orphan removal |
| `invoice`                    | `identifier` (PK), `order_identifier` (FK, UQ), `issue_date`, `total_amount`, `status` | |
| `shipment`                   | `identifier` (PK), `order_identifier` (FK), `origin_warehouse_identifier` (FK), `handled_by` (FK users, nullable), `status`, `dispatch_date`, `delivery_date` | |
| `return_request`             | `identifier` (PK), `order_identifier` (FK), `buyer_identification` (FK), `reason`, `status`, `request_date`, `resolution_date` | |
| `refund`                     | `identifier` (PK), `return_request_identifier` (FK, UQ), `amount`, `status`, `processed_date` | |
| `operation`                  | `operation_id` (PK, auto), `operation_type`, `execution_date`, `performed_by` (FK users), `affected_entity_type`, `affected_entity_identifier` | polymorphic reference |

Rules:

* `identifier` of `TraceableEntity` subclasses is a `String` generated by the adapter on `save` (UUID).
* Value Objects are stored as their `code` (`VARCHAR`).
* Money is `DECIMAL(15,2)`.

### MongoDB Adapter

Responsible for storing the audit trail.

| Component       | Responsibility |
| --------------- | -------------- |
| `documents/`    | `AuditLogDocument` (`@Document("audit_logs")`). |
| `repositories/` | `AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String>`. |
| `mappers/`      | `AuditLog` ↔ `AuditLogDocument`. |
| `adapters/`     | `AuditLogMongoAdapter implements AuditLogRepositoryPort`. |

`AuditLogDocument`:

```json
{
  "_id": "auditId",
  "operationType": "ORDER_PAYMENT",
  "operationDate": "2026-11-16T14:30:00",
  "performedBy": { "userId": 7, "username": "buyer01" },
  "userRole": "BUYER",
  "affectedEntity": { "type": "Order", "identifier": "a1b2..." },
  "details": { "previousStatus": "PENDING_PAYMENT", "newStatus": "PAID", "hasPhysicalItems": true }
}
```

When reading back, the mapper rebuilds `performedBy` as a reference `User` (userId, username, role) and `affectedEntity` as an instance of the concrete `TraceableEntity` subclass named by `type`, containing only its `identifier`.

---

# Domain

The Domain layer is the core of the application. It contains all business rules and must remain independent from Spring Data, JPA, MongoDB, HTTP, REST, JSON and SQL.

| Package         | Content |
| --------------- | ------- |
| `models/`       | `Person`, `MarketplaceParticipant`, `Buyer`, `Seller`, `User`, `TraceableEntity`, `Warehouse`, `Product`, `ProductVariant`, `InventoryItem`, `InventoryMovement`, `Order`, `OrderItem`, `Invoice`, `Shipment`, `ReturnRequest`, `Refund`, `Operation`, `AuditLog` (see *Domain Model.md*). |
| `valueobjects/` | `DomainCatalog` and the business catalogs: `SystemRole`, `UserStatus`, `BuyerStatus`, `SellerStatus`, `WarehouseType`, `WarehouseStatus`, `ProductType`, `ProductStatus`, `InventoryItemStatus`, `InventoryMovementType`, `OrderStatus`, `InvoiceStatus`, `ShipmentStatus`, `ReturnStatus`, `RefundStatus`, `OperationType` (see *Domain Value Objects.md*). |
| `enums/`        | Technical enumerations without business metadata: `ApprovalDecision`. |
| `services/`     | One use case per class, organized by subdomain (see *Domain Services.md* and `services/*.md`). |
| `ports/out/`    | Output Ports (see *Output-ports.md*). |
| `exceptions/`   | `DomainException` and its subclasses `EntityNotFoundException`, `InvalidCredentialsException`, `InvalidStatusTransitionException`, `UnauthorizedOperationException`. |

## Input Ports

As in the reference implementation, the domain services **are** the use cases and are invoked directly by the controllers; `ports/in` is not used. If the course requires explicit input ports, each service can implement a `XxxUseCase` interface without changing its logic.

## Catalog codes

Value Objects are `public static final` instances with a private constructor; they are compared by `code`. Adapters must convert a stored or received code back to the **same constant**. Implemented in `application.adapters.shared.ValueObjectResolver` (shared by the REST and persistence adapters, never used by the domain):

```java
public final class ValueObjectResolver {
    public static <T extends DomainCatalog> T fromCode(Class<T> type, String code) {
        // reflection over the public static final fields of `type`,
        // returning the constant whose getCode() equals `code`
    }
}
```

Unknown codes must produce a `DomainException` (HTTP 422).

---

# Infrastructure

Infrastructure contains the technical configuration required by the application. It does not contain business logic.

## Config

* Jackson configuration (`JavaTimeModule`, ISO dates, `BigDecimal` as plain numbers).
* CORS configuration for the frontend, if any.

## Database

* MySQL datasource and JPA settings.
* MongoDB connection.
* `DatabaseConfig` enables each Spring Data module on its own repository package (`@EnableJpaRepositories`, `@EnableMongoRepositories`).
* Transaction manager: Spring Boot only auto-configures the JPA transaction manager, so `@Transactional` applies to MySQL. Do **not** declare a `MongoTransactionManager` (it would require a replica set and make `@Transactional` ambiguous).
* Dependency: Spring Data MongoDB requires `spring-boot-starter-data-mongodb` (the `spring-boot-starter-mongodb` starter only brings the driver).

Example `application.properties`:

```properties
spring.application.name=nexusmarket
spring.datasource.url=jdbc:mysql://localhost:3306/nexusmarket?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.mongodb.uri=mongodb://localhost:27017/nexusmarket
security.jwt.secret=${JWT_SECRET:change-me-to-a-32-byte-minimum-secret}
security.jwt.expiration-minutes=120
```

Spring Boot 4 renamed the MongoDB client properties from `spring.data.mongodb.*` to `spring.mongodb.*`, and requires an explicit `spring.data.mongodb.representation.big-decimal` to store `BigDecimal` values (amounts in audit details). See `src/main/resources/application.properties` for the complete configuration, and `compose.yaml` to start MySQL and MongoDB with Docker.

## Security

| Component | Responsibility |
| --------- | -------------- |
| `SecurityConfig` | Stateless `SecurityFilterChain`; public routes: `POST /api/auth/login`, `POST /api/auth/register-buyer`; everything else authenticated. CSRF disabled (stateless API). |
| `PasswordSecurityAdapter` | Implements `PasswordServicePort` with `BCryptPasswordEncoder`. |
| `JwtSecurityAdapter` | Implements `JwtServicePort`: signs a token with `sub = username`, claims `userId` and `role`. |
| `JwtConfig` | HS256 `SecretKey` (from `security.jwt.secret`, ≥ 32 bytes), `JwtEncoder` and `JwtDecoder` (Spring Security's Nimbus integration, dependency `spring-security-oauth2-jose`). |
| `JwtAuthenticationFilter` | Validates the token on each request (signature, expiration, issuer) and stores an `AuthenticatedPrincipal(userId, username, role)` in the `SecurityContext`. Created inside `SecurityConfig`, not as a bean, so it runs only in the security chain. |
| `RestAuthenticationEntryPoint` | Answers 401 with a JSON body for protected routes without a valid token. |
| `AuthenticatedUserResolver` | Loads the full domain `User` (with `participant`) for the current request through `UserRepositoryPort`, and gives it to the controllers as `requestingUser`. |

Role-based authorization stays in the domain (`services/authorization`). Spring Security only guarantees authentication (RG-01 at the transport level); it must **not** duplicate role rules with `@PreAuthorize`, so that the business rules have a single source of truth.

---

# Transactions

Domain services are not annotated with `@Transactional` (the domain does not depend on JPA). Each controller method — or an application-level facade in `adapters/in/rest` — executes the use case inside one transaction:

```text
@Transactional (adapter)
   └── DomainService.execute(...)
          ├── port.save / port.update     (MySQL — inside the transaction)
          └── RegisterOperationAndAudit   (operation: MySQL inside · audit log: MongoDB outside)
```

Use cases that **require** atomicity:

| Use case | Why |
| -------- | --- |
| `ConfirmOrderService` | Several reservations; a failure must roll back the previous ones. |
| `ConfirmOrderPaymentService` | Order, invoice and shipments. |
| `DispatchOrderService` | Shipments, stock outbound and order. |
| `ConfirmShipmentDeliveryService` | Shipment and order finalization. |
| `ConfirmReturnReceptionService` | Stock returns and return status. |
| `ProcessRefundService` | Refund, return and invoice. |
| `RegisterSellerService` | Seller and first warehouse. |
| Buyer registration endpoint | Buyer and user. |

To avoid two buyers reserving the same units concurrently, `InventoryItemMySqlAdapter.findByVariant` used during reservation should lock the rows (`@Lock(PESSIMISTIC_WRITE)`) or the entity should use optimistic locking (`@Version`).

---

# Dependency Flow

Dependencies always point toward the domain.

```text
REST Controller
        │
        ▼
Domain Service (use case)
        │
        ▼
Output Port
        │
        ▼
Persistence / Security Adapter
        │
        ▼
MySQL · MongoDB · JWT · BCrypt
```

The domain never depends on adapters or infrastructure.

---

# Implementation Order (suggested)

1. **Infrastructure / database**: properties, datasource, Mongo, repository scanning. ✅
2. **MySQL entities, repositories, mappers and adapters** for every port, and the MongoDB audit adapter. ✅
3. **Security**: `PasswordSecurityAdapter`, `JwtSecurityAdapter`, filter, `SecurityConfig`, `AuthenticatedUserResolver`. ✅
4. **Operation (MySQL) and AuditLog (Mongo) adapters** — every write service needs them. ✅
5. `AuthController`, `UserController`, `SellerController`, `WarehouseController`, `GlobalExceptionHandler` → first end-to-end test (admin registers seller). ✅
6. Catalog and inventory adapters + controllers. ✅
7. Order, invoice and shipment adapters + controllers (complete business flow steps 5–8). ✅
8. Returns, refunds, reports and audit controllers. ✅
9. Data seeding: `AdminUserInitializer` (`infrastructure/database`) creates the initial `ADMINISTRATOR` from the `nexusmarket.admin.*` properties, since staff users can only be created by an administrator. ✅

---

# Benefits

- Technology independence.
- High maintainability.
- Clear separation of concerns.
- Improved testability (the domain is testable with in-memory port implementations).
- Easy replacement of frameworks or databases.
- Reusable business logic.

---

# Architectural Constraints

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities and documents must never be exposed through the API nor enter the Domain.
5. Communication between technologies and the Domain occurs only through Ports.
6. Adapters implement Ports but never define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Every dependency points toward the Domain.
9. Business entities remain framework-independent.
10. Internal domain services are never exposed through controllers.
11. Authorization rules live only in the domain authorization services.
12. The Domain must be fully testable without infrastructure components.
