# Microfinance Backend (Spring Boot)

## REST Endpoints
- POST /api/auth/login
- POST /api/loans
- POST /api/loans/approve
- POST /api/loans/disburse
- POST /api/payments/repayments
- POST /api/webhooks/{provider}

## Mobile Money Flow (Server)
1. Client calls /api/payments/repayments with idempotencyKey
2. Server creates Payment (PENDING), calls provider adapter
3. Provider sends callback to /api/webhooks/{provider}
4. Server validates signature, updates Payment, posts repayment

## Desktop Outbox + Sync (Outline)
- Local SQLite table `outbox` with fields: id, endpoint, method, payload, idempotencyKey, status, retries, lastError, createdAt
- When offline, queue requests locally with status=PENDING
- Background sync checks connectivity and replays pending items in order
- On success, mark as SENT and update local cache
- Use idempotencyKey for safe retry
