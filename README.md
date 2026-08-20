# Tiki-Techa

A full-stack fantasy football + tactical simulation platform. Blends classic Fantasy Premier League with manager-style gameplay: transfers, budgets, simulated matches, and (soon) an AI-assisted opponent.

Built as a portfolio project and fun game for the new season

## Tech Stack

- **Backend:** Java 21, Spring Boot (Spring Web, Spring Data JPA, Spring Security)
- **Database:** PostgreSQL 15
- **Data Pipeline:** Python (psycopg2, requests) — ingests real Premier League data from the official Fantasy Premier League API
- **Frontend:** Next.js (App Router), TypeScript, React
- **Auth:** JWT (stateless, via Spring Security)
- **Containerization:** Docker Compose (PostgreSQL)

## Architecture

```
┌─────────────┐        ┌──────────────────┐        ┌─────────────┐
│   Next.js   │ <────> │   Spring Boot     │ <────> │ PostgreSQL  │
│  (frontend) │  REST  │    (backend)      │  JPA   │             │
└─────────────┘        └──────────────────┘        └─────────────┘
                                 ^
                                 │ UPSERT
                        ┌──────────────────┐
                        │  Python ingestion │
                        │  (data-pipeline)  │
                        └──────────────────┘
                                 │
                                 v
                        Fantasy Premier League API
```

Backend is the single source of truth and API gateway. The Python pipeline runs independently to seed/refresh player, team, and fixture data directly into PostgreSQL — it does not go through the backend.

Within the backend, business logic follows a **Controller → Service → Repository** layering: controllers handle HTTP concerns only, services own validation and business rules (the transfer ledger, match simulation), and repositories handle persistence.

## Features Implemented So Far

**Auth & Users**
- User registration and login with BCrypt password hashing
- Stateless JWT-based authentication, validated on every request via a custom Spring Security filter
- Global CORS + security configuration protecting all endpoints except public auth/player/match routes
- Sensitive fields (password hashes) excluded from all API responses via `@JsonIgnore`

**Data Pipeline**
- Python script ingesting real Premier League team, player, and fixture data from the official FPL API
- UPSERT semantics (safe to re-run for weekly data refreshes) with transactional rollback on failure
- Correct currency handling: prices stored as `BigDecimal`/`Decimal` throughout, never floating point

**Player Data API**
- `GET /api/players` with combinable, optional filtering (position, team, max cost) via JPA Specifications
- Relational data model (`Team` ← `Player`, `Team` ← `Fixture`) with proper foreign key constraints

**Squad Builder & Financial Ledger**
- Full FPL-style 15-player squads (11 starters + 4 bench), backed by a dedicated `SquadPlayer` join entity carrying purchase price and starting status
- Buy/sell transfer endpoints with strict `BigDecimal` precision — no floating-point rounding errors in the budget
- **Concurrency-safe transactions**: pessimistic row-locking (`SELECT ... FOR UPDATE`) on a squad's budget, preventing race conditions from simultaneous transfers
- Full validation chain on every purchase: duplicate squad/player checks, budget sufficiency, positional squad limits (max 2 GK / 5 DEF / 5 MID / 3 FWD), and a max-3-players-per-real-world-team rule
- Selling refunds at **current market price** rather than original purchase price — a deliberate design choice letting users make real trading decisions, distinct from real FPL's rules
- Full transaction audit log (`GET /api/transfers`, filterable by user/type/price range)
- Lineup management: submit a full starting XI in one request, validated as a whole (exact position-count bounds, all players confirmed to belong to the squad) before being applied atomically

**Match Simulation Engine**
- Head-to-head match simulation between two real users starting XI
- Attack/defense ratings derived from real ingested player statistics (goals, assists, clean sheets, goals conceded), split by position group
- Expected goals modeled as a genuine **Poisson process** (goals sampled via Knuth's algorithm) rather than arbitrary randomness — the same statistical approach used in real football analytics models
- Full match event timeline: goals placed at realistic minutes and attributed to real attacking players; independent per-player card probability (yellow/red)

**Frontend**
- Paginated player listing page fetching live data from the backend
- Loading and error states handled explicitly
- Remaining UI (squad builder, match results) deferred to a dedicated design pass — see Roadmap

## Getting Started

Services must be started in this order — later steps depend on earlier ones being up.

**1. Start PostgreSQL**
```bash
docker compose up -d
```

**2. Start the backend**
```bash
cd backend
./gradlew bootRun
```
Confirm it's running: `curl http://localhost:8080/api/health`

**3. Seed the database**
```bash
cd data-pipeline
python -m venv venv
source venv/Scripts/activate   # git bash on Windows
pip install psycopg2-binary requests
python ingest.py
```

**4. Start the frontend**
```bash
cd frontend
npm install
npm run dev
```
Visit `http://localhost:3000/players`.

## API Overview

| Method | Endpoint                  | Auth required | Description                                   |
|--------|----------------------------|:--------------:|------------------------------------------------|
| POST   | `/api/auth/register`       | No             | Create a new user account                      |
| POST   | `/api/auth/login`          | No             | Authenticate and receive a JWT                  |
| GET    | `/api/players`             | No*            | List players, optionally filtered by `positionId`, `teamId`, `maxCost` |
| GET    | `/api/health`              | No             | Backend + database connectivity check           |
| POST   | `/api/squads`               | Yes            | Create a squad for the authenticated user       |
| GET    | `/api/squads/me`            | Yes            | Get the authenticated user's squad and players  |
| PATCH  | `/api/squads/lineup`        | Yes            | Set the starting XI (validated as a whole)      |
| POST   | `/api/transfers/buy`        | Yes            | Buy a player into the authenticated user's squad|
| POST   | `/api/transfers/sell`       | Yes            | Sell a player from the authenticated user's squad, refunded at current market price |
| GET    | `/api/transfers`            | No*            | List transactions, optionally filtered by `userId`, `type`, `minAmount`, `maxAmount` |
| POST   | `/api/matches`               | No*            | Simulate a match between two squads and return the result |

\* Public for now during development; access level to be finalized during later testing.

## Roadmap

- [x] **Phase 1** — Infrastructure & pipe check (Docker, Spring Boot, Next.js talking to each other)
- [x] **Phase 2** — Auth & user persistence (JWT, BCrypt, Spring Security)
- [x] **Phase 3** — Core data ingestion & read-only player view
- [x] **Phase 4** — Squad builder & financial ledger (BigDecimal precision, transactional locking, positional validation)
- [x] **Phase 5** — Match simulation engine v1 (Poisson-based scoring, event timeline)
- [ ] **Phase 6** — AI valuation model & heuristic AI manager
- [ ] **Phase 7** — Web usability & UI polish pass

## Notes

- Money is always handled as `BigDecimal` (Java) / `Decimal` (Python) — never floating point — to avoid rounding errors in the transfer market and ledger.
- Transfer market concurrency (e.g. two users buying the last available player simultaneously) is handled via transactional locking at the database level — see Phase 4.
- Service methods that require the pessimistic lock or perform multiple related writes are annotated `@Transactional` at the service layer; controllers stay free of transaction management to avoid nested-transaction pitfalls (e.g. `UnexpectedRollbackException` when an inner exception marks a shared transaction rollback-only).