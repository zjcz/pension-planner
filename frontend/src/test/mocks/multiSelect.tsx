import { useState } from 'react';
import type { ReactNode } from 'react';

interface Option {
  label: string;
  value: number | string;
}

interface MultiSelectProps {
  id?: string;
  value?: (number | string)[];
  options?: Option[];
  onChange?: (e: { value: (number | string)[] }) => void;
  onFilter?: (e: { filter: string }) => void;
  panelFooterTemplate?: ReactNode;
  placeholder?: string;
  [key: string]: unknown;
}

export function MultiSelect({
  id,
  value = [],
  options = [],
  onChange,
  onFilter,
  panelFooterTemplate,
  placeholder,
}: MultiSelectProps) {
  const [open, setOpen] = useState(false);
  const [filter, setFilter] = useState('');

  return (
    <div data-testid={`${id}-multiselect`}>
      <input
        id={id}
        placeholder={placeholder}
        value={open ? filter : ''}
        onFocus={() => setOpen(true)}
        onChange={(e) => {
          const next = e.target.value;
          setFilter(next);
          onFilter?.({ filter: next });
        }}
      />
      {open && (
        <div data-testid={`${id}-panel`}>
          {options
            .filter((o) => o.label.toLowerCase().includes(filter.toLowerCase()))
            .map((o) => (
              <label key={String(o.value)}>
                <input type="checkbox" checked={value.includes(o.value)} onChange={() => onChange?.({ value: select(value, o.value) })} />
                {o.label}
              </label>
            ))}
          {filter !== '' && panelFooterTemplate}
        </div>
      )}
    </div>
  );
}

function select(current: (number | string)[], value: number | string): (number | string)[] {
  return current.includes(value) ? current.filter((v) => v !== value) : [...current, value];
}