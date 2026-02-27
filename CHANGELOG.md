# Changelog

## [Unreleased]

## [Phase 4] - 2024-05-20

### Added
- **Task 4.1.1**: Implemented Frontend Layout (Navbar & Sidebar).
    - Created `Navbar` component with phase indicator and theme toggle.
    - Created `Sidebar` component rendering tasks from tracker data.
    - Created `MainLayout` to orchestrate navigation and content.
    - Added `Dashboard` page as the default route.
    - Defined TypeScript interfaces for `Tracker`, `Phase`, and `Task`.
    - Added `mockTracker` data for UI development.

## [Phase 3] - 2024-05-19

### Added
- **Task 3.2.1**: Agent Broadcasting Mechanism
  - Integrated Spring Cloud Stream with RabbitMQ/In-Memory binder for event broadcasting.
  - Implemented `AgentEventPublisher` to emit events (PM_PLANNING, SA_DESIGN, etc.).
  - Added `AgentEventListener` to subscribe to broadcasted events.

- **Task 3.1.4**: File I/O Service for Code Generation
  - Implemented `FileService` to read/write files within the project workspace.
  - Secured file access to prevent unauthorized directory traversal.
  - Added integration tests for file operations.

- **Task 3.1.3**: SA Agent Integration
  - Implemented `SaAgent` service using Spring AI.
  - Defined system prompts for System Architect role.
  - Integrated SA workflow into `StateMachineEngine`.

- **Task 3.1.2**: PM Agent Integration
  - Implemented `PmAgent` service using Spring AI.
  - Defined system prompts for Project Manager role.
  - Integrated PM workflow into `StateMachineEngine`.

- **Task 3.1.1**: Prompt Template Builder
  - Created `LLMPromptTemplateBuilder` for dynamic prompt generation.
  - Supported variable substitution and template management.

## [Phase 2] - 2024-05-18

### Added
- **Task 2.4.1**: Documentation
  - Added `docs/backend/state_machine.md` explaining the core state machine architecture.

- **Task 2.3.1**: Project Controller & Chat API
  - Implemented REST endpoints for creating projects and processing chat messages.
  - Connected Controller to `ProjectService` and `StateMachineEngine`.

- **Task 2.2.5**: Guard Conditions
  - Implemented logic to pause state machine when user input is required.

- **Task 2.2.4**: SA State Logic
  - Implemented state transitions for System Architect (SA) phase.

- **Task 2.2.3**: PM State Logic
  - Implemented state transitions for Project Manager (PM) phase.

- **Task 2.2.2**: State Machine Engine
  - Implemented the core engine interface and base context.

- **Task 2.2.1**: Enums
  - Defined `AgentRole` and `State` enums.

- **Task 2.1.2**: Repositories
  - Created Spring Data JPA repositories for Project and Conversation entities.

- **Task 2.1.1**: Entities
  - Defined JPA entities for `Project` and `Conversation`.

## [Phase 1] - 2024-05-15

### Added
- **Task 1.4.3**: Docker Compose
    - Added `docker-compose.yml` for orchestrating backend, frontend, and database services.

- **Task 1.4.2**: Frontend Dockerfile
    - Created multi-stage Dockerfile for building and serving the React frontend.

- **Task 1.4.1**: Backend Dockerfile
    - Created Dockerfile for the Spring Boot backend.

- **Task 1.3.4**: Frontend Home Page
    - Created a basic landing page using DaisyUI components.

- **Task 1.3.3**: DaisyUI Integration
    - Configured Tailwind CSS with DaisyUI plugin.
    - Set up default themes (light/dark).

- **Task 1.3.2**: Tailwind CSS Setup
    - Installed and configured Tailwind CSS and PostCSS.

- **Task 1.3.1**: Frontend Initialization
    - Initialized React project using Vite + TypeScript.

- **Task 1.2.3**: Spring Cloud Stream
    - Added Spring Cloud Stream dependencies.
    - Configured in-memory message queue for testing.

- **Task 1.2.2**: LLM Test Endpoint
    - Created `/api/test-ai` to verify OpenAI connection.

- **Task 1.2.1**: Spring AI Setup
    - Added `spring-ai-openai` dependency.
    - Configured API keys and base settings.

- **Task 1.1.5**: MinIO Integration
    - Added S3 client dependencies.
    - Configured MinIO connection settings.

- **Task 1.1.4**: Health Check
    - Implemented `/api/health` endpoint.

- **Task 1.1.3**: Database Setup
    - Configured H2 Database in memory mode.
    - Set up JPA/Hibernate dependencies.

- **Task 1.1.2**: Config & Virtual Threads
    - Updated `application.yml`.
    - Enabled Java 21 Virtual Threads.

- **Task 1.1.1**: Project Initialization
    - Created Spring Boot 3.2+ project structure.
    - Configured Maven build.
