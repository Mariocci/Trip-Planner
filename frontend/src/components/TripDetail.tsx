import { useState, useEffect } from 'react';
import type { Trip, Activity, Expense, Participant } from '../types/index';
import ActivityList from './ActivityList';
import ExpenseList from './ExpenseList';
import ParticipantList from './ParticipantList';
import './TripDetail.css';
import { api } from '../api/axios-config';

interface TripDetailProps {
  trip: Trip;
  userId: number;
  onTripUpdated?: () => void;
}

type DetailTab = 'activities' | 'expenses' | 'participants';

function TripDetail({ trip, userId, onTripUpdated }: TripDetailProps) {
  const [activeTab, setActiveTab] = useState<DetailTab>('activities');
  const [activities, setActivities] = useState<Activity[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadTabData(activeTab);
  }, [trip.putovanjeId, activeTab]);

  const loadTabData = async (tab: DetailTab) => {
    setLoading(true);
    try {
      switch (tab) {
        case 'activities':
          await loadActivities();
          break;
        case 'expenses':
          await loadExpenses();
          break;
        case 'participants':
          await loadParticipants();
          break;
      }
    } finally {
      setLoading(false);
    }
  };

  const loadActivities = async () => {
    try {
      const response = await api.get<Activity[]>(
        `/trips/${trip.putovanjeId}/activities?userId=${userId}`
      );
      setActivities(response.data);
    } catch (error) {
      console.error('Error loading activities:', error);
    }
  };

  const loadExpenses = async () => {
    try {
      const response = await api.get<Expense[]>(
        `/trips/${trip.putovanjeId}/expenses?userId=${userId}`
      );
      setExpenses(response.data);
    } catch (error) {
      console.error('Error loading expenses:', error);
    }
  };

  const loadParticipants = async () => {
    try {
      const response = await api.get<Participant[]>(
        `/trips/${trip.putovanjeId}/participants?userId=${userId}`
      );
      setParticipants(response.data);
    } catch (error) {
      console.error('Error loading participants:', error);
    }
  };

  return (
    <div className="trip-detail">
      <div className="detail-header">
        <h2>{trip.naziv} - Details</h2>
        <p>{trip.opis}</p>
        <p className="trip-dates">
          {trip.datumPoc} to {trip.datumKraj}
        </p>
      </div>

      <div className="detail-tabs">
        <button
          className={activeTab === 'activities' ? 'active' : ''}
          onClick={() => setActiveTab('activities')}
        >
          Activities
        </button>
        <button
          className={activeTab === 'expenses' ? 'active' : ''}
          onClick={() => setActiveTab('expenses')}
        >
          Expenses
        </button>
        <button
          className={activeTab === 'participants' ? 'active' : ''}
          onClick={() => setActiveTab('participants')}
        >
          Participants
        </button>
      </div>

      <div className="detail-content">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <>
            {activeTab === 'activities' && (
              <ActivityList
                tripId={trip.putovanjeId}
                userId={userId}
                activities={activities}
                onRefresh={loadActivities}
              />
            )}
            {activeTab === 'expenses' && (
              <ExpenseList
                tripId={trip.putovanjeId}
                userId={userId}
                expenses={expenses}
                onRefresh={() => {
                  loadExpenses();
                  onTripUpdated?.();
                }}
              />
            )}
            {activeTab === 'participants' && (
              <ParticipantList
                tripId={trip.putovanjeId}
                userId={userId}
                participants={participants}
                onRefresh={loadParticipants}
              />
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default TripDetail;
