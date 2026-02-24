package com.jules.factory.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioBucketInitializerTest {

    @Mock
    private MinioClient minioClient;

    @Test
    void testInitCreatesBucketWhenNotExists() throws Exception {
        // Arrange
        MinioBucketInitializer initializer = new MinioBucketInitializer(minioClient);
        ReflectionTestUtils.setField(initializer, "bucketName", "test-bucket");

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // Act
        initializer.init();

        // Assert
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));
    }

    @Test
    void testInitSkipsCreationWhenExists() throws Exception {
        // Arrange
        MinioBucketInitializer initializer = new MinioBucketInitializer(minioClient);
        ReflectionTestUtils.setField(initializer, "bucketName", "test-bucket");

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        initializer.init();

        // Assert
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient, never()).setBucketPolicy(any(SetBucketPolicyArgs.class));
    }

    @Test
    void testInitHandlesException() throws Exception {
        // Arrange
        MinioBucketInitializer initializer = new MinioBucketInitializer(minioClient);
        ReflectionTestUtils.setField(initializer, "bucketName", "test-bucket");

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new RuntimeException("Connection refused"));

        // Act
        // Should not throw exception
        initializer.init();

        // Assert
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
    }
}
