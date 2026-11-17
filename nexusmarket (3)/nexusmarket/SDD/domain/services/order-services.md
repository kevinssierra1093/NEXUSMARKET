# Cart and Order Services

## Introduction

This document defines the services belonging to the **Cart and Order Management** subdomain of NexusMarket (OBJ-07 "Gestionar el carrito de compras", OBJ-08 "Controlar el ciclo completo de los pedidos", Domain 7 "Gestión de Pedidos").

The order is the formal commercial commitment of the platform and its lifecycle is the **central process** of the system.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.order`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
addToCart(String buyerId, String variantId, int quantity);
```

Correct:

```java
addToCart(User requestingUser, ProductVariant variant, Integer quantity);
```

The buyer is resolved from the authenticated user (`requestingUser.participant`), never received as a separate identifier.

## Cart = Order in CART status

There is no separate `Cart` entity. The cart is the buyer's `Order` while `orderStatus = CART`; a buyer has at most one order in `CART`. This is the only state in which lines may be added, changed or removed.

## Order lifecycle

```text
CART ──ConfirmOrder──► PENDING_PAYMENT ──ConfirmOrderPayment──► PAID ──DispatchOrder──► SHIPPED ──(all shipments delivered)──► FINALIZED
                                                                 │
                                                                 └── digital-only order ─────────────────────────────────────► FINALIZED
```

* Every transition is irreversible.
* Once the order leaves `CART` it is an immutable commercial commitment except for the state transition itself.
* A `FINALIZED` order can **never** be modified (Validation, section 11). Returns and refunds are separate requests that refer to it.

## Order lines

```text
Order
 └── items : List<OrderItem>
        ├── variant    : ProductVariant
        ├── quantity   : Integer
        ├── unitPrice  : BigDecimal   (variant price when the line was added)
        └── warehouse  : Warehouse?   (assigned on confirmation; null for digital products)
```

## Internal services

`ResolveBuyerCartService`, `CalculateOrderTotalService` and `FinalizeOrderService` are internal and must not be exposed through controllers.

---

# Cart and Order Services

1. Add Item to Cart
2. Update Cart Item Quantity
3. Remove Item from Cart
4. Consult Cart
5. Confirm Order
6. Confirm Order Payment
7. Dispatch Order
8. Consult Order
9. Resolve Buyer Cart *(internal)*
10. Calculate Order Total *(internal)*
11. Finalize Order *(internal)*

---

# 1. Add Item to Cart

| Item          | Value |
| ------------- | ----- |
| Class         | `AddItemToCartService` |
| Method        | `Order execute(User requestingUser, ProductVariant variant, Integer quantity)` |
| Authorization | Active BUYER with an `ACTIVE` buyer (via `ResolveBuyerCartService`) |
| Output Ports  | `OrderRepositoryPort`, `ProductVariantRepositoryPort` |
| Operation     | `CART_UPDATE` (affected entity: the cart order) |

## Description

Adds a variant to the buyer's cart. The cart is created when it does not exist. If the variant is already in the cart, the quantities are added.

## Domain Validations

1. `quantity > 0`.
2. The variant must exist.
3. The product of the variant must be `PUBLISHED`.

4. **Physical products**: the total quantity of the variant in the cart (existing + added) cannot exceed the available stock (`CheckStockAvailabilityService`: one `AVAILABLE` record in an `ACTIVE` warehouse must cover it — the same rule as the reservation). Digital products have no stock limit.

Stock is **not** reserved here; reservation happens when the order is confirmed. Another buyer may consume the stock in between, so the confirmation validates it again.

## Processing

```text
cart = ResolveBuyerCartService.findOrCreate(requestingUser)
line exists ? line.quantity += quantity
            : new OrderItem(variant, quantity, unitPrice = variant.price)
OrderRepositoryPort.update(cart)
```

## Audit details

`action = ADD_ITEM`, `variant`, `quantity`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active buyer user. |
| `EntityNotFoundException` | Buyer or variant not found. |
| `DomainException` | Invalid quantity, buyer not `ACTIVE`, product not published, insufficient stock (`Insufficient stock for '<product>': requested N unit(s), available M.`). |

---

