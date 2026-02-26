package com.tibame.app_generator.service;

import com.tibame.app_generator.enums.ImportStatus;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectImportService {

    private final GitService gitService;
    private final ProjectRepository projectRepository;
    private final AnalysisService analysisService;
    private final MetricsService metricsService;

    @Async
    public void importProjectAsync(UUID projectId) {
        metricsService.incrementImportTotal();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        try {
            log.info("Starting async import for project {}", projectId);

            Path bareRepoPath = gitService.getBareRepoPath(project.getUser().getId(), projectId);

            // Clone
            gitService.cloneFromRemote(project.getRemoteRepoUrl(), bareRepoPath);

            // Update status
            project.setImportStatus(ImportStatus.SUCCESS);
            project.setGitRepoPath(bareRepoPath.toString());
            projectRepository.save(project);
            metricsService.incrementImportSuccess();

            log.info("Import success for project {}. Triggering analysis.", projectId);

            // Trigger analysis
            analysisService.analyzeProject(projectId);

        } catch (Exception e) {
            log.error("Import failed for project {}", projectId, e);
            metricsService.incrementImportFailed();
            project.setImportStatus(ImportStatus.FAILED);
            project.setImportFailureReason(e.getMessage());
            projectRepository.save(project);
        }
    }
}
