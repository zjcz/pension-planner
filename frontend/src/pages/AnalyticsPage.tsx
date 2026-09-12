import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Chart } from 'primereact/chart';
import { Toolbar } from 'primereact/toolbar';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { PageError } from '../components/PageError';
import { PageLoading } from '../components/PageLoading';
import { useAnalytics } from '../hooks/useAnalytics';

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP', maximumFractionDigits: 0 }).format(value);
}

const FALLBACK = ['#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF6384', '#FF9F40', '#7BC043', '#F37735', '#FFC425', '#C9CBCF'];

function colorFor(i: number, explicit: string | null): string {
  return explicit ?? FALLBACK[i % FALLBACK.length];
}

export default function AnalyticsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { data, isLoading, isError, error } = useAnalytics();

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const start = (
    <div className="flex align-items-center gap-2">
      <Button icon="pi pi-arrow-left" severity="secondary" text onClick={() => navigate('/')} aria-label="Back" />
      <span className="font-bold">Analytics</span>
    </div>
  );

  const end = (
    <div className="flex align-items-center gap-3">
      <span className="text-secondary">Signed in as {user?.username}</span>
      <Button label="Log Out" icon="pi pi-sign-out" severity="secondary" onClick={onLogout} />
    </div>
  );

  if (isLoading) {
    return (
      <div>
        <Toolbar start={start} end={end} />
        <PageLoading />
      </div>
    );
  }

  if (isError) {
    return (
      <div>
        <Toolbar start={start} end={end} />
        <PageError message={apiErrorMessage(error)} />
      </div>
    );
  }

  const hasProjections = data && data.projections.length > 1;
  const hasGrowthCost = data && data.pensionGrowthCosts.length > 0;
  const hasHistory = data && data.pensionHistorySeries.length > 0;

  // Chart 1: Growth Projection
  const projectionData = hasProjections ? {
    labels: data!.projections.map((p) => String(p.year)),
    datasets: [
      {
        label: 'Portfolio Value',
        data: data!.projections.map((p) => p.totalValue),
        borderColor: '#36A2EB',
        backgroundColor: '#36A2EB33',
        fill: true,
        tension: 0.3,
      },
    ],
  } : null;

  const projectionOpts = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (v: string | number) => formatCurrency(Number(v)) },
      },
    },
  };

  // Chart 2: Growth vs Cost
  const growthCostData = hasGrowthCost ? {
    labels: data!.pensionGrowthCosts.map((gc) => gc.name),
    datasets: [
      {
        label: 'Net Growth',
        data: data!.pensionGrowthCosts.map((gc) => gc.growthValue),
        backgroundColor: data!.pensionGrowthCosts.map((gc, i) => colorFor(i, gc.color) + '99'),
        borderColor: data!.pensionGrowthCosts.map((gc, i) => colorFor(i, gc.color)),
        borderWidth: 1,
      },
      {
        label: 'Cumulative Charges',
        data: data!.pensionGrowthCosts.map((gc) => gc.cumulativeCharges),
        backgroundColor: '#FF638466',
        borderColor: '#FF6384',
        borderWidth: 1,
      },
    ],
  } : null;

  const growthCostOpts = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' as const } },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (v: string | number) => formatCurrency(Number(v)) },
      },
    },
  };

  // Chart 3: Income vs Target (horizontal bar)
  const incomeBreakdownItems: { label: string; value: number; color: string }[] = [];
  if (data) {
    let colorIndex = 0;
    data.pensionIncomeBreakdown.forEach((pi) => {
      incomeBreakdownItems.push({ label: pi.name, value: pi.projectedAnnualAmount, color: colorFor(colorIndex, pi.color) });
      colorIndex += 1;
    });
    data.statePensionBreakdown.forEach((sp) => {
      incomeBreakdownItems.push({ label: sp.name, value: sp.yearlyAmount, color: FALLBACK[colorIndex % FALLBACK.length] });
      colorIndex += 1;
    });
    data.otherIncomeBreakdown.forEach((oi) => {
      incomeBreakdownItems.push({ label: oi.name, value: oi.annualAmount, color: FALLBACK[colorIndex % FALLBACK.length] });
      colorIndex += 1;
    });
  }

  const incomeData = incomeBreakdownItems.length > 0 ? {
    labels: incomeBreakdownItems.map((item) => item.label),
    datasets: [{
      label: 'Annual Amount',
      data: incomeBreakdownItems.map((item) => item.value),
      backgroundColor: incomeBreakdownItems.map((item) => item.color + '99'),
      borderColor: incomeBreakdownItems.map((item) => item.color),
      borderWidth: 1,
    }],
  } : null;

  const incomeOpts = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y' as const,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        beginAtZero: true,
        ticks: { callback: (v: string | number) => formatCurrency(Number(v)) },
      },
    },
  };

  // Chart 4: Historical Portfolio Trend
  const historyData = hasHistory ? {
    labels: (() => {
      const allDates = new Set<string>();
      data!.pensionHistorySeries.forEach((s) => s.dataPoints.forEach((dp) => allDates.add(dp.date)));
      return [...allDates].sort();
    })(),
    datasets: data!.pensionHistorySeries.map((s) => ({
      label: s.name,
      data: (() => {
        const dateMap = new Map(s.dataPoints.map((dp) => [dp.date, dp.value]));
        const allDates = [...new Set(data!.pensionHistorySeries.flatMap((ss) => ss.dataPoints.map((dp) => dp.date)))].sort();
        return allDates.map((d) => dateMap.get(d) ?? null);
      })(),
      borderColor: s.color ?? '#333',
      backgroundColor: (s.color ?? '#333') + '33',
      tension: 0.3,
      borderWidth: s.name === 'Total' ? 3 : 1.5,
      borderDash: s.name === 'Total' ? undefined : undefined,
    })),
  } : null;

  const historyOpts = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' as const } },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (v: string | number) => formatCurrency(Number(v)) },
      },
    },
  };

  return (
    <div>
      <Toolbar start={start} end={end} />
      <div className="p-4">
        {/* Growth Projection */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Growth Projection</h3>
          {projectionData ? (
            <div style={{ height: '300px' }}>
              <Chart type="line" data={projectionData} options={projectionOpts} style={{ height: '100%' }} />
            </div>
          ) : (
            <p className="text-secondary">Add statements and a retirement date to see projections.</p>
          )}
        </div>

        {/* Growth vs Cost */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Growth vs. Charges</h3>
          {growthCostData ? (
            <div style={{ height: '300px' }}>
              <Chart type="bar" data={growthCostData} options={growthCostOpts} style={{ height: '100%' }} />
            </div>
          ) : (
            <p className="text-secondary">Add statements with paid-in amounts and charges to see this chart.</p>
          )}
        </div>

        {/* Income vs Target */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Projected Income Breakdown</h3>
          {incomeData ? (
            <div style={{ height: `${Math.max(150, incomeBreakdownItems.length * 50)}px` }}>
              <Chart type="bar" data={incomeData} options={incomeOpts} style={{ height: '100%' }} />
            </div>
          ) : (
            <p className="text-secondary">No income sources configured yet.</p>
          )}
        </div>

        {/* Historical Portfolio Trend */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Historical Portfolio Trend</h3>
          {historyData ? (
            <div style={{ height: '300px' }}>
              <Chart type="line" data={historyData} options={historyOpts} style={{ height: '100%' }} />
            </div>
          ) : (
            <p className="text-secondary">Add multiple statements to see the trend over time.</p>
          )}
        </div>
      </div>
    </div>
  );
}
