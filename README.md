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

## Project Analysis & Pipeline

### Import Lifecycle
- **Pending**: Project created, waiting for git clone.
- **Cloning**: Asynchronously cloning remote repository. Idempotent operations prevent concurrent clones.
- **Success/Failed**: Completion status with timestamps and failure reasons.
- **Retry**: Use the "Sync" button to re-trigger import if failed or to pull latest changes.

### Code Analysis
- **Automatic Trigger**: Analysis runs automatically after successful import/clone.
- **Metadata**: Tracks file count, class count, method count, and analysis duration.
- **Concurrency Guard**: Prevents multiple analysis jobs from running simultaneously for the same project.
- **Dashboard**: View real-time status, metadata metrics, and recent operation logs in the "Analysis" panel.

## Resilient Workflow Execution

### Automatic Retries
The workflow engine includes a robust retry mechanism for transient failures (e.g., LLM timeouts, network glitches).
- **Max Retries**: Default 3 attempts per task.
- **Backoff Strategy**: Exponential backoff (initial delay 5s, factor 2.0).
- **Status**: Tasks waiting for retry display a `RETRY_WAIT` status and an orange refresh icon.
- **History**: Detailed attempt history (timestamp, error message) is available in the task summary view.

### Manual Controls
- **Retry Task**: Manually trigger a retry for any failed task. This resets the retry counter and forces a new execution attempt.
- **Retry Run**: Resume a failed workflow run from the last failed step.

## Troubleshooting

### Common Issues
- **Import Stuck in Cloning**: Check backend logs for Git timeout or network issues. The system auto-recovers on restart, or you can manually trigger "Sync".
- **Analysis Failed**: Often due to unparseable Java syntax. Check the "Analysis Failure Reason" in the dashboard.
- **Database JSON Errors**: The system uses a custom converter for JSON data in H2/PostgreSQL. Ensure your database dialect is correctly configured in `application.yml`.
- **Task Retries**: If a task fails repeatedly, check the "Retry History" in the task summary. Persistent failures (e.g., invalid prompt) may require manual intervention or code changes.

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
