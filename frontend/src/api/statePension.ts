import { http } from './client';
import type { StatePension, StatePensionRequest } from '../types';

export const statePensionApi = {
  list: () =>
    http.get<StatePension[]>('/state-pension').then((response) => response.data),

  get: (id: number) =>
    http.get<StatePension>(`/state-pension/${id}`).then((response) => response.data),

  create: (request: StatePensionRequest) =>
    http.post<StatePension>('/state-pension', request).then((response) => response.data),

  update: (id: number, request: StatePensionRequest) =>
    http.put<StatePension>(`/state-pension/${id}`, request).then((response) => response.data),

  delete: (id: number) => http.delete<void>(`/state-pension/${id}`),
};