import { useState, useEffect } from 'react';
import { api } from '../api/axios-config';
import './WorkflowManager.css';

interface Task {
  taskId: string;
  taskName: string;
  processInstanceId: string;
  assignee: string;
  tripId: number;
  userId: number;
}

interface ProcessStatus {
  status: string;
  processInstanceId?: string;
  currentTask?: string;
  variables?: {
    tripId: number;
    userId: number;
    approved?: boolean;
    bookingConfirmed?: boolean;
  };
}

function WorkflowManager() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [tripId, setTripId] = useState('');
  const [userId, setUserId] = useState('');
  const [processInstanceId, setProcessInstanceId] = useState('');
  const [processStatus, setProcessStatus] = useState<ProcessStatus | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadTasks();
    const interval = setInterval(loadTasks, 3000);
    return () => clearInterval(interval);
  }, []);

  const loadTasks = async () => {
    try {
      const response = await api.get<Task[]>('/workflow/tasks');
      setTasks(response.data);
    } catch (error) {
      console.error('Error loading tasks:', error);
    }
  };

  const startProcess = async () => {
    if (!tripId || !userId) {
      alert('Unesite Trip ID i User ID');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/workflow/start', {
        tripId: parseInt(tripId),
        userId: parseInt(userId),
      });
      alert(`Proces pokrenut! ID: ${response.data.processInstanceId}`);
      setTripId('');
      setUserId('');
      loadTasks();
    } catch (error) {
      console.error('Error starting process:', error);
      alert('Greška pri pokretanju procesa');
    } finally {
      setLoading(false);
    }
  };

  const completeTask = async (taskId: string, approved: boolean) => {
    setLoading(true);
    try {
      await api.post(`/workflow/tasks/${taskId}/complete`, {
        approved: approved,
      });
      alert(`Task ${approved ? 'odobren' : 'odbijen'}!`);
      loadTasks();
    } catch (error) {
      console.error('Error completing task:', error);
      alert('Greška pri završavanju taska');
    } finally {
      setLoading(false);
    }
  };

  const sendBookingConfirmation = async (processInstanceId: string, confirmed: boolean) => {
    setLoading(true);
    try {
      await api.post('/workflow/message/booking-confirmation', {
        processInstanceId: processInstanceId,
        bookingConfirmed: confirmed,
      });
      alert(`Booking ${confirmed ? 'potvrđen' : 'nije potvrđen'}!`);
      loadTasks();
    } catch (error) {
      console.error('Error sending booking confirmation:', error);
      alert('Greška pri slanju booking potvrde');
    } finally {
      setLoading(false);
    }
  };

  const checkProcessStatus = async () => {
    if (!processInstanceId) {
      alert('Unesite Process Instance ID');
      return;
    }

    setLoading(true);
    try {
      const response = await api.get<ProcessStatus>(
        `/workflow/process/${processInstanceId}/status`
      );
      setProcessStatus(response.data);
    } catch (error) {
      console.error('Error checking process status:', error);
      alert('Greška pri provjeri statusa');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="workflow-manager">
      <h1>Camunda Workflow Manager</h1>

      {/* Start Process Section */}
      <div className="workflow-section">
        <h2>Pokreni novi proces odobrenja putovanja</h2>
        <div className="form-group">
          <input
            type="number"
            placeholder="Trip ID"
            value={tripId}
            onChange={(e) => setTripId(e.target.value)}
          />
          <input
            type="number"
            placeholder="User ID"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
          <button onClick={startProcess} disabled={loading}>
            Pokreni proces
          </button>
        </div>
      </div>

      {/* Active Tasks Section */}
      <div className="workflow-section">
        <h2>Aktivni taskovi ({tasks.length})</h2>
        {tasks.length === 0 ? (
          <p className="no-tasks">Nema aktivnih taskova</p>
        ) : (
          <div className="tasks-list">
            {tasks.map((task) => (
              <div key={task.taskId} className="task-card">
                <h3>{task.taskName}</h3>
                <p>
                  <strong>Trip ID:</strong> {task.tripId}
                </p>
                <p>
                  <strong>User ID:</strong> {task.userId}
                </p>
                <p>
                  <strong>Process Instance:</strong> {task.processInstanceId}
                </p>

                {task.taskName.includes('Manager') && (
                  <div className="task-actions">
                    <button
                      className="btn-approve"
                      onClick={() => completeTask(task.taskId, true)}
                      disabled={loading}
                    >
                      ✓ Odobri
                    </button>
                    <button
                      className="btn-reject"
                      onClick={() => completeTask(task.taskId, false)}
                      disabled={loading}
                    >
                      ✗ Odbij
                    </button>
                  </div>
                )}

                {task.taskName.includes('booking') && (
                  <div className="task-actions">
                    <button
                      className="btn-approve"
                      onClick={() => sendBookingConfirmation(task.processInstanceId, true)}
                      disabled={loading}
                    >
                      ✓ Potvrdi Booking
                    </button>
                    <button
                      className="btn-reject"
                      onClick={() => sendBookingConfirmation(task.processInstanceId, false)}
                      disabled={loading}
                    >
                      ✗ Booking neuspješan
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Process Status Check Section */}
      <div className="workflow-section">
        <h2>Pošalji booking potvrdu</h2>
        <p className="info-text">Simulira poruku od booking agenta</p>
        <div className="form-group">
          <input
            type="text"
            placeholder="Process Instance ID"
            value={processInstanceId}
            onChange={(e) => setProcessInstanceId(e.target.value)}
          />
          <button
            className="btn-approve"
            onClick={() => {
              if (!processInstanceId) {
                alert('Unesite Process Instance ID');
                return;
              }
              sendBookingConfirmation(processInstanceId, true);
            }}
            disabled={loading}
          >
            ✓ Potvrdi Booking
          </button>
          <button
            className="btn-reject"
            onClick={() => {
              if (!processInstanceId) {
                alert('Unesite Process Instance ID');
                return;
              }
              sendBookingConfirmation(processInstanceId, false);
            }}
            disabled={loading}
          >
            ✗ Booking neuspješan
          </button>
        </div>
      </div>

      {/* Process Status Check Section */}
      <div className="workflow-section">
        <h2>Provjeri status procesa</h2>
        <div className="form-group">
          <input
            type="text"
            placeholder="Process Instance ID"
            value={processInstanceId}
            onChange={(e) => setProcessInstanceId(e.target.value)}
          />
          <button onClick={checkProcessStatus} disabled={loading}>
            Provjeri status
          </button>
        </div>

        {processStatus && (
          <div className="process-status">
            <h3>Status: {processStatus.status}</h3>
            {processStatus.currentTask && (
              <p>
                <strong>Trenutni task:</strong> {processStatus.currentTask}
              </p>
            )}
            {processStatus.variables && (
              <div>
                <p>
                  <strong>Trip ID:</strong> {processStatus.variables.tripId}
                </p>
                <p>
                  <strong>Odobren:</strong>{' '}
                  {processStatus.variables.approved === null
                    ? 'Čeka odluku'
                    : processStatus.variables.approved
                    ? 'Da'
                    : 'Ne'}
                </p>
                <p>
                  <strong>Booking potvrđen:</strong>{' '}
                  {processStatus.variables.bookingConfirmed === null
                    ? 'Čeka potvrdu'
                    : processStatus.variables.bookingConfirmed
                    ? 'Da'
                    : 'Ne'}
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default WorkflowManager;
