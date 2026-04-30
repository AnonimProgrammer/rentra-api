ALTER TABLE rents
    ADD COLUMN rental_agency_id BINARY(16) NOT NULL AFTER vehicle_id,
    ADD KEY idx_rents_rental_agency_id (rental_agency_id),
    ADD CONSTRAINT fk_rents_rental_agency
        FOREIGN KEY (rental_agency_id) REFERENCES rental_agencies (id);

ALTER TABLE reservations
    ADD COLUMN rental_agency_id BINARY(16) NOT NULL AFTER vehicle_id,
    ADD KEY idx_reservations_rental_agency_id (rental_agency_id),
    ADD CONSTRAINT fk_reservations_rental_agency
        FOREIGN KEY (rental_agency_id) REFERENCES rental_agencies (id);
