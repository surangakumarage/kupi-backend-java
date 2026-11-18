# OAuth Token Endpoint Security Configuration

## Overview
The `/oauth/token` endpoint uses **OAuth2 Client Credentials** stored in the database. This is different from the OTP endpoints which use custom headers (app-key and Authorization).

---

## 🔐 OAuth Token Endpoint Security

### Endpoint
- **URL:** `POST /kupi-auth-service/oauth/token`
- **Authentication:** OAuth2 Client Credentials (HTTP Basic Auth)
- **Storage:** Database table `oauth_client_details`

---

## 📋 Configuration Sources

### 1. Authorization Server Configuration
**File:** `kupi-auth-service/src/main/java/com/kupi/cc/auth/configuration/AuthorizationServerConfiguration.java`

**Key Components:**
- **Line 70-72:** OAuth token endpoint security configured
- **Line 85-86:** Client details loaded from database
- **Line 94-143:** Token endpoint configuration with custom exception handling

```java
@Override
public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource).passwordEncoder(passwordEncoder);  // Credentials from DB
}
```

### 2. Auth Filter
**File:** `kupi-auth-service/src/main/java/com/kupi/cc/auth/configuration/AuthFilter.java`

**Key Components:**
- **Line 31:** Token endpoint pattern `/oauth/token`
- **Line 56-60:** Filter registration for token endpoint
- **Line 90-112:** Validation logic for password grant type

```java
private static final String TOKEN_ENDPOINT = "/oauth/token";

// Validates username, password, and user role
if (request.getServletPath().equals(TOKEN_ENDPOINT) &&
    request.getParameter(GRANT_TYPE).equals(PSW_FIELD)) {
    // Validates fields and role
}
```

---

## 🗄️ Database Storage

### Table: `oauth_client_details`

**Location:** `schema.sql` (lines 88-101)

**Structure:**
```sql
CREATE TABLE oauth_client_details (
    client_id VARCHAR(255) NOT NULL PRIMARY KEY,
    client_secret VARCHAR(255) NOT NULL,
    web_server_redirect_uri VARCHAR(2048),
    scope VARCHAR(255),
    access_token_validity INT,
    refresh_token_validity INT,
    resource_ids VARCHAR(1024),
    authorized_grant_types VARCHAR(1024),
    authorities VARCHAR(1024),
    additional_information VARCHAR(4096),
    autoapprove VARCHAR(255)
);
```

### Current Client Credentials

**Default Client (from schema.sql):**
```sql
INSERT INTO oauth_client_details VALUES (
    'tokomobile',                      -- client_id
    '$2y$12$ZwonUte3P7EmlwXHhxrly...', -- client_secret (encrypted)
    'http://localhost:9191/login',     -- redirect_uri
    'READ,WRITE,UPDATE',               -- scope
    '3600',                            -- access_token_validity
    '10000',                           -- refresh_token_validity
    'tokomobile,oauth2-resource',      -- resource_ids
    'authorization_code,password,refresh_token,implicit',
    '{}',                              -- additional_information
    NULL                               -- autoapprove
);
```

---

## 🔑 How to Get OAuth Token

### Request Format

```bash
curl -X POST http://localhost:8080/kupi-auth-service/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "tokomobile:{client_secret}" \
  -d "grant_type=password" \
  -d "username={user_mobile_or_email}" \
  -d "password={user_password}" \
  -d "user_role={USER|MERCHANT|ADMIN|BANK}" \
  -d "scope=READ,WRITE,UPDATE"
```

### Example Request

```bash
curl -X POST http://localhost:8080/kupi-auth-service/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "tokomobile:your_client_secret" \
  -d "grant_type=password" \
  -d "username=john@example.com" \
  -d "password=Password123" \
  -d "user_role=USER" \
  -d "scope=READ,WRITE,UPDATE"
```

### Response

```json
{
  "access_token": "eyJhbGc...",
  "token_type": "bearer",
  "refresh_token": "eyJhbGc...",
  "expires_in": 3599,
  "scope": "READ WRITE UPDATE",
  "jti": "abcd1234..."
}
```

---

## 🔄 Comparison: OTP vs OAuth Token Security

| Feature | OTP Endpoints | OAuth Token Endpoint |
|---------|---------------|---------------------|
| **request-otp** | Custom `app-key` header | - |
| **verify-and-register** | Custom `Authorization` header | - |
| **verify-and-login** | Custom `Authorization` header | - |
| **/oauth/token** | - | OAuth2 Client Credentials (HTTP Basic Auth) |
| **Storage** | `application.yml` environment variables | `oauth_client_details` database table |
| **Validation** | Servlet Filters (ApiHeaderFilter, AuthorizationHeaderFilter) | OAuth2 AuthorizationServerConfiguration |
| **Grant Types** | N/A (Direct token generation) | password, authorization_code, refresh_token, implicit |

---

## 🔐 Security Layers for OAuth Token

### Layer 1: Client Authentication (HTTP Basic)
```
Authorization: Basic base64(client_id:client_secret)
Example: Authorization: Basic dG9rb21vYmlsZTpwYXNzd29yZA==
```

