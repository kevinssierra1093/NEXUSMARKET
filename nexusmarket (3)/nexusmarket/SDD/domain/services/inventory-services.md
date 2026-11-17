# Inventory Services

## Introduction

This document defines the services belonging to the **Inventory Management** subdomain of NexusMarket (OBJ-06, Domain 6 "Gestión del Inventario").

Inventory is **distributed**: every stock record is bound to exactly one `ProductVariant` and one `Warehouse`. Every change of stock is recorded as an immutable `InventoryMovement`.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.inventory`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
registerInbound(String variantId, String warehouseId, int quantity);
```

Correct:

```java
registerInbound(User requestingUser, InventoryItem item, Integer quantity);
```

where `item.variant` and `item.warehouse` are Domain Models.

## Status vs. event

```text
InventoryItem       (current state: quantityAvailable, quantityReserved, status)
      │
      └── generates ──► InventoryMovement   (event: type, quantity, date, performedBy, relatedOrder)
```

## Movement types

| InventoryMovementType | Effect on `InventoryItem`                          | Produced by |
| --------------------- | -------------------------------------------------- | ----------- |
| `INBOUND`             | `available += q`                                   | `RegisterInventoryInboundService` |
| `RESERVATION`         | `available -= q`, `reserved += q`                  | `ReserveInventoryService` |
| `SALE_OUTBOUND`       | `reserved -= q`                                    | `RegisterSaleOutboundService` |
| `ADJUSTMENT`          | `available = new value`, or status change          | `AdjustInventoryService`, `ChangeInventoryItemStatusService` |
| `RETURN`              | `available += q`                                   | `RegisterInventoryReturnService` |

## Critical rules

* **Negative stock is never permitted** under any circumstance (`quantityAvailable ≥ 0`, `quantityReserved ≥ 0`).
* Non-existent stock, or stock marked `DAMAGED`, can **never be reserved** (Validation, section 11).
* Digital products do not use inventory.

## Responsibility matrix

| Operation                  | Allowed roles |
| -------------------------- | ------------- |
| Administer inventory       | Owning SELLER, LOGISTICS_OPERATOR (`AuthorizeInventoryOperationService`) |
| Consult inventory          | Owning SELLER, LOGISTICS_OPERATOR, SUPERVISOR (`AuthorizeInventoryConsultationService`) |

## Internal services

`ReserveInventoryService`, `RegisterSaleOutboundService`, `RegisterInventoryReturnService` and `RecordInventoryMovementService` are **internal**: they are invoked by order, return or inventory services that have already authorized the requesting user, and must not be exposed through controllers.

---

# Inventory Services

1. Register Inventory Inbound
2. Adjust Inventory
3. Change Inventory Item Status
4. Consult Inventory
5. Consult Inventory Movements
6. Reserve Inventory *(internal)*
7. Register Sale Outbound *(internal)*
8. Register Inventory Return *(internal)*
9. Record Inventory Movement *(internal)*
10. Check Stock Availability *(internal)*

---

# 1. Register Inventory Inbound

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterInventoryInboundService` |
| Method        | `InventoryItem execute(User requestingUser, InventoryItem item, Integer quantity)` |
| Authorization | `AuthorizeInventoryOperationService` (owning SELLER or LOGISTICS_OPERATOR) |
| Output Ports  | `InventoryItemRepositoryPort`, `ProductVariantRepositoryPort`, `WarehouseRepositoryPort` |
| Movement      | `INBOUND` |
| Operation     | `INVENTORY_INBOUND` (affected entity: the inventory item) |

## Description

Registers units entering a warehouse (initial stock — business flow step 3 — or restock). Creates the inventory record when the (variant, warehouse) pair has none.

## Input

```text
InventoryItem
 ├── variant    : ProductVariant   (required)
 └── warehouse  : Warehouse        (required)
