import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import './App.css'
import TripMaster from './components/TripMaster'
import TripDetail from './components/TripDetail'
import type { Trip } from './types/index'
import { api, setupAxiosInterceptor } from './api/axios-config'

function App() {
  const { isLoading, isAuthenticated, loginWithRedirect, logout, user, getAccessTokenSilently } = useAuth0();
  const [trips, setTrips] = useState<Trip[]>([]);
  const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null);
  const [loading, setLoading] = useState(false);
  const [userId, setUserId] = useState<number | null>(null);

  // Setup axios interceptor when Auth0 is ready
  useEffect(() => {
    if (isAuthenticated) {
      setupAxiosInterceptor(getAccessTokenSilently);
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  // Fetch user info from backend to get database korisnikId
  const fetchUserInfo = async () => {
    try {
      // Wait a bit for axios interceptor to be set up
      await new Promise(resolve => setTimeout(resolve, 100));
      const response = await api.get<{ korisnikId: number }>('/auth/me');
      console.log('User info from backend:', response.data);
      setUserId(response.data.korisnikId);
    } catch (error) {
      console.error('Error fetching user info:', error);
    }
  };

  const loadTrips = async () => {
    if (!userId) return;
    
    setLoading(true);
    try {
      const response = await api.get<Trip[]>(`/trips?userId=${userId}`);
      setTrips(response.data);
    } catch (error) {
      console.error('Error loading trips:', error);
    } finally {
      setLoading(false);
    }
  };

  // Fetch user info first, then load trips
  useEffect(() => {
    if (isAuthenticated) {
      console.log('User authenticated, fetching user info...');
      fetchUserInfo();
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (userId) {
      console.log('User ID received:', userId, 'loading trips...');
      loadTrips();
    }
  }, [userId]);

  // Show loading spinner while Auth0 is initializing
  if (isLoading) {
    return (
      <div className="app-container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <h2>Loading...</h2>
        </div>
      </div>
    );
  }

  // Show login screen if not authenticated
  if (!isAuthenticated) {
    return (
      <div className="app-container">
        <header>
          <h1>Trip Planner</h1>
        </header>
        <main style={{ textAlign: 'center', padding: '50px' }}>
          <h2>Welcome to Trip Planner</h2>
          <p>Please log in to manage your trips</p>
          <button 
            onClick={() => loginWithRedirect()}
            style={{
              padding: '12px 24px',
              fontSize: '16px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              marginTop: '20px'
            }}
          >
            Log In with Auth0
          </button>
        </main>
      </div>
    );
  }

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
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          {user && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              {user.picture && <img src={user.picture} alt={user.name} style={{ width: '32px', height: '32px', borderRadius: '50%' }} />}
              <span>{user.name || user.email}</span>
            </div>
          )}
          <button 
            onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
            style={{
              padding: '8px 16px',
              fontSize: '14px',
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Log Out
          </button>
        </div>
      </header>
      
      <main>
        {userId && (
          <TripMaster
            trips={trips}
            selectedTrip={selectedTrip}
            onSelectTrip={setSelectedTrip}
            onTripCreated={handleTripCreated}
            onTripUpdated={handleTripUpdated}
            onTripDeleted={handleTripDeleted}
            loading={loading}
            userId={userId}
          />
        )}

        {selectedTrip && userId && (
          <TripDetail
            trip={selectedTrip}
            userId={userId}
            onTripUpdated={loadTrips}
          />
        )}
      </main>
    </div>
  )
}

export default App
