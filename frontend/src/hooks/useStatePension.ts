import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { statePensionApi } from '../api/statePension';
import type { StatePensionRequest } from '../types';

const STATE_PENSION_KEY = ['statePension'];

export function useStatePensionList() {
  return useQuery({
    queryKey: STATE_PENSION_KEY,
    queryFn: statePensionApi.list,
  });
}

export function useCreateStatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: StatePensionRequest) => statePensionApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STATE_PENSION_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useUpdateStatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: StatePensionRequest }) =>
      statePensionApi.update(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STATE_PENSION_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useDeleteStatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => statePensionApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STATE_PENSION_KEY });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}