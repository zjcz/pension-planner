import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

async function waitForStatePensionTable() {
  await waitFor(() => {
    expect(screen.getByText('State Pension')).toBeInTheDocument();
  });
}

describe('FE smoke: state pensions table + add/edit/delete flows', () => {
  it('renders the state pensions table with existing rows', async () => {
    render(<HomePage />);
    await waitForStatePensionTable();

    expect(screen.getByText('State Pensions')).toBeInTheDocument();
    expect(screen.getByText('Partner Pension')).toBeInTheDocument();
    expect(screen.getByText('£11,000')).toBeInTheDocument();
  });

  it('creates a record via the Add State Pension dialog and shows it in the table', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForStatePensionTable();

    await user.click(screen.getByRole('button', { name: /Add State Pension/i }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Add State Pension')).toBeInTheDocument();

    await user.type(within(dialog).getByLabelText(/Name \*/i), 'New SP');
    await user.type(within(dialog).getByLabelText(/Yearly Amount \*/i), '9000');
    await user.type(within(dialog).getByLabelText(/Takes Effect Year \*/i), '2035');
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.getByText('New SP')).toBeInTheDocument();
    });
  });

  it('edits a record via the dialog pre-populated with existing values', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForStatePensionTable();

    const row = screen.getByText('State Pension').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Edit' }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Edit State Pension')).toBeInTheDocument();
    const nameInput = within(dialog).getByLabelText(/Name \*/i) as HTMLInputElement;
    expect(nameInput.value).toBe('State Pension');

    await user.clear(nameInput);
    await user.type(nameInput, 'My State Pension');
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.getByText('My State Pension')).toBeInTheDocument();
    });
  });

  it('deletes a record via the confirm dialog', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForStatePensionTable();

    const row = screen.getByText('Partner Pension').closest('tr');
    expect(row).not.toBeNull();
    await user.click(within(row as HTMLElement).getByRole('button', { name: 'Delete' }));

    const confirm = await screen.findByRole('dialog');
    expect(within(confirm).getByText(/Delete "Partner Pension"\?/)).toBeInTheDocument();
    await user.click(within(confirm).getByRole('button', { name: 'Delete' }));

    await waitFor(() => {
      expect(screen.queryByText('Partner Pension')).not.toBeInTheDocument();
    });
  });
});