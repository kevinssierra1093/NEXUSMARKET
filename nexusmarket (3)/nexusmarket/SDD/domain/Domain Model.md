# Domain Model — NexusMarket

## Introduction

This Domain Model represents the core business entities of NexusMarket, derived from the *Especificación Funcional del Negocio*. It follows the same modeling conventions used across the organization's reference projects: Object-Oriented Design and Domain-Driven Design (DDD), with explicit object relationships (not raw identifiers) and inheritance used only to represent genuine business specialization.

The model distinguishes between:

* **Persons**, which represent identifiable people and their role within the system.
* **Marketplace Participants**, which represent people or organizations with a commercial relationship with the platform (Buyer, Seller).
* **Users**, which represent system identities used for authentication and authorization.
* **Catalog & Inventory entities**, which represent products, variants, warehouses and stock.
* **Commercial entities**, which represent the order lifecycle: Order, Invoice, Shipment, Return, Refund.

---

# Domain Class Hierarchy

```text
Person (Abstract)
├── MarketplaceParticipant (Abstract)
│   ├── Buyer
│   └── Seller
│
└── User

TraceableEntity (Abstract)   <- provides `identifier`; equivalent to BankingProduct
├── Warehouse
├── Product
├── ProductVariant
├── InventoryItem
├── Order
├── Invoice
├── Shipment
├── ReturnRequest
└── Refund

InventoryMovement            (event, own `movementId`)
OrderItem                    (order line, identified by order + variant)
Operation                    (traceability event)
AuditLog                     (traceability detail)
```

---

# Domain Relationships

```text
Person
   │
   ├── MarketplaceParticipant
   │      ├── Buyer
   │      └── Seller
   │
   └── User
          │
          └── participant : MarketplaceParticipant (optional)

Seller
   │
   ├── owns ──────────────> Warehouse (seller warehouses)
   └── publishes ─────────> Product

Product
   └── has ────────────────> ProductVariant

Warehouse ── ProductVariant ──> InventoryItem
                                      │
                                      └── generates ──> InventoryMovement

Buyer
   └── places ─────────────> Order

Order
   ├── contains ───────────> OrderItem (ProductVariant, quantity, price)
   ├── generates ──────────> Invoice
   ├── generates ──────────> Shipment (one per origin warehouse)
   └── may generate ───────> ReturnRequest
                                   │
                                   └── may generate ──> Refund

Shipment
   ├── departsFrom ────────> Warehouse
   └── handledBy ──────────> User (role LOGISTICS_OPERATOR)
```

---

# Entities

---

# TraceableEntity (Abstract)

## Description

Common base of every business entity that can be the subject of an `Operation` / `AuditLog` (equivalent to `BankingProduct` in the reference architecture). It only carries the `identifier`, assigned by the persistence adapter when the entity is saved.

## Attributes

| Attribute  | Type   | Description                      |
| ---------- | ------ | -------------------------------- |
| identifier | String | Unique identifier of the entity. |

---

# Person (Abstract)

## Description

Represents any identifiable individual within NexusMarket. Centralizes identity and contact information shared by marketplace participants and system users.

The `role` assigned to a person determines the responsibilities and permissions associated with that person within the platform (RG-02: each user has a single role).

This class cannot be instantiated directly.

## Attributes

| Attribute      | Type       | Description                                             |
| -------------- | ---------- | --------------------------------------------------------|
| identification | String     | Unique national/legal identification document. Unique.  |
| fullName       | String     | Full name of the person or legal name of the business.  |
| email          | String     | Primary registered email address. Unique.                |
| phoneNumber    | String     | Primary contact phone number.                            |
| address        | String     | Registered contact address.                              |
| role           | SystemRole | Business role that defines responsibilities and permissions. |

## Relationships

* A `Person` may be specialized as a `MarketplaceParticipant`.
* A `Person` may be represented as a `User` for system access.

---

# MarketplaceParticipant (Abstract)

## Description

Represents any person or organization with a commercial relationship with NexusMarket: buying or selling products through the platform.

A `MarketplaceParticipant` never administers information belonging to another participant (RG-03).

This class specializes `Person`. This class cannot be instantiated directly.

## Relationships

* A `MarketplaceParticipant` may be associated with one or more `User` instances for system access.

---

# Buyer

## Description

Represents a person who purchases products published on the platform.