# 2. Update Cart Item Quantity

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateCartItemQuantityService` |
| Method        | `Order execute(User requestingUser, ProductVariant variant, Integer newQuantity)` |
| Authorization | Active BUYER (via `ResolveBuyerCartService`) |
| Output Ports  | `OrderRepositoryPort` |
| Operation     | `CART_UPDATE` (affected entity: the cart order) |

## Description

Replaces the quantity of a variant already in the cart.

## Domain Validations

1. `newQuantity > 0` (to remove a line, use *Remove Item from Cart*).
2. Physical products: `newQuantity` cannot exceed the available stock (`CheckStockAvailabilityService`).
2. The cart must exist.
3. The variant must be in the cart.

## Audit details

`action = UPDATE_QUANTITY`, `variant`, `previousQuantity`, `newQuantity`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | Invalid quantity or buyer not enabled. |
| `EntityNotFoundException` | Cart or cart item not found. |

---

# 3. Remove Item from Cart

| Item          | Value |
| ------------- | ----- |
| Class         | `RemoveItemFromCartService` |
| Method        | `Order execute(User requestingUser, ProductVariant variant)` |
| Authorization | Active BUYER (via `ResolveBuyerCartService`) |
| Output Ports  | `OrderRepositoryPort` |
| Operation     | `CART_UPDATE` (affected entity: the cart order) |

## Description

Removes a variant from the cart.

## Audit details

`action = REMOVE_ITEM`, `variant`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Cart not found or the variant is not in the cart. |

---

# 4. Consult Cart

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultCartService` |
| Methods       | `Order execute(User requestingUser)`<br>`BigDecimal executeTotal(User requestingUser)` |
| Authorization | Active BUYER (via `ResolveBuyerCartService`) |
| Output Ports  | — (through `ResolveBuyerCartService`) |
| Operation     | Not audited (read-only) |

## Description

Returns the current cart of the buyer, or its total amount.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The buyer has no cart. |

---

# 5. Confirm Order

| Item          | Value |
| ------------- | ----- |
| Class         | `ConfirmOrderService` |
| Method        | `Order execute(User requestingUser, Order order)` |
| Authorization | Owning BUYER (`ValidateBuyerOwnershipService`) |
| Output Ports  | `OrderRepositoryPort` |
| Internal services | `ReserveInventoryService`, `CalculateOrderTotalService` |
| Operation     | `ORDER_CONFIRMATION` (affected entity: the order) |

## Description

Business flow step 5 "Compra": the buyer confirms the cart. `CART → PENDING_PAYMENT`.

## Domain Validations

1. The order must exist and belong to the requesting buyer.
2. The current status must be `CART`.
3. The buyer must be `ACTIVE`.
4. The cart must not be empty.
5. Every product in the cart must still be `PUBLISHED`.

## Processing

```text
for each line:
    if product.type = PHYSICAL:
        item = ReserveInventoryService(variant, quantity, order)
        line.warehouse = item.warehouse
status = PENDING_PAYMENT
OrderRepositoryPort.update(order)
```

## Audit details

`previousStatus`, `newStatus`, `items`, `total`.

## Transaction

**Must** run in a single transaction: if the reservation of any line fails, the reservations already made for previous lines must be rolled back.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order not found. |
| `UnauthorizedOperationException` | Not the owning buyer. |
| `InvalidStatusTransitionException` | Order not in `CART`. |
| `DomainException` | Buyer not enabled, empty cart, product no longer available, insufficient stock. |

---

# 6. Confirm Order Payment

| Item          | Value |
| ------------- | ----- |
| Class         | `ConfirmOrderPaymentService` |
| Method        | `Order execute(User requestingUser, Order order)` |
| Authorization | Owning BUYER |
| Output Ports  | `OrderRepositoryPort` |
| Internal services | `IssueInvoiceService`, `CreateShipmentsService`, `FinalizeOrderService` |
| Operation     | `ORDER_PAYMENT` (affected entity: the order) |

## Description

Business flow step 6 "Transacción": the payment is validated and the preparation flow starts. `PENDING_PAYMENT → PAID`.

Payment gateways are out of scope of the specification; this service records the confirmation of the payment.

## Domain Validations

1. The order must exist and belong to the requesting buyer.
2. The current status must be `PENDING_PAYMENT`.

## Processing

```text
status = PAID, paymentDate = now
OrderRepositoryPort.update(order)
IssueInvoiceService(order)
if the order has PHYSICAL items:
    CreateShipmentsService(order)        (one PREPARING shipment per origin warehouse)
register ORDER_PAYMENT
if the order has only DIGITAL items:
    FinalizeOrderService(order)          (immediate delivery)
```

## Audit details

