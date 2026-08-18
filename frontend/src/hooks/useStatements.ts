import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { statementsApi } from '../api/statements';
import type { StatementRequest } from '../types';

export function useStatements(pensionId: number) {
  return useQuery({
    queryKey: ['pensions', pensionId, 'statements'],
    queryFn: () => statementsApi.list(pensionId),
  });
}

export function useCreateStatement(pensionId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: StatementRequest) => statementsApi.create(pensionId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions', pensionId, 'statements'] });
    },
  });
}

export function useUpdateStatement(pensionId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ statementId, request }: { statementId: number; request: StatementRequest }) =>
      statementsApi.update(pensionId, statementId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions', pensionId, 'statements'] });
    },
  });
}

export function useDeleteStatement(pensionId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (statementId: number) => statementsApi.delete(pensionId, statementId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions', pensionId, 'statements'] });
    },
  });
}
