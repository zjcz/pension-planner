import type { ChangeEvent } from 'react';

interface Option {
  label: string;
  value: unknown;
}

interface DropdownProps {
  id?: string;
  value?: unknown;
  options?: Option[];
  onChange?: (e: { value: unknown }) => void;
  placeholder?: string;
  [key: string]: unknown;
}

export function Dropdown({ id, value, options = [], onChange, placeholder }: DropdownProps) {
  const selectedIndex = options.findIndex((o) => o.value === value);

  const handleChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const idx = Number(e.target.value);
    if (!Number.isNaN(idx) && options[idx]) {
      onChange?.({ value: options[idx].value });
    }
  };

  return (
    <select
      id={id}
      aria-label={placeholder}
      value={selectedIndex >= 0 ? String(selectedIndex) : ''}
      onChange={handleChange}
    >
      {options.map((opt, idx) => (
        <option key={`${opt.label}-${idx}`} value={String(idx)}>
          {opt.label}
        </option>
      ))}
    </select>
  );
}
