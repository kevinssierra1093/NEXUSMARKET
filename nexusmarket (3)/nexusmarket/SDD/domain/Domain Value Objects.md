# Domain Value Objects — NexusMarket

## Introduction

Value Objects represent immutable, controlled business concepts within NexusMarket. They are defined by their values, not by identity, and are used to avoid scattering raw strings (`"ACTIVE"`, `"PAID"`, etc.) throughout the application.

All business catalogs inherit from `DomainCatalog`, following the same convention used across the organization's reference projects.

---

# Value Object Hierarchy

```text
DomainCatalog (Abstract)
├── SystemRole
├── UserStatus
├── BuyerStatus
├── SellerStatus
├── WarehouseType
├── WarehouseStatus
├── ProductType
├── ProductStatus
├── InventoryItemStatus
├── InventoryMovementType
├── OrderStatus
├── InvoiceStatus
├── ShipmentStatus
├── ReturnStatus
├── RefundStatus
└── OperationType
```

---

# DomainCatalog (Abstract)

## Description

Generic business catalog providing a consistent structure (`code`, `name`, `description`) for controlled business values.

## Attributes

| Attribute   | Type   | Description                                     |
| ----------- | ------ | ------------------------------------------------- |
| code        | String | Unique business identifier of the catalog value.    |
| name        | String | Human-readable display name.                          |
| description | String | Business definition of the catalog value.               |

---

# SystemRole

## Description

Represents the responsibilities and permissions of a person within NexusMarket (Section 5, Participantes del Negocio). Defined in `Person` and inherited by `MarketplaceParticipant` and `User`.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code                | Name                | Description                                                      |
| -------------------- | -------------------- | -------------------------------------------------------------------|
| BUYER                 | Buyer                | Person who purchases products published on the platform.               |
| SELLER                 | Seller                | Person or business responsible for registering and managing products.  |
| LOGISTICS_OPERATOR       | Logistics Operator    | Responsible for the physical operation of warehouses and dispatches.      |
| ADMINISTRATOR              | Administrator         | Responsible for administering sellers and warehouses.                       |
| SUPERVISOR                    | Supervisor             | Read-only profile for operational monitoring and consultation.               |

---

# UserStatus

## Description

Represents the current status of a user's access to the platform (Domain 1). Independent from `BuyerStatus` / `SellerStatus`, which represent the commercial relationship, not system access.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                       |
| -------- | -------- | ----------------------------------------------------|
| ACTIVE     | Active     | User can access the system normally.                  |
| INACTIVE     | Inactive   | User exists but cannot perform system operations.         |
| BLOCKED        | Blocked    | User access has been suspended.                              |

---

# BuyerStatus

## Description

Represents the commercial standing of a buyer, i.e., their ability to place orders (Domain 2, "Estado comercial").

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name       | Description                                          |
| -------- | ---------- | -------------------------------------------------------|
| ACTIVE     | Active       | Buyer may place orders normally.                            |
| RESTRICTED   | Restricted   | Buyer may browse but has limited purchasing capability.        |
| BLOCKED        | Blocked      | Buyer is not permitted to place orders.                          |

---

# SellerStatus

## Description

Represents the operational status of a seller within the marketplace (Domain 3).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                             |
| -------- | -------- | -----------------------------------------------------------|
| ACTIVE     | Active     | Seller may publish and manage products normally.               |
| SUSPENDED    | Suspended  | Seller's ability to publish or sell is temporarily disabled.       |
| INACTIVE       | Inactive   | Seller relationship exists but is not currently operating.            |

---

# WarehouseType

## Description

Classifies a warehouse by ownership (Domain 4).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code        | Name        | Description                                     |
| ----------- | ----------- | ---------------------------------------------------|
| MARKETPLACE   | Marketplace   | Warehouse owned and operated by NexusMarket.          |
| SELLER          | Seller        | Warehouse owned and operated by a registered Seller.       |

---

# WarehouseStatus

## Description

Represents the operational availability of a warehouse.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                    |
| -------- | -------- | --------------------------------------------------|
| ACTIVE     | Active     | Warehouse is operational and may hold/dispatch stock. |
| INACTIVE     | Inactive   | Warehouse is temporarily out of operation.               |

---

# ProductType

