package com.tibame.app_generator.model;

import com.tibame.app_generator.enums.ContainerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "container_instances")
public class ContainerInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "container_id", length = 255)
    private String containerId;

    @Column(unique = true, length = 100)
    private String subdomain;

    @Column(name = "internal_ip", length = 50)
    private String internalIp;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ContainerStatus status;

    @UpdateTimestamp
    @Column(name = "last_access_at")
    private ZonedDateTime lastAccessAt;
}
