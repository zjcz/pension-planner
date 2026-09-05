import '@testing-library/jest-dom/vitest';
import { beforeAll, afterAll, afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import { server } from './server';
import { resetHandlersState } from './handlers';

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

(globalThis as any).ResizeObserver = ResizeObserverMock;

vi.mock('primereact/calendar', async () => {
  const { Calendar } = await import('./mocks/calendar');
  return { default: Calendar, Calendar };
});

vi.mock('primereact/inputnumber', async () => {
  const { InputNumber } = await import('./mocks/inputNumber');
  return { default: InputNumber, InputNumber };
});

vi.mock('primereact/chart', async () => {
  const { Chart } = await import('./mocks/chart');
  return { default: Chart, Chart };
});

vi.mock('primereact/multiselect', async () => {
  const { MultiSelect } = await import('./mocks/multiSelect');
  return { default: MultiSelect, MultiSelect };
});

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
  resetHandlersState();
  localStorage.clear();
});

afterAll(() => {
  server.close();
});
