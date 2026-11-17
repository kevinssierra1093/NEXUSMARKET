# Services

## Introduction

This document provides a conceptual overview of the services that compose **NexusMarket**, the centralized marketplace platform described in the *Especificación Funcional del Negocio*.

The services described here define the main business capabilities exposed by the system. At this level, each service is described only in terms of its purpose and responsibility within the domain.

The detailed definition of each service — inputs, outputs, business rules, validations, authorization requirements, domain interactions, exceptions and persistence considerations — is documented in separate files organized by **subdomain** (see [Service Organization](#service-organization)).

Every service is implemented as **one use case per class** under `application.domain.services.<subdomain>`, exposes an `execute(...)` method (plus `executeXxx(...)` variants for consultations) and receives **Domain Models or Value Objects** as parameters, never primitive identifiers or DTOs.

The service documentation is divided into the following subdomains:

| Subdomain                         | Package                   | Specification objective |
| --------------------------------- | ------------------------- | ----------------------- |
| User and Authentication Management | `services.user`           | OBJ-01                  |
| Buyer Management                  | `services.buyer`          | OBJ-03                  |
| Seller Management                 | `services.seller`         | OBJ-02                  |
| Warehouse Management              | `services.warehouse`      | OBJ-04                  |
| Catalog Management                | `services.catalog`        | OBJ-05                  |
| Inventory Management              | `services.inventory`      | OBJ-06                  |
| Cart and Order Management         | `services.order`          | OBJ-07, OBJ-08          |
| Invoice Management                | `services.invoice`        | OBJ-09                  |
| Shipment Management               | `services.shipment`       | OBJ-10                  |
| Return Management                 | `services.returns`        | OBJ-11                  |
| Refund Management                 | `services.refund`         | OBJ-11                  |
| Administrative Reports            | `services.report`         | OBJ-12                  |
| Operation and Audit Management    | `services.operation`      | Traceability            |
| Authorization                     | `services.authorization`  | RG-01, RG-02, RG-03     |

### Public and internal services

* **Public services** are use cases invoked by the input adapters (REST controllers).
* **Internal services** are domain services invoked only by other domain services as part of a larger use case (for example, `ReserveInventoryService` is invoked by `ConfirmOrderService`). They assume the caller has already performed the authorization checks and must **not** be exposed through a controller.

---

# User and Authentication Management Services

## Register Participant User

Creates the system access (`User`) of an already registered `Buyer` or `Seller`. The user role must be `BUYER` or `SELLER` and must match the associated participant.

## Register Staff User

Creates a system user for internal staff (`LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`). Restricted to the `ADMINISTRATOR` role. Staff users have no associated participant.

## Login

Authenticates a system user with their credentials and returns a JWT that identifies the user in subsequent operations.

## Consult User

Retrieves a system user. A user may always consult themselves; administrative roles may consult any user or the full list.

## Change User Status

Changes the access status of a user (`ACTIVE`, `INACTIVE`, `BLOCKED`). Restricted to administrators; an administrator cannot change their own status.

## Change User Password

Changes the password of a user. Allowed for the user themselves or for an administrator.

---

# Buyer Management Services

## Register Buyer

Self-registration of a new buyer. This is the only operation that does not require an authenticated user (the buyer has no access to the platform yet).

## Consult Buyer

Retrieves a buyer. A buyer can only consult their own data; administrators and supervisors can consult any buyer or the full list.

## Update Buyer

Updates the contact data and delivery addresses of a buyer. Only the buyer who owns the data can update it.

## Change Buyer Status

Changes the commercial status of a buyer (`ACTIVE`, `RESTRICTED`, `BLOCKED`). Restricted to administrators.

---

# Seller Management Services

## Register Seller

Incorporates a seller together with the seller's first warehouse (business flow step 1, *Incorporación*). Sellers cannot self-register; only an administrator can execute this service.

## Consult Seller

Retrieves a seller with their warehouses and products. A seller can only consult themselves; administrators and supervisors can consult any seller or the full list.

## Update Seller

Updates the contact data and business name of a seller. Only the owning seller can update it.

## Change Seller Status

Changes the operational status of a seller (`ACTIVE`, `SUSPENDED`, `INACTIVE`). Restricted to administrators.

---

# Warehouse Management Services

## Register Warehouse

Registers a new marketplace or seller warehouse. Restricted to administrators.

## Consult Warehouse

Retrieves one warehouse, the warehouses of a seller, or all warehouses, according to the role of the requesting user.

## Update Warehouse

Updates the name and address of a warehouse. Restricted to administrators.

## Change Warehouse Status

Activates or deactivates a warehouse. Restricted to administrators.

---

# Catalog Management Services

## Register Product

Registers a product with at least one variant. The product starts `SUSPENDED` (not visible) and is published once its initial stock exists (business flow steps 2 to 4).

## Add Product Variant

Adds a new sellable variant (color, size, model…) with its price to an existing product.

## Update Product

Updates the commercial name and description of a product.

## Publish Product

Makes a suspended product visible in the public catalog (`SUSPENDED → PUBLISHED`). Physical products require available stock.

## Suspend Product

Temporarily removes a published product from the public catalog (`PUBLISHED → SUSPENDED`).

## Discontinue Product

Permanently discontinues a product. `DISCONTINUED` is a terminal status.

## Consult Product

Retrieves a product with its variants. Non-published products are only visible to their owner and to administrative roles.

## Consult Published Catalog

Retrieves the public catalog (published products) or the products of a specific seller.

---

# Inventory Management Services

## Register Inventory Inbound

Registers units entering a warehouse (initial stock or restock). Creates the inventory record when it does not exist.

## Adjust Inventory

Manually corrects the available quantity of an inventory record. Negative stock is never permitted.

## Change Inventory Item Status

Marks an inventory record as `DAMAGED` (unsellable) or back to `AVAILABLE`.

## Consult Inventory

Retrieves the inventory records of a product variant or of a warehouse.

## Consult Inventory Movements

Retrieves the movement history of an inventory record.

## Reserve Inventory *(internal)*

Reserves available stock for an order being confirmed. Non-existent or damaged stock can never be reserved.

## Register Sale Outbound *(internal)*

Releases reserved stock when an order is dispatched (`SALE_OUTBOUND`).

## Register Inventory Return *(internal)*

Re-enters returned units in the origin warehouse when a return is received.

## Record Inventory Movement *(internal)*

Persists the `InventoryMovement` event that produced a change in an inventory record.

---

# Cart and Order Management Services

## Add Item to Cart

Adds a product variant to the buyer's cart (the buyer's order in `CART` status), creating the cart when it does not exist.

## Update Cart Item Quantity

Changes the quantity of a variant already in the cart.

## Remove Item from Cart

Removes a variant from the cart.

## Consult Cart

Retrieves the buyer's current cart and its total.

## Confirm Order

Confirms the cart (`CART → PENDING_PAYMENT`) and reserves stock for its physical items.

## Confirm Order Payment

Confirms the payment (`PENDING_PAYMENT → PAID`), issues the invoice and creates the shipments. Digital-only orders are finalized immediately.

## Dispatch Order

A logistics operator dispatches every prepared shipment of the order (`PAID → SHIPPED`) and the reserved stock leaves the warehouses.

## Consult Order

Retrieves an order, the orders of a buyer, or the orders in a given status.

## Resolve Buyer Cart *(internal)*

Resolves the authenticated buyer and returns (or creates) their cart.

## Calculate Order Total *(internal)*

Calculates the total amount of an order from its lines.

## Finalize Order *(internal)*

Closes the order (`→ FINALIZED`) once every shipment is delivered or, for digital-only orders, right after payment. A finalized order can never be modified.

---

# Invoice Management Services

## Consult Invoice

Retrieves the invoice of an order.

## Issue Invoice *(internal)*

Issues the invoice of an order when its payment is confirmed.

## Annul Invoice *(internal)*

Annuls the invoice of an order when the refund of the order is processed.

---

# Shipment Management Services

## Update Shipment Status

Tracks transport: `DISPATCHED → IN_TRANSIT`, `IN_TRANSIT → FAILED`, `FAILED → IN_TRANSIT`.

## Confirm Shipment Delivery

Confirms the delivery of a shipment. When every shipment of the order is delivered, the order is finalized (business flow step 8, *Cierre*).

## Consult Shipment

Retrieves the shipments of an order.

## Create Shipments *(internal)*

Creates one `PREPARING` shipment per origin warehouse when an order is paid.

---

# Return Management Services

## Request Return

The buyer requests the return of one product (order line) of a finalized order.

## Approve Return

An administrator approves a requested return.

## Reject Return

An administrator rejects a requested return.

## Confirm Return Reception

A logistics operator receives the returned product; only its units re-enter the origin warehouse.

## Consult Return

Retrieves a return request, the returns of a buyer, or the returns in a given status.

---

# Refund Management Services

## Request Refund

The buyer requests the refund of a received return (or of an approved return for a digital product). The amount is the subtotal of the returned product only.

## Approve Refund

An administrator approves a requested refund.

## Reject Refund

An administrator rejects a requested refund.

## Process Refund

An administrator processes an approved refund: the return is completed and the invoice annulled.

## Consult Refund

Retrieves the refund of a return request, or all refunds.

---

# Administrative Reports Services

## Consult Orders Report

Consolidates orders by status and total sales.

## Consult Inventory Report

Consolidates available, reserved and damaged stock, and available units per warehouse.

## Consult Returns Report

Consolidates returns and refunds by status and total refunded amount.

## Consult Seller Report

Report of one seller (or of every seller for administrative roles): products, sales, available inventory, stock movements caused by purchases, dispatches, returns and adjustments, returns and refunds, and net sales. Allowed for the owning seller, administrators and supervisors.

---

# Operation and Audit Management Services

## Register Operation and Audit

Registers, in a single step, the `Operation` and the detailed `AuditLog` of every significant business action. It is the entry point used by every other service.

## Register Operation *(internal)*

Persists an `Operation`.

## Register Audit Log *(internal)*

Persists an `AuditLog`.

## Consult Operations

Retrieves operations by user, affected entity or operation type.

## Consult Audit Logs

Retrieves audit records by user, affected entity or operation type.

---

# Authorization Services

## Validate User Authorization Status

RG-01: every operation must be executed by an authenticated, active user.

## Validate Role Authorization

RG-02 / RG-03: the user must have one of the roles allowed for the operation.

## Validate Buyer Ownership

A buyer never administers information belonging to another buyer.

## Validate Seller Ownership

A seller only administers their own products, warehouses and inventory.

## Authorize Administration

Administrator-only operations: sellers, warehouses, users, buyer status, returns and refunds resolution.

## Authorize Administrative Consultation

Administrative consultation (reports, operations, audit logs, global lists): administrators and supervisors.

## Authorize Product Operation

Product registration and maintenance: only the owning seller.

## Authorize Inventory Operation

Inventory administration: the owning seller or a logistics operator.

## Authorize Inventory Consultation

Inventory consultation: the owning seller, logistics operators and supervisors.

## Authorize Order Operation

Order management: the owning buyer, a seller with products in the order, or a logistics operator.

## Authorize Order Consultation

Order consultation: the same actors as order management, plus supervisors.

## Authorize Refund Operation

Returns and refunds: the owning buyer or an administrator; supervisors may consult.

---

# Service Organization

The services described in this document provide the **high-level service catalog** of the system. They intentionally do not describe implementation details or complete business workflows.

Detailed specifications are maintained in separate Markdown files organized by subdomain:

```text
services/
├── user-authentication-services.md
├── buyer-services.md
├── seller-services.md
├── warehouse-services.md
├── catalog-services.md
├── inventory-services.md
├── order-services.md
├── invoice-services.md
├── shipment-services.md
├── return-services.md
├── refund-services.md
├── report-services.md
├── operation-audit-services.md
└── authorization-services.md
```

---

# Main Business Flow

The services cooperate to implement the general business flow of the specification (section 6.1):

```text
1. Incorporation    RegisterSellerService            (seller + first warehouse)
2. Catalog          RegisterProductService           (product SUSPENDED + variants)
3. Inventory        RegisterInventoryInboundService  (initial stock)
4. Publication      PublishProductService            (SUSPENDED -> PUBLISHED)
5. Purchase         AddItemToCartService ... ConfirmOrderService
                                                     (CART -> PENDING_PAYMENT, ReserveInventoryService)
6. Transaction      ConfirmOrderPaymentService       (-> PAID, IssueInvoiceService, CreateShipmentsService)
7. Logistics        DispatchOrderService             (-> SHIPPED, RegisterSaleOutboundService)
                    UpdateShipmentStatusService      (IN_TRANSIT / FAILED)
8. Closure          ConfirmShipmentDeliveryService   (-> DELIVERED, FinalizeOrderService -> FINALIZED)

Post-sale           RequestReturnService -> ApproveReturnService -> ConfirmReturnReceptionService
                    RequestRefundService -> ApproveRefundService -> ProcessRefundService (AnnulInvoiceService)
```
