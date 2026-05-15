# Requirements Document

## Introduction

The Trip Planner is a web-based travel planning application that enables users to create, manage, and organize travel plans with activities, expenses, and participants. The system integrates OAuth authentication for secure user access and Google Maps API for location services, providing a comprehensive platform for collaborative trip planning.

## Glossary

- **System**: The Trip Planner web application
- **Frontend Layer**: The web client tier responsible for presentation and user interaction
- **Backend Layer**: The application server tier containing Interface Layer, Business Logic Layer, and Data Access Layer
- **Interface Layer**: The backend sub-layer handling HTTP requests, responses, and routing (also called Controller Layer)
- **Business Logic Layer**: The backend sub-layer containing domain logic, business rules, and validation
- **Data Access Layer**: The backend sub-layer handling database operations and ORM queries
- **Database Layer**: The relational SQL database tier storing application data
- **User**: An authenticated person using the application
- **Trip**: A travel plan with defined start and end dates
- **Activity**: A scheduled event within a trip with specific time and location
- **Expense**: A financial record associated with a trip
- **Participant**: A user associated with a trip with a defined role
- **Organizer**: A participant with administrative privileges for a trip
- **Category**: A classification type for activities (Culture, Gastronomy, Nature, Nightlife)
- **Location**: A geographic place with address details
- **OAuth_Provider**: An external authentication service (Google or Facebook)
- **Maps_API**: Google Maps API service for location data
- **Database**: The relational database storing application data

## Requirements

### Requirement 1: User Authentication

**User Story:** As a user, I want to authenticate using my Google or Facebook account, so that I can securely access the application without creating a new password.

#### Acceptance Criteria

1. WHEN a user selects OAuth login, THE System SHALL redirect the user to the selected OAuth_Provider authorization page
2. WHEN the OAuth_Provider returns an authorization code, THE System SHALL exchange it for an access token
3. WHEN authentication succeeds, THE System SHALL create or retrieve the User record in the Database
4. THE System SHALL store the OAuth_Provider name and OAuth_Provider user identifier for each User
5. WHEN authentication fails, THE System SHALL display an error message and return the user to the login page
6. THE System SHALL support Google as an OAuth_Provider
7. THE System SHALL support Facebook as an OAuth_Provider

### Requirement 2: User Profile Management

**User Story:** As a user, I want to view and update my profile information, so that other participants can identify me in shared trips.

#### Acceptance Criteria

1. THE System SHALL store first name, last name, and email address for each User
2. WHEN a User first authenticates, THE System SHALL retrieve profile data from the OAuth_Provider
3. WHEN a User views their profile, THE System SHALL display their first name, last name, email, and OAuth_Provider
4. WHEN a User updates their first name or last name, THE System SHALL save the changes to the Database
5. THE System SHALL enforce unique email addresses across all User records

### Requirement 3: Trip Creation

**User Story:** As a user, I want to create a new trip with dates and description, so that I can start planning my travel activities.

#### Acceptance Criteria

1. WHEN a User creates a Trip, THE System SHALL require a trip name, start date, and end date
2. WHEN a User creates a Trip, THE System SHALL accept an optional description up to 500 characters
3. WHEN a User creates a Trip, THE System SHALL automatically set the creating User as an Organizer Participant
4. WHEN a User creates a Trip, THE System SHALL initialize the total expense to zero
5. IF the end date is before the start date, THEN THE System SHALL reject the Trip creation and display an error message
6. WHEN Trip creation succeeds, THE System SHALL save the Trip to the Database and display a success confirmation

### Requirement 4: Trip Management

**User Story:** As an organizer, I want to edit or delete trips I created, so that I can keep my travel plans up to date.

#### Acceptance Criteria

1. WHEN an Organizer views a Trip, THE System SHALL display options to edit or delete the Trip
2. WHEN an Organizer edits a Trip, THE System SHALL allow modification of name, description, start date, and end date
3. WHEN an Organizer deletes a Trip, THE System SHALL prompt for confirmation before deletion
4. WHEN an Organizer confirms Trip deletion, THE System SHALL remove the Trip and all associated Activities, Expenses, and Participants from the Database
5. WHEN a non-Organizer Participant views a Trip, THE System SHALL not display edit or delete options
6. IF an Organizer attempts to set an end date before the start date, THEN THE System SHALL reject the update and display an error message

### Requirement 5: Participant Management

**User Story:** As an organizer, I want to add other users to my trip and assign roles, so that we can collaborate on trip planning.

#### Acceptance Criteria

1. WHEN an Organizer adds a Participant, THE System SHALL require a User email address and a role
2. THE System SHALL support two Participant roles: "organizer" and "sudionik" (participant)
3. WHEN an Organizer adds a Participant, THE System SHALL verify the User exists in the Database by email
4. IF the User email does not exist, THEN THE System SHALL display an error message
5. WHEN a Participant is added successfully, THE System SHALL create a Participant record linking the User and Trip
6. WHEN an Organizer removes a Participant, THE System SHALL delete the Participant record from the Database
7. THE System SHALL prevent removal of the last Organizer from a Trip

