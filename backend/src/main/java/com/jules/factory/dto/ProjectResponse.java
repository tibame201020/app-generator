package com.jules.factory.dto;

import com.jules.factory.domain.enums.ProjectState;
import java.time.LocalDateTime;

public record ProjectResponse(Long id, String name, String description, ProjectState status, LocalDateTime createdAt) {
}
