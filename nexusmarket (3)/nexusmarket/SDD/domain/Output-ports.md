# Output Ports

## Introduction

Output Ports define the contracts through which the NexusMarket Domain communicates with external resources.

The Domain owns these interfaces (`application.domain.ports.out`). Domain Services must never depend directly on:

- MySQL
- MongoDB
- JPA
- Spring Data repositories
- HTTP
- REST
- external APIs

When a Domain Service requires information or functionality outside the Domain, it must use the corresponding Output Port. The implementations of these ports belong to the Adapter layer (`application.adapters.out`).

---

# Architectural Rule

The dependency flow must always be:

```text
Domain Service
      |
      v
Output Port
      |
      v
Output Adapter
      |
      v
External Resource
```

For example:

```text
ConfirmOrderService
      |
      v
OrderRepositoryPort
      |
      v
OrderMySqlAdapter
      |
      v
MySQL
```

For auditing:

```text
RegisterAuditLogService
      |
      v
AuditLogRepositoryPort
      |
      v
AuditLogMongoAdapter
      |
      v
MongoDB
```

---

# General Parameter Rule

Output Port methods work with Domain Models. They must not receive DTOs, persistence entities, or primitive identifiers when the corresponding Domain Model already exists.

Incorrect:

```java
Optional<Order> findById(String orderId);
```

Correct:

```java
Optional<Order> findByIdentifier(Order order);
```

Incorrect:

```java
void updateStock(String inventoryId, int quantity);
```

Correct:

```java
void update(InventoryItem item);
```

## Search criteria convention

When a port method receives a Domain Model as a search criterion, the adapter reads **only** the attribute named by the method:

| Method                                  | Attribute used by the adapter                                   |
| --------------------------------------- | --------------------------------------------------------------- |
| `findByIdentifier(X x)`                 | `x.identifier` (inherited from `TraceableEntity`)               |
| `findByIdentification(Buyer/Seller p)`  | `p.identification`                                              |
| `existsByIdentification(Person p)`      | `p.identification`                                              |
| `existsByEmail(Person p)`               | `p.email`                                                       |
| `findById(User u)`                      | `u.userId`                                                      |
| `findByUsername(User u)`                | `u.username`                                                    |
| `findByStatus(Order o)`                 | `o.orderStatus`                                                 |
| `findByStatus(Product p)`               | `p.status`                                                      |
| `findByStatus(ReturnRequest r)`         | `r.status`                                                      |
| `findByVariantAndWarehouse(InventoryItem i)` | `i.variant.identifier` + `i.warehouse.identifier`          |
| `findByType(Operation o)`               | `o.operationType`                                               |
| `findByOperationType(AuditLog a)`       | `a.operationType`                                               |

## Identifier rule

* `save(...)` **creates** the record, assigns the identifier (`identifier`, `userId`, `operationId`, `movementId` or `auditId`) and **returns** the Domain Model with that identifier populated. Services always continue working with the returned instance.
* `update(...)` overwrites an existing record identified by its identifier. It never creates new records.

---

# Output Ports

## 1. UserRepositoryPort

### Responsibility

Provides persistence and query capabilities for `User` (system identities of buyers, sellers and staff).

### Methods

```java
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUsername(User user);

    Optional<User> findById(User user);

    boolean existsByUsername(User user);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<User> findAll();

    void update(User user);
}
```

### Adapter notes

* `participant` is stored as one of two nullable foreign keys (`buyer_identification` or `seller_identification`) and is rehydrated as a `Buyer` or `Seller` instance; both are `null` for staff users.
* `password` is always stored already encrypted (the service calls `PasswordServicePort.encrypt` before `save`/`update`).

### Main Consumers

- User and Authentication Services (`LoginService`, `RegisterParticipantUserService`, `RegisterStaffUserService`, `ConsultUserService`, `ChangeUserStatusService`, `ChangeUserPasswordService`)
- `RegisterBuyerService`, `RegisterSellerService` (platform-wide uniqueness of identification and email)

---

## 2. BuyerRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Buyer`.

### Methods

```java
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Optional<Buyer> findByIdentification(Buyer buyer);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<Buyer> findAll();

    void update(Buyer buyer);
}
```

### Adapter notes

* `additionalAddresses` is a list and must be persisted in a child table (e.g. `buyer_additional_address`).

### Main Consumers

