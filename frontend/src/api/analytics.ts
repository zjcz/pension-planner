import { http } from './client';
import type { AnalyticsDto } from '../types';

export const analyticsApi = {
  get: () =>
    http.get<AnalyticsDto>('/analytics').then((response) => response.data),
};
