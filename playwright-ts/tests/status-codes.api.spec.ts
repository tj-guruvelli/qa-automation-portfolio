import { expect, test } from '@playwright/test';

/**
 * API-layer checks using Playwright's request fixture: no browser is launched.
 * /status_codes/{code} answers with that HTTP status and a short explanation.
 */
test.describe('Status code endpoints', { tag: '@api' }, () => {
  test('GET /status_codes/404 responds 404 and explains the status', async ({ request }) => {
    const response = await request.get('/status_codes/404');

    expect(response.status()).toBe(404);
    expect(response.headers()['content-type']).toContain('text/html');
    expect(await response.text()).toContain('This page returned a 404 status code.');
  });

  test('GET /status_codes/200 responds 200', async ({ request }) => {
    const response = await request.get('/status_codes/200');

    expect(response.ok()).toBe(true);
    expect(await response.text()).toContain('This page returned a 200 status code.');
  });
});
