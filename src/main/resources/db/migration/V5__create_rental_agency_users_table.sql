CREATE TABLE rental_agency_users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    rental_agency_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rental_agency_users_user_agency (user_id, rental_agency_id),
    KEY idx_rental_agency_users_agency_id (rental_agency_id),
    KEY idx_rental_agency_users_user_id (user_id),
    CONSTRAINT fk_rental_agency_users_agency
        FOREIGN KEY (rental_agency_id) REFERENCES rental_agencies (id),
    CONSTRAINT fk_rental_agency_users_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
