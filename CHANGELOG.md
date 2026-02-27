# Changelog

## [Unreleased]

### Added
- **Resilient Workflow Execution**: Implemented automatic retry logic with exponential backoff for AI agent tasks.
  - **Retry Policy**: Default 3 retries, initial delay 5s, backoff factor 2.0.
  - **Observability**: Added `attempt_history` to `AgentTask` to track failure timestamps and error messages.
  - **Status**: Introduced `RETRY_WAIT` status and `RETRY_SCHEDULED` event.
- **Frontend Enhancements**:
  - **RunDetailsPanel**: Updated to display retry counts ("Retry 1/3") and visualize `RETRY_WAIT` status with a spinning refresh icon.
  - **Task Summary**: Added "Retry History" section to the expanded task view, showing a timeline of failed attempts.
  - **Manual Retry**: Improved "Retry Task" button with loading state and disabled logic during API calls.
- **Project Import**: Added functionality to import projects from remote Git repositories.
- **Code Analysis**: Implemented AST-based Java code analysis (Packages, Classes, Methods, Fields, Dependencies) with a new Analysis View.
- **Workflow Observability**: Implemented `WorkflowRun` entity and `RunDetailsPanel` for end-to-end workflow monitoring.
- **Task Context Persistence**: `AgentTask` now stores `inputContext` and `contextData` (output) snapshots to enable safe retries.

### Changed
- **WorkflowExecutor**: Updated to support asynchronous task retries using `CompletableFuture.delayedExecutor`, preventing blocking of the main workflow loop.
- **AgentTaskService**: Exposed `publishEvent` to support retry scheduling events.
- **WorkflowService**: Refactored to support persistent runs and stateful execution (startRun, resumeRun).
- **LlmAgentExecutionService**: Updated to use persistent input context for LLM prompts.
- **ProjectPage**: Replaced `TaskPanel` with enhanced `RunDetailsPanel`.

### Fixed
- **Integration Tests**: Added `WorkflowRetryIntegrationTest` to verify retry logic, state transitions, and history recording.
- Improved task error handling and logging details.
- Fixed JSON deserialization issues for analysis results in H2/PostgreSQL using `AnalysisResultConverter`.
- Resolved concurrent import/analysis conflicts with idempotency checks.

### Enhanced
- **Project Import**: Added detailed lifecycle tracking (PENDING, CLONING, SUCCESS, FAILED) with timestamps and logs.
- **Analysis Dashboard**: Added `ProjectStatusCard` to visualize import/analysis status, file metrics, and recent activity logs.
- **Reliability**: Added concurrency guards for analysis jobs and idempotent re-import (sync) endpoint.
