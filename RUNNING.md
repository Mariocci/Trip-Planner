# Running the Trip Planner Application

## Backend (Spring Boot)

1. **Build the project:**
   ```bash
   mvn clean install -DskipTests
   ```

2. **Run the backend:**
   ```bash
   cd presentation
   mvn spring-boot:run
   ```
   
   Or run from IntelliJ: Open `TripPlannerApplication.java` and click Run

3. **Backend will start on:** `http://localhost:8080`

4. **H2 Database Console:** `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/tripplanner`
   - Username: `sa`
   - Password: (leave empty)

## Frontend (React + Vite)

1. **Install dependencies (first time only):**
   ```bash
   cd frontend
   npm install
   ```

2. **Run the frontend:**
   ```bash
   npm run dev
   ```

3. **Frontend will start on:** `http://localhost:5173`

## Using the Application

1. Start the backend first
2. Then start the frontend
3. Open `http://localhost:5173` in your browser
4. The app uses a test user (ID: 1) automatically
5. Create trips, add activities, expenses, and participants!

## API Endpoints

- **Trips:** `GET/POST/PUT/DELETE /api/trips`
- **Activities:** `GET/POST/PUT/DELETE /api/trips/{tripId}/activities`
- **Expenses:** `GET/POST/PUT/DELETE /api/trips/{tripId}/expenses`
- **Participants:** `GET/POST/PUT/DELETE /api/trips/{tripId}/participants`

All endpoints require `userId` query parameter (currently hardcoded to 1).

## Troubleshooting

- **Port 8080 already in use:** Stop other applications using port 8080
- **Port 5173 already in use:** The frontend will automatically use the next available port
- **CORS errors:** Make sure backend is running and CORS is enabled in controllers
- **Database errors:** Delete the `./data` folder and restart to reset the database
