# Implementation Plan: Trip Planner Application

## Overview

This implementation plan breaks down the Trip Planner application into discrete coding tasks following a 3-tier architecture with a 3-layer backend structure. The application will be built using Spring Boot (Java) for the backend, Spring Data JPA with Hibernate for data access, PostgreSQL for the database, React with Vite and TypeScript for the frontend, and will include comprehensive unit and integration testing across all layers.

The implementation follows a bottom-up approach: Database → Data Access Layer → Business Logic Layer → Interface Layer → Frontend → Testing.

## Tasks

- [ ] 1. Project Setup and Architecture Foundation
  - [ ] 1.1 Create Spring Boot project structure with Maven modules
    - Create multi-module Maven project with modules: trip-planner-presentation (controllers), trip-planner-business (services), trip-planner-data-access (repositories), trip-planner-domain (entities/DTOs)
    - Configure module dependencies following layered architecture (presentation → business → data-access → domain)
    - Add Maven dependencies: Spring Boot Starter Web, Spring Boot Starter Data JPA, PostgreSQL Driver, Spring Boot Starter Security, Spring Boot Starter OAuth2 Client, MapStruct, Lombok, JUnit 5, Mockito, Spring Boot Starter Test
    - Configure application.properties with PostgreSQL connection and server port
    - _Requirements: 15.1, 15.2, 15.3, 15.11_
  
  - [-] 1.2 Create React frontend project with Vite
    - Initialize React project with TypeScript using Vite: npm create vite@latest trip-planner-frontend -- --template react-ts
    - Install dependencies: npm install react-router-dom axios @mui/material @emotion/react @emotion/styled @tanstack/react-query
    - Set up folder structure: src/components, src/pages, src/services, src/hooks, src/types, src/utils
    - Configure TypeScript with strict mode in tsconfig.json
    - Configure Vite proxy for backend API in vite.config.ts
    - _Requirements: 19.1_
  
  - [-] 1.3 Configure development environment and tooling
    - Set up application.properties with PostgreSQL connection: spring.datasource.url=jdbc:postgresql://localhost:5432/tripplanner
    - Configure CORS in Spring Boot WebMvcConfigurer for frontend-backend communication (allow http://localhost:5173)
    - Set up ESLint and Prettier for frontend: npm install -D eslint prettier eslint-config-prettier
    - Configure .gitignore for both Maven project and Vite frontend
    - Create docker-compose.yml for PostgreSQL database container
    - _Requirements: 15.1, 16.7_

- [ ] 2. Database Layer and Entity Framework Setup
  - [~] 2.1 Create domain entities in TripPlanner.Domain
    - Create User entity with properties: KorisnikId, Ime, Prezime, Email, OauthProvider, OauthId
    - Create Trip entity with properties: PutovanjeId, Naziv, Opis, DatumPoc, DatumKraj, UkTrosak
    - Create Activity entity with properties: AktivnostId, Naziv, Opis, DatumVrijemePoc, DatumVrijemeKraj, PutovanjeId, LokacijaId
    - Create Location entity with properties: LokacijaId, Naziv, Adresa, Grad, Drzava
    - Create Expense entity with properties: TrosakId, Iznos, Opis, Datum, PutovanjeId
    - Create Participant entity with properties: SudionikId, Uloga, PutovanjeId, KorisnikId
    - Create Category entity with properties: KategorijaId, Naziv, Opis
    - _Requirements: 14.1, 14.2_
  
  - [~] 2.2 Configure Entity Framework DbContext and relationships
    - Create TripPlannerDbContext in TripPlanner.DataAccess
    - Configure entity relationships: Trip has many Activities, Expenses, Participants; Activity has one Location; Activity has many Categories (many-to-many)
    - Configure cascade delete: Trip deletion cascades to Activities, Expenses, Participants
    - Configure unique constraints: User.Email must be unique
    - Configure decimal precision for UkTrosak and Iznos (10,2)
    - _Requirements: 14.2, 14.3, 14.4, 14.5_
  
  - [~] 2.3 Create and apply database migrations
    - Generate initial migration with all entities and relationships
    - Create database seed data for predefined Categories: Kultura, Gastronomija, Priroda, Noćni život
    - Apply migration to create database schema
    - Verify database structure and constraints
    - _Requirements: 10.1, 14.1_
  
  - [~] 2.4 Create DTOs in TripPlanner.Domain
    - Create CreateTripDTO, UpdateTripDTO, TripResponseDTO
    - Create CreateActivityDTO, UpdateActivityDTO, ActivityResponseDTO
    - Create CreateExpenseDTO, UpdateExpenseDTO, ExpenseResponseDTO
    - Create AddParticipantDTO, UpdateParticipantDTO, ParticipantResponseDTO
    - Create CreateLocationDTO, LocationResponseDTO
    - Create UserResponseDTO, UpdateUserDTO
    - Create OAuthUserData, LocationSearchResult, MapsPlaceData
    - _Requirements: 3.1, 3.2, 6.1, 6.2, 8.1, 11.1_

- [ ] 3. Data Access Layer Implementation
  - [~] 3.1 Implement UserRepository
    - Create IUserRepository interface with methods: FindByIdAsync, FindByEmailAsync, FindByOAuthIdAsync, CreateAsync, UpdateAsync, DeleteAsync
    - Implement UserRepository with Entity Framework queries
    - Add unit tests for UserRepository using in-memory database
    - _Requirements: 1.3, 2.1, 14.5, 15.8_
  
  - [~] 3.2 Implement TripRepository
    - Create ITripRepository interface with methods: FindByIdAsync, FindByUserIdAsync, CreateAsync, UpdateAsync, DeleteAsync, UpdateTotalExpenseAsync
    - Implement TripRepository with EF queries including JOIN to participants
    - Implement ORDER BY DatumPoc DESC for trip listing
    - Add unit tests for TripRepository
    - _Requirements: 3.1, 4.1, 13.1, 13.4, 15.8_
  
  - [~] 3.3 Implement ActivityRepository
    - Create IActivityRepository interface with methods: FindByIdAsync, FindByTripIdAsync, CreateAsync, UpdateAsync, DeleteAsync, AssignCategoryAsync, RemoveCategoryAsync, FindCategoriesByActivityIdAsync
    - Implement ActivityRepository with EF queries including JOINs to Location and Categories
    - Implement ORDER BY DatumVrijemePoc for activity listing
    - Add unit tests for ActivityRepository
    - _Requirements: 6.1, 6.6, 7.1, 10.2, 13.5, 15.8_
  
  - [~] 3.4 Implement ExpenseRepository
    - Create IExpenseRepository interface with methods: FindByIdAsync, FindByTripIdAsync, CreateAsync, UpdateAsync, DeleteAsync, SumByTripIdAsync
    - Implement ExpenseRepository with EF queries including SUM aggregate
    - Add unit tests for ExpenseRepository
    - _Requirements: 11.1, 11.4, 11.5, 12.1, 15.8_
  
  - [~] 3.5 Implement ParticipantRepository
    - Create IParticipantRepository interface with methods: FindByIdAsync, FindByTripIdAsync, FindByTripAndUserAsync, CreateAsync, UpdateAsync, DeleteAsync, CountOrganizersByTripIdAsync
    - Implement ParticipantRepository with EF queries including JOINs to User and Trip
    - Add unit tests for ParticipantRepository
    - _Requirements: 5.1, 5.5, 5.7, 15.8_
  
  - [~] 3.6 Implement LocationRepository and CategoryRepository
    - Create ILocationRepository interface with methods: FindByIdAsync, FindByNameAndCityAsync, CreateAsync, UpdateAsync, DeleteAsync
    - Implement LocationRepository with EF queries
    - Create ICategoryRepository interface with methods: FindAllAsync, FindByIdAsync, FindByIdsAsync
    - Implement CategoryRepository with EF queries
    - Add unit tests for both repositories
    - _Requirements: 8.1, 8.3, 8.5, 10.1, 15.8_

- [ ] 4. Business Logic Layer Implementation
  - [~] 4.1 Implement AuthService with OAuth logic
    - Create IAuthService interface with methods: InitiateOAuthFlow, HandleOAuthCallback, CreateOrUpdateUser, GenerateSessionToken, ValidateSessionToken
    - Implement AuthService with OAuth provider validation (Google, Facebook)
    - Implement JWT token generation and validation
    - Implement user creation/update from OAuth data
    - Add unit tests for AuthService using Moq for UserRepository
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 1.7, 15.5, 15.6_
  
  - [~] 4.2 Implement UserService
    - Create IUserService interface with methods: GetUserByIdAsync, GetUserByEmailAsync, UpdateUserProfileAsync, ValidateEmailUniquenessAsync
    - Implement UserService with email uniqueness validation
    - Implement profile data format validation
    - Add unit tests for UserService
    - _Requirements: 2.1, 2.3, 2.4, 2.5, 15.6_
  
  - [~] 4.3 Implement TripService with business rules
    - Create ITripService interface with methods: CreateTripAsync, GetTripByIdAsync, ListUserTripsAsync, UpdateTripAsync, DeleteTripAsync, ValidateUserIsOrganizerAsync, ValidateUserIsParticipantAsync, RecalculateTotalExpenseAsync
    - Implement date validation: end date >= start date
    - Implement authorization checks: only organizers can update/delete
    - Implement automatic organizer assignment on trip creation
    - Implement cascade delete logic
    - Add unit tests for TripService covering all business rules
    - _Requirements: 3.1, 3.3, 3.4, 3.5, 4.1, 4.2, 4.4, 4.6, 13.3, 15.6_
  
  - [~] 4.4 Implement ActivityService with scheduling logic
    - Create IActivityService interface with methods: CreateActivityAsync, GetActivityByIdAsync, ListTripActivitiesAsync, UpdateActivityAsync, DeleteActivityAsync, AssignCategoriesAsync, RemoveCategoryAsync
    - Implement datetime validation: end datetime > start datetime
    - Implement participant authorization checks
    - Implement category assignment logic (many-to-many)
    - Add unit tests for ActivityService covering all business rules
    - _Requirements: 6.1, 6.2, 6.4, 6.5, 6.6, 7.1, 7.2, 7.3, 7.4, 10.2, 10.3, 15.6_
  
  - [~] 4.5 Implement ExpenseService with total calculation
    - Create IExpenseService interface with methods: CreateExpenseAsync, GetExpenseByIdAsync, ListTripExpensesAsync, UpdateExpenseAsync, DeleteExpenseAsync
    - Implement expense amount validation (positive, 2 decimal places)
    - Implement trip total recalculation on create/update/delete
    - Implement participant authorization checks
    - Add unit tests for ExpenseService covering total calculation logic
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 12.3, 12.4, 12.5, 15.6_
  
  - [~] 4.6 Implement ParticipantService with role management
    - Create IParticipantService interface with methods: AddParticipantAsync, ListTripParticipantsAsync, UpdateParticipantRoleAsync, RemoveParticipantAsync, ValidateLastOrganizerRemovalAsync
    - Implement organizer-only authorization for participant management
    - Implement user existence validation by email
    - Implement last organizer removal prevention
    - Implement duplicate participant prevention
    - Add unit tests for ParticipantService covering all business rules
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 15.6_
  
  - [~] 4.7 Implement LocationService with Google Maps integration
    - Create ILocationService interface with methods: SearchLocationsAsync, CreateLocationFromMapsResultAsync, CreateLocationManuallyAsync, GetLocationByIdAsync
    - Create GoogleMapsAPIClient for Maps API communication
    - Implement location search with Maps API error handling
    - Implement fallback to manual location creation
    - Add unit tests for LocationService with mocked Maps API client
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3, 9.4, 9.5, 15.6_
  
  - [~] 4.8 Implement CategoryService
    - Create ICategoryService interface with methods: ListAllCategoriesAsync, GetCategoryByIdAsync
    - Implement CategoryService as read-only service
    - Add unit tests for CategoryService
    - _Requirements: 10.1, 10.4, 15.6_

- [ ] 5. Interface Layer (API Controllers) Implementation
  - [~] 5.1 Implement AuthController
    - Create AuthController with endpoints: GET /api/auth/google, GET /api/auth/facebook, GET /api/auth/callback, POST /api/auth/logout
    - Implement OAuth flow initiation and callback handling
    - Implement JWT token response formatting
    - Add input validation and error handling
    - Add unit tests for AuthController using Moq for AuthService
    - _Requirements: 1.1, 1.5, 15.4, 15.5, 16.1, 16.2, 16.3, 16.6, 16.7, 16.8_
  
  - [~] 5.2 Implement UserController
    - Create UserController with endpoints: GET /api/users/profile, PUT /api/users/profile
    - Implement authentication middleware requirement
    - Implement request parsing and response formatting
    - Add input validation using FluentValidation
    - Add unit tests for UserController
    - _Requirements: 2.3, 2.4, 15.4, 15.5, 16.1, 16.4, 16.6, 16.7, 16.8, 17.1_
  
  - [~] 5.3 Implement TripController
    - Create TripController with endpoints: GET /api/trips, GET /api/trips/:id, POST /api/trips, PUT /api/trips/:id, DELETE /api/trips/:id
    - Implement authentication and authorization checks
    - Implement request validation for date ranges
    - Implement proper HTTP status codes (200, 201, 400, 403, 404)
    - Add unit tests for TripController covering all endpoints
    - _Requirements: 3.1, 3.5, 4.1, 4.2, 4.3, 4.5, 13.1, 13.2, 13.3, 15.4, 15.5, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 17.2, 17.3, 17.4_
  
  - [~] 5.4 Implement ActivityController
    - Create ActivityController with endpoints: GET /api/trips/:tripId/activities, GET /api/activities/:id, POST /api/trips/:tripId/activities, PUT /api/activities/:id, DELETE /api/activities/:id
    - Implement authentication and participant authorization
    - Implement datetime validation
    - Implement category assignment handling
    - Add unit tests for ActivityController
    - _Requirements: 6.1, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 10.2, 10.5, 13.5, 15.4, 15.5, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_
  
  - [~] 5.5 Implement ExpenseController
    - Create ExpenseController with endpoints: GET /api/trips/:tripId/expenses, GET /api/expenses/:id, POST /api/trips/:tripId/expenses, PUT /api/expenses/:id, DELETE /api/expenses/:id
    - Implement authentication and participant authorization
    - Implement amount validation
    - Implement proper status codes
    - Add unit tests for ExpenseController
    - _Requirements: 11.1, 11.2, 12.1, 12.2, 12.3, 12.4, 15.4, 15.5, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_
  
  - [~] 5.6 Implement ParticipantController
    - Create ParticipantController with endpoints: GET /api/trips/:tripId/participants, POST /api/trips/:tripId/participants, PUT /api/participants/:id, DELETE /api/participants/:id
    - Implement organizer-only authorization
    - Implement email validation and user lookup
    - Implement last organizer removal prevention
    - Add unit tests for ParticipantController
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 15.4, 15.5, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 17.4_
  
  - [~] 5.7 Implement LocationController and CategoryController
    - Create LocationController with endpoints: GET /api/locations/search, GET /api/locations/:id, POST /api/locations
    - Implement Google Maps API search integration
    - Create CategoryController with endpoint: GET /api/categories
    - Add unit tests for both controllers
    - _Requirements: 8.1, 8.2, 9.1, 9.2, 9.3, 10.1, 10.4, 15.4, 15.5, 16.1, 16.2, 16.6, 16.7, 16.8_
  
  - [~] 5.8 Implement global error handling middleware
    - Create exception handling middleware for catching all errors
    - Implement error response formatting with descriptive messages
    - Implement error logging with timestamps and context
    - Prevent exposure of internal error details in production
    - Add tests for error handling scenarios
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5_

- [~] 6. Checkpoint - Backend API Complete
  - Ensure all backend tests pass
  - Verify API endpoints with Postman/Insomnia
  - Check database migrations and seed data
  - Ask the user if questions arise


- [ ] 7. Frontend Implementation - Authentication and Layout
  - [~] 7.1 Create authentication service and context
    - Create AuthService with methods: loginWithGoogle, loginWithFacebook, logout, getCurrentUser
    - Create AuthContext for managing authentication state
    - Implement JWT token storage in localStorage
    - Create useAuth hook for accessing authentication state
    - _Requirements: 1.1, 1.6, 1.7, 19.3_
  
  - [~] 7.2 Implement login page with OAuth buttons
    - Create LoginPage component with Google and Facebook login buttons
    - Implement OAuth redirect flow
    - Implement callback handling and token storage
    - Add error message display for authentication failures
    - _Requirements: 1.1, 1.5, 19.3_
  
  - [~] 7.3 Create main layout and navigation
    - Create AppLayout component with navigation menu
    - Implement navigation links: Trips, Profile, Logout
    - Create ProtectedRoute component for authenticated routes
    - Implement conditional rendering based on authentication state
    - _Requirements: 19.2, 19.3, 19.4_
  
  - [~] 7.4 Implement user profile page
    - Create ProfilePage component displaying user information
    - Create ProfileEditForm for updating first name and last name
    - Implement form validation and error display
    - Implement API integration for profile updates
    - _Requirements: 2.3, 2.4, 19.5, 19.6_

- [ ] 8. Frontend Implementation - Master-Detail (Trip and Activities)
  - [~] 8.1 Create Trip list page with search
    - Create TripListPage component displaying all user trips
    - Implement trip cards showing name, dates, and total expense
    - Implement search/filter functionality
    - Implement "Create New Trip" button
    - Add loading states and error handling
    - _Requirements: 13.1, 13.2, 13.4, 19.4, 19.5_
  
  - [~] 8.2 Implement Trip creation form
    - Create CreateTripForm component with fields: name, description, start date, end date
    - Implement date validation: end date >= start date
    - Implement form validation with inline error messages
    - Implement API integration for trip creation
    - Display success confirmation on creation
    - _Requirements: 3.1, 3.2, 3.5, 3.6, 19.5, 19.6_
  
  - [~] 8.3 Implement Trip detail page (Master view)
    - Create TripDetailPage component displaying trip information
    - Display trip name, description, dates, and total expense
    - Show tabs for Activities, Expenses, and Participants
    - Implement Edit and Delete buttons (organizer only)
    - Implement navigation to activity details
    - _Requirements: 4.1, 4.5, 13.3, 19.5_
  
  - [~] 8.4 Implement Trip edit and delete functionality
    - Create EditTripForm component with pre-filled data
    - Implement date validation on edit
    - Implement delete confirmation dialog
    - Implement API integration for update and delete
    - Handle authorization errors (non-organizer attempts)
    - _Requirements: 4.1, 4.2, 4.3, 4.6, 19.5, 19.6_
  
  - [~] 8.5 Implement Activity list within Trip (Detail view)
    - Create ActivityList component displaying activities for a trip
    - Display activity cards with name, datetime, location, and categories
    - Implement sorting by start datetime
    - Implement "Add Activity" button
    - Implement navigation to activity edit
    - _Requirements: 6.1, 7.1, 10.4, 13.5, 19.5_
  
  - [~] 8.6 Implement Activity creation form with Location dropdown
    - Create CreateActivityForm component with fields: name, description, start datetime, end datetime, location, categories
    - Implement Location dropdown populated from API
    - Implement Category multi-select with checkboxes
    - Implement datetime validation: end > start
    - Implement Google Maps location search integration
    - Add manual location creation fallback
    - _Requirements: 6.1, 6.2, 6.4, 6.5, 6.6, 8.1, 8.2, 9.1, 9.2, 9.3, 10.2, 10.3, 19.5, 19.6_
  
  - [~] 8.7 Implement Activity edit and delete functionality
    - Create EditActivityForm component with pre-filled data
    - Implement datetime validation on edit
    - Implement location dropdown update
    - Implement category multi-select update
    - Implement delete functionality
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 10.5, 19.5, 19.6_

- [ ] 9. Frontend Implementation - Code List (Category Management)
  - [~] 9.1 Create Category list page
    - Create CategoryListPage component displaying all categories
    - Display category name and description in a table or cards
    - Implement search functionality
    - Note: Categories are read-only (no create/edit/delete for predefined categories)
    - _Requirements: 10.1, 10.4, 19.5_

- [ ] 10. Frontend Implementation - Expense and Participant Management
  - [~] 10.1 Implement Expense list within Trip
    - Create ExpenseList component displaying expenses for a trip
    - Display expense cards with amount, description, and date
    - Display total expense sum prominently
    - Implement "Add Expense" button
    - _Requirements: 11.1, 12.1, 12.2, 19.5_
  
  - [~] 10.2 Implement Expense creation and edit forms
    - Create CreateExpenseForm component with fields: amount, description, date
    - Implement amount validation (positive, 2 decimals)
    - Create EditExpenseForm component
    - Implement delete functionality
    - Display updated total expense after operations
    - _Requirements: 11.1, 11.2, 11.3, 12.3, 12.4, 12.5, 19.5, 19.6_
  
  - [~] 10.3 Implement Participant list within Trip
    - Create ParticipantList component displaying participants for a trip
    - Display participant name, email, and role
    - Implement "Add Participant" button (organizer only)
    - Implement role update and remove buttons (organizer only)
    - _Requirements: 5.1, 5.5, 19.5_
  
  - [~] 10.4 Implement Participant add and manage functionality
    - Create AddParticipantForm component with fields: email, role
    - Implement email validation and user lookup
    - Implement role dropdown (organizer/sudionik)
    - Implement participant removal with last organizer prevention
    - Display error messages for validation failures
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.6, 5.7, 19.5, 19.6_

- [~] 11. Checkpoint - Frontend Complete
  - Ensure all frontend components render correctly
  - Test all CRUD operations through the UI
  - Verify form validations and error displays
  - Test authentication flow end-to-end
  - Ask the user if questions arise

- [ ] 12. Integration Testing Implementation
  - [~] 12.1 Set up integration test infrastructure
    - Create test database configuration for integration tests
    - Set up WebApplicationFactory for API testing
    - Configure test data seeding and cleanup
    - Create base integration test class with common setup
    - _Requirements: 14.1, 15.1_
  
  - [~] 12.2 Write integration tests for Trip flow
    - Test complete flow: Create trip → Add activity → Add expense → Verify total
    - Test trip authorization: Non-organizer cannot delete trip
    - Test cascade delete: Deleting trip removes activities, expenses, participants
    - Test trip listing with participant filtering
    - _Requirements: 3.1, 3.3, 4.4, 6.1, 11.4, 13.1, 14.3, 17.3, 17.4_
  
  - [~] 12.3 Write integration tests for Activity flow
    - Test complete flow: Create activity → Assign categories → Update location → Verify
    - Test datetime validation across layers
    - Test participant authorization for activity management
    - Test activity listing sorted by datetime
    - _Requirements: 6.1, 6.4, 6.6, 7.1, 7.4, 10.2, 13.5_
  
  - [~] 12.4 Write integration tests for Expense flow
    - Test complete flow: Create expense → Verify trip total → Update expense → Verify recalculation → Delete expense → Verify total
    - Test expense amount validation
    - Test participant authorization for expense management
    - _Requirements: 11.1, 11.4, 11.5, 11.6, 12.3, 12.4, 12.5_
  
  - [~] 12.5 Write integration tests for Participant flow
    - Test complete flow: Add participant by email → Update role → Verify permissions → Remove participant
    - Test last organizer removal prevention
    - Test duplicate participant prevention
    - Test organizer-only authorization
    - _Requirements: 5.1, 5.3, 5.4, 5.5, 5.6, 5.7, 17.4_
  
  - [~] 12.6 Write integration tests for Authentication flow
    - Test OAuth callback handling and user creation
    - Test JWT token generation and validation
    - Test protected endpoint access with valid/invalid tokens
    - Test user profile update flow
    - _Requirements: 1.1, 1.2, 1.3, 2.4, 17.1, 17.2, 17.5_
  
  - [~] 12.7 Write integration tests for Location and Category
    - Test location creation from Maps API data
    - Test manual location creation fallback
    - Test location reuse across activities
    - Test category listing and assignment
    - _Requirements: 8.1, 8.3, 8.5, 9.1, 9.4, 9.5, 10.1, 10.2_

- [ ] 13. Frontend Component Testing
  - [ ]* 13.1 Write unit tests for authentication components
    - Test LoginPage OAuth button clicks and redirects
    - Test AuthContext state management
    - Test ProtectedRoute authorization logic
    - Test token storage and retrieval
    - _Requirements: 1.1, 19.3_
  
  - [ ]* 13.2 Write unit tests for Trip components
    - Test TripListPage rendering and search
    - Test CreateTripForm validation and submission
    - Test TripDetailPage data display and navigation
    - Test EditTripForm validation and update
    - _Requirements: 3.1, 3.5, 4.1, 13.1, 19.5, 19.6_
  
  - [ ]* 13.3 Write unit tests for Activity components
    - Test ActivityList rendering and sorting
    - Test CreateActivityForm validation (datetime, location, categories)
    - Test EditActivityForm update and delete
    - Test Location dropdown and Maps search integration
    - _Requirements: 6.1, 6.4, 7.1, 7.4, 9.1, 10.2, 19.5, 19.6_
  
  - [ ]* 13.4 Write unit tests for Expense and Participant components
    - Test ExpenseList rendering and total calculation display
    - Test CreateExpenseForm amount validation
    - Test ParticipantList rendering with role-based buttons
    - Test AddParticipantForm email validation
    - _Requirements: 5.1, 11.1, 12.1, 19.5, 19.6_

- [ ] 14. Final Integration and Deployment Preparation
  - [~] 14.1 Configure OAuth providers (Google and Facebook)
    - Register application with Google OAuth Console
    - Register application with Facebook Developer Portal
    - Configure redirect URIs for both providers
    - Update appsettings.json with client IDs and secrets
    - Test OAuth flow with real providers
    - _Requirements: 1.1, 1.6, 1.7_
  
  - [~] 14.2 Configure Google Maps API
    - Enable Google Maps Places API and Geocoding API
    - Obtain API key and configure restrictions
    - Update appsettings.json with Maps API key
    - Test location search functionality
    - _Requirements: 9.1, 9.2, 9.3_
  
  - [~] 14.3 Set up production database
    - Configure production database connection string
    - Apply migrations to production database
    - Seed predefined categories
    - Configure database backup strategy
    - _Requirements: 10.1, 14.1_
  
  - [~] 14.4 Create API documentation
    - Document all API endpoints with request/response examples
    - Create Swagger/OpenAPI specification
    - Document authentication requirements
    - Document error response formats
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7_
  
  - [~] 14.5 Build and deploy application
    - Build frontend production bundle
    - Configure frontend environment variables for API URL
    - Build backend release configuration
    - Deploy backend API to hosting service
    - Deploy frontend to static hosting or CDN
    - Verify deployment and test end-to-end
    - _Requirements: 15.1, 19.1_

- [~] 15. Final Checkpoint - Complete Application
  - Run all unit tests and verify 100% pass rate
  - Run all integration tests and verify 100% pass rate
  - Test complete user journey: Register → Create trip → Add activities → Add expenses → Invite participants
  - Verify all assignment requirements are met: Master-Detail CRUD, Code List, 3-layer testing
  - Ask the user if questions arise

## Notes

- Tasks marked with `*` are optional frontend component tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- The implementation follows a bottom-up approach: Database → Data Access → Business Logic → Interface → Frontend
- All three layers (Presentation/Controllers, Business/Services, Data Access/Repositories) have dedicated unit tests
- Integration tests prove connectivity between all layers
- The Master-Detail pattern is implemented with Trip (master) and Activities (details)
- The Code List pattern is implemented with Category management (read-only predefined categories)
- Complex validation includes: date range validation, datetime overlap checks, last organizer prevention, email uniqueness
- Checkpoints ensure incremental validation at major milestones


## Task Dependency Graph

```json
{
  "waves": [
    {
      "id": 0,
      "tasks": ["1.1", "1.2", "1.3"]
    },
    {
      "id": 1,
      "tasks": ["2.1", "2.4"]
    },
    {
      "id": 2,
      "tasks": ["2.2"]
    },
    {
      "id": 3,
      "tasks": ["2.3"]
    },
    {
      "id": 4,
      "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5", "3.6"]
    },
    {
      "id": 5,
      "tasks": ["4.1", "4.2", "4.8"]
    },
    {
      "id": 6,
      "tasks": ["4.3", "4.7"]
    },
    {
      "id": 7,
      "tasks": ["4.4", "4.5", "4.6"]
    },
    {
      "id": 8,
      "tasks": ["5.1", "5.2", "5.7"]
    },
    {
      "id": 9,
      "tasks": ["5.3", "5.5", "5.6"]
    },
    {
      "id": 10,
      "tasks": ["5.4"]
    },
    {
      "id": 11,
      "tasks": ["5.8"]
    },
    {
      "id": 12,
      "tasks": ["7.1"]
    },
    {
      "id": 13,
      "tasks": ["7.2", "7.3"]
    },
    {
      "id": 14,
      "tasks": ["7.4", "8.1", "9.1"]
    },
    {
      "id": 15,
      "tasks": ["8.2", "8.3"]
    },
    {
      "id": 16,
      "tasks": ["8.4", "8.5", "10.1", "10.3"]
    },
    {
      "id": 17,
      "tasks": ["8.6", "10.2", "10.4"]
    },
    {
      "id": 18,
      "tasks": ["8.7"]
    },
    {
      "id": 19,
      "tasks": ["12.1"]
    },
    {
      "id": 20,
      "tasks": ["12.2", "12.3", "12.4", "12.5", "12.6", "12.7"]
    },
    {
      "id": 21,
      "tasks": ["13.1", "13.2", "13.3", "13.4"]
    },
    {
      "id": 22,
      "tasks": ["14.1", "14.2"]
    },
    {
      "id": 23,
      "tasks": ["14.3", "14.4"]
    },
    {
      "id": 24,
      "tasks": ["14.5"]
    }
  ]
}
```