A buyer never administers information of other buyers, sellers, or inventory (business restriction, Domain 2).

## Inherits From

`MarketplaceParticipant`

## Attributes

| Attribute            | Type            | Description                                             |
| --------------------- | --------------- | -------------------------------------------------------- |
| primaryAddress        | String          | Main address used for deliveries. Required.               |
| additionalAddresses   | List\<String\>  | Secondary delivery addresses. Optional, empty by default. |
| status                 | BuyerStatus     | Current commercial standing that determines whether the buyer may place orders. |

## Relationships

* A `Buyer` places zero or more `Order` instances.
* A `Buyer` may file zero or more `ReturnRequest` instances.

---

# Seller

## Description

Represents a business or individual authorized to sell products through NexusMarket. Sellers cannot self-register; they are incorporated by an `Administrator` (Domain 3 business rule).

## Inherits From

`MarketplaceParticipant`

## Attributes

| Attribute        | Type           | Description                                             |
| ------------------ | -------------- | -------------------------------------------------------- |
| businessName        | String         | Commercial/trade name of the seller.                      |
| registrationDate    | LocalDate      | Date the seller was incorporated by an Administrator.     |
| status               | SellerStatus   | Current operational status of the seller.                  |
| warehouses           | List\<Warehouse\> | Warehouses owned by the seller. Not populated by default; loaded on demand. |
| products             | List\<Product\>   | Products published by the seller. Not populated by default; loaded on demand. |

## Relationships

* A `Seller` owns zero or more `Warehouse` instances.
* A `Seller` publishes zero or more `Product` instances.

---

# User

## Description

Represents a system identity used for authentication and authorization. Inherits identity and role information from `Person`.

A `User` may be linked to a `MarketplaceParticipant` when it represents a Buyer or Seller account. Internal roles (`Operador Logístico`, `Administrador`, `Supervisor`) exist without an associated participant, just as bank employees exist without an associated customer.

## Inherits From

`Person`

## Attributes

| Attribute   | Type                    | Description                                                        |
| ------------ | ----------------------- | -------------------------------------------------------------------- |
| userId        | Integer                 | Internal unique identifier of the system user.                       |
| username      | String                  | Login name used during authentication.                                |
| password      | String                  | Secure password hash.                                                  |
| status        | UserStatus              | Current status of the user's system access.                            |
| participant   | MarketplaceParticipant? | Buyer or Seller represented by this user, when applicable.             |

## Relationships

* A `User` may be associated with zero or one `MarketplaceParticipant`.
* A `User` with role `LOGISTICS_OPERATOR` may be assigned to `Shipment` instances.

---

# Warehouse

## Description

Represents a physical location where inventory is administered (Domain 4). Warehouses are classified as belonging to the Marketplace itself or to a specific Seller.

## Attributes

| Attribute   | Type            | Description                                                  |
| ------------ | --------------- | -------------------------------------------------------------- |
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| name          | String          | Descriptive name of the warehouse.                                |
| address       | String          | Physical location of the warehouse.                               |
| type          | WarehouseType   | Classifies the warehouse as MARKETPLACE or SELLER owned.           |
| owner         | Seller?         | Seller who owns the warehouse. Null when type is MARKETPLACE.       |
| status        | WarehouseStatus | Current operational status.                                        |

## Relationships

* A `Warehouse` may belong to one `Seller` (when `type = SELLER`).
* A `Warehouse` holds zero or more `InventoryItem` instances.
* A `Warehouse` may be the origin of zero or more `Shipment` instances.

---

# Product

## Description

Represents a catalog entry offered for sale (Domain 5). Differentiates physical products, which require inventory and dispatch, from digital products, which are delivered immediately after payment.

## Attributes

| Attribute    | Type            | Description                                                  |
| ------------- | --------------- | -------------------------------------------------------------- |
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| seller         | Seller          | Seller who registered and owns the product.                        |
| name           | String          | Commercial name of the product.                                   |
| description    | String          | Detailed description of the product.                               |
| type           | ProductType     | PHYSICAL or DIGITAL.                                               |
| status         | ProductStatus   | PUBLISHED, SUSPENDED, or DISCONTINUED.                              |
| variants       | List\<ProductVariant\> | Variants of the product (color, size, model, etc.). Empty by default for products without variants. |

## Relationships

* A `Product` belongs to one `Seller`.
* A `Product` has zero or more `ProductVariant` instances.

