package com.tibame.app_generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "platform.docker")
public class DockerProperties {

    /**
     * Docker Daemon Host URL.
     * e.g. unix:///var/run/docker.sock or tcp://localhost:2375
     */
    private String host;

    /**
     * Docker API Version.
     * Default: 1.41
     */
    private String apiVersion = "1.41";

    /**
     * Docker Registry URL (Optional).
     */
    private String registryUrl;

    /**
     * Path where workspaces are stored on the host.
     * Default: /data/workspaces
     */
    private String workspacePath = "/data/workspaces";

    /**
     * Domain name for dynamic routing.
     * Default: dev.platform.com
     */
    private String domain = "dev.platform.com";

    /**
     * Memory limit for containers in bytes.
     * Default: 512MB
     */
    private long memoryLimit = 512 * 1024 * 1024L;

    /**
     * CPU Period for containers.
     * Default: 100000
     */
    private long cpuPeriod = 100000L;

    /**
     * CPU Quota for containers.
     * Default: 50000 (0.5 CPU)
     */
    private long cpuQuota = 50000L;

    /**
     * The internal port the application listens on.
     * Default: 8080
     */
    private String containerPort = "8080";

    /**
     * The user ID:Group ID to run the container as.
     * Default: 1000:1000
     */
    private String containerUser = "1000:1000";

    /**
     * Default Docker image if no specific stack image is found.
     * Default: ubuntu:22.04
     */
    private String defaultImage = "ubuntu:22.04";

    /**
     * Map of TechStack to Docker image.
     */
    private Map<String, String> stackImages = new HashMap<>();
}
