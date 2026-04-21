CREATE TABLE auth_providers (
    id BINARY(16) NOT NULL PRIMARY KEY,
    type TEXT NOT NULL,
    UNIQUE KEY uk_auth_providers_type (type(255))
);

CREATE TABLE users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone_number TEXT,
    birth_date DATE NOT NULL,
    status TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email(255))
);

CREATE TABLE roles (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    UNIQUE KEY uk_roles_name (name(255))
);

CREATE TABLE user_auth (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    provider_id BINARY(16) NOT NULL,
    provider_user_id TEXT,
    email TEXT,
    password_hash TEXT,
    metadata JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_auth_provider_email (provider_id, email(255)),
    UNIQUE KEY uk_user_auth_provider_user (provider_id, provider_user_id(255)),
    KEY idx_user_auth_user_id (user_id),
    CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_auth_provider FOREIGN KEY (provider_id) REFERENCES auth_providers (id)
);

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE rental_services (
    id BINARY(16) NOT NULL PRIMARY KEY,
    owner_user_id BINARY(16) NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    location_lat DECIMAL(10,7),
    location_lng DECIMAL(10,7),
    status TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_rental_services_owner_user_id (owner_user_id),
    CONSTRAINT fk_rental_services_owner FOREIGN KEY (owner_user_id) REFERENCES users (id)
);

CREATE TABLE vehicles (
    id BINARY(16) NOT NULL PRIMARY KEY,
    rental_service_id BINARY(16) NOT NULL,
    category TEXT NOT NULL,
    brand TEXT NOT NULL,
    model TEXT NOT NULL,
    transmission TEXT NOT NULL,
    fuel_type TEXT NOT NULL,
    seat_count INT UNSIGNED NOT NULL,
    status TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_vehicles_rental_service_id (rental_service_id),
    CONSTRAINT fk_vehicles_rental_service FOREIGN KEY (rental_service_id) REFERENCES rental_services (id)
);

CREATE TABLE vehicle_media (
    id BINARY(16) NOT NULL PRIMARY KEY,
    vehicle_id BINARY(16) NOT NULL,
    image_url TEXT NOT NULL,
    KEY idx_vehicle_media_vehicle_id (vehicle_id),
    CONSTRAINT fk_vehicle_media_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

CREATE TABLE vehicle_rates (
    id BINARY(16) NOT NULL PRIMARY KEY,
    vehicle_id BINARY(16) NOT NULL,
    type TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    currency TEXT NOT NULL,
    KEY idx_vehicle_rates_vehicle_id (vehicle_id),
    CONSTRAINT fk_vehicle_rates_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

CREATE TABLE rents (
    id BINARY(16) NOT NULL PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    vehicle_id BINARY(16) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    rating TINYINT UNSIGNED,
    status TEXT NOT NULL,
    starts_at DATETIME NOT NULL,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_rents_customer_id (customer_id),
    KEY idx_rents_vehicle_id (vehicle_id),
    CONSTRAINT fk_rents_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_rents_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);
