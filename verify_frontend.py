from playwright.sync_api import sync_playwright

def verify_internal_log_panel():
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        # 1. Navigate to the app
        print("Navigating to app...")
        page.goto("http://localhost:5173")

        # 2. Wait for app to load
        page.wait_for_selector("text=Jules Software Factory")
        print("App loaded.")

        # 3. Check if Internal Log Panel header is visible (collapsed state)
        # The header contains "Internal Logs" text
        log_header = page.get_by_text("Internal Logs")
        if log_header.is_visible():
            print("Internal Log Panel header is visible.")
        else:
            print("Error: Internal Log Panel header NOT found.")

        # 4. Click to expand
        print("Clicking to expand log panel...")
        log_header.click()

        # 5. Check if content area is visible
        # We look for "No system logs yet..." or any log entry.
        # Since the demo data in App.tsx doesn't push to 'messages' context (it uses 'demoMessages'),
        # the 'messages' array in context starts empty unless we simulate websocket.
        # But wait, the 'messages' in context come from useAgentStream.
        # In dev, without backend, it might be empty or error.
        # The component renders "No system logs yet..." if empty.

        empty_state = page.get_by_text("No system logs yet...")
        if empty_state.is_visible():
             print("Log panel expanded and showing empty state.")
        else:
             print("Log panel expanded but empty state text not found (maybe logs exist?).")

        # 6. Take Screenshot
        print("Taking screenshot...")
        page.screenshot(path="verification_log_panel.png")
        print("Screenshot saved to verification_log_panel.png")

        browser.close()

if __name__ == "__main__":
    verify_internal_log_panel()
