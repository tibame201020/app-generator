package com.tibame.app_generator.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 讀取 application.yml 中 platform.storage 區段的配置。
 * 應用啟動時自動建立 repos 與 workspaces 目錄。
 */
@Data
@ConfigurationProperties(prefix = "platform.storage")
public class StorageProperties {

    private String reposPath = "./data/repos";
    private String workspacesPath = "./data/workspaces";

    /**
     * 取得 repos 目錄的絕對路徑
     */
    public Path getReposDir() {
        return Paths.get(reposPath).toAbsolutePath().normalize();
    }

    /**
     * 取得 workspaces 目錄的絕對路徑
     */
    public Path getWorkspacesDir() {
        return Paths.get(workspacesPath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(getReposDir());
        Files.createDirectories(getWorkspacesDir());
    }
}
