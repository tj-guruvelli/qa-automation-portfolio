import { expect, test } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

// Published demo credentials for the-internet.herokuapp.com/login (not a secret).
const VALID_USER = 'tomsmith';
const VALID_PASSWORD = 'SuperSecretPassword!';

test.describe('Login', { tag: '@smoke' }, () => {
  test('valid credentials reach the secure area', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();

    await login.loginAs(VALID_USER, VALID_PASSWORD);

    await expect(page).toHaveURL(/\/secure$/);
    await expect(page.getByRole('heading', { level: 2 })).toHaveText('Secure Area');
    await expect(login.flash).toContainText('You logged into a secure area!');
  });

  test('wrong password shows an error and stays on the login page', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();

    await login.loginAs(VALID_USER, 'not-the-password');

    await expect(login.flash).toContainText('Your password is invalid!');
    await expect(page).toHaveURL(/\/login$/);
  });
});
