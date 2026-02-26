package com.jules.factory.controller;

import com.jules.factory.dto.ChatRequest;
import com.jules.factory.dto.CreateProjectRequest;
import com.jules.factory.dto.ProjectResponse;
import com.jules.factory.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<Void> processChat(@PathVariable Long id, @RequestBody ChatRequest request) {
        projectService.processChat(id, request);
        return ResponseEntity.ok().build();
    }
}
