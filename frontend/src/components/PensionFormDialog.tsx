import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
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

const COLOR_OPTIONS = [
  { label: 'None', value: null },
  { label: 'Red', value: '#E74C3C' },
  { label: 'Dark Red', value: '#C0392B' },
  { label: 'Pink', value: '#E91E63' },
  { label: 'Purple', value: '#9C27B0' },
  { label: 'Deep Purple', value: '#673AB7' },
  { label: 'Indigo', value: '#3F51B5' },
  { label: 'Blue', value: '#2196F3' },
  { label: 'Light Blue', value: '#03A9F4' },
  { label: 'Cyan', value: '#00BCD4' },
  { label: 'Teal', value: '#009688' },
  { label: 'Green', value: '#4CAF50' },
  { label: 'Light Green', value: '#8BC34A' },
  { label: 'Lime', value: '#CDDC39' },
  { label: 'Yellow', value: '#FFEB3B' },
  { label: 'Amber', value: '#FFC107' },
  { label: 'Orange', value: '#FF9800' },
  { label: 'Deep Orange', value: '#FF5722' },
  { label: 'Brown', value: '#795548' },
  { label: 'Grey', value: '#9E9E9E' },
  { label: 'Blue Grey', value: '#607D8B' },
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
          <Dropdown
            id="pension-color"
            value={color}
            options={COLOR_OPTIONS}
            onChange={(event) => setColor(event.value)}
            valueTemplate={(option) =>
              option?.value ? (
                <div className="flex align-items-center gap-2">
                  <span
                    className="inline-block border-circle"
                    style={{ width: '1rem', height: '1rem', backgroundColor: option.value, border: '1px solid var(--surface-border)' }}
                  />
                  <span>{option.label}</span>
                </div>
              ) : (
                <span>{option?.label ?? 'None'}</span>
              )
            }
            itemTemplate={(option) =>
              option?.value ? (
                <div className="flex align-items-center gap-2">
                  <span
                    className="inline-block border-circle"
                    style={{ width: '1rem', height: '1rem', backgroundColor: option.value, border: '1px solid var(--surface-border)' }}
                  />
                  <span>{option.label}</span>
                </div>
              ) : (
                <span>{option?.label ?? 'None'}</span>
              )
            }
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
