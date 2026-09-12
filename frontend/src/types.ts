export interface User {
  id: number;
  username: string;
  createdAt: string | null;
}

export interface AuthConfig {
  allowRegistration: boolean;
}

export interface AppInfo {
  name: string;
  version: string;
}

export interface Settings {
  targetIncome: number | null;
  retirementDate: string | null;
  auditEnabled: boolean;
}

export type PensionStatus = 'ACTIVE' | 'CLOSED';

export interface Tag {
  id: number;
  name: string;
}

export interface TagRequest {
  name: string;
}

export interface Pension {
  pensionId: number;
  name: string;
  maturityDate: string;
  notes: string | null;
  status: PensionStatus;
  statusDate: string | null;
  color: string | null;
  tags: Tag[];
  providerName: string | null;
  policyNumber: string | null;
  workplaceName: string | null;
}

export interface PensionRequest {
  name: string;
  maturityDate: string;
  notes: string | null;
  status: PensionStatus;
  color: string | null;
  tagIds: number[] | null;
  providerName: string | null;
  policyNumber: string | null;
  workplaceName: string | null;
}

export interface Statement {
  statementId: number;
  pensionId: number;
  statementDate: string;
  planValue: number;
  projectedAnnualAmount: number;
  yearlyCharges: number | null;
  transferValue: number | null;
  amountPaidIn: number | null;
  statementNotes: string | null;
}

export interface StatementRequest {
  statementDate: string;
  planValue: number;
  projectedAnnualAmount: number;
  yearlyCharges: number | null;
  transferValue: number | null;
  amountPaidIn: number | null;
  statementNotes: string | null;
}

export interface StatePension {
  id: number;
  name: string;
  yearlyAmount: number;
  takesEffectYear: number;
  notes: string | null;
}

export interface StatePensionRequest {
  name: string;
  yearlyAmount: number;
  takesEffectYear: number;
  notes: string | null;
}

export interface OtherIncome {
  id: number;
  name: string;
  annualAmount: number;
  notes: string | null;
  tags: Tag[];
}

export interface OtherIncomeRequest {
  name: string;
  annualAmount: number;
  notes: string | null;
  tagIds: number[] | null;
}

export interface DashboardDto {
  totalPortfolioValue: number;
  totalProjectedAnnualIncome: number;
  targetIncome: number | null;
  statePensions: {
    id: number;
    name: string;
    yearlyAmount: number;
    takesEffectYear: number;
  }[];
  otherIncome: {
    id: number;
    name: string;
    annualAmount: number;
  }[];
  retirementDate: string | null;
}

export interface AnalyticsDto {
  today: string;
  retirementDate: string | null;
  targetIncome: number | null;
  projections: { year: number; totalValue: number }[];
  pensionGrowthCosts: { name: string; color: string | null; growthValue: number; cumulativeCharges: number }[];
  pensionIncomeBreakdown: { name: string; color: string | null; projectedAnnualAmount: number }[];
  statePensionBreakdown: { name: string; yearlyAmount: number }[];
  otherIncomeBreakdown: { name: string; annualAmount: number }[];
  pensionHistorySeries: {
    name: string;
    color: string | null;
    dataPoints: { date: string; value: number }[];
  }[];
}

export interface PensionAuditEntry {
  auditId: number;
  action: string;
  auditTimestamp: string;
  name: string;
  maturityDate: string | null;
  notes: string | null;
  status: string | null;
  statusDate: string | null;
  color: string | null;
  providerName: string | null;
  policyNumber: string | null;
  workplaceName: string | null;
}

export interface PensionStatementAuditEntry {
  auditId: number;
  action: string;
  auditTimestamp: string;
  statementId: number;
  statementDate: string;
  planValue: number;
  projectedAnnualAmount: number;
  yearlyCharges: number | null;
  transferValue: number | null;
  amountPaidIn: number | null;
  statementNotes: string | null;
}

export interface OtherIncomeAuditEntry {
  auditId: number;
  action: string;
  auditTimestamp: string;
  id: number;
  name: string;
  annualAmount: number;
  notes: string | null;
}

export interface StatePensionAuditEntry {
  auditId: number;
  action: string;
  auditTimestamp: string;
  id: number;
  name: string;
  yearlyAmount: number;
  takesEffectYear: number;
  notes: string | null;
}
