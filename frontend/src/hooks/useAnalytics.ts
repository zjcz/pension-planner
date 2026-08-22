import { useQuery } from '@tanstack/react-query';
import { analyticsApi } from '../api/analytics';

export function useAnalytics() {
  return useQuery({
    queryKey: ['analytics'],
    queryFn: analyticsApi.get,
  });
}