- Buyer Services
- `ResolveBuyerCartService`
- `RegisterParticipantUserService`, `RegisterStaffUserService`, `RegisterSellerService` (uniqueness)

---

## 3. SellerRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Seller`.

### Methods

```java
public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findByIdentification(Seller seller);

    boolean existsByIdentification(Person person);

    boolean existsByEmail(Person person);

    List<Seller> findAll();

    void update(Seller seller);
}
```

### Adapter notes

* `warehouses` and `products` are **not** loaded by this adapter (empty lists by default). `ConsultSellerService` populates them on demand through `WarehouseRepositoryPort` and `ProductRepositoryPort`.

### Main Consumers

- Seller Services
- `RegisterWarehouseService`, `RegisterProductService`
- `RegisterBuyerService`, `RegisterParticipantUserService`, `RegisterStaffUserService` (uniqueness)

---

## 4. WarehouseRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Warehouse`.

### Methods

```java
public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findByIdentifier(Warehouse warehouse);

    List<Warehouse> findBySeller(Seller seller);

    List<Warehouse> findAll();

    void update(Warehouse warehouse);
}
```

### Adapter notes

* `owner` is `null` when `type = MARKETPLACE`. When loaded, `owner` must contain at least `identification` and `status`.

### Main Consumers

- Warehouse Services
- `RegisterSellerService`, `ConsultSellerService`
- `RegisterInventoryInboundService`, `ConsultInventoryService`

---

## 5. ProductRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Product`.

### Methods

```java
public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findByIdentifier(Product product);

    List<Product> findBySeller(Seller seller);

    List<Product> findByStatus(Product product);

    void update(Product product);
}
```

### Adapter notes

* `save` and `update` persist **only** the product. Variants are persisted through `ProductVariantRepositoryPort` (`RegisterProductService` saves them one by one after the product).
* When loaded, `seller` must contain `identification`, `businessName` and `status`.

### Main Consumers

- Catalog Services
- `ConsultSellerService`

---

## 6. ProductVariantRepositoryPort

### Responsibility

Provides persistence and query capabilities for `ProductVariant`.

### Methods

```java
public interface ProductVariantRepositoryPort {

    ProductVariant save(ProductVariant variant);

    Optional<ProductVariant> findByIdentifier(ProductVariant variant);

    List<ProductVariant> findByProduct(Product product);

    void update(ProductVariant variant);
}
```

### Adapter notes

* `attributes` is a `Map<String,String>` and must be persisted in a child table (e.g. `product_variant_attribute`) or as JSON.
* When loaded, `product` must contain `identifier`, `name`, `type`, `status` and `product.seller` (identification and status).

### Main Consumers

- Catalog Services
- `RegisterInventoryInboundService`, `ConsultInventoryService`
- `AddItemToCartService`

---

## 7. InventoryItemRepositoryPort

### Responsibility

Provides persistence and query capabilities for `InventoryItem` (distributed stock of a variant in a warehouse).

### Methods

```java
public interface InventoryItemRepositoryPort {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findByIdentifier(InventoryItem item);

    Optional<InventoryItem> findByVariantAndWarehouse(InventoryItem item);

    List<InventoryItem> findByVariant(ProductVariant variant);

    List<InventoryItem> findByWarehouse(Warehouse warehouse);

    List<InventoryItem> findAll();

    void update(InventoryItem item);
}
```

### Adapter notes

* The pair (variant, warehouse) is unique: the table must declare a unique constraint on both columns.
* `quantity_available` and `quantity_reserved` must declare a `CHECK (>= 0)` constraint as a last line of defense; the domain already validates it.
* When loaded, `variant.product.seller` and `warehouse.status` must be available (used by `ReserveInventoryService` and the inventory authorization services).

### Main Consumers

- Inventory Services
- `PublishProductService`
- `ConsultInventoryReportService`

---

## 8. InventoryMovementRepositoryPort

### Responsibility

Persists the immutable stock movement events (`INBOUND`, `RESERVATION`, `SALE_OUTBOUND`, `ADJUSTMENT`, `RETURN`).

### Methods

```java
public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);

    List<InventoryMovement> findByInventoryItem(InventoryItem item);

    List<InventoryMovement> findByOrder(Order order);
}
```

### Adapter notes

* Movements are never updated or deleted (there is no `update` method).
* `relatedOrder` is nullable.