## Description

Differentiates physical products, which require inventory and dispatch, from digital products, delivered immediately after payment (Domain 5).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                       |
| -------- | -------- | ------------------------------------------------------|
| PHYSICAL   | Physical   | Tangible product requiring inventory and shipment.       |
| DIGITAL      | Digital    | Intangible product delivered immediately upon payment.       |

---

# ProductStatus

## Description

Represents the publication state of a product in the catalog (Domain 5).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code          | Name          | Description                                   |
| ------------- | ------------- | -------------------------------------------------|
| PUBLISHED       | Published       | Product is visible in the public catalog.            |
| SUSPENDED         | Suspended       | Product is temporarily hidden from the catalog.          |
| DISCONTINUED        | Discontinued    | Product is permanently retired from the catalog.             |

---

# InventoryItemStatus

## Description

Represents the physical condition of a stock record, controlling whether it may be reserved (Domain 6, Validaciones Críticas).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                       |
| -------- | -------- | ------------------------------------------------------|
| AVAILABLE  | Available  | Stock is in good condition and may be reserved/sold.    |
| DAMAGED      | Damaged    | Stock is damaged and can never be reserved or sold.        |

---

# InventoryMovementType

## Description

Represents the category of a stock movement (Domain 6).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code             | Name             | Description                                             |
| ----------------- | ----------------- | -----------------------------------------------------------|
| INBOUND              | Inbound              | Stock entering the warehouse (initial load or restock).        |
| RESERVATION            | Reservation          | Stock temporarily reserved for a pending order.                     |
| SALE_OUTBOUND             | Sale Outbound        | Stock permanently removed due to a completed sale.                       |
| ADJUSTMENT                   | Adjustment           | Manual correction of recorded stock levels.                                 |
| RETURN                          | Return               | Stock re-entering the warehouse due to a buyer return.                          |

---

# OrderStatus

## Description

Represents the lifecycle state of an order — the central process of the system (Domain 7).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code            | Name            | Description                                             |
| ---------------- | ---------------- | -----------------------------------------------------------|
| CART                | Cart                | Provisional product selection; freely editable by the buyer. |
| PENDING_PAYMENT       | Pending Payment     | Order confirmed; awaiting financial confirmation.                 |
| PAID                    | Paid                | Payment confirmed; preparation process may begin.                      |
| SHIPPED                    | Shipped             | Order has physically left the warehouse.                                    |
| FINALIZED                     | Finalized           | Order successfully delivered and closed.                                        |

## Lifecycle

```text
CART
  │
  ▼
PENDING_PAYMENT
  │
  ▼
PAID
  │
  ▼
SHIPPED
  │
  ▼
FINALIZED
```

---

# InvoiceStatus

## Description

Represents the state of a commercial invoice generated for a paid order (OBJ-09).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                  |
| -------- | -------- | -------------------------------------------------|
| ISSUED     | Issued     | Invoice has been generated and is valid.             |
| ANNULLED     | Annulled   | Invoice has been cancelled (e.g. due to a full refund).  |

---

# ShipmentStatus

## Description

Represents the logistics state of a shipment (OBJ-10).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                      |
| ---------- | ---------- | -----------------------------------------------------|
| PREPARING    | Preparing    | Order is being packed at the origin warehouse.           |
| DISPATCHED     | Dispatched   | Shipment has left the warehouse.                              |
| IN_TRANSIT       | In Transit   | Shipment is in transport toward the buyer.                        |
| DELIVERED          | Delivered    | Shipment was successfully delivered.                                  |
| FAILED                | Failed       | Delivery attempt failed and requires resolution.                          |

---

# ReturnStatus

## Description

Represents the state of a buyer's return request (OBJ-11).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                        |
| ---------- | ---------- | -------------------------------------------------------|
| REQUESTED    | Requested    | Return request submitted by the buyer.                     |
| APPROVED       | Approved     | Return request approved; awaiting receipt of goods.             |
| REJECTED         | Rejected     | Return request denied.                                             |
| RECEIVED           | Received     | Returned goods received at the warehouse.                              |
| COMPLETED             | Completed    | Return process closed (refund issued or exchange completed).               |

---

# RefundStatus

## Description

