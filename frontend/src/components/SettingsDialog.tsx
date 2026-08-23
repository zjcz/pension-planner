import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import { useSettings, useUpdateSettings } from '../hooks/useSettings';

interface SettingsDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function SettingsDialog({ visible, onHide }: SettingsDialogProps) {
  const { data: settings, isLoading, isError, error } = useSettings();
  const updateSettings = useUpdateSettings();

  const [targetIncome, setTargetIncome] = useState<number | null>(null);
  const [retirementDate, setRetirementDate] = useState<Date | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (visible && settings) {
      setTargetIncome(settings.targetIncome);
      setRetirementDate(settings.retirementDate ? new Date(`${settings.retirementDate}T00:00:00`) : null);
      setFormError(null);
    }
  }, [visible, settings]);

  const handleSave = async () => {
    if (targetIncome != null && targetIncome < 0) {
      setFormError('Target income must be ≥ 0');
      return;
    }
    setFormError(null);
    const dateStr = retirementDate
      ? `${retirementDate.getFullYear()}-${String(retirementDate.getMonth() + 1).padStart(2, '0')}-${String(retirementDate.getDate()).padStart(2, '0')}`
      : null;
    try {
      await updateSettings.mutateAsync({ targetIncome, retirementDate: dateStr });
      onHide();
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label="Save" icon="pi pi-check" onClick={handleSave} loading={updateSettings.isPending} />
    </div>
  );

  return (
    <Dialog
      header="Settings"
      visible={visible}
      onHide={onHide}
      style={{ width: '32rem' }}
      footer={footer}
      modal
    >
      {isLoading ? (
        <p className="text-secondary">Loading...</p>
      ) : isError ? (
        <Message severity="error" text={apiErrorMessage(error)} className="w-full" />
      ) : (
        <div className="flex flex-column gap-4">
          <div className="flex flex-column gap-2">
            <label htmlFor="targetIncome">Target Annual Income</label>
            <InputNumber
              id="targetIncome"
              value={targetIncome}
              onValueChange={(e) => setTargetIncome(e.value ?? null)}
              mode="currency"
              currency="GBP"
              locale="en-GB"
              minFractionDigits={0}
              maxFractionDigits={0}
              className="w-full"
            />
            <small className="text-secondary">The annual income you aim to have in retirement.</small>
          </div>

          <div className="flex flex-column gap-2">
            <label htmlFor="retirementDate">Retirement Date</label>
            <Calendar
              id="retirementDate"
              value={retirementDate}
              onChange={(e) => setRetirementDate(e.value ?? null)}
              view="month"
              dateFormat="mm/yy"
              yearNavigator
              yearRange="2026:2080"
              showIcon
              className="w-full"
            />
            <small className="text-secondary">When you plan to retire.</small>
          </div>

          {formError && <Message severity="error" text={formError} />}
        </div>
      )}
    </Dialog>
  );
}
