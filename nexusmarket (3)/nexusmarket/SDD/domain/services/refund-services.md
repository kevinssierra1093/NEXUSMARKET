# Refund Services

## Introduction

This document defines the services belonging to the **Refund Management** subdomain of NexusMarket (OBJ-11 "Administrar devoluciones y reembolsos", process "Gestión de reembolsos").

A `Refund` is the monetary reimbursement issued as a consequence of the return of **one product**. Its amount is the subtotal of the returned line (`unitPrice × quantity`). The invoice is annulled only when every product of the order has been returned and refunded.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.refund`

---

# Architectural Principles

## Domain Model Parameters

```java
requestRefund(User requestingUser, ReturnRequest returnRequest);
processRefund(User requestingUser, Refund refund);
```

## Refund lifecycle

```text
REQUESTED ──Approve──► APPROVED ──Process──► PROCESSED
    │
    └──Reject──► REJECTED
```

## When a refund can be requested

| Order content            | Required return status |
| ------------------------ | ---------------------- |
| Physical product         | `RECEIVED` (goods back in the warehouse) |
| Digital product          | `APPROVED` (nothing to receive) |

## Responsibility matrix

"Gestión Reembolsos": Buyer and Administrator (section 12). The buyer requests; the administrator approves, rejects and processes; supervisors may consult.

---

# Refund Services

1. Request Refund
2. Approve Refund
3. Reject Refund
4. Process Refund
5. Consult Refund

---

# 1. Request Refund

| Item          | Value |
| ------------- | ----- |
| Class         | `RequestRefundService` |
| Method        | `Refund execute(User requestingUser, ReturnRequest returnRequest)` |
| Authorization | `AuthorizeRefundOperationService` (owning BUYER or ADMINISTRATOR) |
| Output Ports  | `RefundRepositoryPort`, `ReturnRequestRepositoryPort`, `OrderRepositoryPort` |
| Internal services | `CalculateOrderTotalService` |
| Operation     | `REFUND_REQUEST` (affected entity: the refund) |

## Domain Validations

1. The return request and its order must exist.
2. The return must be ready (see table above).
3. The return must not already have a refund.

## Processing

```text
amount = line.unitPrice × returnRequest.quantity   (only the returned product)
status = REQUESTED
RefundRepositoryPort.save
```

## Audit details

`returnRequest`, `amount`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Return or order not found. |
| `UnauthorizedOperationException` | Not the owning buyer nor an administrator. |
| `DomainException` | Return not ready, or refund already exists. |

---

# 2. Approve Refund

| Item          | Value |
| ------------- | ----- |
| Class         | `ApproveRefundService` |
| Method        | `Refund execute(User requestingUser, Refund refund)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `RefundRepositoryPort` |
| Operation     | `REFUND_APPROVAL` (affected entity: the refund) |

## Description

`REQUESTED → APPROVED`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active administrator. |
| `EntityNotFoundException` | Refund not found. |
| `InvalidStatusTransitionException` | Refund not `REQUESTED`. |

---

# 3. Reject Refund

| Item          | Value |
| ------------- | ----- |
| Class         | `RejectRefundService` |
| Method        | `Refund execute(User requestingUser, Refund refund)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `RefundRepositoryPort` |
| Operation     | `REFUND_REJECTION` (affected entity: the refund) |

## Description

`REQUESTED → REJECTED`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active administrator. |
| `EntityNotFoundException` | Refund not found. |
| `InvalidStatusTransitionException` | Refund not `REQUESTED`. |

---

# 4. Process Refund

| Item          | Value |
| ------------- | ----- |
| Class         | `ProcessRefundService` |
| Method        | `Refund execute(User requestingUser, Refund refund)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `RefundRepositoryPort`, `ReturnRequestRepositoryPort`, `OrderRepositoryPort` |
| Internal services | `AnnulInvoiceService` |
| Operation     | `REFUND_PROCESSING` (affected entity: the refund) |

## Description

The amount is returned to the buyer. `APPROVED → PROCESSED`.

## Processing

```text
refund.status = PROCESSED, processedDate = now
returnRequest.status = COMPLETED
if every line of the order has a COMPLETED return:
    AnnulInvoiceService(order)        (invoice ISSUED → ANNULLED)
else:
    invoice stays ISSUED              (partial return: the other products keep their state)
```

The order itself remains `FINALIZED` and is not modified.

## Audit details

`previousStatus`, `newStatus`, `amount`, `variant`, `invoiceAnnulled`.

## Transaction

Refund, return and invoice must be updated atomically.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active administrator. |
| `EntityNotFoundException` | Refund, return, order or invoice not found. |
| `InvalidStatusTransitionException` | Refund not `APPROVED`, or invoice not `ISSUED`. |

---

# 5. Consult Refund

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultRefundService` |
| Methods       | `Refund executeByReturnRequest(User requestingUser, ReturnRequest returnRequest)`<br>`List<Refund> executeAll(User requestingUser)` |
| Authorization | `executeByReturnRequest`: owning BUYER, ADMINISTRATOR, SUPERVISOR<br>`executeAll`: ADMINISTRATOR, SUPERVISOR |
| Output Ports  | `RefundRepositoryPort`, `ReturnRequestRepositoryPort` |
| Operation     | Not audited (read-only) |

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Return or refund not found. |
| `UnauthorizedOperationException` | Not authorized. |
