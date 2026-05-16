# Trip Planner - Setup Instructions

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven (or use included Maven wrapper)

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <your-repo-url>
cd Trip-Planner
```

### 2. Configure Environment Variables

```bash
# Copy example env file
cp .env.example .env

# Generate JWT secret
pwsh ./generate-jwt-secret.ps1

# Edit .env and add your credentials
```

### 3. Setup Auth0 (Required)

Follow the detailed guide in [AUTH0_SETUP.md](./AUTH0_SETUP.md)

**Quick steps:**
1. Create Auth0 account at https://auth0.com
2. Create a "Regular Web Application"
3. Configure callback URLs:
   - Callback: `http://localhost:8080/login/oauth2/code/auth0`
   - Logout: `http://localhost:5173`
   - Web Origins: `http://localhost:5173`
4. Copy Domain, Client ID, and Client Secret to `.env`

### 4. Start Database

```bash
docker-compose up -d
```

### 5. Start Backend

```bash
# Build the project
./mvnw clean install -DskipTests

# Run the application
java -jar presentation/target/presentation-1.0.0-SNAPSHOT.jar
```

**Or with Maven:**
```bash
cd presentation
mvn spring-boot:run
```

Backend will start on: http://localhost:8080

### 6. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend will start on: http://localhost:5173

## 🔐 Authentication Setup

### Create Test Users

**Option 1: Auth0 Dashboard**
1. Go to User Management → Users
2. Click "Create User"
3. Enter email and password

**Option 2: Social Login**
1. Go to Authentication → Social
2. Enable Google/Facebook
3. Configure provider credentials

## 📊 Database Access

### PostgreSQL
- **Host**: localhost:5432
- **Database**: tripplanner
- **Username**: tripplanner_user
- **Password**: tripplanner_pass

### Connect with psql:
```bash
docker exec -it mario-db-1 psql -U tripplanner_user -d tripplanner
```

## 🧪 Testing

### Run Backend Tests
```bash
./mvnw test
```

### Run Frontend Tests
```bash
cd frontend
npm test
```

## 📁 Project Structure

```
Trip-Planner/
├── domain/              # Domain entities and DTOs
├── data-access/         # Repositories (Data Access Layer)
├── business/            # Services (Business Logic Layer)
├── presentation/        # Controllers (Presentation Layer)
├── frontend/            # React Vite frontend
├── docker-compose.yml   # PostgreSQL database
└── .kiro/specs/         # Project specifications
```

## 🛠️ Development

### Backend Architecture

**3-Tier Layered Architecture:**
```
Presentation Layer (Controllers)
        ↓
Business Logic Layer (Services)
        ↓
Data Access Layer (Repositories)
        ↓
Database (PostgreSQL)
```

### API Endpoints

**Authentication:**
- `GET /api/auth/login` - Initiate Auth0 login
- `POST /api/auth/logout` - Logout

**Users:**
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile

**Trips:**
- `GET /api/trips` - List all trips
- `GET /api/trips/{id}` - Get trip details
- `POST /api/trips` - Create new trip
- `PUT /api/trips/{id}` - Update trip
- `DELETE /api/trips/{id}` - Delete trip

**Activities:**
- `GET /api/trips/{tripId}/activities` - List activities
- `POST /api/trips/{tripId}/activities` - Create activity
- `PUT /api/activities/{id}` - Update activity
- `DELETE /api/activities/{id}` - Delete activity

**Expenses:**
- `GET /api/trips/{tripId}/expenses` - List expenses
- `POST /api/trips/{tripId}/expenses` - Create expense
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense

**Participants:**
- `GET /api/trips/{tripId}/participants` - List participants
- `POST /api/trips/{tripId}/participants` - Add participant
- `DELETE /api/participants/{id}` - Remove participant

**Locations:**
- `GET /api/locations/search?query={query}` - Search locations
- `GET /api/locations/{id}` - Get location details
- `POST /api/locations` - Create location

**Categories:**
- `GET /api/categories` - List all categories

## 🐛 Troubleshooting

### Backend won't start
- Check if PostgreSQL is running: `docker ps`
- Verify environment variables are set
- Check logs for errors

### Frontend can't connect to backend
- Verify backend is running on port 8080
- Check CORS configuration in WebConfig.java
- Verify proxy settings in vite.config.ts

### Auth0 errors
- "Invalid redirect_uri": Check callback URL matches exactly
- "Client authentication failed": Verify CLIENT_ID and CLIENT_SECRET
- "CORS error": Add frontend URL to Allowed Web Origins

### Database connection errors
- Ensure Docker container is running
- Check database credentials in application.properties
- Verify PostgreSQL port 5432 is not in use

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/)
- [Auth0 Documentation](https://auth0.com/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run tests
4. Submit a pull request

## 📝 License

See LICENSE file for details.
