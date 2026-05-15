# Trip Planner Application

A web-based travel planning application built with Spring Boot following a 3-tier architecture with a 3-layer backend structure.

## Architecture

The application follows a strict layered architecture pattern:

### Tier 1: Frontend Layer (Presentation Tier)
- Web client built with React + TypeScript + Vite
- Communicates with backend through REST API

### Tier 2: Backend Layer (Application Tier)
- **Presentation Layer** (`trip-planner-presentation`): REST API Controllers
- **Business Logic Layer** (`trip-planner-business`): Services with domain logic
- **Data Access Layer** (`trip-planner-data-access`): Repositories for database operations

### Tier 3: Database Layer (Data Tier)
- PostgreSQL relational database

## Project Structure

```
trip-planner-parent/
├── trip-planner-domain/          # Domain entities and DTOs
│   └── src/main/java/com/tripplanner/domain/
│       ├── entity/               # JPA entities
│       └── dto/                  # Data Transfer Objects
│
├── trip-planner-data-access/     # Data Access Layer
│   └── src/main/java/com/tripplanner/dataaccess/
│       └── repository/           # Spring Data JPA repositories
│
├── trip-planner-business/        # Business Logic Layer
│   └── src/main/java/com/tripplanner/business/
│       └── service/              # Business services
│
└── trip-planner-presentation/    # Presentation Layer
    └── src/main/java/com/tripplanner/
        ├── TripPlannerApplication.java  # Main application
        └── presentation/
            └── controller/       # REST API controllers
```

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security + OAuth2 Client
- **Authentication**: JWT tokens
- **Mapping**: MapStruct
- **Utilities**: Lombok

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Client
- MapStruct
- Lombok
- JUnit 5
- Mockito
- Spring Boot Starter Test

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- Node.js 18+ (for frontend)

## Database Setup

1. Install PostgreSQL
2. Create database:
```sql
CREATE DATABASE tripplanner;
```

3. Update credentials in `trip-planner-presentation/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tripplanner
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Building the Project

```bash
# Build all modules
mvn clean install

# Run the application
cd trip-planner-presentation
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Module Dependencies

Following the layered architecture:
- `trip-planner-presentation` → `trip-planner-business` → `trip-planner-data-access` → `trip-planner-domain`
- Each layer only communicates with its adjacent layer
- No layer skipping allowed

## OAuth Configuration

Before running the application, configure OAuth providers in `application.properties`:

1. **Google OAuth**: Register at [Google Cloud Console](https://console.cloud.google.com/)
2. **Facebook OAuth**: Register at [Facebook Developers](https://developers.facebook.com/)

Update the client IDs and secrets in the configuration file.

## Features

- User authentication via Google/Facebook OAuth
- Trip creation and management
- Activity scheduling with locations
- Expense tracking
- Participant management with roles
- Google Maps integration for locations
- Activity categorization

## API Documentation

Once running, API documentation will be available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html` (to be configured)

## Testing

```bash
# Run all tests
mvn test

# Run tests for specific module
cd trip-planner-business
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.