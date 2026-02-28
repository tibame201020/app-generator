# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
- Task 4.2.1: Wrote docs/frontend/theme_and_styling.md, configured dracula as default theme and added Navbar/Sidebar examples.
- Initiated project boilerplate and AI Agent protocols.
- Task 1.4.1: Created Backend Dockerfile with multi-stage build.
- Task 1.4.2: Created Dockerfile for Frontend (Vite + React) development with hot-reload support.
- Task 1.1.1: Initialized Spring Boot 3 project skeleton and Maven settings.
- Task 1.1.2: Configured application.yml with server port 8080 and enabled Java 21 Virtual Threads.
- Task 1.1.3: Configured H2 Database Memory mode and JPA dependencies.
- Task 1.1.4: Created /api/health endpoint for basic system health check.
- Task 1.1.5: Added MinIO S3 Client dependency and configuration with auto-bucket creation logic.
- Task 1.2.1: Added Spring AI OpenAI dependency and basic configuration.
- Task 1.2.2: Created /api/test-ai endpoint to test LLM connection.
- Task 1.2.3: Implemented in-memory Message Queue foundation using Spring ApplicationEventPublisher.
- Task 1.3.1: Initialized Vite React + TypeScript project with Router and configured proxy settings.
- Task 1.3.2: Installed Tailwind CSS, PostCSS, and configured them.
- Task 1.3.3: Installed DaisyUI Plugin and configured themes.
- Task 1.3.4: Created Home Page and rendered DaisyUI Test Component.
- Task 1.4.3: Created docker-compose.yml for full stack orchestration with MinIO and Volume mounts.
- Task 2.1.1: Implemented Project and Conversation JPA Entities using Snowflake ID Generator.
- Task 2.1.2: Created Spring Data JPA Repositories for Project and Conversation with ordering support.
- Task 2.2.1: Defined AgentRole Enum (USER, PM, UIUX, SA, PG, SYSTEM) and ProjectState Enum (REQUIREMENT_GATHERING, ARCHITECTURE_DESIGN, IMPLEMENTATION, REVIEW).
- Task 2.2.2: Implemented StateMachineEngine interface and StateContext record for managing agent state transitions.
- Task 2.2.3: Implemented PM State Logic with PMStateHandler and StateMachineEngineImpl. Added status field to Project entity (Self-Healing).
- Task 2.2.4: Implemented SA State Logic (SAStateHandler) to propose dummy architecture and transition to Implementation phase.
- Task 2.2.5: Implemented Guard logic in StateMachineEngine to block Agent execution when waiting for User input.
- Task 2.3.1: Implemented ProjectController and ProjectService to handle project creation and chat interactions, triggering the state machine.
- Task 2.4.1: Wrote docs/backend/state_machine.md with detailed architecture, roles, and state diagrams.
- Task 3.1.1: Implemented PromptTemplateBuilder for centralized System Prompt management (PM, SA) and conversation history construction.
- Task 3.1.2: Integrated Spring AI ChatModel into PMStateHandler for interactive requirement gathering with LLM. Added [REQUIREMENTS_GATHERED] token for state transition.
- Task 3.1.3: Integrated Spring AI with Structured Output in SAStateHandler to enforce valid JSON architecture proposals using BeanOutputConverter.
- Task 3.1.4: Implemented WorkspaceFileService for saving generated code to local file system (Docker volume) and uploading to MinIO, including path traversal protection and public URL generation.
- Task 3.2.1: Implemented Agent Broadcasting mechanism using Spring WebSocket and ApplicationEventPublisher. Agents now broadcast thinking/speaking events to /ws/agents.
- Task 4.1.1: Implemented core UI layout components (Navbar, Sidebar, MainLayout) using DaisyUI.
- Task 4.1.2: Implemented WebSocket Client Hook and AgentContext for real-time agent message streaming.
- Task 4.1.3: Implemented ChatMessage component with Role-based styling, Markdown rendering, and Code Syntax Highlighting.
- Task 4.1.4: Implemented InternalLogPanel component to display real-time Agent system logs via WebSocket, separating internal thoughts from user chat.
