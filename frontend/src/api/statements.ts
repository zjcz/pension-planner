import { http } from './client';
import type { Statement, StatementRequest } from '../types';

export const statementsApi = {
  list: (pensionId: number) =>
    http.get<Statement[]>(`/pensions/${pensionId}/statements`).then((response) => response.data),

  get: (pensionId: number, statementId: number) =>
    http.get<Statement>(`/pensions/${pensionId}/statements/${statementId}`).then((response) => response.data),

  create: (pensionId: number, request: StatementRequest) =>
    http.post<Statement>(`/pensions/${pensionId}/statements`, request).then((response) => response.data),

  update: (pensionId: number, statementId: number, request: StatementRequest) =>
    http.put<Statement>(`/pensions/${pensionId}/statements/${statementId}`, request).then((response) => response.data),

  delete: (pensionId: number, statementId: number) =>
    http.delete<void>(`/pensions/${pensionId}/statements/${statementId}`),
};
