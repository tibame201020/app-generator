package com.tibame.app_generator.config;

import com.github.dockerjava.api.DockerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigValidationRunner implements CommandLineRunner {

    private final DockerClient dockerClient;
    private final StorageProperties storageProperties;

    @Value("${platform.llm.api-key:demo}")
    private String openAiApiKey;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Environment Validation...");

        // 1. Check OpenAI API Key
        if ("demo".equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn(">>> CRITICAL WARNING: 'platform.llm.api-key' is set to '{}'. LLM features will NOT work properly.", openAiApiKey);
        } else {
            String maskedKey = openAiApiKey.length() > 5 ? openAiApiKey.substring(0, 5) + "..." : "***";
            log.info("✅ OpenAI API Key configured (starts with: {})", maskedKey);
        }

        // 2. Check Storage Paths
        checkDirectory("Repos", storageProperties.getReposDir());
        checkDirectory("Workspaces", storageProperties.getWorkspacesDir());

        // 3. Check Docker Connectivity
        try {
            dockerClient.pingCmd().exec();
            log.info("✅ Docker Daemon is reachable.");
        } catch (Exception e) {
            log.error("❌ Docker Daemon is NOT reachable: {}", e.getMessage());
            log.error(">>> Please ensure Docker Desktop is running and exposed on the configured host/socket.");
            // We do not throw exception here to allow app to start, but critical features will fail.
        }

        log.info("Environment Validation Completed.");
    }

    private void checkDirectory(String name, Path path) {
        if (Files.exists(path) && Files.isDirectory(path) && Files.isWritable(path)) {
            log.info("✅ {} Directory is ready: {}", name, path);
        } else {
            log.error("❌ {} Directory issue at: {}", name, path);
            if (!Files.exists(path)) log.error("   - Directory does not exist.");
            else if (!Files.isDirectory(path)) log.error("   - Path is not a directory.");
            else if (!Files.isWritable(path)) log.error("   - Application does not have write permissions.");
        }
    }
}
