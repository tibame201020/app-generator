package com.tibame.app_generator.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.tibame.app_generator.config.DockerProperties;
import com.tibame.app_generator.enums.ContainerStatus;
import com.tibame.app_generator.model.ContainerInstance;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.ContainerInstanceRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;
    private final DockerProperties properties;
    private final ContainerInstanceRepository containerInstanceRepository;
    private final ProjectRepository projectRepository;

    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    /**
     * Starts the container for the given project.
     * Creates it if it doesn't exist.
     */
    @Transactional
    public void startProjectContainer(UUID projectId) {
        log.info("Request to start container for project: {}", projectId);
        ensureContainer(projectId);
    }

    /**
     * Stops the container for the given project.
     */
    @Transactional
    public void stopProjectContainer(UUID projectId) {
        log.info("Request to stop container for project: {}", projectId);
        String containerName = projectId.toString();

        try {
            dockerClient.stopContainerCmd(containerName).exec();
            updateContainerStatus(projectId, ContainerStatus.STOPPED, null);
        } catch (NotFoundException e) {
            log.warn("Container not found when stopping: {}", containerName);
            updateContainerStatus(projectId, ContainerStatus.STOPPED, null);
        } catch (NotModifiedException e) {
            log.info("Container {} already stopped", containerName);
            updateContainerStatus(projectId, ContainerStatus.STOPPED, null);
        } catch (Exception e) {
            log.error("Error stopping container {}: {}", containerName, e.getMessage());
            throw new RuntimeException("Failed to stop container", e);
        }
    }

    /**
     * Restarts the container for the given project.
     */
    @Transactional
    public void restartProjectContainer(UUID projectId) {
        stopProjectContainer(projectId);
        startProjectContainer(projectId);
    }

    /**
     * Removes the container for the given project.
     */
    @Transactional
    public void removeProjectContainer(UUID projectId) {
        log.info("Request to remove container for project: {}", projectId);
        String containerName = projectId.toString();
        try {
            dockerClient.removeContainerCmd(containerName).withForce(true).exec();
        } catch (NotFoundException e) {
            log.warn("Container not found when removing: {}", containerName);
        } catch (Exception e) {
            log.error("Error removing container {}: {}", containerName, e.getMessage());
            throw new RuntimeException("Failed to remove container", e);
        }
    }

    /**
     * Gets the status of the container for the given project.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectContainerStatus(UUID projectId) {
        ContainerInstance instance = containerInstanceRepository.findByProjectId(projectId)
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (instance == null) {
            result.put("status", ContainerStatus.STOPPED);
            return result;
        }

        result.put("status", instance.getStatus());
        if (instance.getStatus() == ContainerStatus.RUNNING) {
            result.put("previewUrl", "/proxy/" + projectId + "/");
            result.put("internalIp", instance.getInternalIp());
        }
        return result;
    }

    /**
     * Ensures that a container exists and is running for the project.
     */
    private void ensureContainer(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        String containerName = projectId.toString();
        String imageName = getImageForStack(project);

        try {
            // Check if container exists
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();

            if (!Boolean.TRUE.equals(containerInfo.getState().getRunning())) {
                log.info("Container {} exists but is not running. Starting...", containerName);
                dockerClient.startContainerCmd(containerName).exec();
                containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            }

            // Update status
            String internalIp = getContainerIpInternal(containerInfo);
            updateContainerStatus(projectId, ContainerStatus.RUNNING, internalIp);

        } catch (NotFoundException e) {
            log.info("Container {} does not exist. Creating...", containerName);
            createAndStartContainer(project, imageName);
        }
    }

    private String getImageForStack(Project project) {
        if (project.getTechStack() != null) {
            String image = properties.getStackImages().get(project.getTechStack().name());
            if (image != null) {
                return image;
            }
        }
        return properties.getDefaultImage();
    }

    private void createAndStartContainer(Project project, String imageName) {
        String projectIdStr = project.getId().toString();
        validateProjectId(projectIdStr);

        // Define host bind path and ensure it exists
        Path workspacePath = Paths.get(properties.getWorkspacePath(), projectIdStr);
        try {
            if (!Files.exists(workspacePath)) {
                Files.createDirectories(workspacePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create workspace directory: " + workspacePath, e);
        }

        String hostPathString = workspacePath.toAbsolutePath().toString();

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withCpuPeriod(properties.getCpuPeriod())
                .withCpuQuota(properties.getCpuQuota())
                .withMemory(properties.getMemoryLimit())
                .withMemorySwap(properties.getMemoryLimit())
                .withPidsLimit(100L)
                .withBinds(Bind.parse(hostPathString + ":/app"));

        String routerRule = String.format("Host(`%s.%s`)", projectIdStr, properties.getDomain());

        Map<String, String> labels = new HashMap<>();
        labels.put("traefik.enable", "true");
        labels.put("traefik.http.routers." + projectIdStr + ".rule", routerRule);
        labels.put("traefik.http.services." + projectIdStr + ".loadbalancer.server.port", properties.getContainerPort());
        labels.put("traefik.http.routers." + projectIdStr + ".entrypoints", "web");

        try {
            String containerId = dockerClient.createContainerCmd(imageName)
                    .withName(projectIdStr)
                    .withHostConfig(hostConfig)
                    .withLabels(labels)
                    .withUser(properties.getContainerUser())
                    .withTty(true)
                    .exec()
                    .getId();

            dockerClient.startContainerCmd(containerId).exec();

            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            String internalIp = getContainerIpInternal(response);

            ContainerInstance instance = containerInstanceRepository.findByProjectId(project.getId())
                    .orElse(ContainerInstance.builder()
                            .project(project)
                            .subdomain(projectIdStr) // Using projectId as subdomain for now
                            .build());

            instance.setContainerId(containerId);
            instance.setStatus(ContainerStatus.RUNNING);
            instance.setInternalIp(internalIp);
            containerInstanceRepository.save(instance);

        } catch (Exception e) {
            log.error("Failed to create/start container for project {}", project.getId(), e);
            throw new RuntimeException("Failed to create container", e);
        }
    }

    private void updateContainerStatus(UUID projectId, ContainerStatus status, String internalIp) {
        ContainerInstance instance = containerInstanceRepository.findByProjectId(projectId)
                .orElse(ContainerInstance.builder()
                        .project(projectRepository.getReferenceById(projectId))
                        .subdomain(projectId.toString())
                        .build());

        instance.setStatus(status);
        if (internalIp != null) {
            instance.setInternalIp(internalIp);
        }

        // If stopping, maybe clear internal IP? Keeping it is fine too.

        containerInstanceRepository.save(instance);
    }

    private String getContainerIpInternal(InspectContainerResponse response) {
        var networks = response.getNetworkSettings().getNetworks();
        if (networks == null || networks.isEmpty()) {
            // Depending on network mode (e.g. host), might not have IP.
            // But for bridge (default), it should.
            return "127.0.0.1"; // Fallback? Or throw?
        }
        return networks.values().iterator().next().getIpAddress();
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || !PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw new IllegalArgumentException("Invalid projectId: " + projectId);
        }
    }

    // Kept for ProxyServlet compatibility
    public String getContainerIp(String projectId) {
        validateProjectId(projectId);
        try {
            // Try to get from DB first?
            // ProxyServlet needs IP. If we update DB correctly, we can fetch from DB.
            // But getting from Docker is more reliable for "is it actually running?"
            InspectContainerResponse response = dockerClient.inspectContainerCmd(projectId).exec();
            return getContainerIpInternal(response);
        } catch (NotFoundException e) {
            throw new RuntimeException("Container not found for project: " + projectId, e);
        } catch (Exception e) {
            throw new RuntimeException("Error inspecting container for project: " + projectId, e);
        }
    }
}
