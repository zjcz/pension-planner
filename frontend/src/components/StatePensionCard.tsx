import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Message } from 'primereact/message';
import { apiErrorMessage } from '../api/client';
import { useStatePension, useUpsertStatePension } from '../hooks/useStatePension';
import type { StatePensionRequest } from '../types';

export function StatePensionCard() {
  const { data: statePension, isLoading, isError, error } = useStatePension();
  const upsert = useUpsertStatePension();

  const [yearlyAmount, setYearlyAmount] = useState<number | null>(null);
  const [takesEffectYear, setTakesEffectYear] = useState<number | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (statePension) {
      setYearlyAmount(statePension.yearlyAmount);
      setTakesEffectYear(statePension.takesEffectYear);
    }
  }, [statePension]);

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
      const request: StatePensionRequest = {
        yearlyAmount,
        takesEffectYear,
      };
      await upsert.mutateAsync(request);
    } catch (err) {
      setFormError(apiErrorMessage(err));
    }
  };

  if (isLoading) {
    return <Card title="State Pension"><p className="text-secondary">Loading...</p></Card>;
  }

  if (isError) {
    return (
      <Card title="State Pension">
        <Message severity="error" text={apiErrorMessage(error)} className="w-full" />
      </Card>
    );
  }

  return (
    <Card title="State Pension">
      <div className="flex flex-column gap-3">
        <div className="grid">
          <div className="col-12 md:col-6">
            <label htmlFor="sp-yearlyAmount" className="block text-sm text-secondary mb-1">Yearly Amount (£)</label>
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
          <div className="col-12 md:col-6">
            <label htmlFor="sp-takesEffectYear" className="block text-sm text-secondary mb-1">Takes Effect Year</label>
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
        </div>
        {formError && <Message severity="error" text={formError} className="w-full" />}
        <div className="flex justify-content-end">
          <Button label="Save" icon="pi pi-check" onClick={handleSave} loading={upsert.isPending} />
        </div>
      </div>
    </Card>
  );
}
