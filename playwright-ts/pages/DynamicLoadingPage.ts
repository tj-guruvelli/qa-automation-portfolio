import { type Locator, type Page } from '@playwright/test';

/**
 * /dynamic_loading/2 on the-internet: Start shows a spinner for a few seconds,
 * then renders a "Hello World!" element that did not exist before.
 */
export class DynamicLoadingPage {
  readonly start: Locator;
  readonly loading: Locator;
  readonly result: Locator;

  constructor(private readonly page: Page) {
    this.start = page.getByRole('button', { name: 'Start' });
    this.loading = page.locator('#loading');
    this.result = page.locator('#finish').getByRole('heading', { level: 4 });
  }

  async goto(): Promise<void> {
    await this.page.goto('/dynamic_loading/2');
  }

  async clickStart(): Promise<void> {
    await this.start.click();
  }
}
