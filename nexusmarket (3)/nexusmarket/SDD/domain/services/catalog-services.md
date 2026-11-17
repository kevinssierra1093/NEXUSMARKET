# Catalog Services

## Introduction

This document defines the services belonging to the **Catalog Management** subdomain of NexusMarket (OBJ-05, Domain 5 "Gestión del Catálogo").

The services in this subdomain register products and their variants, control their publication status and expose the public catalog.

The services operate exclusively with **Domain Models** and **Value Objects** and obtain external information through **Output Ports**.

Package: `application.domain.services.catalog`

---

# Architectural Principles

## Domain Model Parameters

Incorrect:

```java
publishProduct(String productId);
```

Correct:

```java
publishProduct(User requestingUser, Product product);
```

## Product and variants

```text
Product (seller, name, description, type, status)
 └── variants : List<ProductVariant>   (attributes: Map<String,String>, price)
```

* Every sellable unit is a `ProductVariant`. Stock and order lines always reference a variant, never a generic product.
* A product without differentiating attributes still has **one** variant (with empty `attributes`) that carries its price.

## Physical and digital products

| ProductType | Inventory | Shipment | Delivery |
| ----------- | --------- | -------- | -------- |
| `PHYSICAL`  | Required  | Required | After dispatch and transport |
| `DIGITAL`   | Not used  | Not used | Immediately after payment |

## Product lifecycle

```text
RegisterProduct
      │
      ▼
 SUSPENDED ◄────────────┐
      │                 │
      │ Publish         │ Suspend
      ▼                 │
 PUBLISHED ─────────────┘
      │
      │ Discontinue (from PUBLISHED or SUSPENDED)
      ▼
 DISCONTINUED   (terminal)
```

A newly registered product starts **SUSPENDED** (not visible) and is published once its initial stock exists — business flow steps 2 (Catalog), 3 (Inventory) and 4 (Publication).

## Responsibility matrix

Product registration and maintenance belongs exclusively to the **owning seller** (`AuthorizeProductOperationService`).

---

# Catalog Services

1. Register Product
2. Add Product Variant
3. Update Product
4. Publish Product
5. Suspend Product
6. Discontinue Product
7. Consult Product
8. Consult Published Catalog

---

# 1. Register Product

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort`, `ProductVariantRepositoryPort`, `SellerRepositoryPort` |
| Operation     | `PRODUCT_REGISTRATION` (affected entity: the product) |

## Description

Registers a product together with its variants.

## Input

```text
Product
 ├── seller        (optional: resolved from requestingUser.participant when null)
 ├── name          (required)
 ├── description
 ├── type          (required: PHYSICAL | DIGITAL)
 └── variants      (at least one; each with price > 0)
```

## Domain Validations

1. If `product.seller` is null, it is taken from the requesting user's participant; if it is still null, the operation is rejected.
2. The requesting user must be the owning seller.
3. The seller must exist and be `ACTIVE`.
4. `name` is required; `type` is required.
5. At least one variant; every variant price must be greater than zero.

## Processing

```text
status = SUSPENDED
Product ──► ProductRepositoryPort.save                (without variants)
for each variant:
    variant.product = saved product
    ProductVariantRepositoryPort.save ──► saved.variants
