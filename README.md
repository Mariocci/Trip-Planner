# Trip Planner

Aplikacija za planiranje putovanja.

---

## Pokretanje

### Potrebno

- Java 17+
- Maven 3.6+
- PostgreSQL 14+
- Node.js 18+

---

### 1. Baza podataka

PostgreSQL baza:

CREATE DATABASE tripplanner;
CREATE USER tripplanner_user WITH PASSWORD 'tripplanner_pass';
GRANT ALL PRIVILEGES ON DATABASE tripplanner TO tripplanner_user;

\c tripplanner

GRANT USAGE, CREATE ON SCHEMA public TO tripplanner_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tripplanner_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tripplanner_user;

---

### 2. Backend


mvn clean install
cd presentation
mvn spring-boot:run

Backend: **http://localhost:8080**

---

### 3. Pokreni Frontend

cd frontend
npm install
npm run dev


Frontend: **http://localhost:5173**

---

### 4. Testovi

mvn test

---

**Backend:** Java 17, Spring Boot, PostgreSQL  
**Frontend:** React, TypeScript, Vite  
**Auth:** Auth0  
**API:** Google Places
