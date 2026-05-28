import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ActivityList from '../components/ActivityList';
import type { Activity } from '../types/index';

vi.mock('../api/axios-config', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockActivities: Activity[] = [
  {
    aktivnostId: 1,
    naziv: 'Eiffel Tower Visit',
    opis: 'Morning visit',
    datumVrijemePoc: '2024-06-02T09:00:00',
    datumVrijemeKraj: '2024-06-02T12:00:00',
    putovanjeId: 1,
    lokacijaId: 1,
    location: {
      lokacijaId: 1,
      naziv: 'Eiffel Tower',
      adresa: 'Champ de Mars',
      grad: 'Paris',
      drzava: 'France',
    },
  },
  {
    aktivnostId: 2,
    naziv: 'Louvre Museum',
    opis: 'Afternoon visit',
    datumVrijemePoc: '2024-06-03T14:00:00',
    datumVrijemeKraj: '2024-06-03T18:00:00',
    putovanjeId: 1,
    lokacijaId: 2,
    location: undefined,
  },
];

const defaultProps = {
  tripId: 1,
  userId: 1,
  activities: mockActivities,
  onRefresh: vi.fn(),
};

describe('ActivityList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all activities', () => {
    render(<ActivityList {...defaultProps} />);

    expect(screen.getByText('Eiffel Tower Visit')).toBeInTheDocument();
    expect(screen.getByText('Louvre Museum')).toBeInTheDocument();
  });

  it('shows location name when location is present', () => {
    render(<ActivityList {...defaultProps} />);

    expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
  });

  it('shows N/A when location is missing', () => {
    render(<ActivityList {...defaultProps} />);

    expect(screen.getByText('N/A')).toBeInTheDocument();
  });

  it('shows empty state when no activities', () => {
    render(<ActivityList {...defaultProps} activities={[]} />);

    expect(screen.getByText('No activities yet')).toBeInTheDocument();
  });

  it('toggles the add activity form when Add Activity is clicked', () => {
    render(<ActivityList {...defaultProps} />);

    expect(screen.queryByPlaceholderText('Search for a city or place...')).not.toBeInTheDocument();

    fireEvent.click(screen.getByText('Add Activity'));

    expect(screen.getByPlaceholderText('Search for a city or place...')).toBeInTheDocument();
  });

  it('shows Cancel when form is open', () => {
    render(<ActivityList {...defaultProps} />);

    fireEvent.click(screen.getByText('Add Activity'));

    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  it('hides form when Cancel is clicked', () => {
    render(<ActivityList {...defaultProps} />);

    fireEvent.click(screen.getByText('Add Activity'));
    fireEvent.click(screen.getByText('Cancel'));

    expect(screen.queryByLabelText('Activity Name *')).not.toBeInTheDocument();
  });

  it('pre-fills form with activity data when Edit is clicked', () => {
    render(<ActivityList {...defaultProps} />);

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    expect(screen.getByDisplayValue('Eiffel Tower Visit')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Morning visit')).toBeInTheDocument();
  });

  it('shows Update button when editing an existing activity', () => {
    render(<ActivityList {...defaultProps} />);

    fireEvent.click(screen.getAllByText('Edit')[0]);

    expect(screen.getByRole('button', { name: 'Update' })).toBeInTheDocument();
  });

  it('shows Create button when adding a new activity', () => {
    render(<ActivityList {...defaultProps} />);

    fireEvent.click(screen.getByText('Add Activity'));

    expect(screen.getByRole('button', { name: 'Create' })).toBeInTheDocument();
  });

  it('calls onRefresh after deleting an activity', async () => {
    const { api } = await import('../api/axios-config');
    vi.mocked(api.delete).mockResolvedValueOnce({ data: {} });

    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<ActivityList {...defaultProps} />);

    const deleteButtons = screen.getAllByText('Delete');
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(defaultProps.onRefresh).toHaveBeenCalled();
    });
  });
});
