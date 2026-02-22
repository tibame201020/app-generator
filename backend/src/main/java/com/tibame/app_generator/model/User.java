package com.tibame.app_generator.model;

import com.tibame.app_generator.enums.UserPlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 50)
    @Builder.Default
    private UserPlanType planType = UserPlanType.FREE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
