package com.tibame.app_generator.controller;

import com.tibame.app_generator.dto.analysis.AnalysisResultDTO;
import com.tibame.app_generator.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<?> triggerAnalysis(@PathVariable UUID projectId) {
        analysisService.analyzeProject(projectId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<AnalysisResultDTO> getAnalysis(@PathVariable UUID projectId) {
        AnalysisResultDTO result = analysisService.getAnalysis(projectId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
