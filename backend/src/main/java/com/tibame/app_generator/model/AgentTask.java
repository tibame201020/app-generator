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

    @Column(name = "log_content", columnDefinition = "TEXT")
    private String logContent;

    @Column(name = "progress_pct")
    @Builder.Default
    private Integer progressPct = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
