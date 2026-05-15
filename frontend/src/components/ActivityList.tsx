import { useState } from 'react';
import type { Activity } from '../types/index';

interface ActivityListProps {
  tripId: number;
  userId: number;
  activities: Activity[];
  onRefresh: () => void;
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
  };

  const handleEdit = (activity: Activity) => {
    setFormData({
      naziv: activity.naziv,
      opis: activity.opis || '',
      datumVrijemePoc: activity.datumVrijemePoc,
      datumVrijemeKraj: activity.datumVrijemeKraj,
      lokacijaId: activity.lokacijaId
    });
    setEditingId(activity.aktivnostId);
    setShowForm(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const url = editingId
        ? `http://localhost:8080/api/trips/${tripId}/activities/${editingId}?userId=${userId}`
        : `http://localhost:8080/api/trips/${tripId}/activities?userId=${userId}`;
      
      const method = editingId ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        onRefresh();
        resetForm();
      }
    } catch (error) {
      console.error('Error saving activity:', error);
      alert('Failed to save activity');
    }
  };

  const handleDelete = async (activityId: number) => {
    if (!confirm('Delete this activity?')) return;
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}/activities/${activityId}?userId=${userId}`,
        { method: 'DELETE' }
      );
      
      if (response.ok) {
        onRefresh();
      }
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
