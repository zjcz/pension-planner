import type { ReactNode } from 'react';

interface ChartProps {
  type?: string;
  data?: unknown;
  options?: unknown;
  [key: string]: unknown;
}

export function Chart({ type = 'chart' }: ChartProps): ReactNode {
  return <div role="img" aria-label={type} data-testid={`chart-${type}`} />;
}