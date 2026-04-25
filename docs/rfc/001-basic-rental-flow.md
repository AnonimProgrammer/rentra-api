# RFC-001: Basic Rental Flow

**Status:** Approved

**Author:** Omar Ismayilov

## Short Description

This RFC defines the MVP rental journey for Rentra. The system enables customers to discover nearby rental services, browse available vehicles, reserve and rent a vehicle, and complete the rental with automatic price calculation based on rental duration and configured rate type (for example `HOUR` or `DAY`). The objective is a simple and reliable end-to-end flow without advanced features.

## Flow Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant App as Rentra App
    participant Search as Discovery/Search Service
    participant Rental as Rental Service
    participant Rent as Rent Management
    participant Pricing as Pricing Engine

    Customer->>App: 1. Open app
    App->>Search: 2. Request nearest rental services
    Search-->>App: Return map-based nearby services

    alt 3a. Browse within rental service
        Customer->>App: Select rental service and browse vehicles
        App->>Rental: Fetch service vehicles
        Rental-->>App: Return vehicle list
    else 3b. Search with filters
        Customer->>App: Search by category, price, etc.
        App->>Search: Execute filtered vehicle search
        Search-->>App: Return matching vehicles
    end

    Customer->>App: 4. Select vehicle
    App->>Rent: 5. Create temporary reservation (PENDING)
    Rent-->>App: Reservation created

    Customer->>App: 6. Confirm rental
    App->>Rent: 7. Activate rent and record starts_at
    Rent-->>App: Rent status ACTIVE

    Note over Customer,App: 8. Customer uses the vehicle

    Customer->>App: 9. Complete rental
    App->>Rent: Set completed_at
    Rent->>Pricing: 10. Calculate total from duration + rate type (HOUR, DAY, etc.)
    Pricing-->>Rent: Total price
    Rent-->>App: Rental finalized

    opt 11. Optional feedback
        Customer->>App: Submit rating
        App->>Rent: Save rating
    end
```

## API Design

The following endpoints are sufficient to support the RFC-001 basic rental flow.

- `GET /services` - List rental services for map-based discovery (supports geo params).
- `GET /services/{serviceId}/vehicles` - List vehicles for a specific rental service.
- `GET /vehicles/search?...` - Search vehicles by filters.
- `GET /vehicles/{vehicleId}` - Get vehicle details, rates, and current availability signal.
- `POST /reservations` - Create temporary reservation in `PENDING` state.
- `POST /reservations/{id}/confirm` - Confirm reservation and activate rental.
- `GET /rents/active` - Get customer's currently active rent.
- `POST /rents/{id}/complete` - Complete rental, set completion time, and trigger final price calculation.
- `POST /rents/{id}/rate` - Submit optional customer rating after completion.

## Future Extensions

- Payment integration
- Advanced booking (schedule in advance)
- Cancellation policies and penalties
- Real-time vehicle tracking (GPS)
- Dynamic pricing (demand-based)
- Improved availability system (time windows)
- Search optimization (geo indexing, Elasticsearch)
- Notifications (email, push)
