import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Message } from 'primereact/message';
import { ProgressBar } from 'primereact/progressbar';
import { Tag } from 'primereact/tag';
import { Toolbar } from 'primereact/toolbar';
import { apiErrorMessage } from '../api/client';
import { infoApi } from '../api/info';
import { useAuth } from '../auth/AuthContext';
import { AuditDialog, formatLong } from '../components/AuditDialog';
import { PageError } from '../components/PageError';
import { PensionFormDialog } from '../components/PensionFormDialog';
import { OtherIncomeDialog } from '../components/OtherIncomeDialog';
import { SettingsDialog } from '../components/SettingsDialog';
import { StatePensionDialog } from '../components/StatePensionDialog';
import { usePensionAudit, useOtherIncomeAudit, useStatePensionAudit } from '../hooks/useAudit';
import { useDashboard } from '../hooks/useDashboard';
import { useOtherIncomeList, useCreateOtherIncome, useUpdateOtherIncome, useDeleteOtherIncome } from '../hooks/useOtherIncome';
import { useCreatePension, useDeletePension, usePensions, useUpdatePension } from '../hooks/usePensions';
import { useSettings } from '../hooks/useSettings';
import { useCreateStatePension, useDeleteStatePension, useStatePensionList, useUpdateStatePension } from '../hooks/useStatePension';
import type { Pension, PensionRequest, OtherIncome, OtherIncomeRequest, OtherIncomeAuditEntry, StatePension, StatePensionAuditEntry, StatePensionRequest } from '../types';

function formatCurrency(value: number | null | undefined): string {
  if (value == null) return '£0';
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP', maximumFractionDigits: 0 }).format(value);
}

function formatMonthYear(value: string): string {
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
}

