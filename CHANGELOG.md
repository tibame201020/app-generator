# Changelog

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
