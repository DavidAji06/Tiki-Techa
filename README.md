# Tiki-Techa

A full-stack fantasy football + tactical simulation platform. Blends classic Fantasy Premier League-style squad building with manager-style gameplay — transfers, budgets, and (eventually) simulated matches and an AI-assisted opponent.

Built as a portfolio project deliberately spanning Java, Python, and TypeScript

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

The backend is the single source of truth and API gateway. The Python pipeline runs independently to seed/refresh player, team, and fixture data directly into PostgreSQL — it does not go through the backend.

## Features Implemented So Far

**Auth & Users**
- User registration and login with BCrypt password hashing
- Stateless JWT-based authentication, validated on every request via a custom Spring Security filter
- Global CORS + security configuration protecting all endpoints except public auth/player routes

**Data Pipeline**
- Python script ingesting real Premier League team, player, and fixture data from the official FPL API
- UPSERT semantics (safe to re-run for weekly data refreshes) with transactional rollback on failure
- Correct currency handling: prices stored as `BigDecimal`/`Decimal` throughout, never floating point

**Player Data API**
- `GET /api/players` with combinable, optional filtering (position, team, max cost) via JPA Specifications
- Relational data model (`Team` ← `Player`, `Team` ← `Fixture`) with proper foreign key constraints

**Frontend**
- Paginated player listing page fetching live data from the backend
- Loading and error states handled explicitly

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

| Method | Endpoint             | Auth required | Description                                   |
|--------|-----------------------|:--------------:|------------------------------------------------|
| POST   | `/api/auth/register`  | No             | Create a new user account                      |
| POST   | `/api/auth/login`     | No             | Authenticate and receive a JWT                  |
| GET    | `/api/players`        | No*            | List players, optionally filtered by `positionId`, `teamId`, `maxCost` |
| GET    | `/api/health`         | No             | Backend + database connectivity check           |

\* Public for now during development; access level to be finalized during later testing.

## Roadmap

- [x] **Phase 1** — Infrastructure & pipe check (Docker, Spring Boot, Next.js talking to each other)
- [x] **Phase 2** — Auth & user persistence (JWT, BCrypt, Spring Security)
- [x] **Phase 3** — Core data ingestion & read-only player view
- [x] **Phase 4** — Squad builder & financial ledger (BigDecimal precision, transactional locking, positional validation)
- [ ] **Phase 5** — Match simulation engine v1
- [ ] **Phase 6** — AI valuation model & heuristic AI manager
- [ ] **Phase 7** — Web usability & UI polish pass

## Notes

- Money is always handled as `BigDecimal` (Java) / `Decimal` (Python) — never floating point — to avoid rounding errors in the transfer market and ledger.
- Transfer market concurrency (e.g. two users buying the last available player simultaneously) is handled via transactional locking at the database level — see Phase 4.