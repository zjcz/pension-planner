import type { ChangeEvent } from 'react';

interface InputNumberProps {
  id?: string;
  value?: number | null;
  onValueChange?: (e: { value?: number | null }) => void;
  [key: string]: unknown;
}

export function InputNumber({ id, value, onValueChange }: InputNumberProps) {
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value;
    onValueChange?.({ value: raw === '' ? null : Number(raw) });
  };

  return <input id={id} data-testid={`${id ?? 'inputnumber'}-input`} type="text" inputMode="numeric" value={value ?? ''} onChange={handleChange} />;
}