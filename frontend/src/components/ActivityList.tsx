import { useState, useEffect } from 'react';
import type { Activity } from '../types/index';
import { api } from '../api/axios-config';

interface ActivityListProps {
  tripId: number;
  userId: number;
  activities: Activity[];
  onRefresh: () => void;
}

interface PlaceSuggestion {
  name: string;
  address: string;
  latitude: number;
  longitude: number;
}

function ActivityList({ tripId, userId, activities, onRefresh }: ActivityListProps) {
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    naziv: '',
    opis: '',
    datumVrijemePoc: '',
    datumVrijemeKraj: '',
    lokacijaId: 1
  });
  const [locationQuery, setLocationQuery] = useState('');
  const [locationSuggestions, setLocationSuggestions] = useState<PlaceSuggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState<PlaceSuggestion | null>(null);

  // Debounced location search
  useEffect(() => {
    if (locationQuery.length < 3) {
      setLocationSuggestions([]);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        const response = await api.get<PlaceSuggestion[]>(`/places/search?query=${encodeURIComponent(locationQuery)}`);
        setLocationSuggestions(response.data);
        setShowSuggestions(true);
      } catch (error) {
        console.error('Error searching locations:', error);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [locationQuery]);

  const resetForm = () => {
    setFormData({
      naziv: '',
      opis: '',
      datumVrijemePoc: '',
      datumVrijemeKraj: '',
      lokacijaId: 1
    });
    setEditingId(null);
    setShowForm(false);
    setLocationQuery('');
    setSelectedLocation(null);
    setLocationSuggestions([]);
  };

  const handleLocationSelect = async (location: PlaceSuggestion) => {
    setSelectedLocation(location);
    setLocationQuery(location.name + ', ' + location.address);
    setShowSuggestions(false);
    
    // Create location in backend and get lokacijaId
    try {
      // Parse location data to extract city and country
      const addressParts = location.address.split(',').map(part => part.trim());
      const country = addressParts[addressParts.length - 1] || 'Unknown';
      const city = addressParts.length > 1 ? addressParts[addressParts.length - 2] : location.name;
      
      const locationData = {
        naziv: location.name,
        adresa: location.address,
        grad: city,
        drzava: country
      };
      
      const response = await api.post('/locations', locationData);
      const createdLocation = response.data;
      
      // Update form data with the new location ID
      setFormData(prev => ({ ...prev, lokacijaId: createdLocation.lokacijaId }));
    } catch (error) {
      console.error('Error creating location:', error);
      alert('Failed to save location. Using default location.');
    }
  };

  const handleEdit = (activity: Activity) => {
    setFormData({
      naziv: activity.naziv,
      opis: activity.opis || '',
      datumVrijemePoc: activity.datumVrijemePoc,
      datumVrijemeKraj: activity.datumVrijemeKraj,
      lokacijaId: activity.location?.lokacijaId ?? 1
    });
    if (activity.location) {
      setLocationQuery(activity.location.naziv + (activity.location.adresa ? ', ' + activity.location.adresa : ''));
    }
    setEditingId(activity.aktivnostId);
    setShowForm(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (editingId) {
        await api.put(
          `/trips/${tripId}/activities/${editingId}?userId=${userId}`,
          formData
        );
      } else {
        await api.post(
          `/trips/${tripId}/activities?userId=${userId}`,
          formData
        );
      }
      onRefresh();
      resetForm();
    } catch (error) {
      console.error('Error saving activity:', error);
      alert('Failed to save activity');
    }
  };

  const handleDelete = async (activityId: number) => {
    if (!confirm('Delete this activity?')) return;
    
    try {
      await api.delete(
        `/trips/${tripId}/activities/${activityId}?userId=${userId}`
      );
      onRefresh();
    } catch (error) {
      console.error('Error deleting activity:', error);
      alert('Failed to delete activity');
    }
  };

  return (
    <div className="detail-list">
      <div className="list-header">
        <h3>Activities</h3>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : 'Add Activity'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="detail-form">
          <div className="form-row">
            <div className="form-group">
              <label>Activity Name *</label>
              <input
                type="text"
                value={formData.naziv}
                onChange={(e) => setFormData({ ...formData, naziv: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <input
                type="text"
                value={formData.opis}
                onChange={(e) => setFormData({ ...formData, opis: e.target.value })}
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group" style={{ position: 'relative' }}>
              <label>Location *</label>
              <input
                type="text"
                value={locationQuery}
                onChange={(e) => setLocationQuery(e.target.value)}
                onFocus={() => locationSuggestions.length > 0 && setShowSuggestions(true)}
                placeholder="Search for a city or place..."
                required
              />
              {showSuggestions && locationSuggestions.length > 0 && (
                <div style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  backgroundColor: 'white',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  maxHeight: '200px',
                  overflowY: 'auto',
                  zIndex: 1000,
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                }}>
                  {locationSuggestions.map((suggestion, index) => (
                    <div
                      key={index}
                      onClick={() => handleLocationSelect(suggestion)}
                      style={{
                        padding: '10px',
                        cursor: 'pointer',
                        borderBottom: '1px solid #eee'
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                    >
                      <div style={{ fontWeight: 'bold' }}>{suggestion.name}</div>
                      <div style={{ fontSize: '0.9em', color: '#666' }}>{suggestion.address}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Start Date/Time *</label>
              <input
                type="datetime-local"
                value={formData.datumVrijemePoc}
                onChange={(e) => setFormData({ ...formData, datumVrijemePoc: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>End Date/Time *</label>
              <input
                type="datetime-local"
                value={formData.datumVrijemeKraj}
                onChange={(e) => setFormData({ ...formData, datumVrijemeKraj: e.target.value })}
                required
              />
            </div>
          </div>
          <button type="submit" className="btn-primary">
            {editingId ? 'Update' : 'Create'}
          </button>
        </form>
      )}

      {activities.length === 0 ? (
        <p className="empty-state">No activities yet</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Location</th>
              <th>Start</th>
              <th>End</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {activities.map((activity, index) => (
              <tr key={activity.aktivnostId}>
                <td>{index + 1}</td>
                <td>{activity.naziv}</td>
                <td>{activity.location?.naziv || 'N/A'}</td>
                <td>{new Date(activity.datumVrijemePoc).toLocaleString()}</td>
                <td>{new Date(activity.datumVrijemeKraj).toLocaleString()}</td>
                <td>
                  <button onClick={() => handleEdit(activity)} className="btn-small btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(activity.aktivnostId)} className="btn-small btn-danger">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default ActivityList;
