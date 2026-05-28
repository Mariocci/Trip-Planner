import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import TripDetail from '../components/TripDetail';
import type { Trip } from '../types/index';

vi.mock('../api/axios-config', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockTrip: Trip = {
  putovanjeId: 1,
  naziv: 'Paris Trip',
  opis: 'Summer in Paris',
  datumPoc: '2024-06-01',
  datumKraj: '2024-06-10',
  ukTrosak: 500,
};

const defaultProps = {
  trip: mockTrip,
  userId: 1,
  onTripUpdated: vi.fn(),
};

describe('TripDetail', () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    const { api } = await import('../api/axios-config');
    // Default: all tab data returns empty arrays
    vi.mocked(api.get).mockResolvedValue({ data: [] });
  });

  it('renders trip name and description', async () => {
    render(<TripDetail {...defaultProps} />);

    expect(screen.getByText('Paris Trip - Details')).toBeInTheDocument();
    expect(screen.getByText('Summer in Paris')).toBeInTheDocument();
  });

  it('renders trip dates', () => {
    render(<TripDetail {...defaultProps} />);

    expect(screen.getByText(/2024-06-01 to 2024-06-10/)).toBeInTheDocument();
  });

  it('renders all three tabs', () => {
    render(<TripDetail {...defaultProps} />);

    expect(screen.getByRole('button', { name: 'Activities' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Expenses' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Participants' })).toBeInTheDocument();
  });

  it('shows Activities tab as active by default', () => {
    render(<TripDetail {...defaultProps} />);

    expect(screen.getByRole('button', { name: 'Activities' })).toHaveClass('active');
  });

  it('switches to Expenses tab when clicked', async () => {
    render(<TripDetail {...defaultProps} />);

    fireEvent.click(screen.getByRole('button', { name: 'Expenses' }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Expenses' })).toHaveClass('active');
    });
  });

  it('switches to Participants tab when clicked', async () => {
    render(<TripDetail {...defaultProps} />);

    fireEvent.click(screen.getByRole('button', { name: 'Participants' }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Participants' })).toHaveClass('active');
    });
  });

  it('loads activities on mount', async () => {
    const { api } = await import('../api/axios-config');

    render(<TripDetail {...defaultProps} />);

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        `/trips/${mockTrip.putovanjeId}/activities?userId=${defaultProps.userId}`
      );
    });
  });

  it('loads expenses when Expenses tab is clicked', async () => {
    const { api } = await import('../api/axios-config');

    render(<TripDetail {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: 'Expenses' }));

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        `/trips/${mockTrip.putovanjeId}/expenses?userId=${defaultProps.userId}`
      );
    });
  });

  it('loads participants when Participants tab is clicked', async () => {
    const { api } = await import('../api/axios-config');

    render(<TripDetail {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: 'Participants' }));

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        `/trips/${mockTrip.putovanjeId}/participants?userId=${defaultProps.userId}`
      );
    });
  });
});
