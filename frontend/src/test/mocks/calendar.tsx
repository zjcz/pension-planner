import type { ChangeEvent } from 'react';

interface CalendarProps {
  id?: string;
  value?: Date | Date[] | null;
  onChange?: (e: { value?: Date | Date[] | null }) => void;
  [key: string]: unknown;
}

function toIso(value: Date | Date[] | null | undefined): string {
  if (!value) return '';
  const date = Array.isArray(value) ? value[0] : value;
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function Calendar({ id, value, onChange }: CalendarProps) {
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value;
    const date = raw ? new Date(`${raw}T00:00:00`) : null;
    const valid = date && !Number.isNaN(date.getTime()) ? date : null;
    onChange?.({ value: valid });
  };

  return <input id={id} data-testid={`${id ?? 'calendar'}-input`} value={toIso(value)} onChange={handleChange} readOnly={false} />;
}