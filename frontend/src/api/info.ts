import { http } from './client';
import type { AppInfo } from '../types';

export const infoApi = {
  get: () => http.get<AppInfo>('/info').then((response) => response.data),
};