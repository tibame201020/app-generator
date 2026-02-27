import time
from playwright.sync_api import sync_playwright

def verify_agent_connection():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        print("Navigating to frontend...")
        try:
            page.goto("http://localhost:5173", timeout=30000)
        except Exception as e:
            print(f"Failed to load page: {e}")
            browser.close()
            return

        # Wait for the badge to appear
        print("Waiting for connection badge...")
        try:
            # We expect 'Agent Stream Connected' or 'Disconnected' badge
            # Given we have backend running, it should be connected or at least show the disconnected badge initially
            # The badge has class 'badge-lg'
            badge = page.locator(".badge-lg")
            badge.wait_for(state="visible", timeout=10000)

            # Take a screenshot
            screenshot_path = "verification_agent_stream.png"
            page.screenshot(path=screenshot_path)
            print(f"Screenshot saved to {screenshot_path}")

            # Check text content
            text = badge.text_content()
            print(f"Badge text: {text}")

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="verification_failed.png")

        browser.close()

if __name__ == "__main__":
    # Give some time for services to start
    time.sleep(10)
    verify_agent_connection()
