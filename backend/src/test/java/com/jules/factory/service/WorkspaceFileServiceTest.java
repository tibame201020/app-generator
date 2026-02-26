package com.jules.factory.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class WorkspaceFileServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private WorkspaceFileService workspaceFileService;

    private final String bucketName = "test-bucket";
    private final String minioUrl = "http://localhost:9000";
    private Path tempWorkspaceDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(workspaceFileService, "bucketName", bucketName);
        ReflectionTestUtils.setField(workspaceFileService, "minioUrl", minioUrl);

        // Create a temporary directory for workspace
        tempWorkspaceDir = Files.createTempDirectory("workspace-test");
        ReflectionTestUtils.setField(workspaceFileService, "localWorkspaceRoot", tempWorkspaceDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (Files.exists(tempWorkspaceDir)) {
            Files.walk(tempWorkspaceDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @Test
    void saveFile_shouldSaveLocallyAndUploadToMinio() throws Exception {
        String projectId = "123";
        String filePath = "src/Main.java";
        String content = "public class Main {}";

        // Call method
        String fileUrl = workspaceFileService.saveFile(projectId, filePath, content);

        // Verify local file creation
        Path expectedFile = tempWorkspaceDir.resolve(projectId).resolve(filePath);
        assertTrue(Files.exists(expectedFile), "Local file should exist");
        assertEquals(content, Files.readString(expectedFile), "Content should match");

        // Verify MinIO upload
        verify(minioClient).putObject(any(PutObjectArgs.class));

        // Verify returned URL
        String expectedUrl = String.format("%s/%s/%s/%s", minioUrl, bucketName, projectId, filePath);
        assertEquals(expectedUrl, fileUrl);
    }

    @Test
    void saveFile_shouldPreventPathTraversal() {
        String projectId = "123";
        String unsafeFilePath = "../etc/passwd";
        String content = "malicious content";

        // Although WorkspaceFileService validates ".." before path resolution,
        // we can check if it throws SecurityException.
        assertThrows(SecurityException.class, () -> {
            workspaceFileService.saveFile(projectId, unsafeFilePath, content);
        });
    }

    @Test
    void getFileUrl_shouldReturnCorrectUrl() {
        String projectId = "456";
        String filePath = "readme.md";
        String expectedUrl = "http://localhost:9000/test-bucket/456/readme.md";

        String url = workspaceFileService.getFileUrl(projectId, filePath);
        assertEquals(expectedUrl, url);
    }
}
