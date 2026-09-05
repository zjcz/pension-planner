import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

describe('Phase 5 FE smoke: state pension form loads existing record and saves', () => {
  it('loads the existing state pension record into the form', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Manage State Pension/i }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Manage State Pension')).toBeInTheDocument();

    expect((within(dialog).getByLabelText(/Yearly Amount \*/i) as HTMLInputElement).value).toBe('11000');
    expect((within(dialog).getByLabelText(/Takes Effect Year \*/i) as HTMLInputElement).value).toBe('2040');
  });

  it('saves an edited state pension and reflects it in dashboard totals', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Manage State Pension/i }));

    const dialog = await screen.findByRole('dialog');
    const yearlyInput = within(dialog).getByLabelText(/Yearly Amount \*/i);
    await user.clear(yearlyInput);
    await user.type(yearlyInput, '12000');
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText('£12,000/yr')).toBeInTheDocument();
    });
  });
});