package com.tibame.app_generator.controller;

import com.github.dockerjava.api.DockerClient;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
@Slf4j
public class OpsController {

    private final MetricsService metricsService;
    private final DockerClient dockerClient;
    private final ProjectRepository projectRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        boolean dbUp = false;
        boolean dockerUp = false;

        // Check DB
        try {
            projectRepository.count();
            dbUp = true;
        } catch (Exception e) {
            log.error("Health Check: DB Down", e);
        }

        // Check Docker
        try {
            dockerClient.pingCmd().exec();
            dockerUp = true;
        } catch (Exception e) {
            log.error("Health Check: Docker Down", e);
        }

        boolean overallUp = dbUp && dockerUp;

        status.put("status", overallUp ? "UP" : "DOWN");
        status.put("db", dbUp ? "UP" : "DOWN");
        status.put("docker", dockerUp ? "UP" : "DOWN");

        return ResponseEntity.status(overallUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(status);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        // Readiness check could be more extensive, for now alias to health
        return health();
    }
}
