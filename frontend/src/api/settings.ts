import { http } from './client';
import type { Settings } from '../types';

export const settingsApi = {
  get: () => http.get<Settings>('/settings').then((response) => response.data),

  update: (settings: Settings) =>
    http.put<Settings>('/settings', settings).then((response) => response.data),
};
