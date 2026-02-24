package com.tibame.app_generator.controller;

import com.tibame.app_generator.dto.CreateProjectRequest;
import com.tibame.app_generator.dto.FileTreeNode;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.service.DockerService;
import com.tibame.app_generator.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 專案管理 REST API 控制器。
 * 提供專案 CRUD 及檔案樹/內容讀取端點。
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final DockerService dockerService;

    /**
     * 建立新專案。
     * POST /api/projects?userId={userId}
     * <p>
     * 注意：MVP 階段以 query param 傳遞 userId，
     * 後續整合 Spring Security 後改為從 Authentication 取得。
     */
    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestParam UUID userId,
            @RequestBody CreateProjectRequest request) {
        try {
            Project project = projectService.createProject(
                    userId, request.getName(), request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", project.getId(),
                    "name", project.getName(),
                    "gitRepoPath", project.getGitRepoPath(),
                    "createdAt", project.getCreatedAt()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (GitAPIException | IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to initialize git repository: " + e.getMessage()));
        }
    }

    /**
     * 查詢使用者的所有專案。
     * GET /api/projects?userId={userId}
     */
    @GetMapping
    public ResponseEntity<List<Project>> listProjects(@RequestParam UUID userId) {
        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }

    /**
     * 取得專案檔案樹。
     * GET /api/projects/{id}/files
     */
    @GetMapping("/{id}/files")
    public ResponseEntity<?> getFileTree(@PathVariable UUID id) {
        try {
            List<FileTreeNode> fileTree = projectService.getFileTree(id);
            return ResponseEntity.ok(fileTree);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to read file tree: " + e.getMessage()));
        }
    }

    /**
     * 讀取專案中指定檔案的內容。
     * GET /api/projects/{id}/files/content?path={path}
     */
    @GetMapping("/{id}/files/content")
    public ResponseEntity<?> getFileContent(
            @PathVariable UUID id,
            @RequestParam String path) {
        try {
            String content = projectService.getFileContent(id, path);
            return ResponseEntity.ok(Map.of("path", path, "content", content));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 啟動專案容器。
     * POST /api/projects/{id}/run
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<?> runProject(@PathVariable UUID id) {
        try {
            dockerService.startProjectContainer(id);
            return ResponseEntity.ok(Map.of("message", "Project container started"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 停止專案容器。
     * POST /api/projects/{id}/stop
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopProject(@PathVariable UUID id) {
        try {
            dockerService.stopProjectContainer(id);
            return ResponseEntity.ok(Map.of("message", "Project container stopped"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 重啟專案容器。
     * POST /api/projects/{id}/restart
     */
    @PostMapping("/{id}/restart")
    public ResponseEntity<?> restartProject(@PathVariable UUID id) {
        try {
            dockerService.restartProjectContainer(id);
            return ResponseEntity.ok(Map.of("message", "Project container restarted"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取得專案容器狀態。
     * GET /api/projects/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getProjectStatus(@PathVariable UUID id) {
        try {
            Map<String, Object> status = dockerService.getProjectContainerStatus(id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
