import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import type { OtherIncome } from '../types';

interface OtherIncomeDialogProps {
  visible: boolean;
  item: OtherIncome | null;
  onHide: () => void;
  onSave: (request: { name: string; annualAmount: number; notes: string | null }) => Promise<void>;
  loading: boolean;
}

export function OtherIncomeDialog({ visible, item, onHide, onSave, loading }: OtherIncomeDialogProps) {
  const [name, setName] = useState('');
  const [annualAmount, setAnnualAmount] = useState<number | null>(null);
  const [notes, setNotes] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      if (item) {
        setName(item.name);
        setAnnualAmount(item.annualAmount);
        setNotes(item.notes);
      } else {
        setName('');
        setAnnualAmount(null);
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
    if (annualAmount == null || annualAmount < 0) {
      setFormError('Annual amount must be ≥ 0');
      return;
    }
    setFormError(null);
    try {
      await onSave({ name: name.trim(), annualAmount, notes });
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label="Save" icon="pi pi-check" onClick={handleSave} loading={loading} />
    </div>
  );

  return (
    <Dialog
      header={item ? 'Edit Other Income' : 'Add Other Income'}
      visible={visible}
      onHide={onHide}
      style={{ width: '32rem' }}
      footer={footer}
      modal
    >
      <div className="flex flex-column gap-3">
        <div className="flex flex-column gap-2">
          <label htmlFor="oi-name">Name *</label>
          <InputText
            id="oi-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full"
            autoFocus
          />
        </div>
        <div className="flex flex-column gap-2">
          <label htmlFor="oi-annualAmount">Annual Amount *</label>
          <InputNumber
            id="oi-annualAmount"
            value={annualAmount}
            onValueChange={(e) => setAnnualAmount(e.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
            className="w-full"
          />
        </div>
        <div className="flex flex-column gap-2">
          <label htmlFor="oi-notes">Notes</label>
          <InputTextarea
            id="oi-notes"
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