### Requirement 6: Activity Creation

**User Story:** As a participant, I want to add activities to a trip with time and location details, so that we can schedule our travel itinerary.

#### Acceptance Criteria

1. WHEN a Participant creates an Activity, THE System SHALL require a name, start datetime, end datetime, and Location
2. WHEN a Participant creates an Activity, THE System SHALL accept an optional description up to 500 characters
3. WHEN a Participant creates an Activity, THE System SHALL associate the Activity with the current Trip
4. IF the end datetime is before the start datetime, THEN THE System SHALL reject the Activity creation and display an error message
5. WHEN Activity creation succeeds, THE System SHALL save the Activity to the Database
6. WHEN a Participant creates an Activity, THE System SHALL allow assignment of one or more Categories

### Requirement 7: Activity Management

**User Story:** As a participant, I want to edit or delete activities in a trip, so that I can adjust our schedule as plans change.

#### Acceptance Criteria

1. WHEN a Participant views an Activity, THE System SHALL display options to edit or delete the Activity
2. WHEN a Participant edits an Activity, THE System SHALL allow modification of name, description, start datetime, end datetime, Location, and Categories
3. WHEN a Participant deletes an Activity, THE System SHALL remove the Activity and its Category associations from the Database
4. IF a Participant attempts to set an end datetime before the start datetime, THEN THE System SHALL reject the update and display an error message

### Requirement 8: Location Management

**User Story:** As a participant, I want to specify locations for activities with address details, so that we know where each activity takes place.

#### Acceptance Criteria

1. WHEN a Participant creates a Location, THE System SHALL require a location name, city, and country
2. WHEN a Participant creates a Location, THE System SHALL accept an optional street address up to 255 characters
3. THE System SHALL store each Location with a unique identifier
4. WHEN a Participant selects a Location for an Activity, THE System SHALL link the Activity to the existing Location record
5. THE System SHALL allow reuse of Location records across multiple Activities

### Requirement 9: Google Maps Integration

**User Story:** As a participant, I want to fetch location details from Google Maps, so that I can accurately specify activity locations without manual data entry.

#### Acceptance Criteria

1. WHEN a Participant searches for a location, THE System SHALL send the search query to the Maps_API
2. WHEN the Maps_API returns location results, THE System SHALL display the location name, address, city, and country
3. WHEN a Participant selects a Maps_API result, THE System SHALL create a Location record with the returned data
4. IF the Maps_API is unavailable, THEN THE System SHALL display an error message and allow manual Location entry
5. IF the Maps_API returns no results, THEN THE System SHALL notify the Participant and allow manual Location entry

### Requirement 10: Activity Categorization

**User Story:** As a participant, I want to categorize activities by type, so that I can organize and filter our itinerary by interest.

#### Acceptance Criteria

1. THE System SHALL provide four predefined Categories: "Kultura" (Culture), "Gastronomija" (Gastronomy), "Priroda" (Nature), and "Noćni život" (Nightlife)
2. WHEN a Participant assigns a Category to an Activity, THE System SHALL create an association between the Activity and Category
3. THE System SHALL allow an Activity to be associated with multiple Categories
4. WHEN a Participant views a Trip, THE System SHALL display Category information for each Activity
5. WHEN a Participant removes a Category from an Activity, THE System SHALL delete the association from the Database

### Requirement 11: Expense Tracking

**User Story:** As a participant, I want to record expenses for a trip, so that we can track our spending and stay within budget.

#### Acceptance Criteria

1. WHEN a Participant creates an Expense, THE System SHALL require an amount, date, and associated Trip
2. WHEN a Participant creates an Expense, THE System SHALL accept an optional description up to 500 characters
3. THE System SHALL store Expense amounts as decimal values with two decimal places
4. WHEN an Expense is created, THE System SHALL add the Expense amount to the Trip total expense
5. WHEN an Expense is deleted, THE System SHALL subtract the Expense amount from the Trip total expense
6. WHEN an Expense is updated, THE System SHALL recalculate the Trip total expense

### Requirement 12: Expense Management

**User Story:** As a participant, I want to view, edit, and delete expenses, so that I can maintain accurate financial records for the trip.

#### Acceptance Criteria

1. WHEN a Participant views a Trip, THE System SHALL display all Expenses with amount, description, and date
2. WHEN a Participant views a Trip, THE System SHALL display the total expense sum
3. WHEN a Participant edits an Expense, THE System SHALL allow modification of amount, description, and date
4. WHEN a Participant deletes an Expense, THE System SHALL remove the Expense from the Database
5. THE System SHALL update the Trip total expense whenever an Expense is created, updated, or deleted

### Requirement 13: Trip Viewing and Filtering

**User Story:** As a user, I want to view all trips I'm participating in, so that I can access my travel plans.

#### Acceptance Criteria

