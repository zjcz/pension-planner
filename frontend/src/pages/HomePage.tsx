import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toolbar } from 'primereact/toolbar';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { PensionFormDialog } from '../components/PensionFormDialog';
import { useCreatePension, useDeletePension, usePensions, useUpdatePension } from '../hooks/usePensions';
import type { Pension, PensionRequest } from '../types';

function formatMonthYear(value: string): string {
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
}

function formatDate(value: string | null): string {
  if (!value) return '—';
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

export default function HomePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dialogVisible, setDialogVisible] = useState(false);
  const [editing, setEditing] = useState<Pension | null>(null);

  const { data: pensions, isLoading, isError, error } = usePensions();
  const createPension = useCreatePension();
  const updatePension = useUpdatePension();
  const deletePension = useDeletePension();

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const openCreate = () => {
    setEditing(null);
    setDialogVisible(true);
  };

  const openEdit = (pension: Pension) => {
    setEditing(pension);
    setDialogVisible(true);
  };

  const handleSave = async (request: PensionRequest) => {
    if (editing) {
      await updatePension.mutateAsync({ pensionId: editing.pensionId, request });
    } else {
      await createPension.mutateAsync(request);
    }
    setDialogVisible(false);
  };

  const handleDelete = (pension: Pension) => {
    confirmDialog({
      message: `Delete "${pension.name}"? This cannot be undone.`,
      header: 'Delete Pension',
      acceptLabel: 'Delete',
      acceptClassName: 'p-button-danger',
      accept: () => deletePension.mutate(pension.pensionId),
    });
  };

  const start = (
    <div className="flex align-items-center gap-2">
      <span className="pi pi-home mr-2" />
      <span className="font-bold">Pension Planner</span>
    </div>
  );

  const end = (
    <div className="flex align-items-center gap-3">
      <span className="text-secondary">Signed in as {user?.username}</span>
      <Button label="Add Pension" icon="pi pi-plus" onClick={openCreate} />
      <Button label="Log Out" icon="pi pi-sign-out" severity="secondary" onClick={onLogout} />
    </div>
  );

  const statusBody = (row: Pension) => (
    <Tag value={row.status} severity={row.status === 'ACTIVE' ? 'success' : 'secondary'} />
  );

  const colorBody = (row: Pension) =>
    row.color ? (
      <span
        className="inline-block border-circle"
        style={{ width: '1.25rem', height: '1.25rem', backgroundColor: row.color, border: '1px solid var(--surface-border)' }}
        title={row.color}
      />
    ) : (
      <span className="text-secondary">—</span>
    );

  const actionsBody = (row: Pension) => (
    <div className="flex gap-2">
      <Button icon="pi pi-pencil" severity="secondary" rounded text aria-label="Edit" onClick={() => openEdit(row)} />
      <Button icon="pi pi-trash" severity="danger" rounded text aria-label="Delete" onClick={() => handleDelete(row)} />
    </div>
  );

  return (
    <div>
      <Toolbar start={start} end={end} />

      <div className="p-4">
        <h2 className="mt-0">Pensions</h2>

        {isError && (
          <Message severity="error" text={apiErrorMessage(error)} className="w-full mb-3" />
        )}

        <DataTable
          value={pensions ?? []}
          loading={isLoading}
          stripedRows
          emptyMessage="No pensions yet. Click “Add Pension” to create one."
        >
          <Column field="name" header="Name" sortable />
          <Column header="Maturity" body={(row: Pension) => formatMonthYear(row.maturityDate)} sortable />
          <Column header="Status" body={statusBody} sortable />
          <Column header="Status Date" body={(row: Pension) => formatDate(row.statusDate)} sortable />
          <Column header="Colour" body={colorBody} />
          <Column header="Actions" body={actionsBody} style={{ width: '7rem' }} />
        </DataTable>
      </div>

      <PensionFormDialog
        visible={dialogVisible}
        pension={editing}
        onHide={() => setDialogVisible(false)}
        onSave={handleSave}
      />
      <ConfirmDialog />
    </div>
  );
}