---

# ProductVariant

## Description

Represents a specific sellable version of a `Product`, distinguished by attributes such as color, size or model. Every inventory record and order line item references a `ProductVariant`, not a generic `Product`, so that stock and pricing are always tracked at the exact sellable unit.

## Attributes

| Attribute      | Type                | Description                                             |
| --------------- | ------------------- | ---------------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| product           | Product             | Product this variant belongs to.                            |
| attributes        | Map\<String,String\> | Differentiating attributes, e.g. `{color: red, size: M}`.    |
| price             | BigDecimal          | Sale price of this specific variant.                          |

## Relationships

* A `ProductVariant` belongs to one `Product`.
* A `ProductVariant` may have zero or more `InventoryItem` instances (one per warehouse holding stock).

---

# InventoryItem

## Description

Represents the distributed stock of a `ProductVariant` at a specific `Warehouse` (Domain 6). Inventory is always distributed: it must reference exactly one product variant and one warehouse. Negative stock is never permitted under any circumstance.

## Attributes

| Attribute          | Type               | Description                                                  |
| -------------------- | ------------------ | ---------------------------------------------------------------|
| variant                | ProductVariant     | Product variant this stock record refers to.                     |
| warehouse              | Warehouse          | Warehouse where this stock is physically located.                  |
| quantityAvailable      | Integer            | Units available for sale/reservation. Never negative.               |
| quantityReserved       | Integer            | Units currently reserved by pending orders. Never negative.          |
| status                 | InventoryItemStatus | AVAILABLE or DAMAGED. Damaged stock cannot be reserved.               |

## Relationships

* An `InventoryItem` belongs to one `ProductVariant` and one `Warehouse`.
* An `InventoryItem` generates zero or more `InventoryMovement` instances.

## Business Rule

```text
Stock cannot be reserved when:
  - quantityAvailable is insufficient, or
  - status = DAMAGED
```

---

# InventoryMovement

## Description

Represents a discrete stock movement affecting an `InventoryItem`: inbound receipt, reservation, outbound sale, adjustment, or return (Domain 6).

An inventory movement is an event that occurred; it is distinct from the current stock levels it produced, following the same status-vs-event separation used for `AccountStatus`/`OperationType` in the reference architecture.

## Attributes

| Attribute      | Type                    | Description                                          |
| --------------- | ------------------------ | ------------------------------------------------------|
| movementId        | Integer                  | Unique movement identifier.                              |
| inventoryItem      | InventoryItem            | Inventory record affected by the movement.                |
| movementType       | InventoryMovementType    | Category of the movement.                                  |
| quantity           | Integer                  | Number of units affected by the movement.                   |
| movementDate       | LocalDateTime            | Date and time of the movement.                                |
| performedBy         | User                     | User who executed or triggered the movement.                    |
| relatedOrder        | Order?                   | Order that originated the movement, when applicable (RESERVATION, SALE_OUTBOUND, RETURN). |

## Relationships

* An `InventoryMovement` affects one `InventoryItem`.
* An `InventoryMovement` may reference one `Order`.

---

# Order

## Description

Represents the formal commercial commitment between a buyer and NexusMarket — the central process of the system (Domain 7).

An order begins its life as a working cart: while `orderStatus = CART`, buyers may freely add, update or remove `OrderItem` instances (this covers OBJ-07, "Gestión del carrito de compras", as operations on an order in `CART` state rather than as a separate entity). Once the buyer confirms the order, it advances irreversibly through the remaining lifecycle states.

A finalized order can never be modified (Validation, section 11).

## Attributes

| Attribute      | Type            | Description                                              |
| --------------- | --------------- | ------------------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| buyer              | Buyer           | Buyer who owns the order.                                          |
| items              | List\<OrderItem\> | Product variants and quantities included in the order.                |
| orderStatus        | OrderStatus     | Current lifecycle state of the order.                                 |
| creationDate        | LocalDateTime   | Date and time the order (cart) was created.                            |
| paymentDate          | LocalDateTime?  | Date and time payment was confirmed.                                     |
| dispatchDate          | LocalDateTime?  | Date and time the order left the warehouse.                                |
| deliveryDate           | LocalDateTime?  | Date and time the order was confirmed as delivered.                          |

## Relationships

