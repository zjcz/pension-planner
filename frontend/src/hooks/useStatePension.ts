import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { statePensionApi } from '../api/statePension';
import type { StatePensionRequest } from '../types';

const STATE_PENSION_KEY = ['statePension'];

export function useStatePension() {
  return useQuery({
    queryKey: STATE_PENSION_KEY,
    queryFn: statePensionApi.get,
  });
}

export function useUpsertStatePension() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: StatePensionRequest) => statePensionApi.upsert(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STATE_PENSION_KEY });
    },
  });
}
