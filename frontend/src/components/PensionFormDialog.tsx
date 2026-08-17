import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { ColorPicker } from 'primereact/colorpicker';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import type { Pension, PensionRequest, PensionStatus } from '../types';

interface PensionFormDialogProps {
  visible: boolean;
  pension: Pension | null;
  onHide: () => void;
  onSave: (request: PensionRequest) => Promise<void>;
}

const STATUS_OPTIONS: { label: string; value: PensionStatus }[] = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Closed', value: 'CLOSED' },
];

function toDate(value: string | null | undefined): Date | null {
  return value ? new Date(`${value}T00:00:00`) : null;
}

function toIsoDate(date: Date | null): string | null {
  if (!date) return null;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function PensionFormDialog({ visible, pension, onHide, onSave }: PensionFormDialogProps) {
  const [name, setName] = useState('');
  const [maturityDate, setMaturityDate] = useState<Date | null>(null);
  const [status, setStatus] = useState<PensionStatus | null>('ACTIVE');
  const [color, setColor] = useState<string | null>(null);
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      setName(pension?.name ?? '');
      setMaturityDate(toDate(pension?.maturityDate));
      setStatus(pension?.status ?? 'ACTIVE');
      setColor(pension?.color ?? null);
      setNotes(pension?.notes ?? '');
      setError(null);
    }
  }, [visible, pension]);

  const submit = async () => {
    if (!name.trim()) {
      setError('Name is required');
      return;
    }
    if (!maturityDate) {
      setError('Maturity date is required');
      return;
    }
    if (!status) {
      setError('Status is required');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await onSave({
        name: name.trim(),
        maturityDate: toIsoDate(maturityDate) ?? '',
        notes: notes.trim() === '' ? null : notes,
        status,
        color,
      });
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label={pension ? 'Save Changes' : 'Create'} icon="pi pi-check" onClick={submit} loading={submitting} />
    </div>
  );

  return (
    <Dialog
      header={pension ? 'Edit Pension' : 'Add Pension'}
      visible={visible}
      onHide={onHide}
      style={{ width: '32rem' }}
      footer={footer}
      modal
    >
      <div className="flex flex-column gap-3">
        <div className="flex flex-column gap-2">
          <label htmlFor="pension-name">Name *</label>
          <InputText
            id="pension-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            maxLength={100}
            autoFocus
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-maturity">Maturity Month *</label>
          <Calendar
            id="pension-maturity"
            value={maturityDate}
            onChange={(event) => setMaturityDate(event.value as Date | null)}
            view="month"
            dateFormat="mm/yy"
            yearNavigator
            yearRange="2026:2080"
            showButtonBar
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-status">Status *</label>
          <Dropdown
            id="pension-status"
            value={status}
            options={STATUS_OPTIONS}
            onChange={(event) => setStatus(event.value as PensionStatus)}
            placeholder="Select status"
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-color">Colour</label>
          <ColorPicker
            id="pension-color"
            value={color ?? undefined}
            onChange={(event) => {
              const raw = typeof event.value === 'string' ? event.value : null;
              setColor(raw != null ? (raw.startsWith('#') ? raw : `#${raw}`) : null);
            }}
            format="hex"
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-notes">Notes</label>
          <InputTextarea
            id="pension-notes"
            value={notes}
            onChange={(event) => setNotes(event.target.value)}
            rows={3}
          />
        </div>

        {error && <Message severity="error" text={error} />}
      </div>
    </Dialog>
  );
}
