import { http } from './client';
import type { StatePension, StatePensionRequest } from '../types';

export const statePensionApi = {
  get: () =>
    http.get<StatePension>('/state-pension').then((response) => response.data),

  upsert: (request: StatePensionRequest) =>
    http.put<StatePension>('/state-pension', request).then((response) => response.data),
};
