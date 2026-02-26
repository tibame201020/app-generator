package com.jules.factory.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

@Service
public class WorkspaceFileService {

    private final MinioClient minioClient;
    private final String workspaceRoot;
    private final String bucketName;

    public WorkspaceFileService(MinioClient minioClient,
                                @Value("${app.workspace.root}") String workspaceRoot,
                                @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.workspaceRoot = workspaceRoot;
        this.bucketName = bucketName;
    }

    /**
     * Saves content to both local filesystem and MinIO.
     *
     * @param projectId The project ID.
     * @param filePath  The relative file path within the project.
     * @param content   The content to write.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If the file path is invalid.
     */
    public void saveFile(Long projectId, String filePath, String content) throws IOException {
        validateFilePath(filePath);

        // 1. Write to local filesystem
        Path projectDir = Paths.get(workspaceRoot, String.valueOf(projectId));
        if (!Files.exists(projectDir)) {
            Files.createDirectories(projectDir);
        }

        Path targetPath = projectDir.resolve(filePath).normalize();
        if (!targetPath.startsWith(projectDir)) {
             throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        Path parentDir = targetPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        Files.writeString(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 2. Upload to MinIO
        String objectName = projectId + "/" + filePath;
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("text/plain") // Basic content type, could be improved based on extension
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a presigned URL for the file in MinIO.
     *
     * @param projectId The project ID.
     * @param filePath  The relative file path.
     * @return The presigned URL.
     */
    public String getFileUrl(Long projectId, String filePath) {
        validateFilePath(filePath);
        String objectName = projectId + "/" + filePath;
        try {
             return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate file URL: " + e.getMessage(), e);
        }
    }

    private void validateFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        if (filePath.contains("..")) {
            throw new IllegalArgumentException("File path cannot contain traversal characters (..)");
        }
    }
}
