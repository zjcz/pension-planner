import { http } from './client';
import type { ExportParams } from '../types';

export function formatFilenameTimestamp(date: Date = new Date()): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  const yyyy = date.getFullYear();
  const mm = pad(date.getMonth() + 1);
  const dd = pad(date.getDate());
  const hh = pad(date.getHours());
  const min = pad(date.getMinutes());
  const ss = pad(date.getSeconds());
  return `${yyyy}-${mm}-${dd}_${hh}-${min}-${ss}`;
}

export const exportApi = {
  downloadZip: async (params: ExportParams): Promise<void> => {
    const queryParams: Record<string, string | number | boolean> = {
      format: params.format,
      type: params.type,
    };
    if (params.type === 'PENSIONS' && params.includeStatements !== undefined) {
      queryParams.includeStatements = params.includeStatements;
    }
    if (params.type === 'STATEMENTS' && params.pensionId != null) {
      queryParams.pensionId = params.pensionId;
    }

    const response = await http.get<Blob>('/export', {
      params: queryParams,
      responseType: 'blob',
    });

    const contentDisposition = response.headers['content-disposition'] as string | undefined;
    const filenameMatch = contentDisposition?.match(/filename="?([^";]+)"?/i);
    const filename =
      filenameMatch?.[1] ??
      `pension-planner-${params.type.toLowerCase().replace(/_/g, '-')}-export-${formatFilenameTimestamp()}.zip`;

    const blob = new Blob([response.data], { type: 'application/zip' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
