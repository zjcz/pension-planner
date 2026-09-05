import { screen, waitFor, within, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

describe('Phase 3 FE smoke: modal create/edit flow', () => {
  it('creates a pension via the Add Pension modal and shows it in the table', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Add Pension/i }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Add Pension')).toBeInTheDocument();

    await user.type(within(dialog).getByLabelText(/Name \*/i), 'New SIPP');
    fireEvent.change(within(dialog).getByLabelText(/Maturity Month \*/i), { target: { value: '2045-01-01' } });
    await user.click(within(dialog).getByRole('button', { name: /^Create$/i }));

    await waitFor(() => {
      expect(screen.getByText('New SIPP')).toBeInTheDocument();
    });
  });

  it('requires a maturity date before creating', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Add Pension/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/Name \*/i), 'Incomplete');
    await user.click(within(dialog).getByRole('button', { name: /^Create$/i }));

    expect(await within(dialog).findByText('Maturity date is required')).toBeInTheDocument();
  });

  it('opens edit modal pre-populated with existing pension values', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
    });

    const row = screen.getByText('Aviva Workplace').closest('tr');
    expect(row).not.toBeNull();
    const editButton = within(row as HTMLElement).getByRole('button', { name: /Edit/i });
    await user.click(editButton);

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Edit Pension')).toBeInTheDocument();
    const nameInput = within(dialog).getByLabelText(/Name \*/i) as HTMLInputElement;
    expect(nameInput.value).toBe('Aviva Workplace');
  });

  it('submits an edit that persists to the table via the update flow', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
    });

    const row = screen.getByText('Aviva Workplace').closest('tr');
    const editButton = within(row as HTMLElement).getByRole('button', { name: /Edit/i });
    await user.click(editButton);

    const dialog = await screen.findByRole('dialog');
    const nameInput = within(dialog).getByLabelText(/Name \*/i);
    await user.clear(nameInput);
    await user.type(nameInput, 'Renamed Pension');
    await user.click(within(dialog).getByRole('button', { name: /Save Changes/i }));

    await waitFor(() => {
      expect(screen.getByText('Renamed Pension')).toBeInTheDocument();
    });
  });
});