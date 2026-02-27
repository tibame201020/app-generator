import { test, expect } from '@playwright/test';

test('verify layout components', async ({ page }) => {
  // Navigate to the app
  await page.goto('http://localhost:5173');

  // Verify Navbar
  await expect(page.locator('.navbar')).toBeVisible();
  // Check specifically inside the navbar to avoid ambiguity
  await expect(page.locator('.navbar').getByText('Jules Software Factory')).toBeVisible();
  await expect(page.locator('.navbar').getByText('Phase 4: Frontend UI')).toBeVisible();

  // Verify Sidebar (Drawer)
  // Check for the "Project Tracker" title in the sidebar
  await expect(page.locator('.menu-title').getByText('Project Tracker')).toBeVisible();

  // Check for a phase
  await expect(page.getByText('Phase 1: Foundation & Specs')).toBeVisible();

  // Check for a task status badge
  await expect(page.locator('.badge-success').first()).toBeVisible();

  // Take a screenshot
  await page.screenshot({ path: 'frontend/tests/verification/layout_verification.png', fullPage: true });
});