### Main Consumers

- `RecordInventoryMovementService`
- `ConsultInventoryMovementsService`

---

## 9. OrderRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Order`, including its `OrderItem` lines. The cart is the buyer's order in `CART` status.

### Methods

```java
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByIdentifier(Order order);

    Optional<Order> findCartByBuyer(Buyer buyer);

    List<Order> findByBuyer(Buyer buyer);

    List<Order> findByStatus(Order order);

    List<Order> findAll();

    void update(Order order);
}
```

### Adapter notes

* `update` must persist the **complete list of lines**: added lines, removed lines, changed quantities and the `warehouse` assigned to each line when the order is confirmed. The simplest strategy is to replace the lines of the order (orphan removal).
* `findCartByBuyer` returns the only order of the buyer in `CART` status, if any.
* When loaded, each `OrderItem` must contain `variant.product` (`type`, `status`, `name`, `seller.identification`) and its `warehouse` (nullable). `buyer` must contain `identification` and `status`.
* The table name `order` is a SQL reserved word: use `orders`.

### Main Consumers

- Cart and Order Services
- `ConsultInvoiceService`, `ConsultShipmentService`, `ConfirmShipmentDeliveryService`
- Return and Refund Services
- `ConsultOrdersReportService`

---

## 10. InvoiceRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Invoice`.

### Methods

```java
public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByIdentifier(Invoice invoice);

    Optional<Invoice> findByOrder(Order order);

    void update(Invoice invoice);
}
```

### Adapter notes

* One invoice per order: the `order` column must be unique.

### Main Consumers

- Invoice Services

---

## 11. ShipmentRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Shipment`.

### Methods

```java
public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);

    Optional<Shipment> findByIdentifier(Shipment shipment);

    List<Shipment> findByOrder(Order order);

    void update(Shipment shipment);
}
```

### Adapter notes

* `handledBy` is nullable (assigned when the shipment is dispatched).
* When loaded, `order` must contain at least its `identifier`.

### Main Consumers

- Shipment Services
- `DispatchOrderService`

---

## 12. ReturnRequestRepositoryPort

### Responsibility

Provides persistence and query capabilities for `ReturnRequest`.

### Methods

```java
public interface ReturnRequestRepositoryPort {

    ReturnRequest save(ReturnRequest returnRequest);

    Optional<ReturnRequest> findByIdentifier(ReturnRequest returnRequest);

    List<ReturnRequest> findByOrder(Order order);

    List<ReturnRequest> findByBuyer(Buyer buyer);

    List<ReturnRequest> findByStatus(ReturnRequest returnRequest);

    List<ReturnRequest> findAll();

    void update(ReturnRequest returnRequest);
}
```

### Main Consumers

- Return Services
- Refund Services
- `ConsultReturnsReportService`

---

## 13. RefundRepositoryPort

### Responsibility

Provides persistence and query capabilities for `Refund`.

### Methods

```java
public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findByIdentifier(Refund refund);

    Optional<Refund> findByReturnRequest(ReturnRequest returnRequest);

    List<Refund> findAll();

    void update(Refund refund);
}
```

### Adapter notes

* One refund per return request: the `return_request` column must be unique.

### Main Consumers

- Refund Services
- `ConsultReturnsReportService`

---

## 14. OperationRepositoryPort

### Responsibility

Persists the immutable `Operation` records that provide traceability between the user performing an action and the affected entity.

### Methods

```java
public interface OperationRepositoryPort {

    Operation save(Operation operation);

    Optional<Operation> findById(Operation operation);

    List<Operation> findByUser(User user);

    List<Operation> findByAffectedEntity(TraceableEntity entity);

    List<Operation> findByType(Operation operation);
}
```

### Adapter notes

* `affectedEntity` is polymorphic (`TraceableEntity`) and nullable. Persist it as two columns: `affected_entity_type` (simple class name, e.g. `Order`) and `affected_entity_identifier`.
* `findByAffectedEntity` filters by both columns.
* Operations are never updated or deleted.

### Main Consumers

- `RegisterOperationService`
- `ConsultOperationsService`

---

## 15. AuditLogRepositoryPort

### Responsibility

Persists the detailed, immutable audit trail (`AuditLog`) of every operation.

### Methods

```java
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByAffectedEntity(TraceableEntity entity);

    List<AuditLog> findByOperationType(AuditLog auditLog);
}
```

