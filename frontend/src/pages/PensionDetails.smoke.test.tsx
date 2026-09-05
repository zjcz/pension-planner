import { screen, waitFor, within, fireEvent, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PrimeReactProvider } from 'primereact/api';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../auth/AuthContext';
import PensionDetailsPage from '../pages/PensionDetailsPage';

function makeQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: Infinity },
      mutations: { retry: false },
    },
  });
}

function renderDetails() {
  const client = makeQueryClient();
  render(
    <PrimeReactProvider>
      <QueryClientProvider client={client}>
        <AuthProvider>
          <MemoryRouter initialEntries={['/pensions/1']}>
            <Routes>
              <Route path="/pensions/:id" element={<PensionDetailsPage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    </PrimeReactProvider>,
  );
  return client;
}

describe('Phase 6 FE smoke: statements table + chart render; add/edit/delete flows', () => {
  it('renders the overview, performance chart and statements table', async () => {
    renderDetails();

    await waitFor(() => {
      expect(screen.getByText('Overview')).toBeInTheDocument();
    });

    expect(screen.getByText('Performance')).toBeInTheDocument();
    expect(screen.getByText('Statements')).toBeInTheDocument();
    expect(screen.getAllByText('Aviva Workplace').length).toBeGreaterThan(0);
    await waitFor(() => {
      expect(screen.getByText('1 Jan 2025')).toBeInTheDocument();
    });
    expect(screen.getByText('1 Jan 2026')).toBeInTheDocument();
  });

  it('creates a statement via the dialog and shows it in the table', async () => {
    const user = userEvent.setup();
    renderDetails();

    await waitFor(() => {
      expect(screen.getByText('1 Jan 2025')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Add Statement/i }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Add Statement')).toBeInTheDocument();

    fireEvent.change(within(dialog).getByLabelText(/Statement Date \*/i), { target: { value: '2026-06-01' } });
    await user.type(within(dialog).getByLabelText(/Plan Value \*/i), '31000');
    await user.type(within(dialog).getByLabelText(/Projected Annual Amount \*/i), '1500');
    await user.click(within(dialog).getByRole('button', { name: /^Create$/i }));

    await waitFor(() => {
      expect(screen.getByText('1 Jun 2026')).toBeInTheDocument();
    });
    expect(screen.getByText('£31,000')).toBeInTheDocument();
  });

  it('edits a statement pre-populated with existing values', async () => {
    const user = userEvent.setup();
    renderDetails();

    await waitFor(() => {
      expect(screen.getByText('1 Jan 2025')).toBeInTheDocument();
    });

    const row = screen.getByText('1 Jan 2025').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Edit' }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Edit Statement')).toBeInTheDocument();
    expect((within(dialog).getByLabelText(/Plan Value \*/i) as HTMLInputElement).value).toBe('25000');

    await user.clear(within(dialog).getByLabelText(/Plan Value \*/i));
    await user.type(within(dialog).getByLabelText(/Plan Value \*/i), '26000');
    await user.click(within(dialog).getByRole('button', { name: /Save Changes/i }));

    await waitFor(() => {
      expect(screen.getByText('£26,000')).toBeInTheDocument();
    });
  });

  it('deletes a statement via the confirm dialog', async () => {
    const user = userEvent.setup();
    renderDetails();

    await waitFor(() => {
      expect(screen.getByText('1 Jan 2026')).toBeInTheDocument();
    });

    const row = screen.getByText('1 Jan 2026').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Delete' }));

    const confirm = await screen.findByRole('dialog');
    expect(within(confirm).getByText(/Delete this statement\?/)).toBeInTheDocument();
    await user.click(within(confirm).getByRole('button', { name: 'Delete' }));

    await waitFor(() => {
      expect(screen.queryByText('1 Jan 2026')).not.toBeInTheDocument();
    });
  });
});