import type {
  AnalyticsDto,
  AuthConfig,
  DashboardDto,
  OtherIncome,
  Pension,
  Settings,
  StatePension,
  Statement,
  Tag,
  User,
} from '../types';

export const user: User = { id: 1, username: 'jdoe', createdAt: '2026-01-01T00:00:00' };

export const authConfig: AuthConfig = { allowRegistration: true };

export const settings: Settings = {
  targetIncome: 30000,
  retirementDate: '2045-01-01',
  auditEnabled: true,
};

export const tags: Tag[] = [
  { id: 1, name: 'ISA' },
  { id: 2, name: 'SIPP' },
];

export const pensions: Pension[] = [
  {
    pensionId: 1,
    name: 'Aviva Workplace',
    maturityDate: '2045-01-01',
    notes: 'Employer matched',
    status: 'ACTIVE',
    statusDate: '2026-02-01',
    color: '#2196F3',
    tags: [{ id: 2, name: 'SIPP' }],
    providerName: 'Aviva',
    policyNumber: 'AV-123',
    workplaceName: 'Corp Ltd',
  },
  {
    pensionId: 2,
    name: 'Scottish Widows',
    maturityDate: '2050-06-01',
    notes: null,
    status: 'ACTIVE',
    statusDate: '2026-03-01',
    color: '#4CAF50',
    tags: [],
    providerName: null,
    policyNumber: null,
    workplaceName: null,
  },
];

export const statements: Statement[] = [
  {
    statementId: 10,
    pensionId: 1,
    statementDate: '2025-01-01',
    planValue: 25000,
    projectedAnnualAmount: 1200,
    yearlyCharges: 100,
    transferValue: null,
    amountPaidIn: 20000,
    statementNotes: null,
  },
  {
    statementId: 11,
    pensionId: 1,
    statementDate: '2026-01-01',
    planValue: 28000,
    projectedAnnualAmount: 1350,
    yearlyCharges: 120,
    transferValue: null,
    amountPaidIn: 22000,
    statementNotes: 'Good year',
  },
];

export const otherIncomeItems: OtherIncome[] = [
  {
    id: 1,
    name: 'Rental Income',
    annualAmount: 6000,
    notes: 'Flat in London',
    tags: [{ id: 1, name: 'ISA' }],
  },
  {
    id: 2,
    name: 'Dividends',
    annualAmount: 2000,
    notes: null,
    tags: [],
  },
];

export const statePension: StatePension = {
  id: 1,
  name: 'State Pension',
  yearlyAmount: 11000,
  takesEffectYear: 2040,
};

export const dashboard: DashboardDto = {
  totalPortfolioValue: 28000,
  totalProjectedAnnualIncome: 1350,
  targetIncome: 30000,
  statePension: { yearlyAmount: 11000, takesEffectYear: 2040 },
  otherIncome: [
    { id: 1, name: 'Rental Income', annualAmount: 6000 },
    { id: 2, name: 'Dividends', annualAmount: 2000 },
  ],
  retirementDate: '2045-01-01',
};

export const analytics: AnalyticsDto = {
  today: '2026-09-01',
  retirementDate: '2045-01-01',
  targetIncome: 30000,
  projections: [
    { year: 2026, totalValue: 28000 },
    { year: 2030, totalValue: 45000 },
    { year: 2035, totalValue: 70000 },
  ],
  pensionGrowthCosts: [
    { name: 'Aviva Workplace', color: '#2196F3', growthValue: 8000, cumulativeCharges: 1500 },
  ],
  pensionIncomeBreakdown: [
    { name: 'Aviva Workplace', color: '#2196F3', projectedAnnualAmount: 1350 },
  ],
  statePensionAnnual: 11000,
  otherIncomeBreakdown: [
    { name: 'Rental Income', annualAmount: 6000 },
    { name: 'Dividends', annualAmount: 2000 },
  ],
  pensionHistorySeries: [
    {
      name: 'Total',
      color: '#333333',
      dataPoints: [
        { date: '2025-01-01', value: 25000 },
        { date: '2026-01-01', value: 28000 },
      ],
    },
    {
      name: 'Aviva Workplace',
      color: '#2196F3',
      dataPoints: [
        { date: '2025-01-01', value: 25000 },
        { date: '2026-01-01', value: 28000 },
      ],
    },
  ],
};

const original = {
  pensions: structuredClone(pensions),
  statements: structuredClone(statements),
  otherIncomeItems: structuredClone(otherIncomeItems),
  statePension: structuredClone(statePension),
  settings: structuredClone(settings),
  tags: structuredClone(tags),
  dashboard: structuredClone(dashboard),
  analytics: structuredClone(analytics),
};

export function resetFixtures() {
  pensions.splice(0, pensions.length, ...structuredClone(original.pensions));
  statements.splice(0, statements.length, ...structuredClone(original.statements));
  otherIncomeItems.splice(0, otherIncomeItems.length, ...structuredClone(original.otherIncomeItems));
  Object.assign(statePension, structuredClone(original.statePension));
  Object.assign(settings, structuredClone(original.settings));
  tags.splice(0, tags.length, ...structuredClone(original.tags));
  Object.assign(dashboard, structuredClone(original.dashboard));
  Object.assign(analytics, structuredClone(original.analytics));
}
