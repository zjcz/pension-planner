import { http } from './client';
import type {
  OtherIncomeAuditEntry,
  PensionAuditEntry,
  PensionStatementAuditEntry,
} from '../types';

export const auditApi = {
  forPension: (pensionId: number) =>
    http.get<PensionAuditEntry[]>(`/audit/pensions/${pensionId}`).then((response) => response.data),

  forStatement: (statementId: number) =>
    http.get<PensionStatementAuditEntry[]>(`/audit/statements/${statementId}`).then((response) => response.data),

  forOtherIncome: (id: number) =>
    http.get<OtherIncomeAuditEntry[]>(`/audit/other-income/${id}`).then((response) => response.data),
};
