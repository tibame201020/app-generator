package com.tibame.app_generator.service;

import com.tibame.app_generator.dto.FileTreeNode;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 專案管理服務，封裝專案 CRUD 邏輯並整合 GitService。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GitService gitService;

    /**
     * 建立新專案：寫入 DB 並初始化 Git Bare Repository。
     *
     * @param userId      使用者 ID
     * @param name        專案名稱
     * @param description 專案描述
     * @return 建立的 Project 實體
     */
    @Transactional
    public Project createProject(UUID userId, String name, String description)
            throws GitAPIException, IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 先建立 DB 記錄以取得 projectId
        Project project = Project.builder()
                .user(user)
                .name(name)
                .description(description)
                .gitRepoPath("") // 暫置，初始化後更新
                .build();
        project = projectRepository.save(project);

        // 初始化 Git Bare Repository
        String repoPath = gitService.initBareRepository(userId, project.getId());
        project.setGitRepoPath(repoPath);
        project = projectRepository.save(project);

        log.info("Created project '{}' (id={}) with git repo at: {}",
                name, project.getId(), repoPath);

        return project;
    }

    /**
     * 查詢指定使用者的所有專案。
     */
    public List<Project> getProjectsByUser(UUID userId) {
        return projectRepository.findByUserId(userId);
    }

    /**
     * 依 ID 取得專案。
     */
    public Project getProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    /**
     * 取得專案的檔案樹。
     */
    public List<FileTreeNode> getFileTree(UUID projectId) throws IOException {
        Project project = getProjectById(projectId);
        return gitService.listFiles(project.getUser().getId(), projectId);
    }

    /**
     * 讀取專案中指定檔案的內容。
     */
    public String getFileContent(UUID projectId, String filePath) throws IOException {
        Project project = getProjectById(projectId);
        return gitService.readFileContent(project.getUser().getId(), projectId, filePath);
    }

    /**
     * 儲存專案中指定檔案的內容。
     */
    public void saveFileContent(UUID projectId, String filePath, String content) throws IOException, GitAPIException {
        Project project = getProjectById(projectId);
        gitService.updateFileContent(project.getUser().getId(), projectId, filePath, content);
    }
}