quantity : Integer                 (> 0)
```

## Domain Validations

1. `variant` and `warehouse` are required and must exist.
2. The requesting user must be authorized on the variant (owning seller or logistics operator).
3. `quantity > 0`.
4. The product must be `PHYSICAL`.
5. The warehouse must be `ACTIVE`.
6. A product cannot be stocked in a `SELLER` warehouse owned by **another** seller (marketplace warehouses are allowed).
7. The existing record (if any) must be `AVAILABLE`; stock cannot be added to a `DAMAGED` record.

## Processing

```text
existing = findByVariantAndWarehouse(item)
target   = existing or new record (available 0, reserved 0, AVAILABLE)
target.quantityAvailable += quantity
existing ? update(target) : save(target)
RecordInventoryMovementService(INBOUND, quantity)
```

## Audit details

`quantity`, `availableBefore`, `availableAfter`, `warehouse`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Variant or warehouse not found. |
| `UnauthorizedOperationException` | Role not allowed or seller is not the owner. |
| `DomainException` | Any validation above. |

---

# 2. Adjust Inventory

| Item          | Value |
| ------------- | ----- |
| Class         | `AdjustInventoryService` |
| Method        | `InventoryItem execute(User requestingUser, InventoryItem item, Integer newQuantityAvailable)` |
| Authorization | `AuthorizeInventoryOperationService` |
| Output Ports  | `InventoryItemRepositoryPort` |
| Movement      | `ADJUSTMENT` (quantity = absolute difference) |
| Operation     | `INVENTORY_ADJUSTMENT` (affected entity: the inventory item) |

## Description

Manual correction of the available quantity (physical count, losses, etc.). Reserved units are not modified.

## Domain Validations

1. The inventory record must exist (`findByIdentifier`).
2. `newQuantityAvailable` is required and `≥ 0`.
3. The new value must differ from the current one.

## Audit details

`availableBefore`, `availableAfter`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Record not found. |
| `UnauthorizedOperationException` | Not authorized. |
| `DomainException` | Negative quantity or no change. |

---

# 3. Change Inventory Item Status

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeInventoryItemStatusService` |
| Method        | `InventoryItem execute(User requestingUser, InventoryItem item, InventoryItemStatus newStatus)` |
| Authorization | `AuthorizeInventoryOperationService` |
| Output Ports  | `InventoryItemRepositoryPort` |
| Movement      | `ADJUSTMENT` (quantity = units currently available, i.e. units affected by the status change) |
| Operation     | `INVENTORY_ADJUSTMENT` (affected entity: the inventory item) |

## Description

Marks stock as `DAMAGED` (unsellable, cannot be reserved) or back to `AVAILABLE`.

## Domain Validations

1. The inventory record must exist.
2. The new status must differ from the current one.

## Audit details

`previousStatus`, `newStatus`, `affectedUnits`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Record not found. |
| `UnauthorizedOperationException` | Not authorized. |
| `InvalidStatusTransitionException` | New status equals the current status. |

---

# 4. Consult Inventory

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultInventoryService` |
| Methods       | `List<InventoryItem> executeByVariant(User requestingUser, ProductVariant variant)`<br>`List<InventoryItem> executeByWarehouse(User requestingUser, Warehouse warehouse)` |
| Authorization | `executeByVariant`: `AuthorizeInventoryConsultationService`<br>`executeByWarehouse`: SELLER, LOGISTICS_OPERATOR, SUPERVISOR, ADMINISTRATOR |
| Output Ports  | `InventoryItemRepositoryPort`, `ProductVariantRepositoryPort`, `WarehouseRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

* `executeByVariant`: stock of a variant across every warehouse.
* `executeByWarehouse`: stock held by a warehouse. When the requester is a seller, only the records of the seller's own products are returned.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Variant or warehouse not found. |
| `UnauthorizedOperationException` | Not authorized. |

---

# 5. Consult Inventory Movements

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultInventoryMovementsService` |
| Method        | `List<InventoryMovement> execute(User requestingUser, InventoryItem item)` |
| Authorization | `AuthorizeInventoryConsultationService` |
| Output Ports  | `InventoryItemRepositoryPort`, `InventoryMovementRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves the movement history of an inventory record.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Record not found. |
| `UnauthorizedOperationException` | Not authorized. |

