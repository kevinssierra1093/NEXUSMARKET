# User and Authentication Services

## Introduction

This document defines the services belonging to the **User and Authentication Management** subdomain of NexusMarket (OBJ-01, Domain 1 "Administración de Usuarios").

The services in this subdomain create system identities (`User`), authenticate them and manage their access status and password.

The services operate exclusively with **Domain Models** and **Value Objects**. They must not depend on databases, persistence entities, repositories, frameworks, HTTP or REST. When a service requires information not available in the Domain Models it receives, it obtains it through an **Output Port**.

Package: `application.domain.services.user`

---

# Architectural Principles

## Domain Model Parameters

Services receive Domain Models, never primitive identifiers or DTOs.

Incorrect:

```java
changeUserStatus(Integer userId, String status);
```

Correct:

```java
changeUserStatus(User requestingUser, User user, UserStatus newStatus);
```

## Requesting user

Every service that requires authentication (RG-01) receives the authenticated `User` as its **first parameter** (`requestingUser`). The input adapter resolves it from the JWT; the domain never reads tokens or HTTP headers.

## User and Participant

```text
User
  │
  └── participant : MarketplaceParticipant   (Buyer or Seller; null for staff)
```

* A `BUYER` or `SELLER` user is linked to an existing `Buyer` or `Seller`.
* A staff user (`LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`) has no participant.
* RG-02: each user has a single `SystemRole`.

## Platform-wide uniqueness

Section 11 of the specification: identification document and email must be **unique across the whole platform** (buyers, sellers and users). Services check the three repositories.

---

# User and Authentication Services

1. Register Participant User
2. Register Staff User
3. Login
4. Consult User
5. Change User Status
6. Change User Password

---

# 1. Register Participant User

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterParticipantUserService` |
| Method        | `User execute(User user)` |
| Authorization | None (it completes the registration of an already registered buyer or seller) |
| Output Ports  | `UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `PasswordServicePort` |
| Operation     | Not audited |

## Description

Creates the system access (`User`) of an already registered `Buyer` or `Seller`.

## Input

```text
User
 ├── username, password (plain text), identification, fullName, email, ...
 ├── role         : SystemRole  (BUYER or SELLER)
 └── participant  : Buyer | Seller
```

## Domain Validations

1. `participant` is required.
2. The role must match the participant type:
   * `BUYER` ⇒ `participant` is a `Buyer` that exists (`BuyerRepositoryPort.findByIdentification`).
   * `SELLER` ⇒ `participant` is a `Seller` that exists (`SellerRepositoryPort.findByIdentification`).
   * Any other combination is rejected.
3. `username` must not be in use.
4. No other user may have the same `identification` or `email`.

## Processing

```text
password ──► PasswordServicePort.encrypt
status   ──► UserStatus.ACTIVE
User     ──► UserRepositoryPort.save
```

## Output

The persisted `User` with its `userId`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `DomainException` | Missing participant, role/participant mismatch, duplicated username, identification or email. |
| `EntityNotFoundException` | The associated buyer or seller does not exist. |

## Design observation (pending decision)

The service requires no authenticated user, so anyone who knows the identification of an existing **seller** could create that seller's access. Buyers self-register, but sellers are incorporated by the administrator (Domain 3). Recommended adjustment: when `role = SELLER`, require `AuthorizeAdministrationService` (or expose the seller case only through an administrator endpoint).

---

# 2. Register Staff User

| Item          | Value |
| ------------- | ----- |
| Class         | `RegisterStaffUserService` |
| Method        | `User execute(User requestingUser, User user)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `PasswordServicePort` |
| Operation     | `USER_REGISTRATION` (affected entity: none) |

## Description

Creates a system user for internal staff. Staff users exist without an associated participant, just as bank employees exist without an associated customer in the reference project.

## Input

```text
User   (role ∈ {LOGISTICS_OPERATOR, ADMINISTRATOR, SUPERVISOR})
```

## Domain Validations

1. The role must be `LOGISTICS_OPERATOR`, `ADMINISTRATOR` or `SUPERVISOR`.
2. `username` must not be in use.
3. `identification` and `email` must be unique across users, buyers and sellers.

## Processing

```text
participant ──► null
password    ──► PasswordServicePort.encrypt
status      ──► UserStatus.ACTIVE
User        ──► UserRepositoryPort.save
```

## Audit details

`username`, `role`.

## Output

The persisted `User`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requesting user is not an active administrator. |
| `DomainException` | Invalid staff role or duplicated username/identification/email. |

---

# 3. Login

| Item          | Value |
| ------------- | ----- |
| Class         | `LoginService` |
| Method        | `String execute(User user)` |
| Authorization | None (public operation) |
| Output Ports  | `UserRepositoryPort`, `PasswordServicePort`, `JwtServicePort` |
| Operation     | Not audited |

## Description

Authenticates a user with `username` and plain `password` and returns a JWT.

## Input

```text
User
 ├── username
 └── password   (plain text)
