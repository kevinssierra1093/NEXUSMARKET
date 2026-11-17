# Seller Services

## Introduction

This document defines the services belonging to the **Seller Management** subdomain of NexusMarket (OBJ-02, Domain 3 "Gestión de Vendedores").

The services in this subdomain incorporate sellers into the marketplace and maintain their information and operational status.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.seller`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
registerSeller(String identification, String businessName, String warehouseName);
```

Correct:

```java
registerSeller(User requestingUser, Seller seller, Warehouse firstWarehouse);
```

## Business rule

> Sellers cannot self-register; they are incorporated by the Administrator (Domain 3).

> Business flow, step 1 "Incorporación": the Administrator registers the seller **and its first warehouse**.

## Seller relationships

```text
Seller
 ├── warehouses : List<Warehouse>   (loaded on demand)
 └── products   : List<Product>     (loaded on demand)
```

---

# Seller Services

1. Register Seller
2. Consult Seller
3. Update Seller
4. Change Seller Status

---

# 1. Register Seller

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterSellerService` |
| Method        | `Seller execute(User requestingUser, Seller seller, Warehouse firstWarehouse)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `SellerRepositoryPort`, `BuyerRepositoryPort`, `UserRepositoryPort`, `WarehouseRepositoryPort` |
| Operations    | `SELLER_REGISTRATION` (affected entity: none) and `WAREHOUSE_REGISTRATION` (affected entity: the warehouse) |

## Description

Incorporates a new seller together with the seller's first warehouse.

## Input

```text
Seller
 ├── identification, fullName, email, phoneNumber, address
 └── businessName
Warehouse
 ├── name
 └── address
```

## Domain Validations

1. `fullName` must not be empty.
2. `businessName` is required.
3. `firstWarehouse` is required.
4. `identification` and `email` must be unique across sellers, buyers and users.

## Processing

```text
Seller:    role = SELLER, status = ACTIVE, registrationDate = today ──► SellerRepositoryPort.save
Warehouse: type = SELLER, owner = saved seller, status = ACTIVE      ──► WarehouseRepositoryPort.save
saved seller.warehouses += saved warehouse
```

## Audit details

* `SELLER_REGISTRATION`: `sellerIdentification`, `businessName`.
* `WAREHOUSE_REGISTRATION`: `sellerIdentification`, `name`.

## Output

The persisted `Seller` with its first warehouse in `warehouses`.

## Transaction

The seller and the warehouse must be persisted atomically (single transaction in the input adapter).

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `DomainException` | Missing data, missing first warehouse, duplicated identification or email. |

## Seller access

The seller's system access is created afterwards with `RegisterParticipantUserService` (`role = SELLER`, `participant = seller`).

---

# 2. Consult Seller

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultSellerService` |
| Methods       | `Seller execute(User requestingUser, Seller seller)`<br>`List<Seller> executeAll(User requestingUser)` |
| Authorization | `execute`: SELLER (own data only), ADMINISTRATOR, SUPERVISOR<br>`executeAll`: ADMINISTRATOR, SUPERVISOR |
| Output Ports  | `SellerRepositoryPort`, `WarehouseRepositoryPort`, `ProductRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

`execute` retrieves a seller by `identification` and populates its `warehouses` and `products`. `executeAll` returns every seller without those lists.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, role not allowed, or a seller consulting another seller. |
| `EntityNotFoundException` | The seller does not exist. |

---

# 3. Update Seller

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateSellerService` |
| Method        | `Seller execute(User requestingUser, Seller seller)` |
| Authorization | The owning seller (`ValidateSellerOwnershipService`) |
| Output Ports  | `SellerRepositoryPort` |
| Operation     | Not audited |

## Description

Updates the seller's contact data and business name.

## Updatable attributes

`fullName`, `phoneNumber`, `address`, `businessName`.

## Domain Validations

1. The seller must exist.
2. The requesting user must be the owning seller.
3. `businessName` is required.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester or not the owner. |
| `EntityNotFoundException` | The seller does not exist. |
| `DomainException` | Missing business name. |

---

# 4. Change Seller Status

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeSellerStatusService` |
| Method        | `Seller execute(User requestingUser, Seller seller, SellerStatus newStatus)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `SellerRepositoryPort` |
| Operation     | `SELLER_STATUS_CHANGE` (affected entity: none) |

## Description

Changes the operational status of a seller: `ACTIVE`, `SUSPENDED`, `INACTIVE`. A non-active seller cannot register or publish products, nor receive new warehouses.

## Domain Validations

1. The seller must exist.
2. The new status must differ from the current one.

## Audit details

`sellerIdentification`, `previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `EntityNotFoundException` | The seller does not exist. |
| `InvalidStatusTransitionException` | New status equals the current status. |
