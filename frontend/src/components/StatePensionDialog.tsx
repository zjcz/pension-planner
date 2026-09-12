import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import type { StatePension } from '../types';

interface StatePensionDialogProps {
  visible: boolean;
  item: StatePension | null;
  onHide: () => void;
  onSave: (request: { name: string; yearlyAmount: number; takesEffectYear: number; notes: string | null }) => Promise<void>;
  loading: boolean;
}

export function StatePensionDialog({ visible, item, onHide, onSave, loading }: StatePensionDialogProps) {
  const [name, setName] = useState('');
  const [yearlyAmount, setYearlyAmount] = useState<number | null>(null);
  const [takesEffectYear, setTakesEffectYear] = useState<number | null>(null);
  const [notes, setNotes] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      if (item) {
        setName(item.name);
        setYearlyAmount(item.yearlyAmount);
        setTakesEffectYear(item.takesEffectYear);
        setNotes(item.notes);
      } else {
        setName('');
        setYearlyAmount(null);
        setTakesEffectYear(null);
        setNotes(null);
      }
      setFormError(null);
    }
  }, [visible, item]);

  const handleSave = async () => {
    if (!name.trim()) {
      setFormError('Name is required');
      return;
    }
    if (yearlyAmount == null || yearlyAmount < 0) {
      setFormError('Yearly amount must be ≥ 0');
      return;
    }
    if (takesEffectYear == null || takesEffectYear < 2024) {
      setFormError('Takes effect year must be ≥ 2024');
      return;
    }
    setFormError(null);
    try {
      await onSave({ name: name.trim(), yearlyAmount, takesEffectYear, notes });
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label={item ? 'Save Changes' : 'Create'} icon="pi pi-check" onClick={handleSave} loading={loading} />
    </div>
  );

  return (
    <Dialog
      header={item ? 'Edit State Pension' : 'Add State Pension'}
      visible={visible}
      onHide={onHide}
      style={{ width: '32rem' }}
      footer={footer}
      modal
    >
      <div className="flex flex-column gap-3">
        <div className="flex flex-column gap-2">
          <label htmlFor="sp-name">Name *</label>
          <InputText
            id="sp-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full"
            autoFocus
          />
        </div>
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
        <div className="flex flex-column gap-2">
          <label htmlFor="sp-notes">Notes</label>
          <InputTextarea
            id="sp-notes"
            value={notes ?? ''}
            onChange={(e) => setNotes(e.target.value || null)}
            rows={3}
            className="w-full"
          />
        </div>
        {formError && <Message severity="error" text={formError} />}
      </div>
    </Dialog>
  );
}