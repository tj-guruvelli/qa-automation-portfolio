import { expect, type Locator, type Page } from '@playwright/test';

/** /login on the-internet: username + password form with a flash banner. */
export class LoginPage {
  readonly username: Locator;
  readonly password: Locator;
  readonly submit: Locator;
  readonly flash: Locator;
  readonly heading: Locator;

  constructor(private readonly page: Page) {
    // The inputs have no <label>, so the stable attributes are their ids/names.
    this.username = page.locator('#username');
    this.password = page.locator('#password');
    this.submit = page.getByRole('button', { name: /login/i });
    this.flash = page.locator('#flash');
    this.heading = page.getByRole('heading', { level: 2 });
  }

  async goto(): Promise<void> {
    await this.page.goto('/login');
    await expect(this.heading).toHaveText('Login Page');
  }

  async loginAs(username: string, password: string): Promise<void> {
    await this.username.fill(username);
    await this.password.fill(password);
    await this.submit.click();
  }
}
