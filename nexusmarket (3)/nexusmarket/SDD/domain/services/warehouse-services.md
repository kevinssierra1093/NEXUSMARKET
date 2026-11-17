# Warehouse Services

## Introduction

This document defines the services belonging to the **Warehouse Management** subdomain of NexusMarket (OBJ-04, Domain 4 "Gestión de Bodegas").

The services in this subdomain control the physical storage locations where inventory is administered.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.warehouse`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
registerWarehouse(String name, String address, String sellerId);
```

Correct:

```java
registerWarehouse(User requestingUser, Warehouse warehouse);
```

The owner is a domain relationship:

```text
Warehouse
 └── owner : Seller     (null when type = MARKETPLACE)
```

## Classification

| WarehouseType | Owner      |
| ------------- | ---------- |
| `MARKETPLACE` | none       |
| `SELLER`      | one Seller |

## Responsibility matrix

The Administrator is responsible for the administration of sellers and warehouses (section 5). Every write operation of this subdomain requires `ADMINISTRATOR`.

---

# Warehouse Services

1. Register Warehouse
2. Consult Warehouse
3. Update Warehouse
4. Change Warehouse Status

---

# 1. Register Warehouse

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterWarehouseService` |
| Method        | `Warehouse execute(User requestingUser, Warehouse warehouse)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `WarehouseRepositoryPort`, `SellerRepositoryPort` |
| Operation     | `WAREHOUSE_REGISTRATION` (affected entity: the warehouse) |

## Description

Registers a new marketplace warehouse or an additional seller warehouse. (The first warehouse of a seller is registered by `RegisterSellerService`.)

## Input

```text
Warehouse
 ├── name       (required)
 ├── address    (required)
 ├── type       (required: MARKETPLACE | SELLER)
 └── owner      (required when type = SELLER)
```

## Domain Validations

1. `name`, `address` and `type` are required.
2. If `type = MARKETPLACE`, `owner` is forced to `null`.
3. If `type = SELLER`:
   * `owner` is required;
   * the owner must exist (`SellerRepositoryPort.findByIdentification`);
   * the owner must be `ACTIVE`.

## Processing

```text
status    ──► WarehouseStatus.ACTIVE
Warehouse ──► WarehouseRepositoryPort.save
```

## Audit details

`name`, `type`, `sellerIdentification` (only for seller warehouses).

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `DomainException` | Missing data, missing owner, inactive owner. |
| `EntityNotFoundException` | The owner seller does not exist. |

---

# 2. Consult Warehouse

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultWarehouseService` |
| Methods       | `Warehouse execute(User requestingUser, Warehouse warehouse)`<br>`List<Warehouse> executeBySeller(User requestingUser, Seller seller)`<br>`List<Warehouse> executeAll(User requestingUser)` |
| Authorization | `execute`, `executeBySeller`: ADMINISTRATOR, SUPERVISOR, LOGISTICS_OPERATOR, SELLER (own warehouses only)<br>`executeAll`: ADMINISTRATOR, SUPERVISOR, LOGISTICS_OPERATOR |
| Output Ports  | `WarehouseRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves one warehouse by `identifier`, the warehouses of a seller, or every warehouse.

## Authorization rules

A seller can only consult warehouses whose `owner` is the seller itself; marketplace warehouses are not visible to sellers through `execute`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, role not allowed, seller consulting foreign warehouses. |
| `EntityNotFoundException` | The warehouse does not exist. |

---

# 3. Update Warehouse

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateWarehouseService` |
| Method        | `Warehouse execute(User requestingUser, Warehouse warehouse)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `WarehouseRepositoryPort` |
| Operation     | `WAREHOUSE_UPDATE` (affected entity: the warehouse) |

## Description

Updates the `name` and `address` of a warehouse. `type`, `owner` and `status` are not modified.

## Domain Validations

1. The warehouse must exist.
2. `name` and `address` must not be empty.

## Audit details

`previousName`, `previousAddress`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `EntityNotFoundException` | The warehouse does not exist. |
| `DomainException` | Empty name or address. |

---

# 4. Change Warehouse Status

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeWarehouseStatusService` |
| Method        | `Warehouse execute(User requestingUser, Warehouse warehouse, WarehouseStatus newStatus)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `WarehouseRepositoryPort` |
| Operation     | `WAREHOUSE_UPDATE` (affected entity: the warehouse) |

## Description

Activates or deactivates a warehouse (`ACTIVE`, `INACTIVE`).

Effects of an inactive warehouse on other subdomains:

* No stock can be registered in it (`RegisterInventoryInboundService`).
* Its stock is skipped when reserving inventory (`ReserveInventoryService`).

## Domain Validations

1. The warehouse must exist.
2. The new status must differ from the current one.

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `EntityNotFoundException` | The warehouse does not exist. |
| `InvalidStatusTransitionException` | New status equals the current status. |
