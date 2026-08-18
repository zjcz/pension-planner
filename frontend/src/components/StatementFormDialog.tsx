import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import type { Statement, StatementRequest } from '../types';

interface StatementFormDialogProps {
  visible: boolean;
  statement: Statement | null;
  onHide: () => void;
  onSave: (request: StatementRequest) => Promise<void>;
}

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

export function StatementFormDialog({ visible, statement, onHide, onSave }: StatementFormDialogProps) {
  const [statementDate, setStatementDate] = useState<Date | null>(null);
  const [planValue, setPlanValue] = useState<number | null>(null);
  const [projectedAnnualAmount, setProjectedAnnualAmount] = useState<number | null>(null);
  const [yearlyCharges, setYearlyCharges] = useState<number | null>(null);
  const [transferValue, setTransferValue] = useState<number | null>(null);
  const [amountPaidIn, setAmountPaidIn] = useState<number | null>(null);
  const [statementNotes, setStatementNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      setStatementDate(toDate(statement?.statementDate));
      setPlanValue(statement?.planValue ?? null);
      setProjectedAnnualAmount(statement?.projectedAnnualAmount ?? null);
      setYearlyCharges(statement?.yearlyCharges ?? null);
      setTransferValue(statement?.transferValue ?? null);
      setAmountPaidIn(statement?.amountPaidIn ?? null);
      setStatementNotes(statement?.statementNotes ?? '');
      setError(null);
    }
  }, [visible, statement]);

  const submit = async () => {
    if (!statementDate) {
      setError('Statement date is required');
      return;
    }
    if (planValue == null) {
      setError('Plan value is required');
      return;
    }
    if (projectedAnnualAmount == null) {
      setError('Projected annual amount is required');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await onSave({
        statementDate: toIsoDate(statementDate) ?? '',
        planValue,
        projectedAnnualAmount,
        yearlyCharges,
        transferValue,
        amountPaidIn,
        statementNotes: statementNotes.trim() === '' ? null : statementNotes,
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
      <Button label={statement ? 'Save Changes' : 'Create'} icon="pi pi-check" onClick={submit} loading={submitting} />
    </div>
  );

  return (
    <Dialog
      header={statement ? 'Edit Statement' : 'Add Statement'}
      visible={visible}
      onHide={onHide}
      style={{ width: '36rem' }}
      footer={footer}
      modal
    >
      <div className="flex flex-column gap-3">
        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-date">Statement Date *</label>
          <Calendar
            id="stmt-date"
            value={statementDate}
            onChange={(event) => setStatementDate(event.value as Date | null)}
            dateFormat="dd/mm/yy"
            monthNavigator
            yearNavigator
            yearRange="2020:2080"
            showButtonBar
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-plan-value">Plan Value *</label>
          <InputNumber
            id="stmt-plan-value"
            value={planValue}
            onValueChange={(event) => setPlanValue(event.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-projected">Projected Annual Amount *</label>
          <InputNumber
            id="stmt-projected"
            value={projectedAnnualAmount}
            onValueChange={(event) => setProjectedAnnualAmount(event.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-charges">Yearly Charges</label>
          <InputNumber
            id="stmt-charges"
            value={yearlyCharges}
            onValueChange={(event) => setYearlyCharges(event.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-transfer">Transfer Value</label>
          <InputNumber
            id="stmt-transfer"
            value={transferValue}
            onValueChange={(event) => setTransferValue(event.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-paid-in">Amount Paid In</label>
          <InputNumber
            id="stmt-paid-in"
            value={amountPaidIn}
            onValueChange={(event) => setAmountPaidIn(event.value ?? null)}
            mode="currency"
            currency="GBP"
            locale="en-GB"
            minFractionDigits={0}
            maxFractionDigits={0}
          />
        </div>

        <div className="flex flex-column gap-2">
          <label htmlFor="stmt-notes">Notes</label>
          <InputTextarea
            id="stmt-notes"
            value={statementNotes}
            onChange={(event) => setStatementNotes(event.target.value)}
            rows={3}
          />
        </div>

        {error && <Message severity="error" text={error} />}
      </div>
    </Dialog>
  );
}
