# ADR-002: Reservations Table and Two-Step Rental Activation

## Context

The previous rental flow had a domain gap caused by loose coupling between customer intent and vehicle allocation.

We did not persist which customer reserved which vehicle before rental activation. Because of this, the system lacked a reliable reservation link and could not safely model the handoff from "requested" to "active rental" in a traceable way.

This started to impact correctness of reservation-related logic and made business flow validation harder.

## Decision

We introduce an explicit `reservations` table as the source of truth for customer-to-vehicle reservation mapping.

A reservation record links customer and vehicle before activation, and rental activation is handled as a second step after reservation creation/validation.

Database-level changes:

- Add `reservations` table to persist reservation ownership and state.
- Make rental activation depend on an existing valid reservation (two-step flow: reserve -> activate).

## Consequences

- **Positive:** removes ambiguity by explicitly storing which customer reserved which car.
- **Positive:** improves flow integrity and traceability between reservation and rental activation.
- **Positive:** provides clearer base for reservation status handling and future business rules.
- **Cost:** adds schema and service-layer complexity due to extra lifecycle step.
- **Cost:** requires migration and updates in application logic that previously assumed direct activation.

## Follow-Up

- Align service/application logic with reservation-first activation flow.
- Add/adjust validations to prevent activation without a valid reservation.
- Ensure API and docs clearly describe the two-step reservation -> activation lifecycle.
