-- Rename core business table: rental_services -> rental_agencies
RENAME TABLE rental_services TO rental_agencies;

-- Keep table-local naming consistent after rename
ALTER TABLE rental_agencies
    DROP INDEX idx_rental_services_owner_user_id,
    ADD INDEX idx_rental_agencies_owner_user_id (owner_user_id),
    DROP FOREIGN KEY fk_rental_services_owner,
    ADD CONSTRAINT fk_rental_agencies_owner
        FOREIGN KEY (owner_user_id) REFERENCES users (id);

-- vehicles: rental_service_id -> rental_agency_id
ALTER TABLE vehicles
    DROP FOREIGN KEY fk_vehicles_rental_service,
    DROP INDEX idx_vehicles_rental_service_id,
    CHANGE COLUMN rental_service_id rental_agency_id BINARY(16) NOT NULL,
    ADD INDEX idx_vehicles_rental_agency_id (rental_agency_id),
    ADD CONSTRAINT fk_vehicles_rental_agency
        FOREIGN KEY (rental_agency_id) REFERENCES rental_agencies (id);
