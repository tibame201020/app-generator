package com.tibame.app_generator.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.tibame.app_generator.config.DockerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;
    private final DockerProperties properties;

    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    /**
     * Creates a new container for the project with resource limits and network configuration.
     *
     * @param projectId The project ID, used for container name and routing.
     * @param imageName The image to use for the container.
     * @return The ID of the created container.
     */
    public String createContainer(String projectId, String imageName) {
        log.info("Creating container for project: {} with image: {}", projectId, imageName);

        validateProjectId(projectId);

        // Define host bind path and ensure it exists
        Path workspacePath = Paths.get(properties.getWorkspacePath(), projectId);
        try {
            if (!Files.exists(workspacePath)) {
                Files.createDirectories(workspacePath);
                // Ideally, set permissions here if needed, but 1000:1000 usually works if created by parent process
                // or if we rely on Docker to use the existing ownership.
                // For MVP, we assume the Java process (running as 1000:1000 in container or appropriate user on host)
                // creates it with compatible permissions.
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create workspace directory: " + workspacePath, e);
        }

        String hostPathString = workspacePath.toAbsolutePath().toString();

        // Define resource limits
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withCpuPeriod(properties.getCpuPeriod())
                .withCpuQuota(properties.getCpuQuota())
                .withMemory(properties.getMemoryLimit())
                .withMemorySwap(properties.getMemoryLimit()) // No Swap
                .withPidsLimit(100L)
                .withBinds(Bind.parse(hostPathString + ":/app"));

        // Define Traefik labels
        String routerRule = String.format("Host(`%s.%s`)", projectId, properties.getDomain());

        Map<String, String> labels = new HashMap<>();
        labels.put("traefik.enable", "true");
        labels.put("traefik.http.routers." + projectId + ".rule", routerRule);
        labels.put("traefik.http.services." + projectId + ".loadbalancer.server.port", properties.getContainerPort());
        labels.put("traefik.http.routers." + projectId + ".entrypoints", "web");

        // Create container
        String containerId = dockerClient.createContainerCmd(imageName)
                .withName(projectId)
                .withHostConfig(hostConfig)
                .withLabels(labels)
                .withUser(properties.getContainerUser())
                .withTty(true) // Keep container running
                .exec()
                .getId();

        log.info("Container created with ID: {}", containerId);
        return containerId;
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || !PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            throw new IllegalArgumentException("Invalid projectId: " + projectId);
        }
    }

    /**
     * Starts an existing container.
     *
     * @param containerId The ID of the container to start.
     */
    public void startContainer(String containerId) {
        log.info("Starting container: {}", containerId);
        dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * Stops a running container.
     *
     * @param containerId The ID of the container to stop.
     */
    public void stopContainer(String containerId) {
        log.info("Stopping container: {}", containerId);
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.warn("Error stopping container {}: {}", containerId, e.getMessage());
        }
    }

    /**
     * Removes a container.
     *
     * @param containerId The ID of the container to remove.
     */
    public void removeContainer(String containerId) {
        log.info("Removing container: {}", containerId);
        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception e) {
            log.warn("Error removing container {}: {}", containerId, e.getMessage());
        }
    }

    /**
     * Inspects a container to get its status.
     *
     * @param containerId The ID of the container to inspect.
     * @return The inspection response containing status details.
     */
    public InspectContainerResponse inspectContainer(String containerId) {
        return dockerClient.inspectContainerCmd(containerId).exec();
    }

    /**
     * Retrieves the IP address of the container for the given project.
     *
     * @param projectId The project ID.
     * @return The IP address of the container.
     */
    public String getContainerIp(String projectId) {
        validateProjectId(projectId);
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(projectId).exec();
            // Assuming the container is connected to at least one network
            var networks = response.getNetworkSettings().getNetworks();
            if (networks == null || networks.isEmpty()) {
                throw new RuntimeException("Container has no network attached: " + projectId);
            }
            return networks.values().iterator().next().getIpAddress();
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            throw new RuntimeException("Container not found for project: " + projectId, e);
        } catch (Exception e) {
            throw new RuntimeException("Error inspecting container for project: " + projectId, e);
        }
    }
}
