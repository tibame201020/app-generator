from playwright.sync_api import sync_playwright

def verify_auth_and_ui():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        try:
            # 1. Register a new user
            print("Registering new user...")
            page.goto("http://localhost:5173/login")

            # Switch to register mode
            page.get_by_role("button", name="Register").click()

            page.fill("input[type='text']", "testuser")
            page.fill("input[type='email']", "test@example.com")
            page.fill("input[type='password']", "password123")

            page.get_by_role("button", name="Sign Up").click()

            # Wait for navigation to home
            page.wait_for_url("http://localhost:5173/")
            print("Registration successful!")

            # 2. Verify Home Page
            page.wait_for_selector("text=Your Projects")
            page.screenshot(path="verification_home.png")
            print("Home page verified.")

            # 3. Create a Project
            print("Creating project...")
            page.get_by_role("button", name="Import Project").click()
            page.fill("input[placeholder='Project Name']", "Demo Project")
            page.fill("input[placeholder='https://github.com/username/repo.git']", "https://github.com/tibame/demo.git")

            # Submit import
            page.get_by_role("button", name="Import", exact=True).click()

            # Wait for project to appear in list
            page.wait_for_selector("text=Demo Project")
            print("Project created.")

            # 4. Navigate to Project
            page.get_by_text("Demo Project").click()
            page.wait_for_url(lambda url: "/projects/" in url)
            print("Navigated to project page.")

            # 5. Verify RBAC UI elements
            # Check for Members tab button (Settings view)
            # The toolbar has view switcher: Code, Workflow, Analysis, Settings
            page.screenshot(path="verification_project.png")

            page.get_by_title("Project Settings").click()
            page.wait_for_selector("text=Project Members")
            page.screenshot(path="verification_members.png")
            print("Members tab verified.")

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="verification_error.png")
        finally:
            browser.close()

if __name__ == "__main__":
    verify_auth_and_ui()
