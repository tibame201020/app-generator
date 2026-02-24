# Changelog

## [0.7.0] - 2026-02-24

### Added
- **Real-time Agent Task Pipeline**:
  - Implement WebSocket (STOMP) support in Backend (`/ws`, `/topic/project/{id}/tasks`).
  - Create `AgentTaskService` for managing long-running tasks and publishing structured events.
  - Add `AgentTaskController` to list tasks and simulate execution for testing.
  - Create `TaskEventDTO` and `TaskEventType` for standardized event schema.
- **Frontend Task Integration**:
  - Add `useTaskWebSocket` hook for real-time connection handling.
  - Implement `TaskPanel` component to display task timeline, status, and logs.
  - Integrate `TaskPanel` into `ProjectPage` layout.
  - Add "Simulate Task" feature for end-to-end verification.
- **Testing**:
  - Add unit tests for `AgentTaskService`.

## [0.6.0] - 2026-02-24

### Added
- **Frontend Runtime Controls**:
  - Implement Run/Stop/Restart controls in project toolbar.
  - Add real-time status indicator and adaptive polling.
  - Implement `PreviewPane` for embedded app preview with "Open in New Tab" option.
  - Create `useRuntimeStore` for managing container lifecycle state.
  - Update `ProjectService` to include restart functionality.
- **Testing**:
  - Configure Vitest environment for frontend unit testing.
  - Add unit tests for `useRuntimeStore` and `ProjectToolbar`.

## [0.5.0] - 2026-02-24

### Added
- **Frontend Shell Foundation**:
  - Implement VS Code-style layout with Sidebar, Editor, and Status Bar.
  - Add `FileExplorer` component with recursive tree view.
  - Add `CodeEditor` component using Monaco Editor.
  - Implement state management using `zustand` (`useProjectStore`, `useEditorStore`).
  - Add routing with `react-router-dom` (`HomePage`, `ProjectPage`).
- **Backend Enhancements**:
  - Add `PUT /api/projects/{id}/files/content` endpoint for saving file changes.
  - Implement `saveFileContent` in `ProjectService` and `GitService`.
  - Add integration tests for file updates.
- **Configuration**:
  - Configure Vite proxy for backend API integration.
  - Setup TailwindCSS and TypeScript for frontend.

## [0.4.0] - 2026-02-23

### Added
- **Container Lifecycle Management**:
  - Implement `startProjectContainer`, `stopProjectContainer`, `restartProjectContainer` in `DockerService`.
  - Add REST endpoints in `ProjectController`: `/run`, `/stop`, `/restart`, `/status`.
  - Persist container state in `ContainerInstance` (status, internal IP, container ID).
- **Preview Proxy**:
  - Verify and enable `ProxyServlet` for `/proxy/{projectId}/*` routing.
  - Implement HTML `<base>` tag injection for correct asset resolution.
- **Configuration**:
  - Add `defaultImage` and `stackImages` to `DockerProperties`.
- **Testing**:
  - Add `ProjectRuntimeIntegrationTest` for end-to-end lifecycle verification.
  - Update `DockerServiceTest` and `ReaperTaskTest` to match new API.

## [0.3.0] - 2026-02-23

### Added
- Add `ReaperTask` MVP to scheduled stop/remove idle containers (>15 min).

## [0.2.0] - 2026-02-22

### Added
- Implement `GitService` with JGit for bare repository management.
  - `initBareRepository()`: Create bare git repos with initial commit.
  - `cloneToWorkspace()`: Clone bare repo to session workspace.
  - `commitAndPush()`: Stage, commit, and push changes from workspace.
  - `listFiles()`: Read file tree from bare repo via TreeWalk.
  - `readFileContent()`: Read file blob content from bare repo.
- Add `ProjectService` for project CRUD with automatic Git repo initialization.
- Add `ProjectController` REST API endpoints:
  - `POST /api/projects` — Create project with auto git init.
  - `GET /api/projects` — List user projects.
  - `GET /api/projects/{id}/files` — Get file tree.
  - `GET /api/projects/{id}/files/content?path=` — Read file content.
- Add `StorageProperties` configuration for `platform.storage` settings.
- Add `SecurityConfig` with permitAll for MVP development.
- Add DTOs: `CreateProjectRequest`, `FileTreeNode`.
- Add JGit dependency (`org.eclipse.jgit:6.8.0`).
- Add `GitServiceTest` with 6 unit tests (all passing).

## [0.1.0] - 2026-02-22

### Added
- 初始化專案基礎設施。
- 建立 `docs/` 文件夾，包含詳細系統規格書、UI/UX、後端、資料庫與運維規格。
- 配置 `docker-compose.yml` (PostgreSQL, Traefik)。
- 初始化後端 Spring Boot 專案架構與 `pom.xml`。
- 初始化前端 React/Vite 專案架構與 `package.json`。
- 建立物理存儲目錄 `data/repos` 與 `data/workspaces`。
- 建立 `.gitignore` 排除開發與沙盒暫存路徑。
- 建立 `CHANGELOG.md` 與 `TODO.md`。
- 建立 `JULES_PROMPT.md` 用於引導 AI 協作開發。
