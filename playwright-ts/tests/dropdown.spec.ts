import { expect, test } from '@playwright/test';
import { DropdownPage } from '../pages/DropdownPage';

test.describe('Dropdown', { tag: '@smoke' }, () => {
  test('selecting an option by label updates the selected value', async ({ page }) => {
    const dropdown = new DropdownPage(page);
    await dropdown.goto();
    await expect(dropdown.dropdown).toHaveValue('');

    await dropdown.select('Option 2');

    await expect(dropdown.dropdown).toHaveValue('2');
    await expect(dropdown.dropdown.locator('option:checked')).toHaveText('Option 2');
  });
});