### Layer 2: User Authentication (Form Parameters)
```
grant_type=password
username={user_mobile_or_email}
password={user_password}
user_role={role_type}
```

### Layer 3: Validation (AuthFilter)
- Validates username and password format
- Checks if user role matches actual user role
- Blocks users with status BLOCKED

### Layer 4: OAuth2 Framework
- Generates access token with expiry
- Generates refresh token
- Stores token in `oauth_access_token` table
- Validates token on resource access

---

## 📊 Request Flow for OAuth Token

```
Client Request
    ↓
HTTP Basic Auth (client_id:client_secret)
    ↓
AuthFilter validation
    ├─ Check username/password provided
    ├─ Check password format valid
    ├─ Load user and validate role
    ├─ Check user not BLOCKED
    └─ Allow or Reject
        ↓
    AuthorizationServerConfiguration
        ├─ Verify client credentials from DB
        ├─ Generate access token
        ├─ Generate refresh token
        ├─ Store tokens in oauth_access_token table
        └─ Return token response
```

---

## 🔑 Database Tables Involved

### oauth_client_details
- Stores client credentials
- Used for HTTP Basic auth validation
- Contains scope, grant types, token validity

### oauth_access_token
- Stores issued access tokens
- Contains user info and expiry
- Used for validating future requests

### oauth_refresh_token
- Stores refresh tokens
- Allows getting new access token without re-authenticating

### user
- Contains user credentials
- Used for password validation
- Checks user approval status

---

## 🛠️ Modifying Client Credentials

### To Add New Client

Insert into `oauth_client_details`:
```sql
INSERT INTO oauth_client_details (
    client_id,
    client_secret,
    web_server_redirect_uri,
    scope,
    access_token_validity,
    refresh_token_validity,
    resource_ids,
    authorized_grant_types,
    authorities,
    additional_information,
    autoapprove
) VALUES (
    'new_client_id',
    '{encrypted_password}',
    'http://example.com/callback',
    'READ,WRITE,UPDATE',
    '3600',
    '10000',
    'resource1,resource2',
    'authorization_code,password,refresh_token,implicit',
    '{}',
    '{}',
    NULL
);
```

### To Change Client Secret

```sql
UPDATE oauth_client_details
SET client_secret = '{new_encrypted_password}'
WHERE client_id = 'tokomobile';
```

---

## 🔍 How OTP Endpoints Compare

### OTP Endpoints (New Implementation)
```
POST /api/v1/otp/users/request-otp
    ├─ Header: app-key: <APP_KEY>
    └─ Validation: ApiHeaderFilter (application.yml)

POST /api/v1/otp/users/verify-and-register
    ├─ Header: Authorization: Basic <AUTH_BASIC_TOKEN>
    └─ Validation: AuthorizationHeaderFilter (application.yml)

POST /api/v1/otp/users/verify-and-login
    ├─ Header: Authorization: Basic <AUTH_BASIC_TOKEN>
    └─ Validation: AuthorizationHeaderFilter (application.yml)
```

### OAuth Token Endpoint (Existing)
```
POST /oauth/token
    ├─ HTTP Basic: Authorization: Basic <AUTH_BASIC_TOKEN>
    ├─ Form Data: username, password, user_role, grant_type
    ├─ Validation: AuthFilter + OAuth2 AuthorizationServerConfiguration
    └─ Storage: oauth_client_details table + oauth_access_token table
```

---

## ⚙️ Environment Variables

Only OTP endpoints use environment variables. OAuth token endpoint uses database:

### OTP Endpoints (Environment Variables)
```bash
SPRING_APPLICATION_APP_KEY=<APP_KEY>
AUTH_BASIC_TOKEN=<AUTH_BASIC_TOKEN>
```

### OAuth Token Endpoint (Database)
```
Database: oauth_client_details table
- client_id: tokomobile
- client_secret: (stored encrypted)
```

---

## Summary

| Aspect | OAuth Token | OTP Endpoints |
|--------|-------------|---------------|
| **Authentication Type** | OAuth2 Client Credentials | Custom Headers |
| **Storage** | Database (`oauth_client_details`) | Environment Variables (`application.yml`) |
| **Configuration** | AuthorizationServerConfiguration | ApiHeaderFilter + AuthorizationHeaderFilter |
| **Client ID** | tokomobile | N/A |
| **Token Endpoint** | `/oauth/token` | N/A (Direct generation) |
| **User Authentication** | Required (username/password) | Not required |
| **Token Type** | Bearer Token (JWT) | N/A |
| **Refresh Token** | Supported | Not needed |

---

## 📄 Related Configuration Files

1. **AuthorizationServerConfiguration.java** - OAuth2 server configuration
2. **AuthFilter.java** - Token endpoint validation filter
3. **ApiHeaderFilter.java** - OTP request-otp validation (custom)
4. **AuthorizationHeaderFilter.java** - OTP verify validation (custom)
5. **application.yml** - Environment variables for OTP headers
6. **schema.sql** - Database schema with oauth_client_details

