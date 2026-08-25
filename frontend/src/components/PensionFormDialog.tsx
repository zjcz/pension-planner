import { useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { MultiSelect } from 'primereact/multiselect';
import { Toast } from 'primereact/toast';
import { apiErrorMessage } from '../api/client';
import { useCreateTag, useTags } from '../hooks/useTags';
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
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [tagFilter, setTagFilter] = useState('');
  const [providerName, setProviderName] = useState('');
  const [policyNumber, setPolicyNumber] = useState('');
  const [workplaceName, setWorkplaceName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const toast = useRef<Toast>(null);

  const { data: allTags = [] } = useTags();
  const createTag = useCreateTag();

  useEffect(() => {
    if (visible) {
      setName(pension?.name ?? '');
      setMaturityDate(toDate(pension?.maturityDate));
      setStatus(pension?.status ?? 'ACTIVE');
      setColor(pension?.color ?? null);
      setNotes(pension?.notes ?? '');
      setSelectedTagIds(pension?.tags?.map((t) => t.id) ?? []);
      setProviderName(pension?.providerName ?? '');
      setPolicyNumber(pension?.policyNumber ?? '');
      setWorkplaceName(pension?.workplaceName ?? '');
      setTagFilter('');
      setError(null);
    }
  }, [visible, pension]);

  const tagOptions = allTags.map((t) => ({ label: t.name, value: t.id }));

  const tagFilterLower = tagFilter.trim().toLowerCase();
  const exactMatch = tagFilterLower && allTags.some((t) => t.name.toLowerCase() === tagFilterLower);

  const handleCreateTagFromFilter = async () => {
    const tagName = tagFilter.trim();
    if (!tagName) return;
    try {
      const created = await createTag.mutateAsync({ name: tagName });
      setSelectedTagIds((prev) => [...prev, created.id]);
      setTagFilter('');
      toast.current?.show({ severity: 'success', summary: 'Tag Created', detail: `"${tagName}" added`, life: 2000 });
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  };

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
        tagIds: selectedTagIds.length > 0 ? selectedTagIds : null,
        providerName: providerName.trim() || null,
        policyNumber: policyNumber.trim() || null,
        workplaceName: workplaceName.trim() || null,
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
      <Toast ref={toast} position="top-right" />
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
          <label htmlFor="pension-providerName">Provider Name</label>
          <InputText
            id="pension-providerName"
            value={providerName}
            onChange={(e) => setProviderName(e.target.value)}
            maxLength={100}
            placeholder="e.g. Aviva, Scottish Widows"
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-policyNumber">Policy Number</label>
          <InputText
            id="pension-policyNumber"
            value={policyNumber}
            onChange={(e) => setPolicyNumber(e.target.value)}
            maxLength={100}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-workplaceName">Workplace Name</label>
          <InputText
            id="pension-workplaceName"
            value={workplaceName}
            onChange={(e) => setWorkplaceName(e.target.value)}
            maxLength={100}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="pension-tags">Tags</label>
          <MultiSelect
            id="pension-tags"
            value={selectedTagIds}
            options={tagOptions}
            onChange={(e) => setSelectedTagIds(e.value)}
            placeholder="Select tags"
            display="chip"
            filter
            onFilter={(e) => setTagFilter(e.filter)}
            showClear
            panelFooterTemplate={
              tagFilter.trim() && !exactMatch ? (
                <div className="flex justify-content-between align-items-center px-3 py-2 border-top-1 border-300">
                  <span className="text-sm text-500">
                    Create "<strong>{tagFilter.trim()}</strong>" as new tag
                  </span>
                  <Button
                    icon="pi pi-plus"
                    rounded
                    text
                    size="small"
                    onClick={handleCreateTagFromFilter}
                    aria-label="Create tag"
                  />
                </div>
              ) : null
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
