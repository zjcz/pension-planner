import { useState, useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Chart } from 'primereact/chart';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toolbar } from 'primereact/toolbar';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { StatementFormDialog } from '../components/StatementFormDialog';
import { usePension } from '../hooks/usePensions';
import {
  useStatements,
  useCreateStatement,
  useUpdateStatement,
  useDeleteStatement,
} from '../hooks/useStatements';
import type { Statement, StatementRequest } from '../types';

function formatCurrency(value: number | null): string {
  if (value == null) return '—';
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP', maximumFractionDigits: 0 }).format(value);
}

function formatDate(value: string | null): string {
  if (!value) return '—';
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

export default function PensionDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const pensionId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [dialogVisible, setDialogVisible] = useState(false);
  const [editing, setEditing] = useState<Statement | null>(null);

  useEffect(() => {
    if ((location.state as { addStatement?: boolean } | null)?.addStatement) {
      setEditing(null);
      setDialogVisible(true);
      window.history.replaceState({}, '');
    }
  }, [location.state]);

  const { data: pension, isLoading: pensionLoading, isError: pensionError, error: pensionErr } = usePension(pensionId);
  const { data: statements, isLoading: stmtLoading, isError: stmtError, error: stmtErr } = useStatements(pensionId);
  const createStatement = useCreateStatement(pensionId);
  const updateStatement = useUpdateStatement(pensionId);
  const deleteStatement = useDeleteStatement(pensionId);

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const openCreate = () => {
    setEditing(null);
    setDialogVisible(true);
  };

  const openEdit = (statement: Statement) => {
    setEditing(statement);
    setDialogVisible(true);
  };

  const handleSave = async (request: StatementRequest) => {
    if (editing) {
      await updateStatement.mutateAsync({ statementId: editing.statementId, request });
    } else {
      await createStatement.mutateAsync(request);
    }
    setDialogVisible(false);
  };

  const handleDelete = (statement: Statement) => {
    confirmDialog({
      message: 'Delete this statement? This cannot be undone.',
      header: 'Delete Statement',
      acceptLabel: 'Delete',
      acceptClassName: 'p-button-danger',
      accept: () => deleteStatement.mutate(statement.statementId),
    });
  };

  const chartData = (() => {
    if (!statements || statements.length === 0) return null;
    const color = pension?.color ?? '#336699';
    const sorted = [...statements].sort((a, b) => a.statementDate.localeCompare(b.statementDate));
    const labels = sorted.map((s) => {
      const d = new Date(`${s.statementDate}T00:00:00`);
      return d.toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
    });
    return {
      labels,
      datasets: [
        {
          label: 'Plan Value',
          data: sorted.map((s) => s.planValue),
          backgroundColor: `${color}66`,
          borderColor: color,
          borderWidth: 1,
        },
      ],
    };
  })();

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value: string | number) => formatCurrency(Number(value)),
        },
      },
    },
  };

  const start = (
    <div className="flex align-items-center gap-2">
      <Button icon="pi pi-arrow-left" severity="secondary" text onClick={() => navigate('/')} aria-label="Back" />
      <span className="font-bold">{pension?.name ?? 'Pension Details'}</span>
    </div>
  );

  const end = (
    <div className="flex align-items-center gap-3">
      <span className="text-secondary">Signed in as {user?.username}</span>
      <Button label="Add Statement" icon="pi pi-plus" onClick={openCreate} />
      <Button label="Log Out" icon="pi pi-sign-out" severity="secondary" onClick={onLogout} />
    </div>
  );

  const statusBody = (row: Statement) => (
    <span>{formatDate(row.statementDate)}</span>
  );

  const actionsBody = (row: Statement) => (
    <div className="flex gap-2">
      <Button icon="pi pi-pencil" severity="secondary" rounded text aria-label="Edit" onClick={() => openEdit(row)} />
      <Button icon="pi pi-trash" severity="danger" rounded text aria-label="Delete" onClick={() => handleDelete(row)} />
    </div>
  );

  if (pensionLoading) {
    return (
      <div>
        <Toolbar start={start} end={end} />
        <div className="p-4"><Message severity="info" text="Loading..." className="w-full" /></div>
      </div>
    );
  }

  if (pensionError) {
    return (
      <div>
        <Toolbar start={start} end={end} />
        <div className="p-4"><Message severity="error" text={apiErrorMessage(pensionErr)} className="w-full" /></div>
      </div>
    );
  }

  return (
    <div>
      <Toolbar start={start} end={end} />

      <div className="p-4">
        {stmtError && <Message severity="error" text={apiErrorMessage(stmtErr)} className="w-full mb-3" />}

        {/* Overview Panel */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Overview</h3>
          <div className="grid">
            <div className="col-12 md:col-6 lg:col-3">
              <div className="text-sm text-secondary mb-1">Name</div>
              <div className="font-medium">{pension?.name}</div>
            </div>
            <div className="col-12 md:col-6 lg:col-3">
              <div className="text-sm text-secondary mb-1">Maturity</div>
              <div className="font-medium">{formatDate(pension?.maturityDate ?? null)}</div>
            </div>
            <div className="col-12 md:col-6 lg:col-3">
              <div className="text-sm text-secondary mb-1">Status</div>
              <div>
                <Tag value={pension?.status} severity={pension?.status === 'ACTIVE' ? 'success' : 'secondary'} />
              </div>
            </div>
            <div className="col-12 md:col-6 lg:col-3">
              <div className="text-sm text-secondary mb-1">Colour</div>
              <div className="flex align-items-center gap-2">
                {pension?.color ? (
                  <span
                    className="inline-block border-circle"
                    style={{ width: '1.25rem', height: '1.25rem', backgroundColor: pension.color, border: '1px solid var(--surface-border)' }}
                    title={pension.color}
                  />
                ) : (
                  <span className="text-secondary">—</span>
                )}
              </div>
            </div>
            <div className="col-12 md:col-6 lg:col-9">
              <div className="text-sm text-secondary mb-1">Notes</div>
              <div className="font-medium">{pension?.notes || '—'}</div>
            </div>
          </div>
        </div>

        {/* Performance Chart */}
        <div className="surface-card border-round shadow-1 p-4 mb-4">
          <h3 className="mt-0 mb-3">Performance</h3>
          {chartData ? (
            <div style={{ height: '300px' }}>
              <Chart type="bar" data={chartData} options={chartOptions} style={{ height: '100%' }} />
            </div>
          ) : (
            <p className="text-secondary">No statements yet. Add a statement to see performance.</p>
          )}
        </div>

        {/* Statements Table */}
        <div className="surface-card border-round shadow-1 p-4">
          <h3 className="mt-0 mb-3">Statements</h3>
          <DataTable
            value={statements ?? []}
            loading={stmtLoading}
            stripedRows
            emptyMessage={'No statements yet. Click \u201cAdd Statement\u201d to create one.'}
          >
            <Column header="Date" body={statusBody} sortable sortField="statementDate" />
            <Column header="Plan Value" body={(row: Statement) => formatCurrency(row.planValue)} sortable sortField="planValue" />
            <Column header="Projected Annual" body={(row: Statement) => formatCurrency(row.projectedAnnualAmount)} sortable sortField="projectedAnnualAmount" />
            <Column header="Charges" body={(row: Statement) => formatCurrency(row.yearlyCharges)} sortable sortField="yearlyCharges" />
            <Column header="Transfer Value" body={(row: Statement) => formatCurrency(row.transferValue)} sortable sortField="transferValue" />
            <Column header="Paid In" body={(row: Statement) => formatCurrency(row.amountPaidIn)} sortable sortField="amountPaidIn" />
            <Column field="statementNotes" header="Notes" />
            <Column header="Actions" body={actionsBody} style={{ width: '7rem' }} />
          </DataTable>
        </div>
      </div>

      <StatementFormDialog
        visible={dialogVisible}
        statement={editing}
        onHide={() => setDialogVisible(false)}
        onSave={handleSave}
      />
      <ConfirmDialog />
    </div>
  );
}