```

## Domain Validations

1. The user must exist (`findByUsername`).
2. `PasswordServicePort.matches(raw, stored)` must be `true`.
3. The user status must not be `BLOCKED` or `INACTIVE`.

Both "user not found" and "wrong password" raise the same exception so that the API does not reveal which usernames exist.

## Output

The JWT generated by `JwtServicePort.generateToken(storedUser)`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `InvalidCredentialsException` | Unknown username or wrong password. |
| `DomainException` | User blocked or inactive. |

---

# 4. Consult User

| Item          | Value |
| ------------- | ----- |
| Class         | `ConsultUserService` |
| Methods       | `User execute(User requestingUser, User user)`<br>`List<User> executeAll(User requestingUser)` |
| Authorization | Self, or `AuthorizeAdministrativeConsultationService` (ADMINISTRATOR, SUPERVISOR) |
| Output Ports  | `UserRepositoryPort` |
| Operation     | Not audited (read-only) |

## Description

Retrieves a system user (`execute`, searched by `userId`) or the list of all users (`executeAll`).

## Authorization rules

* `execute`: a user can always consult themselves. Consulting another user requires `ADMINISTRATOR` or `SUPERVISOR`.
* `executeAll`: requires `ADMINISTRATOR` or `SUPERVISOR`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, or consulting another user without an administrative role. |
| `EntityNotFoundException` | The user does not exist. |

## Adapter note

The response must never include the password hash.

---

# 5. Change User Status

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeUserStatusService` |
| Method        | `User execute(User requestingUser, User user, UserStatus newStatus)` |
| Authorization | `AuthorizeAdministrationService` (ADMINISTRATOR) |
| Output Ports  | `UserRepositoryPort` |
| Operation     | `USER_STATUS_CHANGE` (affected entity: none) |

## Description

Changes the access status of a user: `ACTIVE`, `INACTIVE`, `BLOCKED`.

## Domain Validations

1. The user must exist (`findById`).
2. An administrator cannot change the status of their own user.
3. The new status must differ from the current one.

## Audit details

`username`, `previousStatus`, `newStatus`.

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Requester is not an active administrator. |
| `EntityNotFoundException` | The user does not exist. |
| `DomainException` | Administrator changing their own status. |
| `InvalidStatusTransitionException` | New status equals the current status. |

---

# 6. Change User Password

| Item          | Value |
| ------------- | ----- |
| Class         | `ChangeUserPasswordService` |
| Method        | `void execute(User requestingUser, User user, String newPassword)` |
| Authorization | Self or ADMINISTRATOR |
| Output Ports  | `UserRepositoryPort`, `PasswordServicePort` |
| Operation     | Not audited |

## Description

Changes the password of a user.

## Domain Validations

1. The requesting user must be active.
2. The target user must exist.
3. Only the user themselves or an administrator may change the password.
4. The new password must not be empty.

## Processing

```text
newPassword ──► PasswordServicePort.encrypt ──► UserRepositoryPort.update
```

## Exceptions

| Exception | Condition |
| --------- | --------- |
| `UnauthorizedOperationException` | Inactive requester, or a non-admin changing another user's password. |
| `EntityNotFoundException` | The user does not exist. |
| `DomainException` | Empty password. |

---

# Logout

Logout is **not** a domain service. JWTs are stateless: the client discards the token. If server-side revocation is required, it is implemented in `infrastructure/security` (token blacklist) without changing the domain.
