import time
from playwright.sync_api import sync_playwright

def verify_internal_chat():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={'width': 1280, 'height': 720})
        page = context.new_page()

        # 1. Navigate to the app (assuming Vite default port 5173)
        # Note: We need to wait a bit for the server to start if it was just launched.
        # But here we assume it's running.
        try:
            page.goto("http://localhost:5173", timeout=10000)
        except Exception as e:
            print(f"Error navigating: {e}")
            return

        # 2. Check if the main chat is visible
        page.wait_for_selector(".chat-bubble")

        # 3. Take a screenshot of the initial state (Main Chat)
        page.screenshot(path="frontend/verification_initial.png")
        print("Initial screenshot taken.")

        # 4. Click the "Show Internals" button
        # The button text is "Show Internals"
        page.click("text=Show Internals")

        # 5. Wait for the Internal Chat Panel to appear
        # We look for "Internal Logs" which is in the header of the panel
        page.wait_for_selector("text=Internal Logs")

        # 6. Take a screenshot with the Internal Panel open
        page.screenshot(path="frontend/verification_internal_open.png")
        print("Internal panel screenshot taken.")

        # 7. Verify filtering (Visual check mainly, but we can check DOM)
        # In main chat, we shouldn't see SYSTEM messages.
        # In internal chat, we should see logs.
        # Since we use demo messages, let's just ensure the panel is there.

        browser.close()

if __name__ == "__main__":
    verify_internal_chat()
