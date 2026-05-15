import { useState } from 'react';
import type { Trip } from '../types/index';
import './TripMaster.css';

interface TripMasterProps {
  trips: Trip[];
  selectedTrip: Trip | null;
  onSelectTrip: (trip: Trip) => void;
  onTripCreated: (trip: Trip) => void;
  onTripUpdated: (trip: Trip) => void;
  onTripDeleted: (tripId: number) => void;
  loading: boolean;
}

const TEMP_USER_ID = 1;

function TripMaster({
  trips,
  selectedTrip,
  onSelectTrip,
  onTripCreated,
  onTripUpdated,
  onTripDeleted,
  loading
}: TripMasterProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    naziv: '',
    opis: '',
    datumPoc: '',
    datumKraj: ''
  });

  const resetForm = () => {
    setFormData({
      naziv: '',
      opis: '',
      datumPoc: '',
      datumKraj: ''
    });
    setIsEditing(false);
  };

  const handleEdit = (trip: Trip) => {
    setFormData({
      naziv: trip.naziv,
      opis: trip.opis || '',
      datumPoc: trip.datumPoc,
      datumKraj: trip.datumKraj
    });
    setIsEditing(true);
    onSelectTrip(trip);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (isEditing && selectedTrip) {
        // Update existing trip
        const response = await fetch(
          `http://localhost:8080/api/trips/${selectedTrip.putovanjeId}?userId=${TEMP_USER_ID}`,
          {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
          }
        );
        
        if (response.ok) {
          const updatedTrip = await response.json();
          onTripUpdated(updatedTrip);
          resetForm();
        }
      } else {
        // Create new trip
        const response = await fetch(
          `http://localhost:8080/api/trips?userId=${TEMP_USER_ID}`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
          }
        );
        
        if (response.ok) {
          const newTrip = await response.json();
          onTripCreated(newTrip);
          resetForm();
        }
      }
    } catch (error) {
      console.error('Error saving trip:', error);
      alert('Failed to save trip');
    }
  };

  const handleDelete = async (tripId: number) => {
    if (!confirm('Are you sure you want to delete this trip?')) return;
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}?userId=${TEMP_USER_ID}`,
        { method: 'DELETE' }
      );
      
      if (response.ok) {
        onTripDeleted(tripId);
        resetForm();
      }
    } catch (error) {
      console.error('Error deleting trip:', error);
      alert('Failed to delete trip');
    }
  };

  return (
    <div className="trip-master">
      <div className="master-form">
        <div className="form-header">
          <h2>{isEditing ? 'Edit Trip' : 'Create New Trip'}</h2>
          {isEditing && (
            <button type="button" onClick={resetForm} className="btn-secondary">
              Cancel Edit
            </button>
          )}
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Trip Name *</label>
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
            <div className="form-group">
              <label>Start Date *</label>
              <input
                type="date"
                value={formData.datumPoc}
                onChange={(e) => setFormData({ ...formData, datumPoc: e.target.value })}
                required
              />
            </div>
            
            <div className="form-group">
              <label>End Date *</label>
              <input
                type="date"
                value={formData.datumKraj}
                onChange={(e) => setFormData({ ...formData, datumKraj: e.target.value })}
                required
              />
            </div>
          </div>
          
          <div className="form-actions">
            <button type="submit" className="btn-primary">
              {isEditing ? 'Update' : 'Create'}
            </button>
            {isEditing && (
              <button
                type="button"
                onClick={() => selectedTrip && handleDelete(selectedTrip.putovanjeId)}
                className="btn-danger"
              >
                Delete
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="master-list">
        <h3>Your Trips</h3>
        {loading ? (
          <p>Loading...</p>
        ) : trips.length === 0 ? (
          <p className="empty-state">No trips yet. Create your first trip above!</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Total Cost</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {trips.map((trip, index) => (
                <tr
                  key={trip.putovanjeId}
                  className={selectedTrip?.putovanjeId === trip.putovanjeId ? 'selected' : ''}
                >
                  <td>{index + 1}</td>
                  <td>{trip.naziv}</td>
                  <td>{trip.datumPoc}</td>
                  <td>{trip.datumKraj}</td>
                  <td>${trip.ukTrosak?.toFixed(2) || '0.00'}</td>
                  <td>
                    <button
                      onClick={() => handleEdit(trip)}
                      className="btn-small btn-edit"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => onSelectTrip(trip)}
                      className="btn-small btn-view"
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default TripMaster;
