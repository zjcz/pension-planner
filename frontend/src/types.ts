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
