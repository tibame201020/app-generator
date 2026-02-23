package com.tibame.app_generator.task;

import com.tibame.app_generator.enums.ContainerStatus;
import com.tibame.app_generator.model.ContainerInstance;
import com.tibame.app_generator.repository.ContainerInstanceRepository;
import com.tibame.app_generator.service.DockerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReaperTask {

    private final ContainerInstanceRepository containerInstanceRepository;
    private final DockerService dockerService;

    @Scheduled(fixedRate = 60000) // Check every 1 minute
    public void checkIdleContainers() {
        log.debug("Checking for idle containers...");
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(15);
        List<ContainerInstance> idleInstances = containerInstanceRepository.findByStatusAndLastAccessAtBefore(ContainerStatus.RUNNING, threshold);

        if (!idleInstances.isEmpty()) {
            log.info("Found {} idle containers to reap.", idleInstances.size());
        }

        for (ContainerInstance instance : idleInstances) {
            try {
                String containerId = instance.getContainerId();
                log.info("Stopping and removing idle container: {} (Instance ID: {})", containerId, instance.getId());

                if (containerId != null) {
                    dockerService.stopContainer(containerId);
                    dockerService.removeContainer(containerId);
                }

                instance.setStatus(ContainerStatus.STOPPED);
                containerInstanceRepository.save(instance);

            } catch (Exception e) {
                log.error("Failed to reap container for instance: {}", instance.getId(), e);
            }
        }
    }
}
