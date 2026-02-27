from playwright.sync_api import sync_playwright

def verify_frontend():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        try:
            # Navigate to the frontend
            # Assuming the dev server will be running on port 5173 (Vite default)
            page.goto("http://localhost:5173")

            # Wait for the page to load
            page.wait_for_load_state("networkidle")

            # Verify the connection status card is visible
            # The exact text depends on whether backend is running or not.
            # If backend is not running, it should show 'CONNECTING' or 'ERROR' or 'CLOSED'.
            # We just want to see the UI.

            # Take a screenshot
            page.screenshot(path="frontend_verification.png")
            print("Screenshot taken: frontend_verification.png")

        except Exception as e:
            print(f"Verification failed: {e}")
        finally:
            browser.close()

if __name__ == "__main__":
    verify_frontend()
