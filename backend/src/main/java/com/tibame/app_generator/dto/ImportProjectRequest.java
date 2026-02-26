package com.tibame.app_generator.dto;

import lombok.Data;

@Data
public class ImportProjectRequest {
    private String name;
    private String description;
    private String remoteRepoUrl;
    private String defaultBranch; // Optional, default 'main'
}
