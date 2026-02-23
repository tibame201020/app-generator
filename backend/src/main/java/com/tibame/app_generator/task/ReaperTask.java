package com.tibame.app_generator.task;

import com.tibame.app_generator.enums.ContainerStatus;
import com.tibame.app_generator.model.ContainerInstance;
import com.tibame.app_generator.repository.ContainerInstanceRepository;
import com.tibame.app_generator.service.DockerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReaperTask {

    private final ContainerInstanceRepository containerInstanceRepository;
    private final DockerService dockerService;

    @Scheduled(fixedRate = 60000) // Check every 1 minute
    @Transactional
    public void checkIdleContainers() {
        log.debug("Checking for idle containers...");
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(15);
        List<ContainerInstance> idleInstances = containerInstanceRepository.findByStatusAndLastAccessAtBefore(ContainerStatus.RUNNING, threshold);

        if (!idleInstances.isEmpty()) {
            log.info("Found {} idle containers to reap.", idleInstances.size());
        }

        for (ContainerInstance instance : idleInstances) {
            try {
                if (instance.getProject() != null) {
                    UUID projectId = instance.getProject().getId();
                    log.info("Stopping and removing idle container for project: {}", projectId);

                    dockerService.stopProjectContainer(projectId);
                    dockerService.removeProjectContainer(projectId);
                } else {
                    log.warn("ContainerInstance {} has no project associated", instance.getId());
                }

                // DockerService.stopProjectContainer already updates status to STOPPED
                // But we can ensure it here if we want, or rely on service.
                // Since we are in same transaction (if @Transactional works), it should be fine.

            } catch (Exception e) {
                log.error("Failed to reap container for instance: {}", instance.getId(), e);
            }
        }
    }
}
