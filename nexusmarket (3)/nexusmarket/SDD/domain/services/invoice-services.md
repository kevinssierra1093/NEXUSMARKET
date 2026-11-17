# Invoice Services

## Introduction

This document defines the services belonging to the **Invoice Management** subdomain of NexusMarket (OBJ-09 "Administrar la facturación de las compras").

An `Invoice` is the commercial and fiscal record of a paid order. It is created automatically when the payment is confirmed and annulled when every product of the order has been returned and refunded (a partial return keeps the invoice `ISSUED`).

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.invoice`

---

# Architectural Principles

## Domain Model Parameters

The invoice is always reached through its order:

```java
consultInvoice(User requestingUser, Order order);
```

## Invoice lifecycle

```text
ISSUED ──(refund processed)──► ANNULLED
```

## Rules

* One invoice per order.
* `totalAmount` is calculated by `CalculateOrderTotalService` from the order lines (`unitPrice × quantity`).
* Issue and annulment are **internal** services; only consultation is exposed.

---

# Invoice Services

1. Consult Invoice
2. Issue Invoice *(internal)*
3. Annul Invoice *(internal)*

---

# 1. Consult Invoice

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultInvoiceService` |
| Method        | `Invoice execute(User requestingUser, Order order)` |
| Authorization | `AuthorizeOrderConsultationService` (owning buyer, involved seller, logistics operator, supervisor) |
| Output Ports  | `InvoiceRepositoryPort`, `OrderRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves the invoice of an order.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order or invoice not found (orders in `CART` or `PENDING_PAYMENT` have no invoice). |
| `UnauthorizedOperationException` | Not authorized on the order. |

---

# 2. Issue Invoice *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `IssueInvoiceService` |
| Method        | `Invoice execute(User requestingUser, Order order)` |
| Invoked by    | `ConfirmOrderPaymentService` |
| Output Ports  | `InvoiceRepositoryPort` |
| Operation     | `INVOICE_ISSUED` (affected entity: the invoice) |

## Description

Creates the invoice of a paid order: `issueDate = now`, `totalAmount = order total`, `status = ISSUED`.

## Domain Validations

1. The order must not already have an invoice.

## Audit details

`order`, `totalAmount`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | The order already has an invoice. |

---

# 3. Annul Invoice *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `AnnulInvoiceService` |
| Method        | `Invoice execute(User requestingUser, Order order)` |
| Invoked by    | `ProcessRefundService` |
| Output Ports  | `InvoiceRepositoryPort` |
| Operation     | `INVOICE_ANNULLED` (affected entity: the invoice) |

## Description

Annuls the invoice of an order whose refund has been processed: `ISSUED → ANNULLED`.

## Audit details

`order`, `previousStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The order has no invoice. |
| `InvalidStatusTransitionException` | The invoice is not `ISSUED`. |
