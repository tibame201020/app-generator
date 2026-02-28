package com.tibame.app_generator.controller;

import com.tibame.app_generator.dto.CreateProjectRequest;
import com.tibame.app_generator.dto.FileTreeNode;
import com.tibame.app_generator.dto.ImportProjectRequest;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.service.DockerService;
import com.tibame.app_generator.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<?> createProject(
            @AuthenticationPrincipal User user,
            @RequestBody CreateProjectRequest request) {
        try {
            Project project = projectService.createProject(
                    user.getId(), request.getName(), request.getDescription());
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
     * 匯入專案。
     * POST /api/projects/import
     */
    @PostMapping("/import")
    public ResponseEntity<?> importProject(
            @AuthenticationPrincipal User user,
            @RequestBody ImportProjectRequest request) {
        try {
            Project project = projectService.importProject(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to import project: " + e.getMessage()));
        }
    }

    /**
     * 查詢使用者的所有專案。
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<List<Project>> listProjects(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.getProjectsByUser(user.getId()));
    }

    /**
     * 取得專案檔案樹。
     * GET /api/projects/{id}/files
     */
    @GetMapping("/{id}/files")
    @PreAuthorize("@projectSecurityService.isViewer(#id)")
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
    @PreAuthorize("@projectSecurityService.isViewer(#id)")
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
     * 更新專案中指定檔案的內容。
     * PUT /api/projects/{id}/files/content
     */
    @PutMapping("/{id}/files/content")
    @PreAuthorize("@projectSecurityService.isMember(#id)")
    public ResponseEntity<?> updateFileContent(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        String path = payload.get("path");
        String content = payload.get("content");

        if (path == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing path or content"));
        }

        try {
            projectService.saveFileContent(id, path, content);
            return ResponseEntity.ok(Map.of("message", "File saved"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to save file: " + e.getMessage()));
        }
    }

    /**
     * 啟動專案容器。
     * POST /api/projects/{id}/run
     */
    @PostMapping("/{id}/run")
    @PreAuthorize("@projectSecurityService.isMember(#id)")
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
    @PreAuthorize("@projectSecurityService.isMember(#id)")
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
    @PreAuthorize("@projectSecurityService.isMember(#id)")
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
    @PreAuthorize("@projectSecurityService.isViewer(#id)")
    public ResponseEntity<?> getProjectStatus(@PathVariable UUID id) {
        try {
            Map<String, Object> status = dockerService.getProjectContainerStatus(id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/transfer-ownership")
    @PreAuthorize("@projectSecurityService.isAdmin(#id)")
    public ResponseEntity<?> transferOwnership(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, UUID> request) {
        UUID newOwnerId = request.get("newOwnerId");
        if (newOwnerId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "newOwnerId is required"));
        }
        try {
            projectService.transferOwnership(id, user.getId(), newOwnerId);
            return ResponseEntity.ok(Map.of("message", "Ownership transferred"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
