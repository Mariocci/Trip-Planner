import { useState } from 'react';
import type { Expense } from '../types/index';
import { api } from '../api/axios-config';

interface ExpenseListProps {
  tripId: number;
  userId: number;
  expenses: Expense[];
  onRefresh: () => void;
}

function ExpenseList({ tripId, userId, expenses, onRefresh }: ExpenseListProps) {
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    iznos: '',
    opis: '',
    datum: ''
  });

  const resetForm = () => {
    setFormData({
      iznos: '',
      opis: '',
      datum: ''
    });
    setEditingId(null);
    setShowForm(false);
  };

  const handleEdit = (expense: Expense) => {
    setFormData({
      iznos: expense.iznos.toString(),
      opis: expense.opis || '',
      datum: expense.datum
    });
    setEditingId(expense.trosakId);
    setShowForm(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const payload = {
        ...formData,
        iznos: parseFloat(formData.iznos)
      };
      
      if (editingId) {
        await api.put(
          `/trips/${tripId}/expenses/${editingId}?userId=${userId}`,
          payload
        );
      } else {
        await api.post(
          `/trips/${tripId}/expenses?userId=${userId}`,
          payload
        );
      }
      onRefresh();
      resetForm();
    } catch (error) {
      console.error('Error saving expense:', error);
      alert('Failed to save expense');
    }
  };

  const handleDelete = async (expenseId: number) => {
    if (!confirm('Delete this expense?')) return;
    
    try {
      await api.delete(
        `/trips/${tripId}/expenses/${expenseId}?userId=${userId}`
      );
      onRefresh();
    } catch (error) {
      console.error('Error deleting expense:', error);
      alert('Failed to delete expense');
    }
  };

  const totalExpenses = expenses.reduce((sum, exp) => sum + exp.iznos, 0);

  return (
    <div className="detail-list">
      <div className="list-header">
        <h3>Expenses (Total: ${totalExpenses.toFixed(2)})</h3>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : 'Add Expense'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="detail-form">
          <div className="form-row">
            <div className="form-group">
              <label>Amount *</label>
              <input
                type="number"
                step="0.01"
                value={formData.iznos}
                onChange={(e) => setFormData({ ...formData, iznos: e.target.value })}
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
            <div className="form-group">
              <label>Date *</label>
              <input
                type="date"
                value={formData.datum}
                onChange={(e) => setFormData({ ...formData, datum: e.target.value })}
                required
              />
            </div>
          </div>
          <button type="submit" className="btn-primary">
            {editingId ? 'Update' : 'Create'}
          </button>
        </form>
      )}

      {expenses.length === 0 ? (
        <p className="empty-state">No expenses yet</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Amount</th>
              <th>Description</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {expenses.map((expense, index) => (
              <tr key={expense.trosakId}>
                <td>{index + 1}</td>
                <td>${expense.iznos.toFixed(2)}</td>
                <td>{expense.opis}</td>
                <td>{expense.datum}</td>
                <td>
                  <button onClick={() => handleEdit(expense)} className="btn-small btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(expense.trosakId)} className="btn-small btn-danger">
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

export default ExpenseList;