function formatDate(value: string | null): string {
  if (!value) return '—';
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

function daysUntil(dateStr: string): number {
  const target = new Date(`${dateStr}T00:00:00`);
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return Math.ceil((target.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
}

export default function HomePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dialogVisible, setDialogVisible] = useState(false);
  const [editing, setEditing] = useState<Pension | null>(null);
  const [spDialogVisible, setSpDialogVisible] = useState(false);
  const [spEditing, setSpEditing] = useState<StatePension | null>(null);
  const [oiDialogVisible, setOiDialogVisible] = useState(false);
  const [oiEditing, setOiEditing] = useState<OtherIncome | null>(null);
  const [settingsDialogVisible, setSettingsDialogVisible] = useState(false);
  const [pensionAuditTarget, setPensionAuditTarget] = useState<Pension | null>(null);
  const [oiAuditTarget, setOiAuditTarget] = useState<OtherIncome | null>(null);
  const [spAuditTarget, setSpAuditTarget] = useState<StatePension | null>(null);

  const { data: settings } = useSettings();
  const auditEnabled = settings?.auditEnabled ?? false;
  const { data: appInfo } = useQuery({
    queryKey: ['info'],
    queryFn: infoApi.get,
    staleTime: Infinity,
  });
  const { data: pensionAuditRecords = [], isLoading: pensionAuditLoading, isError: pensionAuditError, error: pensionAuditErr } =
    usePensionAudit(pensionAuditTarget?.pensionId ?? -1, pensionAuditTarget != null && auditEnabled);
  const { data: oiAuditRecords = [], isLoading: oiAuditLoading, isError: oiAuditError, error: oiAuditErr } =
    useOtherIncomeAudit(oiAuditTarget?.id ?? -1, oiAuditTarget != null && auditEnabled);
  const { data: spAuditRecords = [], isLoading: spAuditLoading, isError: spAuditError, error: spAuditErr } =
    useStatePensionAudit(spAuditTarget?.id ?? -1, spAuditTarget != null && auditEnabled);

  const { data: pensions, isLoading, isError, error } = usePensions();
  const { data: dashboard, isLoading: dashboardLoading, isError: dashboardError, error: dashboardErr } = useDashboard();
  const { data: otherIncomeItems, isLoading: oiLoading, isError: oiError, error: oiErr } = useOtherIncomeList();
  const { data: statePensionItems, isLoading: spLoading, isError: spError, error: spErr } = useStatePensionList();
  const createPension = useCreatePension();
  const updatePension = useUpdatePension();
  const deletePension = useDeletePension();
  const createOi = useCreateOtherIncome();
  const updateOi = useUpdateOtherIncome();
  const deleteOi = useDeleteOtherIncome();
  const createSp = useCreateStatePension();
  const updateSp = useUpdateStatePension();
  const deleteSp = useDeleteStatePension();

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
      <Button label="Settings" icon="pi pi-cog" onClick={() => setSettingsDialogVisible(true)} />
      <Button label="Analytics" icon="pi pi-chart-bar" onClick={() => navigate('/analytics')} />
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
      <Button icon="pi pi-plus" severity="success" rounded text aria-label="Add Statement" onClick={() => navigate(`/pensions/${row.pensionId}`, { state: { addStatement: true } })} />
      {auditEnabled && (
        <Button icon="pi pi-history" severity="info" rounded text aria-label="Audit History" onClick={() => setPensionAuditTarget(row)} />
      )}
    </div>
  );

  const handleOiSave = async (request: OtherIncomeRequest) => {
    if (oiEditing) {
      await updateOi.mutateAsync({ id: oiEditing.id, request });
    } else {
      await createOi.mutateAsync(request);
    }
    setOiDialogVisible(false);
  };

  const handleOiDelete = (item: OtherIncome) => {
    confirmDialog({
      message: `Delete "${item.name}"? This cannot be undone.`,
      header: 'Delete Other Income',
      acceptLabel: 'Delete',
      acceptClassName: 'p-button-danger',
      accept: () => deleteOi.mutate(item.id),
    });
  };

  const oiActionsBody = (row: OtherIncome) => (
    <div className="flex gap-2">
      <Button icon="pi pi-pencil" severity="secondary" rounded text aria-label="Edit" onClick={() => { setOiEditing(row); setOiDialogVisible(true); }} />
      <Button icon="pi pi-trash" severity="danger" rounded text aria-label="Delete" onClick={() => handleOiDelete(row)} />
      {auditEnabled && (
        <Button icon="pi pi-history" severity="info" rounded text aria-label="Audit History" onClick={() => setOiAuditTarget(row)} />
      )}
    </div>
  );

  const handleSpSave = async (request: StatePensionRequest) => {
    if (spEditing) {
      await updateSp.mutateAsync({ id: spEditing.id, request });
    } else {
      await createSp.mutateAsync(request);
    }
    setSpDialogVisible(false);
  };

  const handleSpDelete = (item: StatePension) => {
    confirmDialog({
      message: `Delete "${item.name}"? This cannot be undone.`,
      header: 'Delete State Pension',
      acceptLabel: 'Delete',
      acceptClassName: 'p-button-danger',
      accept: () => deleteSp.mutate(item.id),
    });
  };

  const spActionsBody = (row: StatePension) => (
    <div className="flex gap-2">
      <Button icon="pi pi-pencil" severity="secondary" rounded text aria-label="Edit" onClick={() => { setSpEditing(row); setSpDialogVisible(true); }} />
      <Button icon="pi pi-trash" severity="danger" rounded text aria-label="Delete" onClick={() => handleSpDelete(row)} />
      {auditEnabled && (
        <Button icon="pi pi-history" severity="info" rounded text aria-label="Audit History" onClick={() => setSpAuditTarget(row)} />
      )}
    </div>
  );

  const countdownDays = dashboard?.retirementDate ? daysUntil(dashboard.retirementDate) : null;
  const incomeProgress = dashboard?.targetIncome && dashboard.targetIncome > 0
    ? Math.min(100, Math.round((dashboard.totalProjectedAnnualIncome / dashboard.targetIncome) * 100))
    : null;

  return (
    <div>
      <Toolbar start={start} end={end} />

      <div className="p-4">
        {/* Summary Cards */}
        {dashboardError && <PageError message={apiErrorMessage(dashboardErr)} />}
        {!dashboardError && dashboardLoading && (
          <div className="grid mb-4">
            {[0, 1, 2, 3].map((i) => (
              <div key={i} className="col-12 md:col-6 lg:col-3">
                <Card className="h-full">
                  <div className="text-secondary" style={{ opacity: 0.6 }}>Loading...</div>
                </Card>
              </div>
            ))}
          </div>
        )}
        {!dashboardLoading && !dashboardError && (
        <div className="grid mb-4">
          <div className="col-12 md:col-6 lg:col-3">
            <Card title="Portfolio Value" className="h-full">
              <div className="text-3xl font-bold">{formatCurrency(dashboard?.totalPortfolioValue)}</div>
            </Card>
          </div>
          <div className="col-12 md:col-6 lg:col-3">
            <Card title="Projected Annual Income" className="h-full">
              <div className="text-3xl font-bold">{formatCurrency(dashboard?.totalProjectedAnnualIncome)}</div>
              {dashboard?.targetIncome != null && (
                <div className="mt-2">
                  <div className="text-sm text-secondary mb-1">vs {formatCurrency(dashboard.targetIncome)} target</div>
                  <ProgressBar value={incomeProgress ?? 0} showValue={false} style={{ height: '8px' }} />
                </div>
              )}
            </Card>
          </div>
          <div className="col-12 md:col-6 lg:col-3">
            <Card title="State Pension &amp; Other Income" className="h-full">
              {(dashboard?.statePensions ?? []).length > 0 && (
                <div className="mb-2">
                  {dashboard!.statePensions.map((sp) => (
                    <div key={sp.id} className="text-sm">
                      {sp.name}: {formatCurrency(sp.yearlyAmount)}/yr
                      {sp.takesEffectYear != null && <span className="text-secondary"> from {sp.takesEffectYear}</span>}
                    </div>
                  ))}
                </div>
              )}
              {(dashboard?.otherIncome ?? []).length > 0 && (
                <div>
                  {dashboard!.otherIncome.map((oi) => (
                    <div key={oi.id} className="text-sm">
                      {oi.name}: {formatCurrency(oi.annualAmount)}/yr
                    </div>
                  ))}
                </div>
              )}
              {((dashboard?.statePensions ?? []).length === 0 && (dashboard?.otherIncome ?? []).length === 0) && (
                <div className="text-sm text-secondary">No income set up</div>
              )}
            </Card>
          </div>
          <div className="col-12 md:col-6 lg:col-3">
            <Card title="Retirement Countdown" className="h-full">
              {dashboard?.retirementDate ? (
                <>
                  <div className="text-3xl font-bold">
                    {countdownDays != null && countdownDays > 0 ? `${countdownDays} days` : 'Retired'}
                  </div>
                  <div className="text-sm text-secondary">{formatDate(dashboard.retirementDate)}</div>
                </>
              ) : (
                <div className="text-secondary">No retirement date set</div>
              )}
            </Card>
          </div>
        </div>
        )}

        {/* Pensions Table */}
        <div className="flex justify-content-between align-items-center mt-0 mb-3">
          <h2 className="m-0">Pensions</h2>
          <Button label="Add Pension" icon="pi pi-plus" onClick={openCreate} />
        </div>

        {isError && (
          <Message severity="error" text={apiErrorMessage(error)} className="w-full mb-3" />
        )}

        <DataTable
          value={pensions ?? []}
          loading={isLoading}
          stripedRows
          emptyMessage={'No pensions yet. Click "Add Pension" to create one.'}
        >
          <Column
            header="Name"
            sortable
            body={(row: Pension) => (
              <span
                className="cursor-pointer text-primary font-semibold hover:underline"
                onClick={() => navigate(`/pensions/${row.pensionId}`)}
              >
                {row.name}
              </span>
            )}
          />
          <Column header="Maturity" body={(row: Pension) => formatMonthYear(row.maturityDate)} sortable />
          <Column header="Status" body={statusBody} sortable />
          <Column header="Status Date" body={(row: Pension) => formatDate(row.statusDate)} sortable />
          <Column header="Colour" body={colorBody} />
          <Column header="Tags" body={(row: Pension) =>
            row.tags && row.tags.length > 0
              ? <div className="flex flex-wrap gap-1">{row.tags.map((t) => <Tag key={t.id} value={t.name} severity="info" className="text-xs" />)}</div>
              : <span className="text-secondary">—</span>
          } />
          <Column header="Actions" body={actionsBody} style={{ width: '12rem' }} />
        </DataTable>

        {/* State Pensions Table */}
        <div className="flex justify-content-between align-items-center mt-5 mb-3">
          <h2 className="m-0">State Pensions</h2>
          <Button label="Add State Pension" icon="pi pi-plus" onClick={() => { setSpEditing(null); setSpDialogVisible(true); }} />
        </div>

        {spError && (
          <Message severity="error" text={apiErrorMessage(spErr)} className="w-full mb-3" />
        )}

        <DataTable
          value={statePensionItems ?? []}
          loading={spLoading}
          stripedRows
          emptyMessage={'No state pensions yet. Click "Add State Pension" to create one.'}
        >
          <Column field="name" header="Name" sortable />
          <Column header="Yearly Amount" body={(row: StatePension) => formatCurrency(row.yearlyAmount)} sortable sortField="yearlyAmount" />
          <Column field="takesEffectYear" header="Takes Effect" sortable />
          <Column field="notes" header="Notes" />
          <Column header="Actions" body={spActionsBody} style={{ width: '10rem' }} />
        </DataTable>

        {/* Other Income Table */}
        <div className="flex justify-content-between align-items-center mt-5 mb-3">
          <h2 className="m-0">Other Income</h2>
          <Button label="Add Income" icon="pi pi-plus" onClick={() => { setOiEditing(null); setOiDialogVisible(true); }} />
        </div>

        {oiError && (
          <Message severity="error" text={apiErrorMessage(oiErr)} className="w-full mb-3" />
        )}

        <DataTable
          value={otherIncomeItems ?? []}
          loading={oiLoading}
          stripedRows
          emptyMessage={'No other income streams yet. Click "Add Income" to create one.'}
        >
          <Column field="name" header="Name" sortable />
          <Column header="Annual Amount" body={(row: OtherIncome) => formatCurrency(row.annualAmount)} sortable sortField="annualAmount" />
          <Column header="Tags" body={(row: OtherIncome) =>
            row.tags && row.tags.length > 0
              ? <div className="flex flex-wrap gap-1">{row.tags.map((t) => <Tag key={t.id} value={t.name} severity="info" className="text-xs" />)}</div>
              : <span className="text-secondary">—</span>
          } />
          <Column field="notes" header="Notes" />
          <Column header="Actions" body={oiActionsBody} style={{ width: '10rem' }} />
        </DataTable>
      </div>

      <PensionFormDialog
        visible={dialogVisible}
        pension={editing}
        onHide={() => setDialogVisible(false)}
        onSave={handleSave}
      />
      <StatePensionDialog
        visible={spDialogVisible}
        item={spEditing}
        onHide={() => setSpDialogVisible(false)}
        onSave={handleSpSave}
        loading={createSp.isPending || updateSp.isPending}
      />
      <OtherIncomeDialog
        visible={oiDialogVisible}
        item={oiEditing}
        onHide={() => setOiDialogVisible(false)}
        onSave={handleOiSave}
        loading={createOi.isPending || updateOi.isPending}
      />
      <SettingsDialog visible={settingsDialogVisible} onHide={() => setSettingsDialogVisible(false)} />
      <AuditDialog
        visible={pensionAuditTarget != null}
        title={pensionAuditTarget?.name ?? 'Pension'}
        records={pensionAuditRecords}
        loading={pensionAuditLoading}
        error={pensionAuditError ? pensionAuditErr : null}
        onHide={() => setPensionAuditTarget(null)}
        columns={[
          { field: 'name', header: 'Name' },
          { field: 'maturityDate', header: 'Maturity' },
          { field: 'status', header: 'Status' },
          { field: 'statusDate', header: 'Status Date' },
          { field: 'providerName', header: 'Provider' },
          { field: 'policyNumber', header: 'Policy No.' },
          { field: 'workplaceName', header: 'Workplace' },
          { field: 'color', header: 'Colour' },
          { field: 'notes', header: 'Notes' },
        ]}
      />
      <AuditDialog
        visible={oiAuditTarget != null}
        title={oiAuditTarget?.name ?? 'Other Income'}
        records={oiAuditRecords}
        loading={oiAuditLoading}
        error={oiAuditError ? oiAuditErr : null}
        onHide={() => setOiAuditTarget(null)}
        columns={[
          { field: 'name', header: 'Name' },
          {
            header: 'Annual Amount',
            body: (row) => formatLong((row as OtherIncomeAuditEntry).annualAmount),
          },
          { field: 'notes', header: 'Notes' },
        ]}
      />
      <AuditDialog
        visible={spAuditTarget != null}
        title={spAuditTarget?.name ?? 'State Pension'}
        records={spAuditRecords}
        loading={spAuditLoading}
        error={spAuditError ? spAuditErr : null}
        onHide={() => setSpAuditTarget(null)}
        columns={[
          { field: 'name', header: 'Name' },
          {
            header: 'Yearly Amount',
            body: (row) => formatLong((row as StatePensionAuditEntry).yearlyAmount),
          },
          { field: 'takesEffectYear', header: 'Takes Effect' },
          { field: 'notes', header: 'Notes' },
        ]}
      />
      <ConfirmDialog />

<footer className="px-4 pb-4 pt-1">
        <div className="text-sm" style={{ color: '#c0c0c0' }}>App Version: v{appInfo?.version ?? ''}</div>
      </footer>
    </div>
  );
}