### Adapter notes

* Implemented with **MongoDB**: `details` is a free-form `Map<String,Object>` that fits a document naturally.
* The document stores a snapshot of the user (`userId`, `username`, `role`) and the affected entity (`type`, `identifier`), not the full objects.
* `auditId` is the MongoDB document id.

### Main Consumers

- `RegisterAuditLogService`
- `ConsultAuditLogsService`

---

## 16. PasswordServicePort

### Responsibility

Encrypts and verifies passwords without exposing the hashing technology to the domain.

### Methods

```java
public interface PasswordServicePort {

    boolean matches(String rawPassword, String encodedPassword);

    String encrypt(String rawPassword);
}
```

### Main Consumers

- `LoginService`
- `RegisterParticipantUserService`, `RegisterStaffUserService`, `ChangeUserPasswordService`

### Architectural Rule

The domain never hashes passwords itself and never compares plain text passwords. The adapter uses Spring Security's `PasswordEncoder` (BCrypt).

---

## 17. JwtServicePort

### Responsibility

Generates the JWT that represents an authenticated session.

### Methods

```java
public interface JwtServicePort {

    String generateToken(User user);
}
```

### Main Consumer

- `LoginService`

### JWT Rule

The token must contain at least the `userId`, `username` and `role` of the user. Token **validation** is an infrastructure concern (security filter); the domain only requests token generation.

---

# Port Organization

```text
domain/
└── ports/
    ├── in/                (not used: domain services are the use cases, same as the reference project)
    └── out/
        ├── UserRepositoryPort
        ├── BuyerRepositoryPort
        ├── SellerRepositoryPort
        ├── WarehouseRepositoryPort
        ├── ProductRepositoryPort
        ├── ProductVariantRepositoryPort
        ├── InventoryItemRepositoryPort
        ├── InventoryMovementRepositoryPort
        ├── OrderRepositoryPort
        ├── InvoiceRepositoryPort
        ├── ShipmentRepositoryPort
        ├── ReturnRequestRepositoryPort
        ├── RefundRepositoryPort
        ├── OperationRepositoryPort
        ├── AuditLogRepositoryPort
        ├── PasswordServicePort
        └── JwtServicePort
```

---

# Mapping to Adapters

```text
domain/ports/out/
        |
        +-- UserRepositoryPort               <-- UserMySqlAdapter
        +-- BuyerRepositoryPort              <-- BuyerMySqlAdapter
        +-- SellerRepositoryPort             <-- SellerMySqlAdapter
        +-- WarehouseRepositoryPort          <-- WarehouseMySqlAdapter
        +-- ProductRepositoryPort            <-- ProductMySqlAdapter
        +-- ProductVariantRepositoryPort     <-- ProductVariantMySqlAdapter
        +-- InventoryItemRepositoryPort      <-- InventoryItemMySqlAdapter
        +-- InventoryMovementRepositoryPort  <-- InventoryMovementMySqlAdapter
        +-- OrderRepositoryPort              <-- OrderMySqlAdapter
        +-- InvoiceRepositoryPort            <-- InvoiceMySqlAdapter
        +-- ShipmentRepositoryPort           <-- ShipmentMySqlAdapter
        +-- ReturnRequestRepositoryPort      <-- ReturnRequestMySqlAdapter
        +-- RefundRepositoryPort             <-- RefundMySqlAdapter
        +-- OperationRepositoryPort          <-- OperationMySqlAdapter
        +-- AuditLogRepositoryPort           <-- AuditLogMongoAdapter
        +-- PasswordServicePort              <-- PasswordSecurityAdapter   (infrastructure/security)
        +-- JwtServicePort                   <-- JwtSecurityAdapter        (infrastructure/security)
```

---

# Database Responsibility

A Port does not necessarily correspond one-to-one with a physical database table. For example:

```text
OrderRepositoryPort
        |
        v
OrderMySqlAdapter
        |
        +-- orders table
        +-- order_item table
```

```text
ProductVariantRepositoryPort
        |
        v
ProductVariantMySqlAdapter
        |
        +-- product_variant table
        +-- product_variant_attribute table
```

The Domain only knows the port; the physical design can evolve without changing the Domain.

---

# Object Graph Hydration

Domain services navigate relationships of the models returned by the ports. The adapters must therefore load (eagerly or through `JOIN FETCH`) at least the following graph:

