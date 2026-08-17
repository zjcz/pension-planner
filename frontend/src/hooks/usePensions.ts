import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { pensionsApi } from '../api/pensions';
import type { PensionRequest } from '../types';

export function usePensions() {
  return useQuery({
    queryKey: ['pensions'],
    queryFn: pensionsApi.list,
  });
}

export function useCreatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: PensionRequest) => pensionsApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions'] });
    },
  });
}

export function useUpdatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ pensionId, request }: { pensionId: number; request: PensionRequest }) =>
      pensionsApi.update(pensionId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions'] });
    },
  });
}

export function useDeletePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (pensionId: number) => pensionsApi.delete(pensionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pensions'] });
    },
  });
}