1. WHEN a User views their trip list, THE System SHALL display all Trips where the User is a Participant
2. WHEN a User views their trip list, THE System SHALL display Trip name, dates, and total expense for each Trip
3. WHEN a User selects a Trip, THE System SHALL display full Trip details including Activities, Expenses, and Participants
4. THE System SHALL sort Trips by start date in descending order by default
5. WHEN a User views Trip details, THE System SHALL display Activities sorted by start datetime

### Requirement 14: Data Persistence

**User Story:** As a user, I want my trip data to be reliably stored, so that I don't lose my planning work.

#### Acceptance Criteria

1. THE System SHALL use a relational Database to store all application data
2. THE System SHALL enforce referential integrity between Trip, Activity, Expense, and Participant records
3. WHEN a Trip is deleted, THE System SHALL cascade delete all associated Activities, Expenses, and Participants
4. WHEN an Activity is deleted, THE System SHALL cascade delete all Category associations
5. THE System SHALL enforce unique constraints on User email addresses

### Requirement 15: System Architecture

**User Story:** As a developer, I want a well-structured 3-tier architecture with layered backend, so that the system is maintainable, testable, and follows separation of concerns.

#### Acceptance Criteria

1. THE System SHALL implement a 3-tier architecture consisting of Frontend Layer, Backend Layer, and Database Layer
2. THE Frontend Layer SHALL be a web client responsible for presentation and user interaction
3. THE Backend Layer SHALL be organized into three distinct sub-layers: Interface Layer, Business Logic Layer, and Data Access Layer
4. THE Interface Layer SHALL handle HTTP requests and responses, route mapping, and input validation
5. THE Interface Layer SHALL delegate all business operations to the Business Logic Layer
6. THE Business Logic Layer SHALL contain all domain logic, business rules, and validation logic
7. THE Business Logic Layer SHALL delegate all database operations to the Data Access Layer
8. THE Data Access Layer SHALL handle all database queries, ORM operations, and data persistence
9. THE Data Access Layer SHALL not contain business logic
10. THE Database Layer SHALL be a relational SQL database
11. THE System SHALL enforce strict layering where each layer only communicates with its adjacent layer
12. THE Interface Layer SHALL not directly access the Data Access Layer

### Requirement 16: RESTful API

**User Story:** As a developer, I want a RESTful API backend, so that the frontend can interact with the system using standard HTTP methods.

#### Acceptance Criteria

1. THE Interface Layer SHALL provide a RESTful API for all data operations
2. THE Interface Layer SHALL use HTTP GET for data retrieval operations
3. THE Interface Layer SHALL use HTTP POST for resource creation operations
4. THE Interface Layer SHALL use HTTP PUT or PATCH for resource update operations
5. THE Interface Layer SHALL use HTTP DELETE for resource deletion operations
6. THE Interface Layer SHALL return appropriate HTTP status codes (200, 201, 400, 401, 404, 500)
7. THE Interface Layer SHALL return data in JSON format
8. THE Interface Layer SHALL parse request bodies and extract parameters before passing to Business Logic Layer

### Requirement 17: Authentication and Authorization

**User Story:** As a user, I want my trip data to be secure, so that only authorized participants can access and modify it.

#### Acceptance Criteria

1. THE Interface Layer SHALL require authentication for all API endpoints except the login endpoint
2. WHEN an unauthenticated User attempts to access a protected endpoint, THE Interface Layer SHALL return HTTP 401 Unauthorized
3. WHEN a User attempts to access a Trip they are not a Participant in, THE Business Logic Layer SHALL reject the operation and THE Interface Layer SHALL return HTTP 403 Forbidden
4. WHEN a non-Organizer attempts to delete a Trip, THE Business Logic Layer SHALL reject the operation and THE Interface Layer SHALL return HTTP 403 Forbidden
5. THE Interface Layer SHALL validate the User's authentication token on each API request

### Requirement 18: Error Handling

**User Story:** As a user, I want clear error messages when something goes wrong, so that I understand what happened and how to fix it.

#### Acceptance Criteria

1. WHEN a validation error occurs, THE Interface Layer SHALL return a descriptive error message identifying the invalid field
2. WHEN a Database error occurs, THE Data Access Layer SHALL propagate the error and THE Interface Layer SHALL return a generic error message without exposing internal details
3. WHEN the Maps_API is unavailable, THE Business Logic Layer SHALL handle the error and THE Interface Layer SHALL return an error message indicating the service is temporarily unavailable
4. WHEN an OAuth_Provider authentication fails, THE Interface Layer SHALL return an error message indicating authentication failure
5. THE System SHALL log all errors with timestamps and context for debugging purposes

### Requirement 19: Web User Interface

**User Story:** As a user, I want an intuitive web interface, so that I can easily navigate and use the application.

#### Acceptance Criteria

1. THE System SHALL provide a web-based user interface accessible via modern web browsers
2. THE System SHALL display a navigation menu with links to trip list, profile, and logout
3. WHEN a User is not authenticated, THE System SHALL display only the login page
4. WHEN a User is authenticated, THE System SHALL display the trip list as the home page
5. THE System SHALL provide forms for creating and editing Trips, Activities, Expenses, and Participants
6. THE System SHALL display validation errors inline on form fields
