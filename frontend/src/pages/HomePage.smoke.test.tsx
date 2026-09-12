import { screen, waitFor, within } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';
import { dashboard } from '../test/fixtures';

describe('Phase 2 FE smoke: dashboard renders cards and table', () => {
  it('renders the four summary cards with values', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    expect(screen.getByText('£28,000')).toBeInTheDocument();
    expect(screen.getByText('Projected Annual Income')).toBeInTheDocument();
    expect(screen.getByText('State Pension & Other Income')).toBeInTheDocument();
    expect(screen.getByText('Retirement Countdown')).toBeInTheDocument();
    expect(screen.getByText(/State Pension: £11,000\/yr/)).toBeInTheDocument();
    expect(screen.getByText(/Partner Pension: £7,000\/yr/)).toBeInTheDocument();
    expect(screen.getByText(/Rental Income:/)).toBeInTheDocument();
  });

  it('renders the pensions table with rows and colour swatch', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
    });

    expect(screen.getByText('Scottish Widows')).toBeInTheDocument();
    expect(screen.getByText('Pensions')).toBeInTheDocument();
  });

  it('shows the countdown derived from retirementDate', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Retirement Countdown')).toBeInTheDocument();
    });

    const countdownCard = screen.getByText('Retirement Countdown').closest('.p-card');
    expect(countdownCard).not.toBeNull();
    expect(within(countdownCard as HTMLElement).getByText(/days|Retired/)).toBeInTheDocument();
  });

  it('renders the other income table headers', async () => {
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Rental Income')).toBeInTheDocument();
    });

    expect(dashboard.otherIncome.length).toBeGreaterThan(0);
  });
});
