import { Dialog } from 'primereact/dialog';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import type { DataTableValueArray } from 'primereact/datatable';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { apiErrorMessage } from '../api/client';

export interface AuditColumnConfig<T> {
  field?: string;
  header: string;
  body?: (row: T) => React.ReactNode;
  style?: React.CSSProperties;
}

interface AuditDialogProps<T> {
  visible: boolean;
  title: string;
  records: T[];
  columns: AuditColumnConfig<T>[];
  loading: boolean;
  error: unknown;
  onHide: () => void;
}

export function actionSeverity(action: string): 'success' | 'info' | 'warning' | 'danger' {
  switch (action) {
    case 'CREATE':
      return 'success';
    case 'UPDATE':
      return 'info';
    case 'DELETE':
      return 'danger';
    default:
      return 'warning';
  }
}

export function formatInstant(value: string | null): string {
  if (!value) return '—';
  const date = new Date(value);
  return date.toLocaleString('en-GB', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatLong(value: number | null | undefined): string {
  if (value == null) return '—';
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP', maximumFractionDigits: 0 }).format(value);
}

export function AuditDialog<T>({ visible, title, records, columns, loading, error, onHide }: AuditDialogProps<T>) {
  return (
    <Dialog
      header={`${title} — Audit History`}
      visible={visible}
      onHide={onHide}
      style={{ width: '55rem' }}
      modal
    >
      {error ? (
        <Message severity="error" text={apiErrorMessage(error)} className="w-full" />
      ) : (
        <DataTable
          value={records as DataTableValueArray}
          loading={loading}
          stripedRows
          scrollable
          scrollHeight="400px"
          emptyMessage="No audit records."
        >
          <Column
            header="Action"
            body={(row: Record<string, unknown>) => {
              const action = String(row.action);
              return <Tag value={action} severity={actionSeverity(action)} />;
            }}
            style={{ width: '7rem' }}
          />
          <Column
            field="auditTimestamp"
            header="When"
            body={(row: Record<string, unknown>) => formatInstant(String(row.auditTimestamp))}
            sortable
          />
          {columns.map((col) => (
            <Column
              key={col.header}
              field={col.field}
              header={col.header}
              body={col.body as (data: Record<string, unknown>) => React.ReactNode}
              style={col.style}
            />
          ))}
        </DataTable>
      )}
      <p className="text-secondary text-sm mt-3 mb-0">
        Audit records are read-only and cannot be edited or deleted.
      </p>
    </Dialog>
  );
}
