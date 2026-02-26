package com.tibame.app_generator.service;

import com.tibame.app_generator.config.StorageProperties;
import com.tibame.app_generator.dto.FileTreeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Git 版本控制核心服務。
 * 使用 JGit 純 Java API 操作 Bare Repository，
 * 負責專案的版本管理、工作區克隆、提交推送等功能。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitService {

    private final StorageProperties storageProperties;

    /**
     * 取得指定使用者/專案的 Bare Repo 路徑
     */
    public Path getBareRepoPath(UUID userId, UUID projectId) {
        return storageProperties.getReposDir()
                .resolve(userId.toString())
                .resolve(projectId.toString() + ".git");
    }

    /**
     * 初始化一個 Bare Git Repository。
     *
     * @param userId    使用者 ID
     * @param projectId 專案 ID
     * @return Bare Repo 的路徑字串
     */
    public String initBareRepository(UUID userId, UUID projectId) throws GitAPIException, IOException {
        Path repoPath = getBareRepoPath(userId, projectId);

        if (Files.exists(repoPath)) {
            log.warn("Bare repo already exists at: {}", repoPath);
            return repoPath.toString();
        }

        Files.createDirectories(repoPath.getParent());

        try (Git git = Git.init()
                .setDirectory(repoPath.toFile())
                .setBare(true)
                .call()) {
            log.info("Initialized bare repository at: {}", repoPath);
        }

        // 建立初始 commit，以確保 HEAD 和 main branch 存在
        Path tempWorkspace = Files.createTempDirectory("git-init-");
        try {
            try (Git workGit = Git.cloneRepository()
                    .setURI(repoPath.toUri().toString())
                    .setDirectory(tempWorkspace.toFile())
                    .call()) {

                // 建立 .gitkeep 檔案作為初始內容
                Files.writeString(tempWorkspace.resolve(".gitkeep"), "");

                workGit.add().addFilepattern(".").call();
                workGit.commit()
                        .setMessage("Initial commit")
                        .setAuthor("AppGenerator", "system@app-generator.local")
                        .call();
                workGit.push().call();
            }
        } finally {
            // 清理暫存目錄
            deleteDirectoryRecursively(tempWorkspace);
        }

        return repoPath.toString();
    }

    /**
     * 從 Bare Repository 克隆到工作區。
     *
     * @param userId    使用者 ID
     * @param projectId 專案 ID
     * @param sessionId 工作階段 ID
     * @return 工作區路徑
     */
    public Path cloneToWorkspace(UUID userId, UUID projectId, String sessionId)
            throws GitAPIException {
        Path repoPath = getBareRepoPath(userId, projectId);
        Path workspacePath = storageProperties.getWorkspacesDir().resolve(sessionId);

        if (Files.exists(workspacePath)) {
            log.warn("Workspace already exists at: {}", workspacePath);
            return workspacePath;
        }

        Git.cloneRepository()
                .setURI(repoPath.toUri().toString())
                .setDirectory(workspacePath.toFile())
                .call()
                .close();

        log.info("Cloned repo to workspace: {}", workspacePath);
        return workspacePath;
    }

    /**
     * 在工作區執行 git add、commit、push 操作。
     *
     * @param workspacePath 工作區路徑
     * @param commitMessage 提交訊息
     */
    public void commitAndPush(Path workspacePath, String commitMessage)
            throws GitAPIException, IOException {
        try (Git git = Git.open(workspacePath.toFile())) {
            git.add().addFilepattern(".").call();

            git.commit()
                    .setMessage(commitMessage)
                    .setAuthor("AppGenerator", "system@app-generator.local")
                    .call();

            git.push().call();

            log.info("Committed and pushed changes from: {}", workspacePath);
        }
    }

    /**
     * 列出 Bare Repository 中 HEAD commit 的所有檔案。
     *
     * @param userId    使用者 ID
     * @param projectId 專案 ID
     * @return 檔案樹節點列表
     */
    public List<FileTreeNode> listFiles(UUID userId, UUID projectId) throws IOException {
        Path repoPath = getBareRepoPath(userId, projectId);

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoPath.toFile())
                .build()) {

            Ref head = repository.exactRef("HEAD");
            if (head == null || head.getObjectId() == null) {
                return Collections.emptyList();
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(head.getObjectId());
                RevTree tree = commit.getTree();

                // 使用 Map 建構樹狀結構
                Map<String, FileTreeNode> nodeMap = new LinkedHashMap<>();
                FileTreeNode root = FileTreeNode.builder()
                        .name("/")
                        .path("")
                        .type("directory")
                        .children(new ArrayList<>())
                        .build();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);

                    while (treeWalk.next()) {
                        String filePath = treeWalk.getPathString();
                        String[] parts = filePath.split("/");

                        // 建構中間目錄節點
                        StringBuilder currentPath = new StringBuilder();
                        FileTreeNode parent = root;

                        for (int i = 0; i < parts.length - 1; i++) {
                            if (currentPath.length() > 0)
                                currentPath.append("/");
                            currentPath.append(parts[i]);
                            String dirPath = currentPath.toString();

                            if (!nodeMap.containsKey(dirPath)) {
                                FileTreeNode dirNode = FileTreeNode.builder()
                                        .name(parts[i])
                                        .path(dirPath)
                                        .type("directory")
                                        .children(new ArrayList<>())
                                        .build();
                                nodeMap.put(dirPath, dirNode);
                                parent.getChildren().add(dirNode);
                            }
                            parent = nodeMap.get(dirPath);
                        }

                        // 加入檔案節點
                        FileTreeNode fileNode = FileTreeNode.builder()
                                .name(parts[parts.length - 1])
                                .path(filePath)
                                .type("file")
                                .children(null)
                                .build();
                        parent.getChildren().add(fileNode);
                    }
                }

                return root.getChildren() != null ? root.getChildren() : Collections.emptyList();
            }
        }
    }

    /**
     * 更新檔案內容並提交。
     *
     * @param userId    使用者 ID
     * @param projectId 專案 ID
     * @param filePath  檔案路徑
     * @param content   新內容
     */
    public void updateFileContent(UUID userId, UUID projectId, String filePath, String content) throws IOException, GitAPIException {
        Path repoPath = getBareRepoPath(userId, projectId);

        // 建立暫存工作區
        Path tempWorkspace = Files.createTempDirectory("git-update-");

        try {
            // Clone
            try (Git git = Git.cloneRepository()
                    .setURI(repoPath.toUri().toString())
                    .setDirectory(tempWorkspace.toFile())
                    .call()) {

                // 寫入檔案
                Path fullPath = tempWorkspace.resolve(filePath);
                if (fullPath.getParent() != null) {
                    Files.createDirectories(fullPath.getParent());
                }
                Files.writeString(fullPath, content);

                // Commit & Push
                git.add().addFilepattern(".").call();
                git.commit()
                        .setMessage("Update " + filePath)
                        .setAuthor("AppGenerator", "system@app-generator.local")
                        .call();
                git.push().call();

                log.info("Updated file '{}' in project {}", filePath, projectId);
            }
        } finally {
            deleteDirectoryRecursively(tempWorkspace);
        }
    }

    /**
     * 從遠端 Repository 克隆到本地 Bare Repository。
     *
     * @param remoteUrl 遠端 Repository URL
     * @param destinationBareRepoPath 目標 Bare Repo 路徑
     */
    public void cloneFromRemote(String remoteUrl, Path destinationBareRepoPath) throws GitAPIException, IOException {
        if (Files.exists(destinationBareRepoPath)) {
            // 如果目錄已存在，檢查是否為有效的 Git Repo
            if (Files.isDirectory(destinationBareRepoPath) && Files.list(destinationBareRepoPath).count() > 0) {
                 log.warn("Target path exists and is not empty: {}", destinationBareRepoPath);
            }
        } else {
             Files.createDirectories(destinationBareRepoPath.getParent());
        }

        log.info("Cloning from {} to {}", remoteUrl, destinationBareRepoPath);

        try (Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(destinationBareRepoPath.toFile())
                .setBare(true)
                .call()) {
            log.info("Cloned successfully to {}", destinationBareRepoPath);
        }
    }

    /**
     * 讀取 Bare Repository 中指定檔案的內容。
     *
     * @param userId    使用者 ID
     * @param projectId 專案 ID
     * @param filePath  檔案路徑（相對於 repo root）
     * @return 檔案內容字串
     */
    public String readFileContent(UUID userId, UUID projectId, String filePath) throws IOException {
        Path repoPath = getBareRepoPath(userId, projectId);

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoPath.toFile())
                .build()) {

            Ref head = repository.exactRef("HEAD");
            if (head == null || head.getObjectId() == null) {
                throw new IOException("Repository has no commits yet");
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(head.getObjectId());
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
                    if (treeWalk == null) {
                        throw new IOException("File not found in repository: " + filePath);
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    return new String(loader.getBytes(), StandardCharsets.UTF_8);
                }
            }
        }
    }

    /**
     * 遞迴刪除目錄（用於清理暫存工作區）
     */
    public void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", p, e);
                        }
                    });
        }
    }
}
