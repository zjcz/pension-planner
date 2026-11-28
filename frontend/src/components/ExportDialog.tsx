import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputSwitch } from 'primereact/inputswitch';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import { exportApi } from '../api/export';
import { usePensions } from '../hooks/usePensions';
import type { ExportFormat, ExportType } from '../types';

interface ExportDialogProps {
  visible: boolean;
  onHide: () => void;
}

const FORMAT_OPTIONS: { label: string; value: ExportFormat }[] = [
  { label: 'CSV', value: 'CSV' },
  { label: 'XML', value: 'XML' },
  { label: 'JSON', value: 'JSON' },
];

const DATA_TYPE_OPTIONS: { label: string; value: ExportType }[] = [
  { label: 'All', value: 'ALL' },
  { label: 'Pensions', value: 'PENSIONS' },
  { label: 'Statements', value: 'STATEMENTS' },
  { label: 'Other Incomes', value: 'OTHER_INCOMES' },
  { label: 'State Pension', value: 'STATE_PENSION' },
];

const ALL_PENSIONS_VALUE = 'ALL' as const;
type PensionSelection = typeof ALL_PENSIONS_VALUE | number;

export function ExportDialog({ visible, onHide }: ExportDialogProps) {
  const { data: pensions = [] } = usePensions();

  const [format, setFormat] = useState<ExportFormat>('CSV');
  const [dataType, setDataType] = useState<ExportType>('ALL');
  const [includeStatements, setIncludeStatements] = useState(true);
  const [selectedPension, setSelectedPension] = useState<PensionSelection>(ALL_PENSIONS_VALUE);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      setFormat('CSV');
      setDataType('ALL');
      setIncludeStatements(true);
      setSelectedPension(ALL_PENSIONS_VALUE);
      setError(null);
    }
  }, [visible]);

  const pensionOptions: { label: string; value: PensionSelection }[] = [
    { label: 'All', value: ALL_PENSIONS_VALUE },
    ...pensions.map((p) => ({ label: p.name, value: p.pensionId })),
  ];

  const handleExport = async () => {
    setExporting(true);
    setError(null);
    try {
      await exportApi.downloadZip({
        format,
        type: dataType,
        includeStatements: dataType === 'PENSIONS' ? includeStatements : undefined,
        pensionId:
          dataType === 'STATEMENTS' && selectedPension !== ALL_PENSIONS_VALUE
            ? selectedPension
            : null,
      });
      onHide();
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setExporting(false);
    }
  };

  const footer = (
    <div className="flex justify-end gap-2">
      <Button label="Cancel" icon="pi pi-times" severity="secondary" onClick={onHide} />
      <Button label="Export" icon="pi pi-download" onClick={handleExport} loading={exporting} />
    </div>
  );

  return (
    <Dialog
      header="Export Data"
      visible={visible}
      onHide={onHide}
      style={{ width: '30rem' }}
      footer={footer}
      modal
    >
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="export-format">Format</label>
          <Dropdown
            id="export-format"
            inputId="export-format-input"
            value={format}
            options={FORMAT_OPTIONS}
            onChange={(e) => setFormat(e.value as ExportFormat)}
            className="w-full"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="export-type">Data to Export</label>
          <Dropdown
            id="export-type"
            inputId="export-type-input"
            value={dataType}
            options={DATA_TYPE_OPTIONS}
            onChange={(e) => setDataType(e.value as ExportType)}
            className="w-full"
          />
        </div>

        {dataType === 'PENSIONS' && (
          <div className="flex items-center gap-4 pt-1">
            <InputSwitch
              id="export-include-statements"
              inputId="export-include-statements-input"
              checked={includeStatements}
              onChange={(e) => setIncludeStatements(e.value)}
            />
            <div>
              <label htmlFor="export-include-statements-input" className="font-medium cursor-pointer">
                Include Statements
              </label>
              <small className="block text-secondary">Include statements data in the export.</small>
            </div>
          </div>
        )}

        {dataType === 'STATEMENTS' && (
          <div className="flex flex-col gap-2">
            <label htmlFor="export-pension">Parent Pension</label>
            <Dropdown
              id="export-pension"
              inputId="export-pension-input"
              value={selectedPension}
              options={pensionOptions}
              onChange={(e) => setSelectedPension(e.value as PensionSelection)}
              className="w-full"
            />
          </div>
        )}

        {error && <Message severity="error" text={error} className="w-full" />}
      </div>
    </Dialog>
  );
}
