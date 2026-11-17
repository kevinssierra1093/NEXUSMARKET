# Authorization Services

## Introduction

This document defines the services belonging to the **Authorization** subdomain of NexusMarket.

These services implement the general restrictions of the specification and the responsibility matrix:

| Code  | Restriction |
| ----- | ----------- |
| RG-01 | Every operation must be executed by an authenticated user. |
| RG-02 | Each user has a single role within the system. |
| RG-03 | No participant may administer information outside their role. |

Authorization services work **exclusively with the Domain Models received** (`User`, `Buyer`, `Seller`, `Product`, `ProductVariant`, `Order`); they use no Output Ports.

Package: `application.domain.services.authorization`

---

# Architectural Principles

## Authentication vs. authorization

* **Authentication** (validating the JWT and resolving the `User`) belongs to `infrastructure/security`.
* **Authorization** (what that user may do) belongs to the domain and is implemented by these services.

The input adapter resolves the authenticated `User` (with `role`, `status` and `participant`) and passes it as `requestingUser`; the domain never reads tokens.

## Roles

| SystemRole           | Participant | Description |
| -------------------- | ----------- | ----------- |
| `BUYER`              | `Buyer`     | Purchases published products. |
| `SELLER`             | `Seller`    | Registers and administers their products and inventory. |
| `LOGISTICS_OPERATOR` | —           | Physical operation of warehouses and dispatches. |
| `ADMINISTRATOR`      | —           | Administration of sellers, warehouses, users, returns and refunds. |
| `SUPERVISOR`         | —           | Consultation and operational follow-up. |

## Exceptions

Every authorization failure raises `UnauthorizedOperationException` (HTTP 403 in the input adapter).

---

# Responsibility Matrix (implemented)

| Process                    | Buyer | Seller | Logistics Op. | Admin | Supervisor |
| -------------------------- | :---: | :----: | :-----------: | :---: | :--------: |
| Register buyer             | self-registration | | | | |
| Register / maintain sellers |      |        |               | ✔     |            |
| Register / maintain warehouses |   |        |               | ✔     |            |
| Register staff users        |      |        |               | ✔     |            |
| Register / maintain products |     | ✔ (own) |              |       |            |
| Administer inventory        |      | ✔ (own) | ✔            |       |            |
| Consult inventory           |      | ✔ (own) | ✔            | ✔ (by warehouse) | ✔ |
| Cart and order confirmation/payment | ✔ (own) |  |          |       |            |
| Dispatch / shipments        |      |        | ✔             |       |            |
| Consult orders              | ✔ (own) | ✔ (involved) | ✔    |       | ✔          |
| Request returns / refunds   | ✔ (own) |     |               | ✔ (refunds) |      |
| Resolve returns / refunds   |      |        |               | ✔     |            |
| Receive returned goods      |      |        | ✔             |       |            |
| Reports, operations, audit  |      |        |               | ✔     | ✔          |

---

# Authorization Services

1. Validate User Authorization Status
2. Validate Role Authorization
3. Validate Buyer Ownership
4. Validate Seller Ownership
5. Authorize Administration
6. Authorize Administrative Consultation
7. Authorize Product Operation
8. Authorize Inventory Operation
9. Authorize Inventory Consultation
10. Authorize Order Operation
11. Authorize Order Consultation
12. Authorize Refund Operation

---

# 1. Validate User Authorization Status

| Item   | Value |
| ------ | ----- |
| Class  | `ValidateUserAuthorizationStatusService` |
| Method | `void execute(User user)` |

RG-01. Fails when `user` is `null` (unauthenticated) or `user.status ≠ ACTIVE`.

---

# 2. Validate Role Authorization

| Item   | Value |
| ------ | ----- |
| Class  | `ValidateRoleAuthorizationService` |
| Method | `void execute(User user, SystemRole... allowedRoles)` |

RG-02 / RG-03. Fails when `user.role` is not one of `allowedRoles`. The message lists the required roles.

---

# 3. Validate Buyer Ownership

| Item   | Value |
| ------ | ----- |
| Class  | `ValidateBuyerOwnershipService` |
| Method | `void execute(User user, Buyer buyer)` |

Succeeds only when `user.role = BUYER`, `user.participant` is a `Buyer` and its `identification` equals `buyer.identification`.

---

# 4. Validate Seller Ownership

| Item   | Value |
| ------ | ----- |
| Class  | `ValidateSellerOwnershipService` |
| Method | `void execute(User user, Seller seller)` |

Succeeds only when `user.role = SELLER`, `user.participant` is a `Seller` and its `identification` equals `seller.identification`.

---

# 5. Authorize Administration

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeAdministrationService` |
| Method | `void execute(User user)` |

Active user with role `ADMINISTRATOR`.

---

# 6. Authorize Administrative Consultation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeAdministrativeConsultationService` |
| Method | `void execute(User user)` |

Active user with role `ADMINISTRATOR` or `SUPERVISOR`.

---

# 7. Authorize Product Operation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeProductOperationService` |
| Method | `void execute(User user, Product product)` |

Active `SELLER` who owns `product.seller`.

---

# 8. Authorize Inventory Operation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeInventoryOperationService` |
| Method | `void execute(User user, ProductVariant variant)` |

Active `SELLER` or `LOGISTICS_OPERATOR`. A seller must own `variant.product.seller`.

---

# 9. Authorize Inventory Consultation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeInventoryConsultationService` |
| Method | `void execute(User user, ProductVariant variant)` |

Active `SELLER`, `LOGISTICS_OPERATOR` or `SUPERVISOR`. A seller must own `variant.product.seller`.

---

# 10. Authorize Order Operation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeOrderOperationService` |
| Method | `void execute(User user, Order order)` |

Active `BUYER`, `SELLER` or `LOGISTICS_OPERATOR`:

* a buyer must own `order.buyer`;
* a seller must have at least one product among the order lines.

---

# 11. Authorize Order Consultation

| Item   | Value |
| ------ | ----- |
| Class  | `AuthorizeOrderConsultationService` |
| Method | `void execute(User user, Order order)` |

Active `SUPERVISOR` (always allowed), otherwise the rules of *Authorize Order Operation*.

---

# 12. Authorize Refund Operation

| Item    | Value |
| ------- | ----- |
| Class   | `AuthorizeRefundOperationService` |
| Methods | `void execute(User user, Buyer buyer)`<br>`void executeConsultation(User user, Buyer buyer)` |

* `execute`: active `BUYER` (owner of `buyer`) or `ADMINISTRATOR`.
* `executeConsultation`: additionally allows `SUPERVISOR`.

---

# Ownership requires the participant

Ownership validations compare `user.participant.identification`. Therefore the input adapter (security layer) must load the `User` **with its participant** when resolving the authenticated user from the JWT.
