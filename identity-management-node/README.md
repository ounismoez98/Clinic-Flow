# Identity Management API — Node.js + Express (Keycloak)

A standalone **identity-management** backend built with **Node.js + Express**
(NOT Spring Boot), integrated with the same **Keycloak** realm (`clinic-flow`)
used by the Clinic-Flow platform.

It demonstrates identity & access management without re-implementing auth:
- It **validates Keycloak access tokens** locally (using Keycloak's public keys).
- It enforces **role-based access** (ADMIN-only endpoints).
- It **manages identities** (users & roles) through Keycloak's **Admin REST API**.

The existing **Angular** frontend logs in via Keycloak (unchanged), then calls
this API with the same token. So a user logs in once and the token works against
both the Spring gateway AND this Node service.

---

## Architecture

```
  Angular  --(Keycloak login)-->  Keycloak (clinic-flow realm, :8180)
  Angular  --(Bearer token)----->  Node Identity API (:5000)
                                      |  1. verify token via JWKS (offline)
                                      |  2. check role (ADMIN for user mgmt)
                                      |  3. call Keycloak Admin API to
                                      |     list/create/delete users, assign roles
                                      v
                                   Keycloak Admin REST API
```

Why this counts as "identity management": the service's whole job is managing
**who exists** (users), **what they are** (roles), and **proving who is calling**
(token validation) — all backed by Keycloak.

---

## Tech

- Node.js 22 + Express (ES modules)
- `jose` — verifies Keycloak JWTs against the realm's JWKS (public keys)
- native `fetch` — calls Keycloak's Admin API
- No database of its own — Keycloak is the source of truth.

---

## Run

1. Make sure **Keycloak is running** (from Clinic-Flow's docker compose) and the
   `clinic-flow` realm exists. Keycloak admin = `admin / admin`.
2. Install & start:

   ```bash
   cd identity-management-node
   npm install
   cp .env.example .env      # adjust if your ports differ
   npm start                 # http://localhost:5000
   ```

3. Start the Angular app (separate terminal) and open the **Identity (IAM)** page
   under Administration in the sidebar:

   ```bash
   cd clinic-flow-frontend
   npm start                 # http://localhost:4200
   ```

---

## Endpoints

All `/api/*` routes require a valid Keycloak Bearer token.
User-management routes additionally require the **ADMIN** role.

| Method | Path                              | Role        | Description                       |
|--------|-----------------------------------|-------------|-----------------------------------|
| GET    | `/health`                         | public      | service up check                  |
| GET    | `/api/profile`                    | any logged-in | the caller's identity (from token) |
| GET    | `/api/roles`                      | any logged-in | list realm roles                |
| GET    | `/api/users?search=`              | ADMIN       | list users                        |
| GET    | `/api/users/:id`                  | ADMIN       | one user + their roles            |
| POST   | `/api/users`                      | ADMIN       | create user (+ optional role)     |
| PUT    | `/api/users/:id`                  | ADMIN       | update firstName/lastName/email/enabled |
| DELETE | `/api/users/:id`                  | ADMIN       | delete user                       |
| POST   | `/api/users/:id/roles/:role`      | ADMIN       | assign a realm role               |
| DELETE | `/api/users/:id/roles/:role`      | ADMIN       | remove a realm role               |

---

## Test with curl

```bash
# 1. Get an ADMIN token from Keycloak
ATOK=$(curl -s -X POST \
  "http://localhost:8180/realms/clinic-flow/protocol/openid-connect/token" \
  -d "client_id=clinic-flow-frontend" -d "grant_type=password" \
  -d "username=admin" -d "password=admin123" \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

# 2. No token -> 401
curl -i http://localhost:5000/api/profile

# 3. List users (admin) -> 200
curl http://localhost:5000/api/users -H "Authorization: Bearer $ATOK"

# 4. A PATIENT token hitting /api/users -> 403 (role guard)
PTOK=$(curl -s -X POST \
  "http://localhost:8180/realms/clinic-flow/protocol/openid-connect/token" \
  -d "client_id=clinic-flow-frontend" -d "grant_type=password" \
  -d "username=patient" -d "password=patient123" \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
curl -i http://localhost:5000/api/users -H "Authorization: Bearer $PTOK"

# 5. Create a user
curl -X POST http://localhost:5000/api/users \
  -H "Authorization: Bearer $ATOK" -H "Content-Type: application/json" \
  -d '{"username":"jdoe","email":"jdoe@test.tn","firstName":"John","lastName":"Doe","password":"changeme123","role":"PATIENT"}'
```

Expected: no token → 401, wrong role → 403, valid admin → 200/201. Users created
here appear in the Keycloak admin console (realm clinic-flow) immediately.

---

## Project structure

```
identity-management-node/
  src/
    config.js                 env + derived Keycloak URLs
    server.js                 entry point
    app.js                    Express app + routes wiring
    middleware/auth.js        token validation (JWKS) + role guard
    services/keycloakAdmin.js Keycloak Admin API client
    routes/
      profile.js              GET /api/profile
      users.js                user CRUD + role assign (ADMIN)
      roles.js                GET /api/roles
```
