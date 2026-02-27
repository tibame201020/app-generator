from playwright.sync_api import sync_playwright

def verify_internal_chat():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={'width': 1280, 'height': 720})
        page = context.new_page()

        # Capture console logs
        page.on("console", lambda msg: print(f"CONSOLE: {msg.text}"))
        page.on("pageerror", lambda err: print(f"PAGE ERROR: {err}"))

        print("Navigating to http://localhost:5173")
        try:
            page.goto("http://localhost:5173", wait_until="networkidle", timeout=10000)
        except Exception as e:
            print(f"Navigation failed: {e}")
            return

        print("Page loaded. Taking initial screenshot.")
        page.screenshot(path="frontend_debug.png")

        # Check for the button
        print("Looking for 'Show Internals' button...")
        try:
            page.click("text=Show Internals", timeout=5000)
            print("Clicked 'Show Internals'.")
            page.screenshot(path="frontend_debug_clicked.png")
        except Exception as e:
            print(f"Could not find or click button: {e}")

        browser.close()

if __name__ == "__main__":
    verify_internal_chat()
