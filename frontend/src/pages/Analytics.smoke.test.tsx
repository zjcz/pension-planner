import { screen, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import AnalyticsPage from '../pages/AnalyticsPage';
import { render } from '../test/test-utils';
import { server } from '../test/server';

describe('Phase 7 FE smoke: all four charts render with fixture data', () => {
  it('renders all four analytics sections with their charts', async () => {
    render(<AnalyticsPage />);

    await waitFor(() => {
      expect(screen.getByText('Growth Projection')).toBeInTheDocument();
    });

    expect(screen.getByText('Growth vs. Charges')).toBeInTheDocument();
    expect(screen.getByText('Projected Income Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Historical Portfolio Trend')).toBeInTheDocument();

    const charts = screen.getAllByRole('img');
    expect(charts).toHaveLength(4);
    expect(screen.getAllByLabelText('line').length).toBeGreaterThan(0);
    expect(screen.getAllByLabelText('bar').length).toBeGreaterThan(0);
  });

  it('shows guidance messages when there is no projection data', async () => {
    server.use(
      http.get('/api/v1/analytics', () =>
        HttpResponse.json({
          today: '2026-09-01',
          retirementDate: null,
          targetIncome: 30000,
          projections: [],
          pensionGrowthCosts: [],
          pensionIncomeBreakdown: [],
          statePensionBreakdown: [],
          otherIncomeBreakdown: [],
          pensionHistorySeries: [],
        }),
      ),
    );

    render(<AnalyticsPage />);

    await waitFor(() => {
      expect(screen.getByText(/Add statements and a retirement date to see projections/)).toBeInTheDocument();
    });

    expect(screen.getByText(/Add statements with paid-in amounts and charges/)).toBeInTheDocument();
    expect(screen.getByText(/No income sources configured yet/)).toBeInTheDocument();
    expect(screen.getByText(/Add multiple statements to see the trend over time/)).toBeInTheDocument();
  });
});