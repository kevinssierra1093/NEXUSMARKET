# Buyer Services

## Introduction

This document defines the services belonging to the **Buyer Management** subdomain of NexusMarket (OBJ-03, Domain 2 "Gestión de Compradores").

The services in this subdomain register, consult and maintain the buyers that take part in commercial processes.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.buyer`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
changeBuyerStatus(String identification, String status);
```

Correct:

```java
changeBuyerStatus(User requestingUser, Buyer buyer, BuyerStatus newStatus);
```

## Key restriction

> The buyer never administers information of other buyers, sellers or inventory (Domain 2, RG-03).

Every buyer-initiated operation validates ownership with `ValidateBuyerOwnershipService`: the `participant` of the requesting user must be a `Buyer` with the same `identification`.

## Buyer and BuyerStatus

`BuyerStatus` (`ACTIVE`, `RESTRICTED`, `BLOCKED`) is the **commercial** standing of the buyer and determines whether they may place orders. It is independent from `UserStatus`, which controls access to the system.

---

# Buyer Services

1. Register Buyer
2. Consult Buyer
3. Update Buyer
4. Change Buyer Status

---

# 1. Register Buyer

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterBuyerService` |
| Method        | `Buyer execute(Buyer buyer)` |
| Authorization | None — the only operation exempt from RG-01, because the buyer has no access yet |
| Output Ports  | `BuyerRepositoryPort`, `SellerRepositoryPort`, `UserRepositoryPort` |
| Operation     | Not audited (there is no authenticated user to attribute it to) |

## Description

Self-registration of a new buyer. After registering, the buyer obtains system access through `RegisterParticipantUserService`.

## Input

```text
Buyer
 ├── identification, fullName, email, phoneNumber, address
 ├── primaryAddress          (required)
 └── additionalAddresses     (optional)
```

## Domain Validations

1. `fullName` must not be empty.
2. `primaryAddress` is required.
3. `identification` must not exist among buyers, sellers or users.
4. `email` must not exist among buyers, sellers or users.

## Processing

```text
role   ──► SystemRole.BUYER
status ──► BuyerStatus.ACTIVE
Buyer  ──► BuyerRepositoryPort.save
```

## Output

The persisted `Buyer`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | Missing full name or primary address; duplicated identification or email. |

## Typical registration flow (input adapter)

```text
POST /api/buyers/register
   1. RegisterBuyerService.execute(buyer)
   2. RegisterParticipantUserService.execute(user with participant = buyer, role = BUYER)
```

Both calls should run inside the same transaction so that a failed user registration does not leave an orphan buyer.

---

# 2. Consult Buyer

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultBuyerService` |
| Methods       | `Buyer execute(User requestingUser, Buyer buyer)`<br>`List<Buyer> executeAll(User requestingUser)` |
| Authorization | `execute`: BUYER (own data only), ADMINISTRATOR, SUPERVISOR<br>`executeAll`: ADMINISTRATOR, SUPERVISOR |
| Output Ports  | `BuyerRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves a buyer by `identification`, or every buyer.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, role not allowed, or a buyer consulting another buyer. |
| `EntityNotFoundException` | The buyer does not exist. |

---

# 3. Update Buyer

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateBuyerService` |
| Method        | `Buyer execute(User requestingUser, Buyer buyer)` |
| Authorization | The owning buyer (`ValidateBuyerOwnershipService`) |
| Output Ports  | `BuyerRepositoryPort` |
| Operation     | Not audited |

## Description

Updates the buyer's contact data and delivery addresses.

## Updatable attributes

`fullName`, `phoneNumber`, `address`, `primaryAddress`, `additionalAddresses`.

`identification`, `email`, `role` and `status` are **not** modified by this service.

## Domain Validations

1. The buyer must exist.
2. The requesting user must be the owning buyer.
3. `primaryAddress` is required.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester or not the owner. |
| `EntityNotFoundException` | The buyer does not exist. |
| `DomainException` | Missing primary address. |

---

# 4. Change Buyer Status

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeBuyerStatusService` |
| Method        | `Buyer execute(User requestingUser, Buyer buyer, BuyerStatus newStatus)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `BuyerRepositoryPort` |
| Operation     | `BUYER_STATUS_CHANGE` (affected entity: none) |

## Description

Changes the commercial status of a buyer. A buyer whose status is not `ACTIVE` cannot use the cart or confirm orders (`ResolveBuyerCartService`, `ConfirmOrderService`).

## Domain Validations

1. The buyer must exist.
2. The new status must differ from the current one.

## Audit details

`buyerIdentification`, `previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `EntityNotFoundException` | The buyer does not exist. |
| `InvalidStatusTransitionException` | New status equals the current status. |
