CREATE TABLE reservations (
    id BINARY(16) NOT NULL PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    vehicle_id BINARY(16) NOT NULL,
    status TEXT NOT NULL,
    reserved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME,
    cancelled_at DATETIME,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_reservations_customer_id (customer_id),
    KEY idx_reservations_vehicle_id (vehicle_id),
    KEY idx_reservations_status (status(255)),
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);
