from playwright.sync_api import sync_playwright

def verify_system_status_panel():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        try:
            # Navigate to the home page
            page.goto("http://localhost:3000/")

            # Wait for the status panel to appear
            # The button has text "System: ..."
            # We look for a button containing "System:"
            # Increase timeout as app might be slow to load
            status_button = page.get_by_text("System:").first

            print("Looking for 'System:' text...")
            status_button.wait_for(timeout=60000)

            print("Status button found.")

            # Click the button to open the popover
            status_button.click()

            # Wait for popover content to be visible
            # It should contain "Backend Status"
            page.get_by_text("Backend Status").wait_for(timeout=10000)
            print("Popover opened and 'Backend Status' found.")

            # Take a screenshot
            page.screenshot(path="verification_status_panel.png")
            print("Screenshot saved to verification_status_panel.png")

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="verification_failed.png")

if __name__ == "__main__":
    verify_system_status_panel()