* An `Order` belongs to one `Buyer`.
* An `Order` contains one or more `OrderItem` instances (while beyond `CART` state).
* An `Order` generates one `Invoice` once paid.
* An `Order` generates one `Shipment` per origin warehouse of its physical items.
* An `Order` may generate zero or more `ReturnRequest` instances.

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

# OrderItem

## Description

Represents a single line item within an order: a specific product variant, quantity, and the price at the time of purchase.

## Attributes

| Attribute    | Type            | Description                                    |
| ------------- | --------------- | -------------------------------------------------|
| order           | Order           | Order this item belongs to.                          |
| variant          | ProductVariant  | Product variant selected.                              |
| quantity          | Integer         | Number of units requested.                                |
| unitPrice          | BigDecimal      | Price of the variant at the moment it was added.            |
| warehouse | Warehouse? | Warehouse the stock was reserved from. Assigned when the order is confirmed; always null for digital products. |

## Relationships

* An `OrderItem` belongs to one `Order`.
* An `OrderItem` references one `ProductVariant`.

---

# Invoice

## Description

Represents the commercial and fiscal record associated with a paid order (Domain 8 / OBJ-09).

## Attributes

| Attribute   | Type           | Description                                     |
| ------------ | -------------- | ---------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| order           | Order          | Order this invoice was generated for.                     |
| issueDate         | LocalDateTime  | Date and time the invoice was issued.                        |
| totalAmount        | BigDecimal     | Total invoiced amount.                                         |
| status              | InvoiceStatus  | Current status of the invoice.                                    |

## Relationships

* An `Invoice` is generated for exactly one `Order`.

---

# Shipment

## Description

Represents the logistics process that moves a physical order from a warehouse to the buyer (Domain "Gestión de envíos" / OBJ-10).

## Attributes

| Attribute     | Type              | Description                                       |
| -------------- | ----------------- | -----------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| order              | Order             | Order being shipped.                                          |
| originWarehouse      | Warehouse         | Warehouse from which the order was dispatched.                   |
| handledBy | User? | Logistics operator assigned when the shipment is dispatched; null while PREPARING. |
| status                | ShipmentStatus    | Current logistics status.                                          |
| dispatchDate            | LocalDateTime?    | Date and time the shipment left the warehouse.                        |
| deliveryDate              | LocalDateTime?    | Date and time the shipment was delivered.                                |

## Relationships

* A `Shipment` corresponds to one `Order`.
* A `Shipment` departs from one `Warehouse`.
* A `Shipment` is handled by one `User` (role `LOGISTICS_OPERATOR`) once dispatched.

---

# ReturnRequest

## Description

Represents a buyer's request to return **one product (order line)** of a finalized order (OBJ-11). The order is never modified; the other lines keep their state.

## Attributes

| Attribute     | Type            | Description                                      |
| -------------- | --------------- | -----------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| order              | Order           | Order the return applies to.                                  |
| buyer               | Buyer           | Buyer requesting the return.                                     |
| variant             | ProductVariant  | Returned product; identifies the order line and, through `variant.product.seller`, the seller. |
| quantity            | Integer         | Returned units (the whole quantity of the order line).          |
| reason               | String          | Reason provided by the buyer.                                       |
| status                | ReturnStatus    | Current status of the return request.                                  |
| requestDate            | LocalDateTime   | Date and time the return was requested.                                   |
| resolutionDate           | LocalDateTime?  | Date and time the return was approved or rejected.                           |

## Relationships

* A `ReturnRequest` refers to one `Order`, one `Buyer` and one `ProductVariant` (the returned line).
* An order can have one active return per product; a rejected return can be requested again.
* A `ReturnRequest` may generate one `Refund`.

---

# Refund

## Description

Represents the monetary reimbursement issued as a consequence of an approved return (OBJ-11).

## Attributes

| Attribute    | Type          | Description                                       |
| ------------- | ------------- | -------------------------------------------------------|
| identifier | String | Unique identifier, inherited from `TraceableEntity`. |
| returnRequest      | ReturnRequest | Return request that originated the refund.                       |
| amount               | BigDecimal    | Amount reimbursed to the buyer.                                     |
| status                 | RefundStatus  | Current status of the refund.                                          |
| processedDate            | LocalDateTime?| Date and time the refund was processed.                                   |

## Relationships

* A `Refund` is generated by exactly one `ReturnRequest`.

---

# Domain Design Rules

