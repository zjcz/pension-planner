import { http } from './client';
import type { DashboardDto } from '../types';

export const dashboardApi = {
  get: () =>
    http.get<DashboardDto>('/dashboard').then((response) => response.data),
};
