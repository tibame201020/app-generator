package com.tibame.app_generator.service;

import com.tibame.app_generator.config.StorageProperties;
import com.tibame.app_generator.dto.FileTreeNode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GitService 單元測試。
 * 使用 @TempDir 建立隔離的暫存目錄，不依賴實際儲存路徑。
 */
class GitServiceTest {

    @TempDir
    Path tempDir;

    private GitService gitService;
    private StorageProperties storageProperties;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testProjectId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        storageProperties = new StorageProperties();
        storageProperties.setReposPath(tempDir.resolve("repos").toString());
        storageProperties.setWorkspacesPath(tempDir.resolve("workspaces").toString());
        storageProperties.init(); // 建立目錄

        gitService = new GitService(storageProperties);
    }

    @Test
    @DisplayName("應能成功初始化 Bare Git Repository")
    void testInitBareRepository() throws Exception {
        String repoPath = gitService.initBareRepository(testUserId, testProjectId);

        // 驗證路徑存在
        Path path = Path.of(repoPath);
        assertTrue(Files.exists(path), "Bare repo should exist at: " + repoPath);

        // 驗證是 Bare Repo
        try (Repository repo = new FileRepositoryBuilder()
                .setGitDir(path.toFile())
                .build()) {
            assertTrue(repo.isBare(), "Repository should be bare");
        }

        // 驗證有初始 commit (HEAD 不為空)
        try (Repository repo = new FileRepositoryBuilder()
                .setGitDir(path.toFile())
                .build()) {
            assertNotNull(repo.exactRef("HEAD").getObjectId(),
                    "Bare repo should have initial commit");
        }
    }

    @Test
    @DisplayName("重複初始化應回傳相同路徑而非拋出異常")
    void testInitBareRepositoryIdempotent() throws Exception {
        String path1 = gitService.initBareRepository(testUserId, testProjectId);
        String path2 = gitService.initBareRepository(testUserId, testProjectId);

        assertEquals(path1, path2, "Re-initializing should return same path");
    }

    @Test
    @DisplayName("應能 Clone 到工作區並從工作區 Commit + Push")
    void testCloneAndCommitAndPush() throws Exception {
        // 1. 初始化 Bare Repo
        gitService.initBareRepository(testUserId, testProjectId);

        // 2. Clone 到工作區
        String sessionId = UUID.randomUUID().toString();
        Path workspacePath = gitService.cloneToWorkspace(testUserId, testProjectId, sessionId);

        assertTrue(Files.exists(workspacePath), "Workspace should exist");
        assertTrue(Files.exists(workspacePath.resolve(".git")), "Should be a git workspace");

        // 3. 在工作區寫入檔案
        Files.writeString(workspacePath.resolve("hello.txt"), "Hello, World!");
        Files.createDirectories(workspacePath.resolve("src"));
        Files.writeString(workspacePath.resolve("src/App.java"), "public class App {}");

        // 4. Commit + Push
        gitService.commitAndPush(workspacePath, "Add hello.txt and App.java");

        // 5. 驗證 Bare Repo 已收到更新
        Path bareRepoPath = gitService.getBareRepoPath(testUserId, testProjectId);
        try (Repository repo = new FileRepositoryBuilder()
                .setGitDir(bareRepoPath.toFile())
                .build();
                Git git = new Git(repo)) {

            var log = git.log().setMaxCount(1).call();
            var latestCommit = log.iterator().next();
            assertEquals("Add hello.txt and App.java", latestCommit.getFullMessage());
        }
    }

    @Test
    @DisplayName("應能列出 Repository 中的完整檔案樹")
    void testListFiles() throws Exception {
        // 設置：建立 repo + 寫入多個檔案 + push
        gitService.initBareRepository(testUserId, testProjectId);
        String sessionId = UUID.randomUUID().toString();
        Path workspace = gitService.cloneToWorkspace(testUserId, testProjectId, sessionId);

        Files.writeString(workspace.resolve("README.md"), "# Test");
        Files.createDirectories(workspace.resolve("src/main/java"));
        Files.writeString(workspace.resolve("src/main/java/App.java"), "class App {}");
        gitService.commitAndPush(workspace, "Add project files");

        // 執行
        List<FileTreeNode> tree = gitService.listFiles(testUserId, testProjectId);

        // 驗證
        assertFalse(tree.isEmpty(), "File tree should not be empty");

        // 應包含 README.md（忽略 .gitkeep）
        boolean hasReadme = tree.stream()
                .anyMatch(n -> "README.md".equals(n.getName()) && "file".equals(n.getType()));
        assertTrue(hasReadme, "Should contain README.md");

        // 應包含 src 目錄
        boolean hasSrcDir = tree.stream()
                .anyMatch(n -> "src".equals(n.getName()) && "directory".equals(n.getType()));
        assertTrue(hasSrcDir, "Should contain src directory");
    }

    @Test
    @DisplayName("應能讀取 Repository 中指定檔案的內容")
    void testReadFileContent() throws Exception {
        // 設置
        String expectedContent = "public class Main { }";
        gitService.initBareRepository(testUserId, testProjectId);
        String sessionId = UUID.randomUUID().toString();
        Path workspace = gitService.cloneToWorkspace(testUserId, testProjectId, sessionId);

        Files.writeString(workspace.resolve("Main.java"), expectedContent);
        gitService.commitAndPush(workspace, "Add Main.java");

        // 執行
        String content = gitService.readFileContent(testUserId, testProjectId, "Main.java");

        // 驗證
        assertEquals(expectedContent, content);
    }

    @Test
    @DisplayName("讀取不存在的檔案應拋出 IOException")
    void testReadFileContentNotFound() throws Exception {
        gitService.initBareRepository(testUserId, testProjectId);

        assertThrows(java.io.IOException.class,
                () -> gitService.readFileContent(testUserId, testProjectId, "nonexistent.txt"));
    }
}
