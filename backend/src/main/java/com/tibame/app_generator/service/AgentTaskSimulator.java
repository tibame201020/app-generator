package com.tibame.app_generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentTaskSimulator {

    private final AgentTaskService agentTaskService;

    @Async
    public void simulateTaskExecution(UUID taskId) {
        try {
            log.info("Starting simulation for task {}", taskId);
            agentTaskService.startTask(taskId);

            // Simulate steps
            int steps = 10;
            for (int i = 1; i <= steps; i++) {
                TimeUnit.SECONDS.sleep(1);
                int progress = i * 10;
                agentTaskService.updateProgress(taskId, progress, "Step " + i + " completed. Progress: " + progress + "%");
            }

            agentTaskService.completeTask(taskId, "Task simulation completed successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            agentTaskService.failTask(taskId, "Task interrupted.");
        } catch (Exception e) {
            log.error("Task simulation failed", e);
            agentTaskService.failTask(taskId, "Task failed: " + e.getMessage());
        }
    }
}
