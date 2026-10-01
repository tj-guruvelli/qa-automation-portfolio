import { type Locator, type Page } from '@playwright/test';

/** /dropdown on the-internet: a single native select element. */
export class DropdownPage {
  readonly dropdown: Locator;

  constructor(private readonly page: Page) {
    this.dropdown = page.getByRole('combobox');
  }

  async goto(): Promise<void> {
    await this.page.goto('/dropdown');
  }

  async select(label: string): Promise<void> {
    await this.dropdown.selectOption({ label });
  }
}
