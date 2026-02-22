package com.tibame.app_generator.controller;

import com.tibame.app_generator.dto.ChatRequest;
import com.tibame.app_generator.dto.CreateProjectRequest;
import com.tibame.app_generator.dto.FileTreeNode;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.service.AgentTaskService;
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
    private final AgentTaskService agentTaskService;

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
     * 建立 Agent chat 任務（MVP 先落庫，後續由 Orchestrator 非同步處理）。
     * POST /api/projects/{id}/chat
     */
    @PostMapping("/{id}/chat")
    public ResponseEntity<?> createChatTask(
            @PathVariable UUID id,
            @RequestBody ChatRequest request) {
        try {
            AgentTask task = agentTaskService.createTask(id, request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("taskId", task.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 查詢專案 Agent 任務歷史。
     * GET /api/projects/{id}/tasks
     */
    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getProjectTasks(@PathVariable UUID id) {
        try {
            List<Map<String, Object>> tasks = agentTaskService.getTasksByProject(id).stream()
                    .map(task -> Map.<String, Object>of(
                            "id", task.getId(),
                            "agentType", task.getAgentType(),
                            "taskName", task.getTaskName(),
                            "status", task.getStatus(),
                            "progressPct", task.getProgressPct(),
                            "createdAt", task.getCreatedAt()))
                    .toList();
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
