# Trip Planner

A full-stack travel planning application — Spring Boot backend + React frontend.

---

## Running Locally

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.6+ |
| PostgreSQL | 14+ |
| Node.js | 18+ |

---

### 1. Database

Create the PostgreSQL database and user:

```sql
CREATE DATABASE tripplanner;
CREATE USER tripplanner_user WITH PASSWORD 'tripplanner_pass';
GRANT ALL PRIVILEGES ON DATABASE tripplanner TO tripplanner_user;
```

---

### 2. Environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Where to get it |
|----------|----------------|
| `AUTH0_DOMAIN` | [Auth0 Dashboard](https://manage.auth0.com) → Applications |
| `AUTH0_CLIENT_ID` | Auth0 Dashboard → Applications |
| `GOOGLE_MAPS_API_KEY` | [Google Cloud Console](https://console.cloud.google.com) → APIs & Services |
| `JWT_SECRET` | Any random 256-bit string |

The database credentials (`DB_*`) match the defaults created in step 1 — change them if you used different values.

---

### 3. Backend

```bash
# From the project root
./mvnw clean install -DskipTests

# Start the API server
./mvnw spring-boot:run -pl presentation
```

The API starts on **http://localhost:8080**.

---

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**.

---

### 5. Running tests

```bash
# All tests (all modules)
./mvnw test

# Single module
./mvnw test -pl data-access
./mvnw test -pl business
./mvnw test -pl presentation

# Single test class
./mvnw test -pl business -Dtest=TripServiceImplTest
```

Test reports are generated in `<module>/target/surefire-reports/`.  
Coverage reports (JaCoCo) are generated in `<module>/target/site/jacoco/index.html`.

---

## Architecture

```
presentation/   ← REST controllers (Spring MVC)
business/       ← Services, business logic
data-access/    ← Spring Data JPA repositories
domain/         ← Entities, DTOs
```

**Stack:** Java 17 · Spring Boot 3.2 · PostgreSQL · Auth0 (OAuth2/JWT) · Google Places API · React + TypeScript + Vite
