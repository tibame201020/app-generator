# Changelog

## [Unreleased]

### Added
- **Project Import**: Added functionality to import projects from remote Git repositories.
- **Code Analysis**: Implemented AST-based Java code analysis (Packages, Classes, Methods, Fields, Dependencies) with a new Analysis View.
- **Workflow Observability**: Implemented `WorkflowRun` entity and `RunDetailsPanel` for end-to-end workflow monitoring.
- **Retry Controls**: Added API endpoints and UI for retrying failed tasks and resuming failed runs.
- **Task Context Persistence**: `AgentTask` now stores `inputContext` and `contextData` (output) snapshots to enable safe retries.
- **Run History**: Backend stores workflow execution history, including status, start/end times.
- **Real-time Updates**: `RunDetailsPanel` polls run status and updates task lists dynamically.

### Changed
- **WorkflowService**: Refactored to support persistent runs and stateful execution (startRun, resumeRun).
- **AgentTaskService**: Updated to link tasks to `WorkflowRun` and save input context before execution.
- **LlmAgentExecutionService**: Updated to use persistent input context for LLM prompts.
- **ProjectPage**: Replaced `TaskPanel` with enhanced `RunDetailsPanel`.

### Fixed
- Improved task error handling and logging details.
- Fixed JSON deserialization issues for analysis results in H2/PostgreSQL using `AnalysisResultConverter`.
- Resolved concurrent import/analysis conflicts with idempotency checks.

### Enhanced
- **Project Import**: Added detailed lifecycle tracking (PENDING, CLONING, SUCCESS, FAILED) with timestamps and logs.
- **Analysis Dashboard**: Added `ProjectStatusCard` to visualize import/analysis status, file metrics, and recent activity logs.
- **Reliability**: Added concurrency guards for analysis jobs and idempotent re-import (sync) endpoint.
- **Testing**: Added integration tests for import failure/retry and analysis concurrency.
