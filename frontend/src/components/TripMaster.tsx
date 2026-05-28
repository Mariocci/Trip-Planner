import { useState } from 'react';
import type { Trip } from '../types/index';
import './TripMaster.css';
import { api } from '../api/axios-config';

interface TripMasterProps {
  trips: Trip[];
  selectedTrip: Trip | null;
  onSelectTrip: (trip: Trip) => void;
  onTripCreated: (trip: Trip) => void;
  onTripUpdated: (trip: Trip) => void;
  onTripDeleted: (tripId: number) => void;
  loading: boolean;
  userId: number;
}

function TripMaster({
  trips,
  selectedTrip,
  onSelectTrip,
  onTripCreated,
  onTripUpdated,
  onTripDeleted,
  loading,
  userId
}: TripMasterProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    naziv: '',
    opis: '',
    datumPoc: '',
    datumKraj: '',
    maxBudget: ''
  });

  const resetForm = () => {
    setFormData({
      naziv: '',
      opis: '',
      datumPoc: '',
      datumKraj: '',
      maxBudget: ''
    });
    setIsEditing(false);
  };

  const handleEdit = (trip: Trip) => {
    setFormData({
      naziv: trip.naziv,
      opis: trip.opis || '',
      datumPoc: trip.datumPoc,
      datumKraj: trip.datumKraj,
      maxBudget: trip.maxBudget != null ? String(trip.maxBudget) : ''
    });
    setIsEditing(true);
    onSelectTrip(trip);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const payload = {
        ...formData,
        maxBudget: formData.maxBudget !== '' ? Number(formData.maxBudget) : null
      };
      if (isEditing && selectedTrip) {
        // Update existing trip
        const response = await api.put<Trip>(
          `/trips/${selectedTrip.putovanjeId}?userId=${userId}`,
          payload
        );
        onTripUpdated(response.data);
        resetForm();
      } else {
        // Create new trip
        const response = await api.post<Trip>(
          `/trips?userId=${userId}`,
          payload
        );
        onTripCreated(response.data);
        resetForm();
      }
    } catch (error) {
      console.error('Error saving trip:', error);
      alert('Failed to save trip');
    }
  };

  const handleDelete = async (tripId: number) => {
    if (!confirm('Are you sure you want to delete this trip?')) return;
    
    try {
      await api.delete(`/trips/${tripId}?userId=${userId}`);
      onTripDeleted(tripId);
      resetForm();
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

          <div className="form-row">
            <div className="form-group">
              <label>Max Budget ($)</label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={formData.maxBudget}
                onChange={(e) => setFormData({ ...formData, maxBudget: e.target.value })}
                placeholder="Optional"
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
                <th>Spent</th>
                <th>Budget</th>
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
                    {trip.maxBudget != null
                      ? `$${trip.maxBudget.toFixed(2)}`
                      : '—'}
                  </td>
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
