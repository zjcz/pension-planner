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
