INSERT INTO auth_providers (id, type)
VALUES (UUID_TO_BIN(UUID(), 1), 'PASSWORD');

INSERT INTO roles (id, name)
VALUES
    (UUID_TO_BIN(UUID(), 1), 'CUSTOMER'),
    (UUID_TO_BIN(UUID(), 1), 'ADMIN');
