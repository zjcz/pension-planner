import { screen, waitFor, within, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

async function waitForTables() {
  await waitFor(() => {
    expect(screen.getByText('Aviva Workplace')).toBeInTheDocument();
  });
  await waitFor(() => {
    expect(screen.getByText('Rental Income')).toBeInTheDocument();
  });
}

describe('Phase 9 FE smoke: tag creation and assignment in pension/income dialogs', () => {
  it('creates a new tag from the pension dialog filter and assigns it', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForTables();

    await user.click(screen.getByRole('button', { name: /Add Pension/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/Name \*/i), 'New SIPP');
    fireEvent.change(within(dialog).getByLabelText(/Maturity Month \*/i), { target: { value: '2045-01-01' } });

    const tagsControl = within(dialog).getByLabelText(/Tags/i);
    await user.click(tagsControl);
    await user.type(tagsControl, 'LISA');

    expect(
      within(dialog).getByText((_, el) => el?.textContent === 'Create "LISA" as new tag'),
    ).toBeInTheDocument();
    await user.click(within(dialog).getByRole('button', { name: 'Create tag' }));

    await user.click(within(dialog).getByRole('button', { name: /^Create$/i }));

    await waitFor(() => {
      expect(screen.getByText('New SIPP')).toBeInTheDocument();
    });
    const row = screen.getByText('New SIPP').closest('tr');
    expect(row).not.toBeNull();
    expect(within(row as HTMLElement).getByText('LISA')).toBeInTheDocument();
  });

  it('assigns an existing tag to a new other income stream', async () => {
    const user = userEvent.setup();
    render(<HomePage />);
    await waitForTables();

    await user.click(screen.getByRole('button', { name: /Add Income/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/Name \*/i), 'Freelance Work');
    await user.type(within(dialog).getByLabelText(/Annual Amount \*/i), '3000');

    const tagsControl = within(dialog).getByLabelText(/Tags/i);
    await user.click(tagsControl);
    await user.click(within(dialog).getByText('ISA'));

    await user.click(within(dialog).getByRole('button', { name: /^Create$/i }));

    await waitFor(() => {
      expect(screen.getByText('Freelance Work')).toBeInTheDocument();
    });
    const row = screen.getByText('Freelance Work').closest('tr');
    expect(row).not.toBeNull();
    expect(within(row as HTMLElement).getByText('ISA')).toBeInTheDocument();
  });
});