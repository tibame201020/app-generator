package com.tibame.app_generator.service;

import com.tibame.app_generator.dto.analysis.AnalysisResultDTO;
import com.tibame.app_generator.model.ProjectAnalysis;
import com.tibame.app_generator.repository.ProjectAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private ProjectAnalysisRepository projectAnalysisRepository;

    // We need to mock other dependencies of AnalysisService to avoid NPE in InjectMocks if constructor injection is used
    // Wait, with @InjectMocks and constructor injection, Mockito tries to match parameters.
    // AnalysisService has (ProjectRepository, ProjectAnalysisRepository, GitService).
    // I need to mock them all even if unused in this test, or use setter injection (which I don't have).
    // Or just let Mockito handle it (it usually passes null for missing mocks if not strict).
    @Mock
    private com.tibame.app_generator.repository.ProjectRepository projectRepository;
    @Mock
    private GitService gitService;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void getAnalysis_ShouldReturnDTO() {
        UUID projectId = UUID.randomUUID();
        AnalysisResultDTO dto = AnalysisResultDTO.builder().build();
        ProjectAnalysis analysis = ProjectAnalysis.builder().analysisJson(dto).build();

        when(projectAnalysisRepository.findByProjectId(projectId)).thenReturn(Optional.of(analysis));

        AnalysisResultDTO result = analysisService.getAnalysis(projectId);
        assertNotNull(result);
    }
}
