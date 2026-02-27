# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- **Authentication & RBAC**: Implemented comprehensive JWT-based authentication with refresh token rotation.
- **Role Management**: Added `ProjectRole` (ADMIN, MEMBER, VIEWER) and UI for managing project members.
- **Frontend Security**: Protected routes, permission-based UI guards (disabled buttons/actions), and automatic token refresh.
- **Backend Security**: Secured all API endpoints with `@PreAuthorize`, ensuring proper access control for project resources.
- **Login Page**: Added registration and login flows.

### Changed
- **API Security**: Migrated from open API to secured endpoints requiring `Bearer` token.
- **User Context**: Removed hardcoded user ID; backend now derives user identity from security context.
- **Project Structure**: Updated `Project` and `User` entities to support ownership and membership.

## [0.1.0] - 2024-05-20

### Added
- Initial release of the App Generator.
- basic project CRUD.
- Docker integration for running generated apps.
- React Flow workflow editor.
- Monaco Editor for code viewing/editing.
