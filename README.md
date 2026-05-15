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
- H2 embedded database (file-based)

## Project Structure

```
trip-planner-parent/
├── domain/                       # Domain entities and DTOs
│   └── src/main/java/com/tripplanner/domain/
│       ├── entity/               # JPA entities
│       └── dto/                  # Data Transfer Objects
│
├── data-access/                  # Data Access Layer
│   └── src/main/java/com/tripplanner/dataaccess/
│       └── repository/           # Spring Data JPA repositories
│
├── business/                     # Business Logic Layer
│   └── src/main/java/com/tripplanner/business/
│       └── service/              # Business services
│
└── presentation/                 # Presentation Layer
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
- **Database**: H2 (embedded, file-based)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security + OAuth2 Client
- **Authentication**: JWT tokens
- **Mapping**: MapStruct
- **Utilities**: Lombok

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database
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
- Node.js 18+ (for frontend)

## Database

The application uses H2 embedded database which requires no separate installation. The database file will be automatically created in the `./data/` directory when you first run the application.

You can access the H2 console at `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:file:./data/tripplanner`
- Username: `sa`
- Password: (leave empty)

## Building the Project

```bash
# Build all modules
mvn clean install

# Run the application
cd presentation
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Module Dependencies

Following the layered architecture:
- `presentation` → `business` → `data-access` → `domain`
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
cd business
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.