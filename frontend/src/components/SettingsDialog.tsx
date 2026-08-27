import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputSwitch } from 'primereact/inputswitch';
import { Message } from 'primereact/message';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { apiErrorMessage } from '../api/client';
import { useSettings, useUpdateSettings } from '../hooks/useSettings';
import { useTags, useCreateTag, useUpdateTag, useDeleteTag } from '../hooks/useTags';

interface SettingsDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function SettingsDialog({ visible, onHide }: SettingsDialogProps) {
  const { data: settings, isLoading: settingsLoading, isError: settingsError, error: settingsErr } = useSettings();
  const updateSettings = useUpdateSettings();

  const { data: tags = [], isLoading: tagsLoading } = useTags();
  const createTag = useCreateTag();
  const updateTag = useUpdateTag();
  const deleteTag = useDeleteTag();

  const [targetIncome, setTargetIncome] = useState<number | null>(null);
  const [retirementDate, setRetirementDate] = useState<Date | null>(null);
  const [auditEnabled, setAuditEnabled] = useState(true);
  const [formError, setFormError] = useState<string | null>(null);

  const [newTagName, setNewTagName] = useState('');
  const [editingTagId, setEditingTagId] = useState<number | null>(null);
  const [editingTagName, setEditingTagName] = useState('');
  const [tagError, setTagError] = useState<string | null>(null);

  useEffect(() => {
    if (visible && settings) {
      setTargetIncome(settings.targetIncome);
      setRetirementDate(settings.retirementDate ? new Date(`${settings.retirementDate}T00:00:00`) : null);
      setAuditEnabled(settings.auditEnabled);
      setFormError(null);
      setNewTagName('');
      setEditingTagId(null);
      setEditingTagName('');
      setTagError(null);
    }
  }, [visible, settings]);

  const handleSaveSettings = async () => {
    if (targetIncome != null && targetIncome < 0) {
      setFormError('Target income must be ≥ 0');
      return;
    }
    setFormError(null);
    const dateStr = retirementDate
      ? `${retirementDate.getFullYear()}-${String(retirementDate.getMonth() + 1).padStart(2, '0')}-${String(retirementDate.getDate()).padStart(2, '0')}`
      : null;
    try {
      await updateSettings.mutateAsync({ targetIncome, retirementDate: dateStr, auditEnabled });
      onHide();
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  const handleCreateTag = async () => {
    const name = newTagName.trim();
    if (!name) return;
    setTagError(null);
    try {
      await createTag.mutateAsync({ name });
      setNewTagName('');
    } catch (err) {
      setTagError(apiErrorMessage(err));
    }
  };

  const handleRenameTag = async (tagId: number) => {
    const name = editingTagName.trim();
    if (!name) return;
    setTagError(null);
    try {
      await updateTag.mutateAsync({ tagId, request: { name } });
      setEditingTagId(null);
      setEditingTagName('');
    } catch (err) {
      setTagError(apiErrorMessage(err));
    }
  };

  const handleDeleteTag = (tagId: number, tagName: string) => {
    confirmDialog({
      message: `Delete tag "${tagName}"? It will be removed from all pensions and other income.`,
      header: 'Delete Tag',
      acceptLabel: 'Delete',
      acceptClassName: 'p-button-danger',
      accept: () => deleteTag.mutate(tagId),
    });
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label="Save" icon="pi pi-check" onClick={handleSaveSettings} loading={updateSettings.isPending} />
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
      <ConfirmDialog />
      {settingsLoading ? (
        <p className="text-secondary">Loading...</p>
      ) : settingsError ? (
        <Message severity="error" text={apiErrorMessage(settingsErr)} className="w-full" />
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

          <div className="flex align-items-center gap-3">
            <InputSwitch
              id="auditEnabled"
              checked={auditEnabled}
              onChange={(e) => setAuditEnabled(e.value)}
            />
            <div>
              <label htmlFor="auditEnabled" className="font-medium cursor-pointer">Enable audit logging</label>
              <small className="block text-secondary">Record changes to an audit trail for pensions and income.</small>
            </div>
          </div>

          {formError && <Message severity="error" text={formError} />}

          <hr className="m-0 border-100" />

          <div className="flex flex-column gap-2">
            <label className="font-semibold">Tags</label>
            <small className="text-secondary">Labels you can assign to pensions and other income.</small>

            {tagsLoading ? (
              <p className="text-secondary text-sm">Loading tags...</p>
            ) : tags.length === 0 ? (
              <p className="text-secondary text-sm">No tags yet. Create one below.</p>
            ) : (
              <div className="flex flex-column gap-1">
                {tags.map((tag) => (
                  <div key={tag.id} className="flex align-items-center gap-2">
                    {editingTagId === tag.id ? (
                      <>
                        <InputText
                          value={editingTagName}
                          onChange={(e) => setEditingTagName(e.target.value)}
                          className="flex-1"
                          autoFocus
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') handleRenameTag(tag.id);
                            if (e.key === 'Escape') { setEditingTagId(null); setEditingTagName(''); }
                          }}
                        />
                        <Button icon="pi pi-check" rounded text size="small" onClick={() => handleRenameTag(tag.id)} aria-label="Save" />
                        <Button icon="pi pi-times" rounded text size="small" severity="secondary" onClick={() => { setEditingTagId(null); setEditingTagName(''); }} aria-label="Cancel" />
                      </>
                    ) : (
                      <>
                        <span className="flex-1 text-sm">{tag.name}</span>
                        <Button icon="pi pi-pencil" rounded text size="small" severity="secondary" onClick={() => { setEditingTagId(tag.id); setEditingTagName(tag.name); }} aria-label="Rename" />
                        <Button icon="pi pi-trash" rounded text size="small" severity="danger" onClick={() => handleDeleteTag(tag.id, tag.name)} aria-label="Delete" />
                      </>
                    )}
                  </div>
                ))}
              </div>
            )}

            <div className="flex gap-2 mt-2">
              <InputText
                value={newTagName}
                onChange={(e) => setNewTagName(e.target.value)}
                placeholder="New tag name"
                className="flex-1"
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleCreateTag(); } }}
              />
              <Button
                label="Add"
                icon="pi pi-plus"
                severity="secondary"
                onClick={handleCreateTag}
                disabled={!newTagName.trim()}
              />
            </div>

            {tagError && <Message severity="error" text={tagError} className="w-full" />}
          </div>
        </div>
      )}
    </Dialog>
  );
}