Represents the processing state of a monetary refund (OBJ-11).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                    |
| ---------- | ---------- | ---------------------------------------------------|
| REQUESTED    | Requested    | Refund requested as a result of an approved return.   |
| APPROVED       | Approved     | Refund approved for processing.                             |
| REJECTED         | Rejected     | Refund request denied.                                          |
| PROCESSED          | Processed    | Refund amount has been returned to the buyer.                       |

---

# OperationType

## Description

Represents the category of a traceable business action registered in `Operation` and `AuditLog`.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code | Name | Description |
| ---- | ---- | ----------- |
| SELLER_REGISTRATION | Seller Registration | A seller was incorporated by an administrator. |
| WAREHOUSE_REGISTRATION | Warehouse Registration | A warehouse was registered. |
| WAREHOUSE_UPDATE | Warehouse Update | A warehouse was updated or its status changed. |
| PRODUCT_REGISTRATION | Product Registration | A product was registered in the catalog. |
| PRODUCT_UPDATE | Product Update | A product or its variants were updated. |
| PRODUCT_PUBLICATION | Product Publication | A product was published. |
| PRODUCT_SUSPENSION | Product Suspension | A product was suspended. |
| PRODUCT_DISCONTINUATION | Product Discontinuation | A product was discontinued. |
| INVENTORY_INBOUND | Inventory Inbound | Stock entered a warehouse. |
| INVENTORY_RESERVATION | Inventory Reservation | Stock was reserved for an order. |
| INVENTORY_SALE_OUTBOUND | Inventory Sale Outbound | Reserved stock left the warehouse due to a sale. |
| INVENTORY_ADJUSTMENT | Inventory Adjustment | Stock levels or condition were manually adjusted. |
| INVENTORY_RETURN | Inventory Return | Returned stock re-entered a warehouse. |
| CART_UPDATE | Cart Update | Items were added, changed or removed in a cart. |
| ORDER_CONFIRMATION | Order Confirmation | A cart was confirmed as an order. |
| ORDER_PAYMENT | Order Payment | Payment of an order was confirmed. |
| ORDER_DISPATCH | Order Dispatch | An order was dispatched from its warehouses. |
| ORDER_FINALIZATION | Order Finalization | An order was finalized after delivery. |
| INVOICE_ISSUED | Invoice Issued | An invoice was issued. |
| INVOICE_ANNULLED | Invoice Annulled | An invoice was annulled. |
| SHIPMENT_UPDATE | Shipment Update | The logistics status of a shipment changed. |
| RETURN_REQUEST | Return Request | A buyer requested a return. |
| RETURN_APPROVAL | Return Approval | A return request was approved. |
| RETURN_REJECTION | Return Rejection | A return request was rejected. |
| RETURN_RECEPTION | Return Reception | Returned goods were received. |
| REFUND_REQUEST | Refund Request | A refund was requested. |
| REFUND_APPROVAL | Refund Approval | A refund was approved. |
| REFUND_REJECTION | Refund Rejection | A refund was rejected. |
| REFUND_PROCESSING | Refund Processing | A refund was paid to the buyer. |
| USER_REGISTRATION | User Registration | A staff user was registered by an administrator. |
| USER_STATUS_CHANGE | User Status Change | The access status of a user was changed. |
| BUYER_STATUS_CHANGE | Buyer Status Change | The commercial status of a buyer was changed. |
| SELLER_STATUS_CHANGE | Seller Status Change | The operational status of a seller was changed. |

---

# Primitive Enumerations

The following concepts represent fixed technical values without business catalog metadata.

---

# ApprovalDecision

## Description

Represents the result of an approval process (e.g., return or refund review).

## Values

```text
APPROVED
REJECTED
```

---

# Value Object Design Rules

* Value Objects are immutable and compared by value.
* Business catalogs must use controlled `DomainCatalog` values instead of raw strings (`"ACTIVE"`, `"PAID"`, `"DAMAGED"`) throughout the codebase.
* `UserStatus` (system access) is independent from `BuyerStatus` / `SellerStatus` (commercial relationship), mirroring the independence between `UserStatus` and `CustomerStatus` in the reference architecture.
* `OrderStatus` represents current state; `InventoryMovementType` and future `OperationType` values represent the events that produced state changes.
