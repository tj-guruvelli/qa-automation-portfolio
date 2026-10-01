import { expect, test } from '@playwright/test';
import { DynamicLoadingPage } from '../pages/DynamicLoadingPage';

test.describe('Dynamic loading', { tag: '@smoke' }, () => {
  test('result rendered after an async delay is waited for explicitly', async ({ page }) => {
    const loading = new DynamicLoadingPage(page);
    await loading.goto();
    await expect(loading.result).toBeHidden();

    await loading.clickStart();

    // Spinner appears, then goes away, then the result element exists and is visible.
    await expect(loading.loading).toBeHidden({ timeout: 15_000 });
    await expect(loading.result).toBeVisible({ timeout: 15_000 });
    await expect(loading.result).toHaveText('Hello World!');
  });
});
