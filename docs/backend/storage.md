# Storage Module (MinIO)

## Overview
This module integrates MinIO (S3-compatible object storage) for handling file I/O operations, such as storing generated code files or user uploads.

## Configuration
The configuration is managed by `MinioConfig` class (for Client Bean) and `MinioBucketInitializer` (for bucket setup).
Properties are defined in `application.yml` under `minio` prefix:
- `minio.url`: The endpoint URL (default: `http://localhost:9000`)
- `minio.access-key`: Access key (default: `mock_access_key`)
- `minio.secret-key`: Secret key (default: `mock_secret_key`)
- `minio.bucket-name`: Default bucket name (default: `jules-bucket`)

## Initialization
On startup, the application checks if the configured bucket exists.
- If not found, it creates the bucket and sets a **public-read** policy to allow direct access (e.g., from frontend).
- If MinIO is unreachable, it logs a warning but allows the application to start (to support CI/CD environments without MinIO).

## Usage
Inject `MinioClient` bean to interact with the storage.
