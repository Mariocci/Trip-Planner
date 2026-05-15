import { useState } from 'react';
import type { Participant } from '../types/index';

interface ParticipantListProps {
  tripId: number;
  userId: number;
  participants: Participant[];
  onRefresh: () => void;
}

function ParticipantList({ tripId, userId, participants, onRefresh }: ParticipantListProps) {
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    uloga: 'PARTICIPANT'
  });

  const resetForm = () => {
    setFormData({
      email: '',
      uloga: 'PARTICIPANT'
    });
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}/participants?requestingUserId=${userId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: formData.email,
            uloga: formData.uloga
          })
        }
      );
      
      if (response.ok) {
        onRefresh();
        resetForm();
      }
    } catch (error) {
      console.error('Error adding participant:', error);
      alert('Failed to add participant');
    }
  };

  const handleDelete = async (participantId: number) => {
    if (!confirm('Remove this participant?')) return;
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}/participants/${participantId}?requestingUserId=${userId}`,
        { method: 'DELETE' }
      );
      
      if (response.ok) {
        onRefresh();
      }
    } catch (error) {
      console.error('Error removing participant:', error);
      alert('Failed to remove participant');
    }
  };

  const handleRoleChange = async (participantId: number, newRole: string) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}/participants/${participantId}/role?requestingUserId=${userId}`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ uloga: newRole })
        }
      );
      
      if (response.ok) {
        onRefresh();
      }
    } catch (error) {
      console.error('Error updating role:', error);
      alert('Failed to update role');
    }
  };

  return (
    <div className="detail-list">
      <div className="list-header">
        <h3>Participants</h3>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : 'Add Participant'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="detail-form">
          <div className="form-row">
            <div className="form-group">
              <label>Email *</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Role *</label>
              <select
                value={formData.uloga}
                onChange={(e) => setFormData({ ...formData, uloga: e.target.value })}
              >
                <option value="ORGANIZER">Organizer</option>
                <option value="PARTICIPANT">Participant</option>
              </select>
            </div>
          </div>
          <button type="submit" className="btn-primary">
            Add
          </button>
        </form>
      )}

      {participants.length === 0 ? (
        <p className="empty-state">No participants yet</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>User ID</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {participants.map((participant, index) => (
              <tr key={participant.sudionikId}>
                <td>{index + 1}</td>
                <td>{participant.korisnikId}</td>
                <td>
                  <select
                    value={participant.uloga}
                    onChange={(e) => handleRoleChange(participant.sudionikId, e.target.value)}
                  >
                    <option value="ORGANIZER">Organizer</option>
                    <option value="PARTICIPANT">Participant</option>
                  </select>
                </td>
                <td>
                  <button
                    onClick={() => handleDelete(participant.sudionikId)}
                    className="btn-small btn-danger"
                  >
                    Remove
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

export default ParticipantList;
