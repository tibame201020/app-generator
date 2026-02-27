from playwright.sync_api import sync_playwright

def verify_internal_chat():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        # Increase viewport width to ensure desktop layout
        context = browser.new_context(viewport={'width': 1280, 'height': 720})
        page = context.new_page()

        print("Navigating to http://localhost:5173")
        try:
            # Wait for network idle to ensure everything loaded
            page.goto("http://localhost:5173", wait_until="networkidle", timeout=30000)
        except Exception as e:
            print(f"Navigation failed: {e}")
            return

        print("Page loaded. Taking initial screenshot.")
        page.screenshot(path="frontend_verification_initial.png")

        # Check for the button
        print("Looking for 'Show Internals' button...")
        try:
            # The button text might be inside a span or just text node, let's try strict text match or partial
            page.click("text=Show Internals", timeout=5000)
            print("Clicked 'Show Internals'.")
        except Exception as e:
            print(f"Could not find or click button: {e}")
            page.screenshot(path="frontend_error_click.png")
            return

        # Wait for the panel to appear
        print("Waiting for Internal Logs panel...")
        try:
            # We look for "Internal Logs" text which is in the header
            page.wait_for_selector("text=Internal Logs", timeout=5000)
            print("Internal Logs panel visible.")
        except Exception as e:
            print(f"Panel did not appear: {e}")
            page.screenshot(path="frontend_error_panel.png")
            return

        # Take screenshot with panel open
        page.screenshot(path="frontend_verification_open.png")
        print("Final screenshot taken: frontend_verification_open.png")

        browser.close()

if __name__ == "__main__":
    verify_internal_chat()
