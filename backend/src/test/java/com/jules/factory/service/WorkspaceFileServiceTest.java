package com.jules.factory.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceFileServiceTest {

    @Mock
    private MinioClient minioClient;

    private WorkspaceFileService workspaceFileService;
    private final String TEST_WORKSPACE_ROOT = "target/test-workspace";
    private final String TEST_BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_WORKSPACE_ROOT));
        workspaceFileService = new WorkspaceFileService(minioClient, TEST_WORKSPACE_ROOT, TEST_BUCKET_NAME);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(TEST_WORKSPACE_ROOT));
    }

    @Test
    void saveFile_shouldSaveToLocalAndMinIO() throws Exception {
        Long projectId = 1L;
        String filePath = "src/Main.java";
        String content = "public class Main {}";

        workspaceFileService.saveFile(projectId, filePath, content);

        // Verify local file exists
        Path savedFile = Paths.get(TEST_WORKSPACE_ROOT, String.valueOf(projectId), filePath);
        assertTrue(Files.exists(savedFile));
        assertEquals(content, Files.readString(savedFile));

        // Verify MinIO upload
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void saveFile_shouldThrowExceptionForPathTraversal() {
        Long projectId = 1L;
        String filePath = "../secret.txt";
        String content = "secret";

        assertThrows(IllegalArgumentException.class, () ->
            workspaceFileService.saveFile(projectId, filePath, content)
        );
    }

    @Test
    void getFileUrl_shouldReturnUrl() throws Exception {
        Long projectId = 1L;
        String filePath = "src/Main.java";
        String expectedUrl = "http://minio/bucket/object";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String url = workspaceFileService.getFileUrl(projectId, filePath);

        assertEquals(expectedUrl, url);
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