| Returned model     | Relationships that must be loaded                                                      | Used by (examples)                                   |
| ------------------ | -------------------------------------------------------------------------------------- | ---------------------------------------------------- |
| `User`             | `participant` (`Buyer`/`Seller` with `identification`)                                 | Ownership validations                                |
| `Warehouse`        | `owner` (`identification`, `status`)                                                   | `RegisterInventoryInboundService`, `ConsultWarehouseService` |
| `Product`          | `seller` (`identification`, `status`)                                                  | `AuthorizeProductOperationService`, `PublishProductService` |
| `ProductVariant`   | `product` → `seller`                                                                   | Inventory authorization, `AddItemToCartService`      |
| `InventoryItem`    | `variant` → `product` → `seller`; `warehouse` (`status`, `type`, `owner`)              | `ReserveInventoryService`, `ConsultInventoryService` |
| `Order`            | `buyer` (`status`); `items` → `variant` → `product` → `seller`; `items.warehouse`      | Whole order flow                                     |
| `Shipment`         | `order` (`identifier`); `originWarehouse`; `handledBy`                                 | `ConfirmShipmentDeliveryService`                     |
| `ReturnRequest`    | `order` (`identifier`); `buyer` (`identification`)                                     | Return and Refund Services                           |
| `Refund`           | `returnRequest` (`identifier`, `buyer`)                                                | Refund Services                                      |

Back-references (`OrderItem.order`, `ProductVariant.product`) must be set by the mappers without recursive mapping (map the parent once and assign the same instance to every child).

---

# Service-to-Port Relationship

| Subdomain                 | Ports used                                                                                         |
| ------------------------- | -------------------------------------------------------------------------------------------------- |
| User and Authentication   | `UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `PasswordServicePort`, `JwtServicePort` |
| Buyer                     | `BuyerRepositoryPort`, `SellerRepositoryPort`, `UserRepositoryPort`                                |
| Seller                    | `SellerRepositoryPort`, `BuyerRepositoryPort`, `UserRepositoryPort`, `WarehouseRepositoryPort`, `ProductRepositoryPort` |
| Warehouse                 | `WarehouseRepositoryPort`, `SellerRepositoryPort`                                                  |
| Catalog                   | `ProductRepositoryPort`, `ProductVariantRepositoryPort`, `SellerRepositoryPort`, `InventoryItemRepositoryPort` |
| Inventory                 | `InventoryItemRepositoryPort`, `InventoryMovementRepositoryPort`, `ProductVariantRepositoryPort`, `WarehouseRepositoryPort` |
| Cart and Order            | `OrderRepositoryPort`, `BuyerRepositoryPort`, `ProductVariantRepositoryPort`, `ShipmentRepositoryPort` |
| Invoice                   | `InvoiceRepositoryPort`, `OrderRepositoryPort`                                                     |
| Shipment                  | `ShipmentRepositoryPort`, `OrderRepositoryPort`                                                    |
| Return                    | `ReturnRequestRepositoryPort`, `OrderRepositoryPort`                                               |
| Refund                    | `RefundRepositoryPort`, `ReturnRequestRepositoryPort`, `OrderRepositoryPort`                       |
| Reports                   | `OrderRepositoryPort`, `InventoryItemRepositoryPort`, `ReturnRequestRepositoryPort`, `RefundRepositoryPort` |
| Operation and Audit       | `OperationRepositoryPort`, `AuditLogRepositoryPort`                                                |
| Authorization             | None (works exclusively with the Domain Models received)                                          |

---

# Final Architectural Rules

1. All Output Ports belong to the Domain.
2. Output Ports are interfaces.
3. Adapters implement Output Ports.
4. Domain Services never access repositories or databases directly.
5. Domain Services never access HTTP or REST directly.
6. Domain Services never depend on JPA, MongoDB or MySQL.
7. Ports use Domain Models rather than DTOs or persistence entities.
8. Domain relationships are never represented by primitive IDs when a Domain Model can represent them.
9. Output Ports are used only when the required information or capability is external to the Domain.
10. A Port is not created merely because the physical database contains another table.
11. Business rules that can be evaluated from Domain Models remain inside the Domain.
12. Persistence adapters translate between Domain Models and persistence representations.
13. The Domain must remain fully testable without a database or external infrastructure.
