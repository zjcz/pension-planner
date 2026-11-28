import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import HomePage from './HomePage';
import { render } from '../test/test-utils';
import { exportApi } from '../api/export';

describe('Export data dialog flow', () => {
  it('opens Export dialog from user menu with default options and conditional fields', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /User menu/i }));
    const exportItem = await screen.findByText('Export');
    await user.click(exportItem);

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Export Data')).toBeInTheDocument();

    const formatSelect = within(dialog).getByLabelText('Format') as HTMLSelectElement;
    const typeSelect = within(dialog).getByLabelText('Data to Export') as HTMLSelectElement;

    // Format defaults to CSV, Data to Export defaults to All
    expect(formatSelect.selectedOptions[0].text).toBe('CSV');
    expect(typeSelect.selectedOptions[0].text).toBe('All');
    expect(dialog.querySelector('input[role="switch"]')).toBeNull();
    expect(within(dialog).queryByLabelText('Parent Pension')).not.toBeInTheDocument();

    // Switch Data to Export to Pensions -> Include Statements switch is visible and defaults to checked (yes)
    await user.selectOptions(typeSelect, within(typeSelect).getByRole('option', { name: 'Pensions' }));
    const toggle = dialog.querySelector('input[role="switch"]') as HTMLInputElement;
    expect(toggle).not.toBeNull();
    expect(toggle.getAttribute('aria-checked')).toBe('true');

    // Parent Pension dropdown is not shown when Pensions is selected
    expect(within(dialog).queryByLabelText('Parent Pension')).not.toBeInTheDocument();

    // Switch Data to Export to Statements
    await user.selectOptions(typeSelect, within(typeSelect).getByRole('option', { name: 'Statements' }));

    // Include Statements switch disappears and Parent Pension dropdown appears defaulting to All
    expect(dialog.querySelector('input[role="switch"]')).toBeNull();
    const pensionSelect = within(dialog).getByLabelText('Parent Pension') as HTMLSelectElement;
    expect(pensionSelect.selectedOptions[0].text).toBe('All');

    // Select 'Aviva Workplace' in Parent Pension dropdown
    await user.selectOptions(
      pensionSelect,
      within(pensionSelect).getByRole('option', { name: 'Aviva Workplace' }),
    );

    // Select XML format
    await user.selectOptions(formatSelect, within(formatSelect).getByRole('option', { name: 'XML' }));

    const downloadSpy = vi.spyOn(exportApi, 'downloadZip').mockResolvedValueOnce();
    await user.click(within(dialog).getByRole('button', { name: /^Export$/i }));

    await waitFor(() => {
      expect(downloadSpy).toHaveBeenCalledWith({
        format: 'XML',
        type: 'STATEMENTS',
        includeStatements: undefined,
        pensionId: 1,
      });
    });
    downloadSpy.mockRestore();
  });

  it('exports Pensions with Include Statements toggled', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /User menu/i }));
    await user.click(await screen.findByText('Export'));

    const dialog = await screen.findByRole('dialog');
    const typeSelect = within(dialog).getByLabelText('Data to Export') as HTMLSelectElement;
    await user.selectOptions(typeSelect, within(typeSelect).getByRole('option', { name: 'Pensions' }));

    const toggle = dialog.querySelector('input[role="switch"]') as HTMLInputElement;
    await user.click(toggle);
    expect(toggle.getAttribute('aria-checked')).toBe('false');

    const downloadSpy = vi.spyOn(exportApi, 'downloadZip').mockResolvedValueOnce();
    await user.click(within(dialog).getByRole('button', { name: /^Export$/i }));

    await waitFor(() => {
      expect(downloadSpy).toHaveBeenCalledWith({
        format: 'CSV',
        type: 'PENSIONS',
        includeStatements: false,
        pensionId: null,
      });
    });
    downloadSpy.mockRestore();
  });

  it('exports All data types by default', async () => {
    const user = userEvent.setup();
    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /User menu/i }));
    await user.click(await screen.findByText('Export'));

    const dialog = await screen.findByRole('dialog');
    const downloadSpy = vi.spyOn(exportApi, 'downloadZip').mockResolvedValueOnce();
    await user.click(within(dialog).getByRole('button', { name: /^Export$/i }));

    await waitFor(() => {
      expect(downloadSpy).toHaveBeenCalledWith({
        format: 'CSV',
        type: 'ALL',
        includeStatements: undefined,
        pensionId: null,
      });
    });
    downloadSpy.mockRestore();
  });
});
