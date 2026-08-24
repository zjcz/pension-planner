import { useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { MultiSelect } from 'primereact/multiselect';
import { Toast } from 'primereact/toast';
import { apiErrorMessage } from '../api/client';
import { useCreateTag, useTags } from '../hooks/useTags';
import type { OtherIncome } from '../types';

interface OtherIncomeDialogProps {
  visible: boolean;
  item: OtherIncome | null;
  onHide: () => void;
  onSave: (request: { name: string; annualAmount: number; notes: string | null; tagIds: number[] | null }) => Promise<void>;
  loading: boolean;
}

export function OtherIncomeDialog({ visible, item, onHide, onSave, loading }: OtherIncomeDialogProps) {
  const [name, setName] = useState('');
  const [annualAmount, setAnnualAmount] = useState<number | null>(null);
  const [notes, setNotes] = useState<string | null>(null);
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [tagFilter, setTagFilter] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const toast = useRef<Toast>(null);

  const { data: allTags = [] } = useTags();
  const createTag = useCreateTag();

  useEffect(() => {
    if (visible) {
      if (item) {
        setName(item.name);
        setAnnualAmount(item.annualAmount);
        setNotes(item.notes);
        setSelectedTagIds(item.tags?.map((t) => t.id) ?? []);
      } else {
        setName('');
        setAnnualAmount(null);
        setNotes(null);
        setSelectedTagIds([]);
      }
      setTagFilter('');
      setFormError(null);
    }
  }, [visible, item]);

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
      setFormError(apiErrorMessage(err));
    }
  };

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
      await onSave({ name: name.trim(), annualAmount, notes, tagIds: selectedTagIds.length > 0 ? selectedTagIds : null });
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
      <Toast ref={toast} position="top-right" />
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
          <label htmlFor="oi-tags">Tags</label>
          <MultiSelect
            id="oi-tags"
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
