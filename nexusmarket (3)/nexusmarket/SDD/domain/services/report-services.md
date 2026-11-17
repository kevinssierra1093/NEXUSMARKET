# Report Services

## Introduction

This document defines the services belonging to the **Administrative Reports** subdomain of NexusMarket (OBJ-12 "Consolidar información administrativa para consulta", process "Consulta de reportes administrativos").

The reports consolidate information from orders, inventory, returns and refunds. They are read-only and are intended for the **Administrator** and the **Supervisor** (consultation and operational follow-up profile).

The services operate exclusively with **Domain Models** and **Value Objects** and obtain information through **Output Ports**.

Package: `application.domain.services.report`

---

# Architectural Principles

## Report output

Each report returns a `Map<String, Object>` with ordered keys (`LinkedHashMap`). The input adapter converts it into a Response DTO; the keys below are the contract between the domain and the adapter.

## Authorization

Global reports use `AuthorizeAdministrativeConsultationService` (active `ADMINISTRATOR` or `SUPERVISOR`). The seller report is also available to the owning `SELLER`.

## Operation

Reports are read-only and are not audited.

---

# Report Services

1. Consult Orders Report
2. Consult Inventory Report
3. Consult Returns Report
4. Consult Seller Report

---

# 1. Consult Orders Report

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultOrdersReportService` |
| Method        | `Map<String, Object> execute(User requestingUser)` |
| Output Ports  | `OrderRepositoryPort` |
| Internal services | `CalculateOrderTotalService` |

## Output keys

| Key              | Type                | Description |
| ---------------- | ------------------- | ----------- |
| `totalOrders`    | `Integer`           | Number of orders (all statuses, including carts). |
| `ordersByStatus` | `Map<String, Long>` | Number of orders per `OrderStatus` code (sorted). |
| `totalSales`     | `BigDecimal`        | Sum of the totals of orders in `PAID`, `SHIPPED` or `FINALIZED`. |

---

# 2. Consult Inventory Report

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultInventoryReportService` |
| Method        | `Map<String, Object> execute(User requestingUser)` |
| Output Ports  | `InventoryItemRepositoryPort` |

## Output keys

| Key                         | Type                   | Description |
| --------------------------- | ---------------------- | ----------- |
| `inventoryRecords`          | `Integer`              | Number of inventory records. |
| `totalAvailableUnits`       | `Integer`              | Sum of `quantityAvailable`. |
| `totalReservedUnits`        | `Integer`              | Sum of `quantityReserved`. |
| `damagedRecords`            | `Long`                 | Records in `DAMAGED` status. |
| `availableUnitsByWarehouse` | `Map<String, Integer>` | Available units per warehouse identifier (sorted). |

---

# 3. Consult Returns Report

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultReturnsReportService` |
| Method        | `Map<String, Object> execute(User requestingUser)` |
| Output Ports  | `ReturnRequestRepositoryPort`, `RefundRepositoryPort` |

## Output keys

| Key               | Type                | Description |
| ----------------- | ------------------- | ----------- |
| `totalReturns`    | `Integer`           | Number of return requests. |
| `returnsByStatus` | `Map<String, Long>` | Returns per `ReturnStatus` code. |
| `refundsByStatus` | `Map<String, Long>` | Refunds per `RefundStatus` code. |
| `totalRefunded`   | `BigDecimal`        | Sum of the amounts of `PROCESSED` refunds. |

---

# 4. Consult Seller Report

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultSellerReportService` |
| Methods       | `Map<String, Object> execute(User requestingUser, Seller seller)`<br>`List<Map<String, Object>> executeAll(User requestingUser)` |
| Authorization | `execute`: owning SELLER (`ValidateSellerOwnershipService`), ADMINISTRATOR, SUPERVISOR<br>`executeAll`: ADMINISTRATOR, SUPERVISOR |
| Output Ports  | `SellerRepositoryPort`, `ProductRepositoryPort`, `OrderRepositoryPort`, `InventoryItemRepositoryPort`, `InventoryMovementRepositoryPort`, `ReturnRequestRepositoryPort`, `RefundRepositoryPort` |
| REST          | `GET /api/reports/sellers/{identification}` · `GET /api/reports/sellers` |

## Description

Behaviour of one seller's business, computed **only from persisted data and without timestamps**: consulting it before and after restarting the application returns exactly the same result. Lists are sorted (product name, identifiers) so the output is deterministic.

## Output keys

| Key | Content |
| --- | ------- |
| `seller` | `identification`, `businessName`, `status` |
| `products` | `total`, `physical`, `digital`, `list` (identifier, name, type, status) |
| `sales` | `ordersWithSales` (orders `PAID`/`SHIPPED`/`FINALIZED` containing the seller's products), `ordersByStatus`, `unitsSold`, `grossSales`, `byProduct` (units and amount per product). Only the seller's lines are counted. |
| `inventory` | `totalAvailableUnits`, `totalReservedUnits`, `damagedRecords`, `movementUnitsByType` (`INBOUND`, `RESERVATION`, `SALE_OUTBOUND`, `RETURN`, `ADJUSTMENT`: units moved by purchases, dispatches, returns and corrections), `items` (stock per variant and warehouse, with its own movements) |
| `returnsAndRefunds` | `totalReturns`, `returnsByStatus`, `unitsReturned` (received or completed), `refundsByStatus`, `totalRefunded` (processed refunds), `items` (return, order, product, quantity, return status, refund status and amount) |
| `summary` | `grossSales`, `totalRefunded`, `netSales = grossSales − totalRefunded` |

Money values have 2 decimals.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, a seller consulting another seller, or a role other than SELLER/ADMINISTRATOR/SUPERVISOR. |
| `EntityNotFoundException` | The seller does not exist. |

---

# Exceptions (all reports)

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester inactive or not `ADMINISTRATOR`/`SUPERVISOR`. |
