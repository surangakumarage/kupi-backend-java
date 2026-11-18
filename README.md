# Kupi Backend (Java)

Spring Boot 3.5 / Java 21 microservices for the Kupi deals platform.

| Service | Folder |
|---|---|
| Service registry (Eureka) | `kupi-discovery-service` |
| API gateway | `kupi-api-gateway` |
| Auth / OAuth2 | `kupi-auth-service` |
| OTP | `kupi-otp-service` |
| Core (deals, merchants, subscriptions) | `kupi-core-service` |
| Location | `kupi-location-service` |
| Notification | `kupi-notification-service` |
| Order & payment | `kupi-order-payment-service` |
| File manager | `kupi-file-manager` |
| Util (mail, SMS) | `kupi-util-service` |

Full list of HTTP endpoints for every service: **[ENDPOINTS.md](ENDPOINTS.md)**.

## Setup

Secrets are not committed. Each service reads them from environment variables.

1. Copy `.env.example` to `.env` at the root and in each service folder, then fill in the values.
2. Start MySQL and Redis: `docker compose up -d`
3. Start all services: `python3 build_and_run_services.py`
   (it loads the root `.env.local` plus each service's `.env`).

Build a single service: `cd kupi-core-service && mvn clean package`.