## Person, MarketplaceParticipant and User

* `User` inherits from `Person`.
* `role` is defined in `Person` and inherited by `MarketplaceParticipant` and `User` (RG-02: one role per user).
* `User.participant` is an explicit relationship and must not be represented through a generic identifier.
* A `User` may exist without a `MarketplaceParticipant` when representing internal staff (`LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`).
* A `Buyer` may never access or modify data belonging to another `Buyer`, nor inventory data (RG-03, Domain 2).
* Sellers cannot self-register; they are created by a `User` with role `ADMINISTRATOR` (Domain 3).

## Catalog and Inventory

* `Product.type = DIGITAL` implies the product does not require `InventoryItem` records or a `Shipment`; delivery is immediate upon payment.
* `Product.type = PHYSICAL` requires at least one `InventoryItem` per warehouse where it is stocked.
* Every `InventoryItem` must reference exactly one `ProductVariant` and one `Warehouse` — never a bare identifier pair.
* `quantityAvailable` and `quantityReserved` must never become negative under any circumstance.
* Stock marked `DAMAGED` can never be reserved.

## Orders

* `Order.orderStatus = CART` is the only state in which items may be freely added or removed.
* Once `orderStatus` advances beyond `CART`, the order becomes an immutable commercial commitment except for the state transition itself.
* An order in `FINALIZED` status can never be modified (Validation, section 11).
* Order status represents the current state of the commercial commitment; `InventoryMovement` and `Shipment` events represent the actions that produced state changes — mirroring the status-vs-event separation of the reference architecture.

## Value Objects

* Value Objects are immutable and compared by value, not identity.
* Business entities reference Value Objects instead of primitive strings for controlled business concepts (status, type, role).
* Simple enumerations are reserved for fixed technical values without business metadata.

---

# Operation

## Description

Immutable record of a business action executed by a user (traceability required by the specification's introduction and consumed by the administrative reports, OBJ-12).

## Attributes

| Attribute      | Type             | Description                                                              |
| -------------- | ---------------- | ------------------------------------------------------------------------ |
| operationId    | Integer          | Unique operation identifier.                                              |
| operationType  | OperationType    | Category of the action (e.g. ORDER_PAYMENT, INVENTORY_RESERVATION).       |
| executionDate  | LocalDateTime    | Date and time of the action.                                              |
| performedBy    | User             | User who executed the action.                                             |
| affectedEntity | TraceableEntity? | Entity affected. Null for actions on persons (e.g. seller registration). |

---

# AuditLog

## Description

Detailed audit trail generated together with each `Operation` by `RegisterOperationAndAuditService`.

## Attributes

| Attribute      | Type                 | Description                                                  |
| -------------- | -------------------- | ------------------------------------------------------------ |
| auditId        | String               | Unique audit identifier.                                      |
| operationType  | OperationType        | Category of the audited action.                               |
| operationDate  | LocalDateTime        | Date and time of the audited action.                          |
| performedBy    | User                 | User who executed the action.                                 |
| userRole       | SystemRole           | Role the user had when executing the action.                  |
| affectedEntity | TraceableEntity?     | Entity affected by the action.                                |
| details        | Map\<String,Object\> | Action-specific data (previous/new status, quantities, ...).  |

---

# Implementation Notes (domain services)

* **Cart = Order in `CART` status.** `AddItemToCartService`, `UpdateCartItemQuantityService` and `RemoveItemFromCartService` operate on the buyer's order while it is in `CART`.
* **Order flow.** `ConfirmOrderService` (CART → PENDING_PAYMENT, reserves stock of physical items) → `ConfirmOrderPaymentService` (→ PAID, issues the invoice, creates one `PREPARING` shipment per origin warehouse) → `DispatchOrderService` (→ SHIPPED, `SALE_OUTBOUND` movements) → `ConfirmShipmentDeliveryService` → `FinalizeOrderService` (→ FINALIZED).
* **Digital products** need no inventory or shipment; an order containing only digital items is finalized right after payment.
* **Returns and refunds** are separate requests that refer to the finalized order (which is never modified). Refunds cover the whole order.
* **Authorization** follows the responsibility matrix: administrators manage sellers, warehouses, users and refund resolution; sellers manage their own products and inventory; logistics operators manage inventory movements, dispatch, delivery and return reception; buyers manage their own cart, orders, returns and refund requests; supervisors only consult.
