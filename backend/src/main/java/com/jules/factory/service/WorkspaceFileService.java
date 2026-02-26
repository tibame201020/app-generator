package com.jules.factory.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceFileService {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceFileService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    // Base directory for local workspace files (can be configured via properties if needed)
    private final Path localWorkspaceRoot = Paths.get("/workspace");

    public WorkspaceFileService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Saves content to both local file system and MinIO storage.
     *
     * @param projectId The project ID (used as a directory name).
     * @param filePath  The relative file path within the project.
     * @param content   The content to write.
     * @return The public URL of the file stored in MinIO.
     * @throws IOException If an I/O error occurs.
     */
    public String saveFile(String projectId, String filePath, String content) throws IOException {
        validatePath(projectId, filePath);

        // 1. Save to local file system
        Path projectDir = localWorkspaceRoot.resolve(projectId);
        Path targetFile = projectDir.resolve(filePath);

        // Ensure the resolved path is still within the project directory (Path Traversal check)
        if (!targetFile.normalize().startsWith(projectDir.normalize())) {
            throw new SecurityException("Access denied: Path traversal attempt detected.");
        }

        try {
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("File saved locally: {}", targetFile);
        } catch (IOException e) {
            logger.error("Failed to save file locally: {}", targetFile, e);
            throw e;
        }

        // 2. Upload to MinIO
        String objectName = projectId + "/" + filePath;
        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, contentBytes.length, -1)
                    .contentType("text/plain") // Default to text/plain, could be enhanced based on extension
                    .build()
            );
            logger.info("File uploaded to MinIO: bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            logger.error("Failed to upload file to MinIO: {}", objectName, e);
            throw new IOException("Failed to upload to MinIO", e);
        }

        return getFileUrl(projectId, filePath);
    }

    /**
     * Generates the public URL for the file stored in MinIO.
     *
     * @param projectId The project ID.
     * @param filePath  The relative file path.
     * @return The public URL string.
     */
    public String getFileUrl(String projectId, String filePath) {
        // Construct URL assuming public read policy
        // Format: http://minio-host:port/bucket-name/project-id/file-path
        String cleanUrl = minioUrl.endsWith("/") ? minioUrl.substring(0, minioUrl.length() - 1) : minioUrl;
        return String.format("%s/%s/%s/%s", cleanUrl, bucketName, projectId, filePath);
    }

    private void validatePath(String projectId, String filePath) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be empty");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        if (filePath.contains("..")) {
             throw new SecurityException("Access denied: Path traversal attempt detected.");
        }
    }
}
