package com.tibame.app_generator.model;

import com.tibame.app_generator.enums.ImportStatus;
import com.tibame.app_generator.enums.TechStack;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "git_repo_path", nullable = false, columnDefinition = "TEXT")
    private String gitRepoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "tech_stack", length = 50)
    private TechStack techStack;

    @Column(name = "remote_repo_url", columnDefinition = "TEXT")
    private String remoteRepoUrl;

    @Column(name = "default_branch", length = 50)
    private String defaultBranch;

    @Column(name = "last_sync_time")
    private ZonedDateTime lastSyncTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_status", length = 20)
    private ImportStatus importStatus;

    @Column(name = "import_failure_reason", columnDefinition = "TEXT")
    private String importFailureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
