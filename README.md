# App Generator

This project is an AI-powered Full-Stack App Generator that autonomously designs, develops, tests, and deploys web applications based on high-level user descriptions.

## Architecture

*   **Backend:** Spring Boot (Java 17), PostgreSQL, Docker-Java, LangChain4j, JGit.
*   **Frontend:** React (Vite), TypeScript, TailwindCSS, Monaco Editor, React Flow, Zustand.
*   **Infrastructure:** Docker (for runtime environments), Traefik (reverse proxy for previews).

## Authentication & RBAC

The system implements JWT-based authentication and Role-Based Access Control (RBAC).

### Roles

*   **ADMIN**: Full access to project. Can delete project and manage members.
*   **MEMBER**: Can edit code, run/stop containers, and retry tasks.
*   **VIEWER**: Read-only access to code, logs, and status.

### Auth API

*   `POST /api/auth/register`: Create a new account.
*   `POST /api/auth/login`: Authenticate and receive Access/Refresh tokens.
*   `POST /api/auth/refresh`: Rotate refresh token and get new access token.
*   `POST /api/auth/logout`: Invalidate refresh token.

### Getting Started

1.  **Start Backend**:
    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

2.  **Start Frontend**:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

3.  **Access Application**:
    Open `http://localhost:5173`. You will be redirected to `/login`. Register a new account to begin.

## Development

### Pre-requisites

*   Java 17+
*   Node.js 18+
*   Docker Desktop / Engine
*   PostgreSQL 14+

### Environment Variables

Configure `backend/src/main/resources/application.yml` or set env vars:

*   `OPENAI_API_KEY`: Your OpenAI API Key.
*   `SPRING_DATASOURCE_URL`: Database URL.
*   `SPRING_DATASOURCE_USERNAME`: Database user.
*   `SPRING_DATASOURCE_PASSWORD`: Database password.
