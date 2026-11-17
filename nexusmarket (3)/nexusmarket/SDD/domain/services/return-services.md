# Return Services

## Introduction

This document defines the services belonging to the **Return Management** subdomain of NexusMarket (OBJ-11 "Administrar devoluciones y reembolsos", process "Gestión de devoluciones").

A `ReturnRequest` is the buyer's request to return **one product (order line)** of a finalized order. Because a finalized order can never be modified (Validation, section 11), the return is a **separate entity** that refers to the order and to the returned variant. The other products of the order keep their state.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.returns`

---

# Architectural Principles

## Domain Model Parameters

```java
requestReturn(User requestingUser, ReturnRequest request);   // request.order : Order, request.variant : ProductVariant
approveReturn(User requestingUser, ReturnRequest request);
```

## Return lifecycle

```text
REQUESTED ──Approve──► APPROVED ──ConfirmReception──► RECEIVED ──(refund processed)──► COMPLETED
    │                     │
    └──Reject──► REJECTED └── digital-only order: refund requested directly from APPROVED ──► COMPLETED
```

`COMPLETED` is set by `ProcessRefundService` (refund subdomain).

## Scope

The return and the refund cover **one product** (the whole quantity of its order line). The seller of the returned product is `variant.product.seller`. A buyer can return several products of the same order with separate requests (one active return per product).

## Responsibility matrix

| Operation          | Role |
| ------------------ | ---- |
| Request            | Owning BUYER |
| Approve / Reject   | ADMINISTRATOR |
| Confirm reception  | LOGISTICS_OPERATOR |
| Consult            | Owning BUYER, ADMINISTRATOR, SUPERVISOR |

---

# Return Services

1. Request Return
2. Approve Return
3. Reject Return
4. Confirm Return Reception
5. Consult Return

---

# 1. Request Return

| Item          | Value |
| ------------- | ----- |
| Class         | `RequestReturnService` |
| Method        | `ReturnRequest execute(User requestingUser, ReturnRequest request)` |
| Authorization | Owning BUYER (`ValidateBuyerOwnershipService`) |
| Output Ports  | `ReturnRequestRepositoryPort`, `OrderRepositoryPort` |
| Operation     | `RETURN_REQUEST` (affected entity: the return request) |

## Input

```text
ReturnRequest
 ├── order   : Order           (required)
 ├── variant : ProductVariant  (required: the product to return)
 └── reason  : String          (required)
```

## Domain Validations

1. The order must exist and belong to the requesting buyer.
2. The order must be `FINALIZED`.
3. `reason` is required.
4. The variant must be one of the lines of the order.
5. That product must not have another return that is not `REJECTED` (one return in progress or completed per product).

## Processing

```text
buyer = order.buyer, variant = order line variant, quantity = line quantity
status = REQUESTED, requestDate = now
ReturnRequestRepositoryPort.save
```

## Audit details

`order`, `variant`, `product`, `seller`, `quantity`, `reason`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order not found, or the variant is not a line of the order. |
| `UnauthorizedOperationException` | Not the owning buyer. |
| `DomainException` | Order not finalized, missing reason or variant, return of that product already in progress. |

---

# 2. Approve Return

| Item          | Value |
| ------------- | ----- |
| Class         | `ApproveReturnService` |
| Method        | `ReturnRequest execute(User requestingUser, ReturnRequest request)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `ReturnRequestRepositoryPort` |
| Operation     | `RETURN_APPROVAL` (affected entity: the return request) |

## Description

`REQUESTED → APPROVED`, `resolutionDate = now`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active administrator. |
| `EntityNotFoundException` | Return not found. |
| `InvalidStatusTransitionException` | Return not `REQUESTED`. |

---

# 3. Reject Return

| Item          | Value |
| ------------- | ----- |
| Class         | `RejectReturnService` |
| Method        | `ReturnRequest execute(User requestingUser, ReturnRequest request)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `ReturnRequestRepositoryPort` |
| Operation     | `RETURN_REJECTION` (affected entity: the return request) |

## Description

`REQUESTED → REJECTED`, `resolutionDate = now`. After a rejection the buyer may file a new return for the same order.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active administrator. |
| `EntityNotFoundException` | Return not found. |
| `InvalidStatusTransitionException` | Return not `REQUESTED`. |

---

# 4. Confirm Return Reception

| Item          | Value |
| ------------- | ----- |
| Class         | `ConfirmReturnReceptionService` |
| Method        | `ReturnRequest execute(User requestingUser, ReturnRequest request)` |
| Authorization | LOGISTICS_OPERATOR |
| Output Ports  | `ReturnRequestRepositoryPort`, `OrderRepositoryPort` |
| Internal services | `RegisterInventoryReturnService` |
| Operation     | `RETURN_RECEPTION` (affected entity: the return request) |

## Description

A logistics operator receives the returned product: **only the units of the returned line** re-enter the warehouse they were dispatched from (`RETURN` movement). `APPROVED → RECEIVED`.

## Domain Validations

1. The return must exist and be `APPROVED`.
2. The returned line must be a physical product (it has a warehouse). Digital products skip this step and request the refund directly.

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active logistics operator. |
| `EntityNotFoundException` | Return, order or inventory record not found. |
| `InvalidStatusTransitionException` | Return not `APPROVED`. |
| `DomainException` | The returned product is digital. |

---

# 5. Consult Return

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultReturnService` |
| Methods       | `ReturnRequest executeById(User requestingUser, ReturnRequest request)`<br>`List<ReturnRequest> executeByBuyer(User requestingUser, Buyer buyer)`<br>`List<ReturnRequest> executeByStatus(User requestingUser, ReturnRequest filter)`<br>`List<ReturnRequest> executeByOrder(User requestingUser, Order order)` |
| Authorization | `executeById`, `executeByBuyer`: `AuthorizeRefundOperationService.executeConsultation` (owning BUYER, ADMINISTRATOR, SUPERVISOR)<br>`executeByStatus`: ADMINISTRATOR, SUPERVISOR<br>`executeByOrder`: whoever can consult the order (owning buyer, involved seller, logistics operator, supervisor) and ADMINISTRATOR |
| Output Ports  | `ReturnRequestRepositoryPort` |
| Operation     | Not audited (read-only) |

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Return not found. |
| `UnauthorizedOperationException` | Not authorized. |
