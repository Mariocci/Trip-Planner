import { useState, useEffect } from 'react'
import './App.css'
import TripMaster from './components/TripMaster'
import TripDetail from './components/TripDetail'
import type { Trip } from './types/index'

// Temporary hardcoded user ID (replace with auth later)
const TEMP_USER_ID = 1;

function App() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null);
  const [loading, setLoading] = useState(false);

  const loadTrips = async () => {
    setLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/trips?userId=${TEMP_USER_ID}`);
      if (response.ok) {
        const data = await response.json();
        setTrips(data);
      }
    } catch (error) {
      console.error('Error loading trips:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTrips();
  }, []);

  const handleTripCreated = (trip: Trip) => {
    setTrips([...trips, trip]);
    setSelectedTrip(trip);
  };

  const handleTripUpdated = (trip: Trip) => {
    setTrips(trips.map(t => t.putovanjeId === trip.putovanjeId ? trip : t));
    setSelectedTrip(trip);
  };

  const handleTripDeleted = (tripId: number) => {
    setTrips(trips.filter(t => t.putovanjeId !== tripId));
    setSelectedTrip(null);
  };

  return (
    <div className="app-container">
      <header>
        <h1>Trip Planner</h1>
      </header>
      
      <main>
        <TripMaster
          trips={trips}
          selectedTrip={selectedTrip}
          onSelectTrip={setSelectedTrip}
          onTripCreated={handleTripCreated}
          onTripUpdated={handleTripUpdated}
          onTripDeleted={handleTripDeleted}
          loading={loading}
        />

        {selectedTrip && (
          <TripDetail
            trip={selectedTrip}
            userId={TEMP_USER_ID}
            onTripUpdated={loadTrips}
          />
        )}
      </main>
    </div>
  )
}

export default App