```

## Audit details

`name`, `type`, `variants` (count).

## Output

The persisted `Product` with its persisted variants.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not a seller, inactive, or not the owner. |
| `EntityNotFoundException` | The seller does not exist. |
| `DomainException` | Inactive seller, missing name/type, no variants, invalid price. |

---

# 2. Add Product Variant

| Item          | Value |
| ------------- | ----- |
| Class         | `AddProductVariantService` |
| Method        | `ProductVariant execute(User requestingUser, Product product, ProductVariant variant)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort`, `ProductVariantRepositoryPort` |
| Operation     | `PRODUCT_UPDATE` (affected entity: the product) |

## Description

Adds a new variant to an existing product.

## Domain Validations

1. The product must exist and belong to the requesting seller.
2. The product must not be `DISCONTINUED`.
3. The variant price must be greater than zero.

## Audit details

`variantIdentifier`, `attributes`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The product does not exist. |
| `UnauthorizedOperationException` | Not the owning seller. |
| `DomainException` | Discontinued product or invalid price. |

---

# 3. Update Product

| Item          | Value |
| ------------- | ----- |
| Class         | `UpdateProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort` |
| Operation     | `PRODUCT_UPDATE` (affected entity: the product) |

## Description

Updates `name` and `description`. The `type`, `status`, `seller` and variants are not modified by this service.

## Domain Validations

1. The product must exist and belong to the requesting seller.
2. The product must not be `DISCONTINUED`.
3. `name` must not be empty.

## Audit details

`previousName`, `previousDescription`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The product does not exist. |
| `UnauthorizedOperationException` | Not the owning seller. |
| `DomainException` | Discontinued product or empty name. |

---

# 4. Publish Product

| Item          | Value |
| ------------- | ----- |
| Class         | `PublishProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort`, `ProductVariantRepositoryPort`, `InventoryItemRepositoryPort` |
| Operation     | `PRODUCT_PUBLICATION` (affected entity: the product) |

## Description

Makes a product visible in the public catalog: `SUSPENDED → PUBLISHED`.

## Domain Validations

1. The product must exist and belong to the requesting seller.
2. The current status must be `SUSPENDED`.
3. The seller must be `ACTIVE`.
4. The product must have at least one variant.
5. If `type = PHYSICAL`, at least one variant must have an `AVAILABLE` inventory record with `quantityAvailable > 0`.

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The product does not exist. |
| `UnauthorizedOperationException` | Not the owning seller. |
| `InvalidStatusTransitionException` | The product is not `SUSPENDED`. |
| `DomainException` | Inactive seller, no variants, no available stock for a physical product. |

---

# 5. Suspend Product

| Item          | Value |
| ------------- | ----- |
| Class         | `SuspendProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort` |
| Operation     | `PRODUCT_SUSPENSION` (affected entity: the product) |

## Description

Temporarily removes a product from the public catalog: `PUBLISHED → SUSPENDED`. Suspended products cannot be added to carts nor confirmed in pending carts.

## Domain Validations

1. The product must exist and belong to the requesting seller.
2. The current status must be `PUBLISHED`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The product does not exist. |
| `UnauthorizedOperationException` | Not the owning seller. |
| `InvalidStatusTransitionException` | The product is not `PUBLISHED`. |

---

# 6. Discontinue Product

| Item          | Value |
| ------------- | ----- |
| Class         | `DiscontinueProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | `AuthorizeProductOperationService` (owning SELLER) |
| Output Ports  | `ProductRepositoryPort` |
| Operation     | `PRODUCT_DISCONTINUATION` (affected entity: the product) |

## Description

Permanently discontinues a product. `DISCONTINUED` is terminal: the product can never be reactivated, updated or receive new variants.

## Domain Validations

1. The product must exist and belong to the requesting seller.
2. The product must not already be `DISCONTINUED`.

## Audit details

`previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `EntityNotFoundException` | The product does not exist. |
| `UnauthorizedOperationException` | Not the owning seller. |
| `InvalidStatusTransitionException` | Already discontinued. |

---

# 7. Consult Product

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultProductService` |
| Method        | `Product execute(User requestingUser, Product product)` |
| Authorization | Any active user for `PUBLISHED` products; otherwise the owning SELLER, ADMINISTRATOR or SUPERVISOR |
| Output Ports  | `ProductRepositoryPort`, `ProductVariantRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves a product by `identifier` and populates its `variants`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, or non-published product consulted by a user who is not the owner or an administrative role. |
| `EntityNotFoundException` | The product does not exist. |

---

# 8. Consult Published Catalog

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultPublishedCatalogService` |
| Methods       | `List<Product> execute(User requestingUser)`<br>`List<Product> executeBySeller(User requestingUser, Seller seller)` |
| Authorization | Any active user |
| Output Ports  | `ProductRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

* `execute`: the public catalog — products in `PUBLISHED` status.
* `executeBySeller`: products of one seller. The owning seller, administrators and supervisors see every status; any other user sees only published products.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Unauthenticated or inactive requester (RG-01). |
