# App Generator - Dynamic System Construction Platform

A Low-Code/No-Code tool using AI agents for system generation, featuring a VS Code-style IDE and Docker-based runtime environments.

## Architecture

- **Frontend**: React, Vite, TypeScript, TailwindCSS, Monaco Editor, Zustand.
- **Backend**: Spring Boot 3, PostgreSQL, Docker Java Client, JGit.
- **Runtime**: Docker Containers managed by the backend, routed via Traefik.

## Features

- **Project Management**: Create, list, and manage projects.
- **Git Integration**: Every project is a Git repository.
- **Web IDE**: VS Code-style editor with file tree and code editing.
- **Instant Preview**: Run projects in isolated Docker containers with live preview (Run/Stop/Restart controls).
- **Agent Task Pipeline**: Real-time task execution and monitoring via WebSockets (Queue, Running, Progress, Completion).
- **Workflow Canvas**: Visual editor for defining and executing AI agent workflows (PM -> SA -> PG -> QA).

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker
- Maven

### Running the Backend

```bash
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

## Development

- **Frontend**: `frontend/src` contains the React application.
- **Backend**: `backend/src` contains the Spring Boot application.
- **Data**: `data/repos` stores bare git repositories, `data/workspaces` stores runtime workspaces.

## License

MIT
