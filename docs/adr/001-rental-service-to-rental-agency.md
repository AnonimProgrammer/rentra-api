# ADR-001: Rename Rental Service Domain to Rental Agency

## Context

The domain term `rental_service` is ambiguous in business discussions and in code. It can mean:

- a business company that owns/manages vehicles, or
- a capability/process offered by the platform.

This ambiguity started to impact business-layer design, naming consistency, and communication across API, persistence, and application logic.

## Decision

We standardize the business owner concept as `rental_agency` across the data model and related naming.

Database-level changes:

- `rental_services` table is renamed to `rental_agencies`.
- `vehicles.rental_service_id` is renamed to `vehicles.rental_agency_id`.

This decision is implemented via Flyway migration `V4`.

## Consequences

- **Positive:** clearer business language in code and schema; less confusion in service and entity naming.
- **Positive:** easier alignment between domain language, ERD, and future class design.
- **Cost:** requires schema migration and follow-up refactoring in code that references legacy names.
- **Cost:** temporary compatibility risk for any scripts/tools still using old table or column names.

## Follow-Up

- Update project classes/repositories to match `rental_agency` naming.
