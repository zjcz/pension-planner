export interface User {
  id: number;
  username: string;
  createdAt: string | null;
}

export interface AuthConfig {
  allowRegistration: boolean;
}

export interface Settings {
  targetIncome: number | null;
  retirementDate: string | null;
}

export type PensionStatus = 'ACTIVE' | 'CLOSED';

export interface Pension {
  pensionId: number;
  name: string;
  maturityDate: string;
  notes: string | null;
  status: PensionStatus;
  statusDate: string | null;
  color: string | null;
}

export interface PensionRequest {
  name: string;
  maturityDate: string;
  notes: string | null;
  status: PensionStatus;
  color: string | null;
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
}

export interface StatePensionRequest {
  yearlyAmount: number;
  takesEffectYear: number;
}

export interface OtherIncome {
  id: number;
  name: string;
  annualAmount: number;
  notes: string | null;
}

export interface OtherIncomeRequest {
  name: string;
  annualAmount: number;
  notes: string | null;
}

export interface DashboardDto {
  totalPortfolioValue: number;
  totalProjectedAnnualIncome: number;
  targetIncome: number | null;
  statePension: {
    yearlyAmount: number;
    takesEffectYear: number;
  } | null;
  otherIncome: {
    id: number;
    name: string;
    annualAmount: number;
  }[];
  retirementDate: string | null;
}
