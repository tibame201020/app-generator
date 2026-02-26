# App Generator - Dynamic System Construction Platform

A Low-Code/No-Code tool using AI agents for system generation, featuring a VS Code-style IDE and Docker-based runtime environments.

## Architecture

- **Frontend**: React, Vite, TypeScript, TailwindCSS, Monaco Editor, Zustand.
- **Backend**: Spring Boot 3, PostgreSQL, Docker Java Client, JGit.
- **Runtime**: Docker Containers managed by the backend, routed via Traefik.

## Features

- **Project Management**: Create, list, and manage projects.
- **Import Project**: Import existing projects from remote Git repositories.
- **Git Integration**: Every project is a Git repository.
- **Code Analysis**: AST-based analysis of Java projects (Packages, Classes, Methods, Fields, Dependencies).
- **Web IDE**: VS Code-style editor with file tree and code editing.
- **Instant Preview**: Run projects in isolated Docker containers with live preview (Run/Stop/Restart controls).
- **Agent Task Pipeline**: Real-time task execution and monitoring via WebSockets (Queue, Running, Progress, Completion).
- **Workflow Canvas**: Visual editor for defining and executing AI agent workflows (PM -> SA -> PG -> QA).
- **Workflow Observability**: Detailed run history, task-level inspection (logs, input context, output summary), and retry controls for failed tasks/runs.

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker
- Maven

### LLM Configuration

Configure your LLM provider in `backend/src/main/resources/application.yml` or via environment variables:

```yaml
platform:
  llm:
    provider: openai
    api-key: ${OPENAI_API_KEY} # Required
    model-name: gpt-4o
    timeout: 60
    max-retries: 3
```

### Running the Backend

```bash
export OPENAI_API_KEY=your_key_here
cd backend
mvn spring-boot:run
```

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`.
The backend runs on `http://localhost:8080`.

## Workflow Run & Retry

- **Inspect Runs**: View history of workflow runs in the "Runs" panel.
- **Task Details**: Click on a task to view its logs, input context, and execution summary.
- **Retry**:
  - **Retry Task**: Re-execute a failed task with its original input context. If successful, the workflow automatically resumes from that point.
  - **Retry Run**: Resume a failed workflow run from the last failed task.

## Development

- **Frontend**: `frontend/src` contains the React application.
- **Backend**: `backend/src` contains the Spring Boot application.
- **Data**: `data/repos` stores bare git repositories, `data/workspaces` stores runtime workspaces.

## License

MIT
