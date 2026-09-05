import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

async function waitForOtherIncomeTable() {
  await waitFor(() => {
    expect(screen.getByText('Rental Income')).toBeInTheDocument();
  });
}

describe('Phase 6 FE smoke: other income table + add/edit/delete flows', () => {
  it('renders the other income table with existing rows', async () => {
    render(<HomePage />);
    await waitForOtherIncomeTable();

    expect(screen.getByText('Other Income')).toBeInTheDocument();
    expect(screen.getByText('Dividends')).toBeInTheDocument();
    expect(screen.getByText('£6,000')).toBeInTheDocument();
  });

  it('creates a stream via the Add Income dialog and shows it in the table', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForOtherIncomeTable();

    await user.click(screen.getByRole('button', { name: /Add Income/i }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Add Other Income')).toBeInTheDocument();

    await user.type(within(dialog).getByLabelText(/Name \*/i), 'Freelance Work');
    await user.type(within(dialog).getByLabelText(/Annual Amount \*/i), '3000');
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.getByText('Freelance Work')).toBeInTheDocument();
    });
  });

  it('edits a stream via the dialog pre-populated with existing values', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForOtherIncomeTable();

    const row = screen.getByText('Rental Income').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Edit' }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Edit Other Income')).toBeInTheDocument();
    const nameInput = within(dialog).getByLabelText(/Name \*/i) as HTMLInputElement;
    expect(nameInput.value).toBe('Rental Income');

    await user.clear(nameInput);
    await user.type(nameInput, 'Flat Rental');
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.getByText('Flat Rental')).toBeInTheDocument();
    });
  });

  it('deletes a stream via the confirm dialog', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForOtherIncomeTable();

    const row = screen.getByText('Dividends').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Delete' }));

    const confirm = await screen.findByRole('dialog');
    expect(within(confirm).getByText(/Delete "Dividends"\?/)).toBeInTheDocument();
    await user.click(within(confirm).getByRole('button', { name: 'Delete' }));

    await waitFor(() => {
      expect(screen.queryByText('Dividends')).not.toBeInTheDocument();
    });
  });
});