`previousStatus`, `newStatus`, `hasPhysicalItems`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order not found. |
| `UnauthorizedOperationException` | Not the owning buyer. |
| `InvalidStatusTransitionException` | Order not in `PENDING_PAYMENT`. |
| `DomainException` | The order already has an invoice. |

---

# 7. Dispatch Order

| Item          | Value |
| ------------- | ----- |
| Class         | `DispatchOrderService` |
| Method        | `Order execute(User requestingUser, Order order)` |
| Authorization | LOGISTICS_OPERATOR |
| Output Ports  | `OrderRepositoryPort`, `ShipmentRepositoryPort` |
| Internal services | `RegisterSaleOutboundService` |
| Operation     | `ORDER_DISPATCH` (affected entity: the order) |

## Description

Business flow step 7 "Logística": every prepared shipment leaves its warehouse and the reserved stock is released as sold. `PAID → SHIPPED`.

## Domain Validations

1. The order must exist.
2. The current status must be `PAID`.
3. The order must have at least one shipment in `PREPARING`.

## Processing

```text
for each PREPARING shipment:
    handledBy = requestingUser, status = DISPATCHED, dispatchDate = now
for each line with warehouse:
    RegisterSaleOutboundService(line, order)
status = SHIPPED, dispatchDate = now
```

## Audit details

`previousStatus`, `newStatus`, `shipments`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Not an active logistics operator. |
| `EntityNotFoundException` | Order not found. |
| `InvalidStatusTransitionException` | Order not in `PAID`. |
| `DomainException` | No shipments pending dispatch; reserved stock lower than the quantity to dispatch. |

---

# 8. Consult Order

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultOrderService` |
| Methods       | `Order executeById(User requestingUser, Order order)`<br>`List<Order> executeByBuyer(User requestingUser, Buyer buyer)`<br>`List<Order> executeByStatus(User requestingUser, Order filter)` |
| Authorization | `executeById`: `AuthorizeOrderConsultationService` (owning buyer, involved seller, logistics operator, supervisor)<br>`executeByBuyer`: owning BUYER or SUPERVISOR<br>`executeByStatus`: LOGISTICS_OPERATOR, SUPERVISOR, ADMINISTRATOR |
| Output Ports  | `OrderRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

* `executeById`: one order.
* `executeByBuyer`: the order history of a buyer.
* `executeByStatus`: operational follow-up, e.g. `PAID` orders pending dispatch (`filter.orderStatus`).

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | Order not found. |
| `UnauthorizedOperationException` | Not authorized. |

---

# 9. Resolve Buyer Cart *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `ResolveBuyerCartService` |
| Methods       | `Order findOrCreate(User requestingUser)`<br>`Order find(User requestingUser)` |
| Output Ports  | `OrderRepositoryPort`, `BuyerRepositoryPort` |

## Description

Resolves the buyer of the authenticated user and returns their cart.

## Validations

1. The user must be active and have role `BUYER`.
2. `participant` must be a `Buyer` that exists.
3. The buyer must be `ACTIVE`.

`findOrCreate` creates a new order (`CART`, `creationDate = now`) when none exists; `find` raises `EntityNotFoundException("Cart")`.

---

# 10. Calculate Order Total *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `CalculateOrderTotalService` |
| Method        | `BigDecimal execute(Order order)` |

## Description

`total = Σ (unitPrice × quantity)` over the order lines. Used for the cart total, the invoice, the refund amount and the reports.

---

# 11. Finalize Order *(internal)*

| Item          | Value |
| ------------- | ----- |
| Class         | `FinalizeOrderService` |
| Method        | `Order execute(User requestingUser, Order order)` |
| Invoked by    | `ConfirmShipmentDeliveryService` (all shipments delivered), `ConfirmOrderPaymentService` (digital-only orders) |
| Output Ports  | `OrderRepositoryPort` |
| Operation     | `ORDER_FINALIZATION` (affected entity: the order) |

## Description

Business flow step 8 "Cierre": `PAID | SHIPPED → FINALIZED`, `deliveryDate = now`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `InvalidStatusTransitionException` | Order not in `PAID` or `SHIPPED`. |

---

# Design observation (pending decision)

The specification does not define order cancellation. As a consequence, stock reserved by an order that stays in `PENDING_PAYMENT` is never released. If the course requires it, a `CancelOrderService` (`PENDING_PAYMENT → CANCELLED`, releasing reservations with a new movement type) would need a new `OrderStatus`, `InventoryMovementType` and `OperationType`.
