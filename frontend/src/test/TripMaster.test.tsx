import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import TripMaster from '../components/TripMaster';
import type { Trip } from '../types/index';

// Mock the api module so no real HTTP calls are made
vi.mock('../api/axios-config', () => ({
  api: {
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockTrips: Trip[] = [
  {
    putovanjeId: 1,
    naziv: 'Paris Trip',
    opis: 'Summer in Paris',
    datumPoc: '2024-06-01',
    datumKraj: '2024-06-10',
    ukTrosak: 500,
  },
  {
    putovanjeId: 2,
    naziv: 'Rome Trip',
    opis: 'Weekend in Rome',
    datumPoc: '2024-07-01',
    datumKraj: '2024-07-05',
    ukTrosak: 0,
  },
];

const defaultProps = {
  trips: mockTrips,
  selectedTrip: null,
  onSelectTrip: vi.fn(),
  onTripCreated: vi.fn(),
  onTripUpdated: vi.fn(),
  onTripDeleted: vi.fn(),
  loading: false,
  userId: 1,
};

describe('TripMaster', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the trip list', () => {
    render(<TripMaster {...defaultProps} />);

    expect(screen.getByText('Paris Trip')).toBeInTheDocument();
    expect(screen.getByText('Rome Trip')).toBeInTheDocument();
  });

  it('shows loading state when loading is true', () => {
    render(<TripMaster {...defaultProps} loading={true} />);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows empty state when there are no trips', () => {
    render(<TripMaster {...defaultProps} trips={[]} />);

    expect(screen.getByText(/No trips yet/i)).toBeInTheDocument();
  });

  it('renders total cost formatted correctly', () => {
    render(<TripMaster {...defaultProps} />);

    expect(screen.getByText('$500.00')).toBeInTheDocument();
    expect(screen.getByText('$0.00')).toBeInTheDocument();
  });

  it('calls onSelectTrip when View Details is clicked', () => {
    render(<TripMaster {...defaultProps} />);

    const viewButtons = screen.getAllByText('View Details');
    fireEvent.click(viewButtons[0]);

    expect(defaultProps.onSelectTrip).toHaveBeenCalledWith(mockTrips[0]);
  });

  it('switches to edit mode when Edit is clicked', () => {
    render(<TripMaster {...defaultProps} />);

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    expect(screen.getByText('Edit Trip')).toBeInTheDocument();
    expect(screen.getByText('Cancel Edit')).toBeInTheDocument();
  });

  it('pre-fills form with trip data when editing', () => {
    render(<TripMaster {...defaultProps} />);

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    expect(screen.getByDisplayValue('Paris Trip')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Summer in Paris')).toBeInTheDocument();
  });

  it('resets form when Cancel Edit is clicked', () => {
    render(<TripMaster {...defaultProps} />);

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    fireEvent.click(screen.getByText('Cancel Edit'));

    expect(screen.getByText('Create New Trip')).toBeInTheDocument();
    expect(screen.queryByText('Cancel Edit')).not.toBeInTheDocument();
  });

  it('highlights the selected trip row', () => {
    render(<TripMaster {...defaultProps} selectedTrip={mockTrips[0]} />);

    const rows = screen.getAllByRole('row');
    // First data row (index 1, after header) should have 'selected' class
    expect(rows[1]).toHaveClass('selected');
    expect(rows[2]).not.toHaveClass('selected');
  });

  it('shows Create button in create mode and Update in edit mode', () => {
    render(<TripMaster {...defaultProps} />);

    expect(screen.getByRole('button', { name: 'Create' })).toBeInTheDocument();

    fireEvent.click(screen.getAllByText('Edit')[0]);

    expect(screen.getByRole('button', { name: 'Update' })).toBeInTheDocument();
  });
});
