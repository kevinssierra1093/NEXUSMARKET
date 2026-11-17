# Shipment Services

## Introduction

This document defines the services belonging to the **Shipment Management** subdomain of NexusMarket (OBJ-10 "Gestionar los procesos logísticos").

A `Shipment` represents the logistics process that moves the physical items of an order from **one** warehouse to the buyer. An order generates one shipment per origin warehouse of its physical items. Digital products never generate shipments.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.shipment`

---

# Architectural Principles

## Domain Model Parameters

```java
updateShipmentStatus(User requestingUser, Shipment shipment, ShipmentStatus newStatus);
```

## Shipment lifecycle

```text
PREPARING ──DispatchOrder──► DISPATCHED ──► IN_TRANSIT ──► DELIVERED
                                 │              │  ▲
                                 │              ▼  │
                                 │            FAILED (retry → IN_TRANSIT)
                                 └──────────────────────────► DELIVERED
```

| Transition                     | Service |
| ------------------------------ | ------- |
| (created) → `PREPARING`        | `CreateShipmentsService` (when the order is paid) |
| `PREPARING` → `DISPATCHED`     | `DispatchOrderService` (order subdomain) |
| `DISPATCHED` → `IN_TRANSIT`    | `UpdateShipmentStatusService` |
| `IN_TRANSIT` → `FAILED`        | `UpdateShipmentStatusService` |
| `FAILED` → `IN_TRANSIT`        | `UpdateShipmentStatusService` |
| `DISPATCHED`/`IN_TRANSIT` → `DELIVERED` | `ConfirmShipmentDeliveryService` |

## Responsibility matrix

The **Logistics Operator** is in charge of the physical operation of warehouses and dispatches. Every write operation requires `LOGISTICS_OPERATOR`.

---

# Shipment Services

1. Update Shipment Status
2. Confirm Shipment Delivery
3. Consult Shipment
4. Create Shipments *(internal)*

---

# 1. Update Shipment Status

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateShipmentStatusService` |
| Method        | `Shipment execute(User requestingUser, Shipment shipment, ShipmentStatus newStatus)` |
| Authorization | LOGISTICS_OPERATOR |
| Output Ports  | `ShipmentRepositoryPort` |
| Operation     | `SHIPMENT_UPDATE` (affected entity: the shipment) |

## Description

Records transport tracking.

## Domain Validations

1. The shipment must exist.
2. Only the transitions `DISPATCHED → IN_TRANSIT`, `IN_TRANSIT → FAILED` and `FAILED → IN_TRANSIT` are allowed. Dispatch and delivery have their own services.

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active logistics operator. |
| `EntityNotFoundException` | Shipment not found. |
| `InvalidStatusTransitionException` | Transition not allowed. |

---

# 2. Confirm Shipment Delivery

| Item          | Value |
| ------------- | ----- |
| Class         | `ConfirmShipmentDeliveryService` |
| Method        | `Shipment execute(User requestingUser, Shipment shipment)` |
| Authorization | LOGISTICS_OPERATOR |
| Output Ports  | `ShipmentRepositoryPort`, `OrderRepositoryPort` |
| Internal services | `FinalizeOrderService` |
| Operation     | `SHIPMENT_UPDATE` (affected entity: the shipment); `ORDER_FINALIZATION` when the order closes |

## Description

Confirms the delivery of a shipment. When **every** shipment of the order is `DELIVERED` and the order is `SHIPPED`, the order is finalized (business flow step 8 "Cierre").

## Domain Validations

1. The shipment must exist.
2. The current status must be `DISPATCHED` or `IN_TRANSIT`.

## Processing

```text
status = DELIVERED, deliveryDate = now
ShipmentRepositoryPort.update(shipment)
order = OrderRepositoryPort.findByIdentifier(shipment.order)
if all shipments of the order are DELIVERED and order.status = SHIPPED:
    FinalizeOrderService(order)
```

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active logistics operator. |
| `EntityNotFoundException` | Shipment or order not found. |
| `InvalidStatusTransitionException` | Shipment not deliverable. |

---

# 3. Consult Shipment

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultShipmentService` |
| Method        | `List<Shipment> executeByOrder(User requestingUser, Order order)` |
| Authorization | `AuthorizeOrderConsultationService` |
| Output Ports  | `ShipmentRepositoryPort`, `OrderRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves the shipments of an order.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order not found. |
| `UnauthorizedOperationException` | Not authorized on the order. |

---

# 4. Create Shipments *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `CreateShipmentsService` |
| Method        | `List<Shipment> execute(User requestingUser, Order order)` |
| Invoked by    | `ConfirmOrderPaymentService` |
| Output Ports  | `ShipmentRepositoryPort` |
| Operation     | `SHIPMENT_UPDATE` per shipment (affected entity: the shipment) |

## Description

Groups the order lines by `OrderItem.warehouse` (lines without warehouse — digital products — are ignored) and creates one shipment per origin warehouse with `status = PREPARING` and no `handledBy`.

## Audit details

`event = CREATED`, `order`, `originWarehouse`, `newStatus`.
