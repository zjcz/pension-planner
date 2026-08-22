import { http } from './client';
import type { OtherIncome, OtherIncomeRequest } from '../types';

export const otherIncomeApi = {
  list: () =>
    http.get<OtherIncome[]>('/other-income').then((response) => response.data),

  get: (id: number) =>
    http.get<OtherIncome>(`/other-income/${id}`).then((response) => response.data),

  create: (request: OtherIncomeRequest) =>
    http.post<OtherIncome>('/other-income', request).then((response) => response.data),

  update: (id: number, request: OtherIncomeRequest) =>
    http.put<OtherIncome>(`/other-income/${id}`, request).then((response) => response.data),

  delete: (id: number) => http.delete<void>(`/other-income/${id}`),
};
