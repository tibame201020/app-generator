package com.tibame.app_generator.model;

import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_tasks")
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_run_id")
    private WorkflowRun workflowRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 50)
    private AgentType agentType;

    @Column(name = "task_name", length = 255)
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_context", columnDefinition = "jsonb")
    private Map<String, Object> inputContext;

    @Column(name = "log_content", columnDefinition = "TEXT")
    private String logContent;

    @Column(name = "progress_pct")
    @Builder.Default
    private Integer progressPct = 0;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "backoff_factor")
    @Builder.Default
    private Double backoffFactor = 2.0;

    @Column(name = "initial_delay_seconds")
    @Builder.Default
    private Integer initialDelaySeconds = 5;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attempt_history", columnDefinition = "jsonb")
    private List<Map<String, Object>> attemptHistory;

    @Column(name = "is_retryable")
    @Builder.Default
    private boolean isRetryable = true;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retry_metadata", columnDefinition = "jsonb")
    private Map<String, Object> retryMetadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