---

# 6. Reserve Inventory *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `ReserveInventoryService` |
| Method        | `InventoryItem execute(User requestingUser, ProductVariant variant, Integer quantity, Order order)` |
| Invoked by    | `ConfirmOrderService` |
| Output Ports  | `InventoryItemRepositoryPort` |
| Movement      | `RESERVATION` (related order) |
| Operation     | `INVENTORY_RESERVATION` (affected entity: the inventory item) |

## Description

Reserves the units of an order line from **one** warehouse that can cover the whole quantity.

## Selection rule

The first inventory record of the variant that satisfies all of:

1. `status = AVAILABLE` (never `DAMAGED`);
2. warehouse `status = ACTIVE`;
3. `quantityAvailable ≥ quantity`.

## Processing

```text
available -= quantity
reserved  += quantity
update(item)
RecordInventoryMovementService(RESERVATION, quantity, order)
```

## Output

The inventory record used. `ConfirmOrderService` assigns its warehouse to the order line (`OrderItem.warehouse`).

## Audit details

`quantity`, `order`, `warehouse`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | `quantity ≤ 0`, or no warehouse has enough available stock. |

---

# 7. Register Sale Outbound *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterSaleOutboundService` |
| Method        | `InventoryItem execute(User requestingUser, OrderItem line, Order order)` |
| Invoked by    | `DispatchOrderService` |
| Output Ports  | `InventoryItemRepositoryPort` |
| Movement      | `SALE_OUTBOUND` (related order) |
| Operation     | `INVENTORY_SALE_OUTBOUND` (affected entity: the inventory item) |

## Description

The reserved units of an order line physically leave the warehouse: `reserved -= line.quantity`.

## Domain Validations

1. The record (`line.variant`, `line.warehouse`) must exist.
2. `quantityReserved ≥ line.quantity`.

## Audit details

`quantity`, `order`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Record not found. |
| `DomainException` | Reserved stock lower than the quantity to dispatch. |

---

# 8. Register Inventory Return *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterInventoryReturnService` |
| Method        | `InventoryItem execute(User requestingUser, OrderItem line, Order order)` |
| Invoked by    | `ConfirmReturnReceptionService` |
| Output Ports  | `InventoryItemRepositoryPort` |
| Movement      | `RETURN` (related order) |
| Operation     | `INVENTORY_RETURN` (affected entity: the inventory item) |

## Description

Returned units re-enter the warehouse from which they were dispatched: `available += line.quantity`.

## Audit details

`quantity`, `order`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Record not found. |

---

# 9. Record Inventory Movement *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `RecordInventoryMovementService` |
| Method        | `InventoryMovement execute(User user, InventoryItem item, InventoryMovementType type, Integer quantity, Order relatedOrder)` |
| Invoked by    | Every inventory service that changes stock |
| Output Ports  | `InventoryMovementRepositoryPort` |

## Description

Builds and persists the immutable `InventoryMovement` (`movementDate = now`, `performedBy = user`, `relatedOrder` nullable).

---

# 10. Check Stock Availability *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `CheckStockAvailabilityService` |
| Methods       | `void execute(ProductVariant variant, int requestedQuantity)`<br>`int availableUnits(ProductVariant variant)` |
| Invoked by    | `AddItemToCartService`, `UpdateCartItemQuantityService` |
| Output Ports  | `InventoryItemRepositoryPort` |

## Description

Blocks a cart operation that asks for more units of a **physical** product than can be sold. `availableUnits` is the largest `quantityAvailable` among the `AVAILABLE` records of the variant in `ACTIVE` warehouses — the same rule `ReserveInventoryService` applies when the order is confirmed. Digital products are not checked.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | `Insufficient stock for '<product>': requested N unit(s), available M.` |

---

# Transactions

Every write service of this subdomain updates the inventory record, inserts a movement and registers an operation/audit. The input adapter must execute each use case inside a single transaction (`@Transactional`) so that a failure leaves no partial changes. The audit log is stored in MongoDB and is not covered by the MySQL transaction (see *Software Architecture*).
