import { useState } from 'react';
import type { Participant } from '../types/index';
import { api } from '../api/axios-config';

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
      await api.post(
        `/trips/${tripId}/participants?requestingUserId=${userId}`,
        {
          email: formData.email,
          uloga: formData.uloga
        }
      );
      onRefresh();
      resetForm();
    } catch (error: any) {
      console.error('Error adding participant:', error);
      
      // Extract error message from response
      const errorMessage = error.response?.data?.message || error.message || 'Failed to add participant';
      
      // Show user-friendly error message
      if (errorMessage.includes('User not found')) {
        alert(`User with email "${formData.email}" doesn't exist in the system. They need to log in at least once before being added as a participant.`);
      } else if (errorMessage.includes('already a participant')) {
        alert('This user is already a participant of this trip.');
      } else if (errorMessage.includes('Access denied')) {
        alert('You do not have permission to add participants to this trip.');
      } else {
        alert(errorMessage);
      }
    }
  };

  const handleDelete = async (participantId: number) => {
    if (!confirm('Remove this participant?')) return;
    
    try {
      await api.delete(
        `/trips/${tripId}/participants/${participantId}?requestingUserId=${userId}`
      );
      onRefresh();
    } catch (error: any) {
      console.error('Error removing participant:', error);
      
      const errorMessage = error.response?.data?.message || error.message || 'Failed to remove participant';
      
      if (errorMessage.includes('last organizer')) {
        alert('Cannot remove the last organizer from the trip.');
      } else if (errorMessage.includes('Access denied')) {
        alert('You do not have permission to remove participants from this trip.');
      } else {
        alert(errorMessage);
      }
    }
  };

  const handleRoleChange = async (participantId: number, newRole: string) => {
    try {
      await api.put(
        `/trips/${tripId}/participants/${participantId}/role?requestingUserId=${userId}`,
        { uloga: newRole }
      );
      onRefresh();
    } catch (error: any) {
      console.error('Error updating role:', error);
      
      const errorMessage = error.response?.data?.message || error.message || 'Failed to update role';
      
      if (errorMessage.includes('last organizer')) {
        alert('Cannot demote the last organizer of the trip.');
      } else if (errorMessage.includes('Access denied')) {
        alert('You do not have permission to change participant roles.');
      } else {
        alert(errorMessage);
      }
      
      // Refresh to revert the UI change
      onRefresh();
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
