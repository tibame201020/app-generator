# Operations Manual

## Environment Variables

The application is configured using the following environment variables. Ensure these are set in `docker-compose.yml` or your deployment environment.

| Variable | Description | Default | Criticality |
| :--- | :--- | :--- | :--- |
| `OPENAI_API_KEY` | API Key for OpenAI (or compatible provider). | `demo` | **Critical** (LLM features will fail without it) |
| `PLATFORM_DOCKER_HOST` | URL to the Docker Daemon socket. | `unix:///var/run/docker.sock` | **Critical** (Sandbox features will fail without it) |
| `PLATFORM_STORAGE_REPOS_PATH` | Path on host to store Git repositories. | `./data/repos` | High |
| `PLATFORM_STORAGE_WORKSPACES_PATH` | Path on host to store project workspaces. | `./data/workspaces` | High |

## Health & Observability

### API Endpoints

The backend exposes the following endpoints for monitoring:

*   **`GET /api/ops/health`**:
    *   Returns HTTP 200 `UP` if Database and Docker are reachable.
    *   Returns HTTP 503 `DOWN` if any critical dependency is missing.
    *   Response: `{ "status": "UP", "db": "UP", "docker": "UP" }`

*   **`GET /api/ops/metrics`**:
    *   Returns simple in-memory counters for system activity.
    *   Response:
        ```json
        {
          "import.total": 10,
          "import.success": 8,
          "import.failed": 2,
          "analysis.avg_duration_ms": 1520.5
        }
        ```

### System Status Panel
The Frontend includes a **System Status** indicator in the bottom-right status bar.
*   **Green Dot**: Backend is healthy.
*   **Red Dot**: Backend is unreachable or unhealthy.
*   **Click**: Opens a popover with detailed metrics.

## Troubleshooting

### 1. Request Tracing (Request ID)
Every HTTP request is assigned a unique UUID `X-Request-ID`.
*   **Frontend**: If an API error occurs, the `RequestId` is logged to the browser console (F12).
*   **Backend Logs**: All logs for that request are tagged with `[requestId=...]`.
    *   `grep "abc-123" app.log` will show the full lifecycle of that request.

### 2. Startup Checks
On application startup, the `ConfigValidationRunner` checks:
1.  `OPENAI_API_KEY` validity (warns if "demo").
2.  File system write permissions.
3.  Docker Daemon connectivity.

Check standard output (`docker logs backend`) immediately after startup for `✅` or `❌` indicators.
