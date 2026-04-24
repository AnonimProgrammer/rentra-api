# Security and Authentication Flow

This document describes how security is implemented in the app today (as-built), including authentication, JWT handling, authorization boundaries, and concrete request examples.

## Current Security Model

- Authentication is stateless JWT-based (`Bearer <token>` in `Authorization` header).
- Only auth endpoints under `/v*/auth/**` are public.
- Every other endpoint requires a valid JWT.
- Roles are embedded into JWT (`roles` claim) and mapped to Spring authorities as `ROLE_<name>`.
- Passwords are hashed with BCrypt before storage.

## Public vs Protected Endpoints

Configured behavior:

- `permitAll`: `/v*/auth/**`
- `authenticated`: every other route

At the moment, the only implemented auth endpoint is:

- `POST /v1/auth/continue` (public)

All future non-auth endpoints are protected by default unless explicitly whitelisted.

## Providers and Role Data

Seeded auth data:

- `auth_providers`: includes `PASSWORD`
- `roles`: includes `CUSTOMER`, `ADMIN`

Current role model:

- New users get `CUSTOMER` role by default.
- `ADMIN` exists and can be assigned through data/admin operations.

## End-to-End Auth Continue Flow

`POST /v1/auth/continue` handles both login and first-time signup.

```mermaid
sequenceDiagram
    actor Client
    participant API as AuthController
    participant Auth as AuthService
    participant Provider as UsernamePasswordAuthProvider
    participant Mapping as IdentityMappingService
    participant DB as DB (users/user_auth/roles)
    participant JWT as JwtTokenService

    Client->>API: POST /v1/auth/continue (provider + credentials [+ profile])
    API->>Auth: continueAuthentication(request)
    Auth->>Provider: authenticate(credentials)
    Provider->>DB: Find PASSWORD identity by providerUserId/email

    alt Existing identity
        Provider-->>Auth: ExternalIdentity(existing)
        Auth->>Mapping: resolve(identity, profile, rawPassword)
        Mapping->>DB: Load existing user via user_auth
        Mapping-->>Auth: IdentityResolution(user, newUser=false)
    else First-time identity
        Provider-->>Auth: ExternalIdentity(new)
        Auth->>Mapping: resolve(identity, profile, rawPassword)
        Mapping->>DB: Create user (ACTIVE) + attach CUSTOMER role
        Mapping->>DB: Create user_auth with BCrypt password hash
        Mapping-->>Auth: IdentityResolution(user, newUser=true)
    end

    Auth->>JWT: issueAccessToken(user)
    Auth->>JWT: issueRefreshToken(user)
    JWT-->>Auth: accessToken + refreshToken
    Auth-->>API: AuthContinueResponse(tokens, newUser)
    API-->>Client: 200 OK
```

## Request Authorization Flow

Every protected request passes through JWT filter logic.

```mermaid
sequenceDiagram
    actor Client
    participant Filter as JwtAuthenticationFilter
    participant JWT as JwtTokenService
    participant SC as Spring Security Context
    participant Endpoint as Protected Endpoint

    Client->>Filter: Request with Authorization header
    alt Missing or non-Bearer header
        Filter->>Endpoint: Continue without authentication
        Endpoint-->>Client: 401 Unauthorized
    else Bearer token present
        Filter->>JWT: parseAccessToken(token)
        alt Token valid
            JWT-->>Filter: Claims (sub + roles)
            Filter->>SC: Set Authentication(userId, ROLE_*)
            Filter->>Endpoint: Continue as authenticated user
            Endpoint-->>Client: 2xx/4xx domain response
        else Token invalid/expired
            Filter->>SC: clearContext()
            Filter->>Endpoint: Continue unauthenticated
            Endpoint-->>Client: 401 Unauthorized
        end
    end
```

## JWT Structure (Current)

Access token claims:

- `sub`: user UUID
- `iat`: issued timestamp
- `exp`: expiry timestamp (`JWT_ACCESS_TTL_SECONDS`, default 900)
- `roles`: array like `["CUSTOMER"]`, `["ADMIN"]`, etc.

Refresh token claims:

- `sub`: user UUID
- `iat`: issued timestamp
- `exp`: expiry timestamp (`JWT_REFRESH_TTL_SECONDS`, default 1209600)
- `type`: `"refresh"`

Signing:

- HMAC key from `JWT_SECRET` (`auth.jwt.secret`)

## Concrete Endpoint Examples (Bruno-aligned)

Base URL from Bruno environment:

- `http://localhost:8080`

### 1) Signup via Continue (new user)

`POST /v1/auth/continue`

```json
{
  "provider": "PASSWORD",
  "credentials": {
    "email": "test@gmail.com",
    "password": "11111111"
  },
  "profile": {
    "firstName": "Test",
    "lastName": "Test",
    "email": "test@gmail.com",
    "birthDate": "1998-04-15"
  }
}
```

Typical response:

```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<jwt-refresh-token>",
  "newUser": true
}
```

### 2) Login via Continue (existing user)

`POST /v1/auth/continue`

```json
{
  "provider": "PASSWORD",
  "credentials": {
    "email": "test@gmail.com",
    "password": "11111111"
  }
}
```

Typical response:

```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<jwt-refresh-token>",
  "newUser": false
}
```

### 3) Calling a protected endpoint

Use the access token in the `Authorization` header:

```http
Authorization: Bearer <jwt-access-token>
```

Any non-`/v*/auth/**` endpoint without valid token returns unauthorized.

## Security Notes (As of Now)

- App is stateless (`SessionCreationPolicy.STATELESS`).
- CSRF is disabled, which is consistent with token-based API usage.
- Password provider accepts either `email` or `username` field from credentials.
- There is currently no refresh endpoint exposed yet, even though refresh tokens are issued.

## Environment Requirements

Required env vars:

- `JWT_SECRET` (must be strong; sample suggests 64-char random secret)
- `JWT_ACCESS_TTL_SECONDS` (default `900`)
- `JWT_REFRESH_TTL_SECONDS` (default `1209600`)
