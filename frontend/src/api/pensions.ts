import { http } from './client';
import type { Pension, PensionRequest } from '../types';

export const pensionsApi = {
  list: () => http.get<Pension[]>('/pensions').then((response) => response.data),

  get: (pensionId: number) =>
    http.get<Pension>(`/pensions/${pensionId}`).then((response) => response.data),

  create: (request: PensionRequest) =>
    http.post<Pension>('/pensions', request).then((response) => response.data),

  update: (pensionId: number, request: PensionRequest) =>
    http.put<Pension>(`/pensions/${pensionId}`, request).then((response) => response.data),

  delete: (pensionId: number) => http.delete<void>(`/pensions/${pensionId}`),
};
