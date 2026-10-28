import { screen, waitFor, within, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import HomePage from '../pages/HomePage';
import { render } from '../test/test-utils';

describe('Phase 8 FE smoke: settings save reflects on dashboard', () => {
  it('loads existing settings into the form', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /User menu/i }));
    const settingsItem = await screen.findByText('Settings');
    await user.click(settingsItem);

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Settings')).toBeInTheDocument();
    expect((within(dialog).getByLabelText(/Target Annual Income/i) as HTMLInputElement).value).toBe('30000');
    expect((within(dialog).getByLabelText(/Retirement Date/i) as HTMLInputElement).value).toBe('2045-01-01');
  });

  it('saves new settings and reflects them in the dashboard cards', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });
    expect(screen.getByText('vs £30,000 target')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /User menu/i }));
    const settingsItem2 = await screen.findByText('Settings');
    await user.click(settingsItem2);

    const dialog = await screen.findByRole('dialog');
    const targetInput = within(dialog).getByLabelText(/Target Annual Income/i);
    await user.clear(targetInput);
    await user.type(targetInput, '40000');
    fireEvent.change(within(dialog).getByLabelText(/Retirement Date/i), { target: { value: '2050-06-01' } });
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    await waitFor(() => {
      expect(screen.getByText('vs £40,000 target')).toBeInTheDocument();
    });
    expect(screen.getByText('1 Jun 2050')).toBeInTheDocument();
  });

  async function setAuditLogging(user: ReturnType<typeof userEvent.setup>, enabled: boolean) {
    await user.click(screen.getByRole('button', { name: /User menu/i }));
    const settingsItem3 = await screen.findByText('Settings');
    await user.click(settingsItem3);

    const dialog = await screen.findByRole('dialog');
    const toggle = dialog.querySelector('input[role="switch"]') as HTMLInputElement;
    if ((toggle.getAttribute('aria-checked') === 'true') !== enabled) {
      await user.click(toggle);
      expect(toggle.getAttribute('aria-checked')).toBe(String(enabled));
    }
    await user.click(within(dialog).getByRole('button', { name: /^Save$/i }));

    if (!enabled) {
      const disable = await screen.findByRole('button', { name: /Disable and delete/i });
      await user.click(disable);
    }
  }

  it('hides and restores the audit history buttons when the setting is toggled', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getAllByLabelText('Audit History')).toHaveLength(6);
    });

    await setAuditLogging(user, false);
    await waitFor(() => {
      expect(screen.queryAllByLabelText('Audit History')).toHaveLength(0);
    });

    await setAuditLogging(user, true);
    await waitFor(() => {
      expect(screen.getAllByLabelText('Audit History')).toHaveLength(6);
    });
  });
});