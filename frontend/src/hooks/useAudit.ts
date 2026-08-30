import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../api/audit';

export function usePensionAudit(pensionId: number, enabled: boolean) {
  return useQuery({
    queryKey: ['audit', 'pension', pensionId],
    queryFn: () => auditApi.forPension(pensionId),
    enabled,
  });
}

export function useStatementAudit(statementId: number, enabled: boolean) {
  return useQuery({
    queryKey: ['audit', 'statement', statementId],
    queryFn: () => auditApi.forStatement(statementId),
    enabled,
  });
}

export function useOtherIncomeAudit(id: number, enabled: boolean) {
  return useQuery({
    queryKey: ['audit', 'otherIncome', id],
    queryFn: () => auditApi.forOtherIncome(id),
    enabled,
  });
}
