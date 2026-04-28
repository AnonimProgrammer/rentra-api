# System Flow

This document extends the basic rental journey with role-based behavior and operational states for a more realistic production flow.

It focuses on three user groups:

- Customer
- Rental Agency User
- System User (Admin panel)

No endpoint design is included in this document.

## Core Business Rules

- A customer can have at most one active rent at a time.
- If a customer has an active rent, the system must block new reservation requests.
- A reservation request becomes an active rent only after agency confirmation during handover.
- When rent is completed, pricing is calculated first; payment operation proceeds after that.
- After completion, vehicle does not become immediately available. It must pass a technical readiness stage before returning to `AVAILABLE`.
- Rent rating is optional (`0` to `5`) and can be skipped.

## High-Level Role Map

```mermaid
flowchart LR
    C[Customer App User]
    A[Rental Agency User]
    S[System User / Admin]

    C --> C1[Discover agencies and vehicles]
    C --> C2[Request reservation and complete rent]
    C --> C3[Optional rating]

    A --> A1[Manage reservations and rents]
    A --> A2[Manage vehicles by role]
    A --> A3[View vehicle rent history]

    S --> S1[Admin panel operations]
    S --> S2[System-wide governance]
```

## 1) Customer Flow

Customer can reach vehicle selection through two entry points:

1. Map-first: customer opens map, sees nearest rental agencies, selects agency, sees best/nearby/price/rating-oriented vehicles.
2. Attribute-first: customer searches vehicles by filters (category, brand, model, etc.).

Both paths converge into the same vehicle discovery flow. In map-first flow, `rentalAgencyId` is simply pre-filled as a search attribute.

```mermaid
sequenceDiagram
    actor Customer
    participant App as Customer App
    participant Discovery as Discovery/Search
    participant Agency as Rental Agency
    participant Rent as Rent Management
    participant Pricing as Price Engine
    participant Payment as Payment Operation

    Customer->>App: Open app and view map
    App->>Discovery: Load nearest agencies in area
    Discovery-->>App: Agencies with geo context

    alt Map-first browsing
        Customer->>App: Select rental agency
        App->>Discovery: Search vehicles with rentalAgencyId + sort criteria
        Discovery-->>App: Vehicle list
    else Attribute-first search
        Customer->>App: Search by category/brand/model/etc.
        App->>Discovery: Search vehicles with filters
        Discovery-->>App: Vehicle list
    end

    Customer->>App: Select vehicle and confirm reservation request
    App->>Rent: Validate active-rent constraint

    alt Customer already has active rent
        Rent-->>App: Reject reservation request
        App-->>Customer: Show cannot reserve until completion
    else No active rent
        App->>Rent: Create reservation request (PENDING)
        Rent-->>App: Reservation request created
        App-->>Customer: Reservation submitted, contact agency

        Note over Customer,Agency: Customer visits agency for handover
        Agency->>Rent: Confirm reservation and start rent
        Rent-->>Agency: Rent ACTIVE

        Note over Customer,App: Customer uses vehicle
        Customer->>App: Complete rent
        App->>Rent: Mark completion
        Rent->>Pricing: Calculate estimated totalAmount (time + rate rules)
        Pricing-->>Rent: totalAmount
        Rent->>Payment: Proceed with payment operation
        Payment-->>Rent: Payment flow result

        Rent->>Agency: Set vehicle to TECHNICAL_READINESS
        Agency->>Agency: Ensure car tech readiness checks
        Agency->>Rent: Mark vehicle AVAILABLE

        opt Optional rating (0..5)
            Customer->>App: Submit rating or skip
            App->>Rent: Save rating if provided
        end
    end
```

### Customer States (Conceptual)

- Reservation: `PENDING` -> `CONFIRMED` (agency confirms) -> closed by conversion to rent.
- Rent: `ACTIVE` -> `COMPLETED` -> payment operation.
- Vehicle after completion: `TECHNICAL_READINESS` -> `AVAILABLE`.

## 2) Rental Agency User Flow

After authentication, this user type can:

- Join an existing rental agency, or
- Create a new rental agency (separate compliance/document flow).

Once associated with an agency, permissions depend on role:

- `MANAGER`: full agency operations.
- `FRONT_AGENT`: reservation and rent operations, but no sensitive fleet management (for example add/remove vehicles).

By default:

- Agency creator/owner is `MANAGER`.
- Newly joined users are `FRONT_AGENT` unless promoted.

```mermaid
flowchart TD
    U[Authenticated User]
    J{Join existing agency<br/>or create new agency}
    C[Create agency flow<br/>documents/compliance]
    R[Join agency membership]
    P[Assigned role]
    M[MANAGER]
    F[FRONT_AGENT]

    U --> J
    J -->|Create| C
    J -->|Join| R
    C --> P
    R --> P
    P -->|Owner default| M
    P -->|Joined user default| F

    M --> M1[Manage vehicles: add/update/remove]
    M --> M2[Manage reservations and rents]
    M --> M3[Access vehicle rent history]
    M --> M4[Manage agency users and roles]

    F --> F1[Process reservations]
    F --> F2[Confirm handover/start rent]
    F --> F3[Complete return workflow]
    F --> F4[Access vehicle rent history]
    F -. restricted .-> F5[No add/remove vehicle permissions]
```

## 3) System User Flow

System users operate from the admin panel and own all admin endpoints.

```mermaid
flowchart LR
    Admin[System User / Admin Panel]
    Auth[Admin Authentication + Authorization]
    Ops[Admin Operations]
    Gov[System Governance]

    Admin --> Auth --> Ops
    Ops --> Gov

    Ops --> O1[Manage platform entities]
    Ops --> O2[Investigate issues]
    Ops --> O3[Moderation and support actions]

    Gov --> G1[Policy enforcement]
    Gov --> G2[Operational oversight]
    Gov --> G3[Audit visibility]
```

## Cross-Flow Notes

- Reservation and rent lifecycle ownership is shared:
  - Customer initiates reservation request.
  - Agency confirms and transitions into active rent.
- Pricing and payment are decoupled at flow level:
  - Pricing computes `totalAmount`.
  - Payment operation proceeds as a follow-up stage.
- Vehicle availability is operationally safe:
  - Completion alone is not enough to return the vehicle to inventory.
  - Technical readiness gate prevents immediate re-listing.
