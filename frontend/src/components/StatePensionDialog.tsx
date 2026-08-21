import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import { useStatePension, useUpsertStatePension } from '../hooks/useStatePension';

interface StatePensionDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function StatePensionDialog({ visible, onHide }: StatePensionDialogProps) {
  const { data: statePension, isLoading, isError, error } = useStatePension();
  const upsert = useUpsertStatePension();

  const [yearlyAmount, setYearlyAmount] = useState<number | null>(null);
  const [takesEffectYear, setTakesEffectYear] = useState<number | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (visible && statePension) {
      setYearlyAmount(statePension.yearlyAmount);
      setTakesEffectYear(statePension.takesEffectYear);
    }
  }, [visible, statePension]);

  const handleSave = async () => {
    if (yearlyAmount == null || takesEffectYear == null) {
      setFormError('All fields are required');
      return;
    }
    if (yearlyAmount < 0) {
      setFormError('Yearly amount must be ≥ 0');
      return;
    }
    if (takesEffectYear < 2024) {
      setFormError('Takes effect year must be ≥ 2024');
      return;
    }
    setFormError(null);
    try {
      await upsert.mutateAsync({ yearlyAmount, takesEffectYear });
      onHide();
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label="Save" icon="pi pi-check" onClick={handleSave} loading={upsert.isPending} />
    </div>
  );

  return (
    <Dialog
      header="Manage State Pension"
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
        <div className="flex flex-column gap-3">
          <div className="flex flex-column gap-2">
            <label htmlFor="sp-yearlyAmount">Yearly Amount *</label>
            <InputNumber
              id="sp-yearlyAmount"
              value={yearlyAmount}
              onValueChange={(e) => setYearlyAmount(e.value ?? null)}
              mode="currency"
              currency="GBP"
              locale="en-GB"
              minFractionDigits={0}
              maxFractionDigits={0}
              className="w-full"
            />
          </div>
          <div className="flex flex-column gap-2">
            <label htmlFor="sp-takesEffectYear">Takes Effect Year *</label>
            <InputNumber
              id="sp-takesEffectYear"
              value={takesEffectYear}
              onValueChange={(e) => setTakesEffectYear(e.value ?? null)}
              min={2024}
              max={2100}
              useGrouping={false}
              className="w-full"
            />
          </div>
          {formError && <Message severity="error" text={formError} />}
        </div>
      )}
    </Dialog>
  );
}
