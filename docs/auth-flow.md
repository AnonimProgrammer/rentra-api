# Authentication Flow

This document describes how authentication and authorization work in Rentra based on the current ERD.

## Security-related tables

- `auth_providers`: lookup table for authentication providers (for example `PASSWORD`, `GOOGLE`, `APPLE`). It defines the provider identity used by `user_auth`.
- `user_auth`: stores user login identities and credentials per provider. For `PASSWORD`, it stores `email` + `password_hash`; for other providers, it stores provider-specific IDs in `provider_user_id` and any extra fields in `metadata`.
- `roles`: lookup table for system roles (currently `CUSTOMER`, `ADMIN`).
- `user_roles`: many-to-many relation between `users` and `roles`. A user can have one or many roles.
- `users`: primary profile record used across the platform. Authentication rows in `user_auth` and permissions in `user_roles` attach to this user ID.

## Roles

- `CUSTOMER`: default role for people using the platform to browse, rent, and manage their own rentals.
- `ADMIN`: back-office role with elevated permissions for operations and platform management.

### Current business mapping

- End customers have role `CUSTOMER`.
- Rental service worker is currently only one person (the owner) and is also treated as role `CUSTOMER` for now.
- We intentionally ignore `rental_service_users` in authorization logic at this stage.

## Authentication provider strategy

- Default and only active provider for now is `PASSWORD`.
- Password auth flow uses `user_auth.email` + `user_auth.password_hash`.
- Additional providers can be introduced later by inserting new records into `auth_providers` and creating matching `user_auth` rows.

## Auth flow (current)

1. User submits email and password.
2. System finds provider `PASSWORD` in `auth_providers`.
3. System loads `user_auth` by (`provider_id`, `email`) and verifies `password_hash`.
4. On success, system resolves `user_id`, loads roles from `user_roles -> roles`, and builds auth context.
5. Access to endpoints is granted based on role checks (`CUSTOMER` or `ADMIN`).

## Registration flow (current)

1. Create a row in `users`.
2. Create a row in `user_auth` with provider `PASSWORD` and hashed password.
3. Assign `CUSTOMER` role in `user_roles` by default.
4. Optionally assign `ADMIN` for privileged staff through an admin-only operation.

## Notes for next iterations

- When we start using `rental_service_users`, we can introduce service-level permissions (for example owner/manager/worker scopes) on top of system roles.
- Social login can be enabled later without changing role design because roles are provider-agnostic.
