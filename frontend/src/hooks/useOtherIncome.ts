import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { otherIncomeApi } from '../api/otherIncome';
import type { OtherIncomeRequest } from '../types';

const OTHER_INCOME_KEY = ['otherIncome'];

export function useOtherIncomeList() {
  return useQuery({
    queryKey: OTHER_INCOME_KEY,
    queryFn: otherIncomeApi.list,
  });
}

export function useOtherIncome(id: number) {
  return useQuery({
    queryKey: [...OTHER_INCOME_KEY, id],
    queryFn: () => otherIncomeApi.get(id),
  });
}

export function useCreateOtherIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: OtherIncomeRequest) => otherIncomeApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: OTHER_INCOME_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useUpdateOtherIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: OtherIncomeRequest }) =>
      otherIncomeApi.update(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: OTHER_INCOME_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useDeleteOtherIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => otherIncomeApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: OTHER_INCOME_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
