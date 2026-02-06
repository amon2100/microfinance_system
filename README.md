# Microfinance System

Author: Kabwigu Amon Eliya

## What it does
A desktop, offline-first microfinance system for SACCOs with member management, savings, loans, transactions, RBAC, and audit logging. It also includes a Spring Boot backend scaffold for centralized data, JWT auth, mobile money repayments, and webhook processing.

### Desktop app capabilities (offline-first)
- Secure authentication with salted PBKDF2 password hashing.
- Role-based access control (DIRECTOR, MANAGER, TELLER) enforced in services and UI.
- Member registration with profile data, searchable list, and profile photo storage.
- Savings accounts: create, deposit, withdraw, and view account balances.
- Loan lifecycle: register, approve, disburse, and track outstanding balances and terms.
- Transactions: record deposits/withdrawals/loan disbursements/repayments and reverse transactions (DIRECTOR only).
- Audit logging for sensitive actions (approvals, reversals, user creation, etc.).
- Professional dashboard UI with sidebar navigation, topbar, search, user profile display, and online/offline status badge.
- Local SQLite database with automatic schema initialization and migrations.
- External IDs for records to enable safe sync and idempotency.
- Outbox-based background sync that queues local changes while offline.
- Auto-sync when online: members, users, savings, loans, transactions, and member photos.
- Validation and business rules enforced in service layer (e.g., teller limits, approval rules).

### Backend API capabilities (Spring Boot scaffold)
- JWT-based authentication and role-based authorization.
- REST endpoints for members, savings, loans, and transactions.
- Sync endpoints for ingesting offline changes and returning updates.
- Idempotent processing with external IDs to avoid duplicates.
- Mobile money payments scaffold (MTN/Airtel adapters) with payment status tracking.
- Webhook endpoints for payment callbacks with verification.
- Audit logging support for server-side actions.
- File storage for member photos.

### Key workflows supported
- Register members, capture and store photos, and manage member profiles.
- Open savings accounts and handle deposits/withdrawals.
- Create loans, approve, disburse, and track repayments.
- Reverse erroneous transactions with full audit trails.
- Operate fully offline and sync automatically when connectivity returns.
- Use centralized backend for consolidated reporting and mobile money collections.

## Software used
- Java 21
- JavaFX (UI)
- SQLite (local offline storage)
- Gradle (build)
- Spring Boot (backend API)
- PostgreSQL (backend database)
- JWT (authentication)

## Modules
- Desktop app (JavaFX) in the project root
- Backend API (Spring Boot) in backend/
