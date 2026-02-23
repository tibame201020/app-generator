package com.tibame.app_generator.servlet;

import com.tibame.app_generator.config.DockerProperties;
import com.tibame.app_generator.service.DockerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;

@Slf4j
public class ProxyServlet extends HttpServlet {

    private final DockerService dockerService;
    private final DockerProperties dockerProperties;
    private final HttpClient httpClient;

    public ProxyServlet(DockerService dockerService, DockerProperties dockerProperties) {
        this(dockerService, dockerProperties, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    protected ProxyServlet(DockerService dockerService, DockerProperties dockerProperties, HttpClient httpClient) {
        this.dockerService = dockerService;
        this.dockerProperties = dockerProperties;
        this.httpClient = httpClient;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        // pathInfo starts with /, split gives ["", "projectId", "rest"...]
        String[] parts = pathInfo.split("/", 3);
        if (parts.length < 2) {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
             return;
        }

        String projectId = parts[1];
        String targetPath = parts.length > 2 ? "/" + parts[2] : "/";

        String query = req.getQueryString();
        if (query != null) {
            targetPath += "?" + query;
        }

        String containerIp;
        try {
            containerIp = dockerService.getContainerIp(projectId);
        } catch (Exception e) {
            log.error("Failed to get container IP for project {}", projectId, e);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Container not found for project: " + projectId);
            return;
        }

        String targetUrl = "http://" + containerIp + ":" + dockerProperties.getContainerPort() + targetPath;
        log.debug("Proxying to: {}", targetUrl);

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(req.getMethod(), HttpRequest.BodyPublishers.noBody());

            if (req.getContentLengthLong() > 0 || "POST".equalsIgnoreCase(req.getMethod()) || "PUT".equalsIgnoreCase(req.getMethod())) {
                requestBuilder.method(req.getMethod(), HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return req.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            // Copy headers
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (isHopByHopHeader(headerName) || headerName.equalsIgnoreCase("Host")) {
                    continue;
                }
                Enumeration<String> headers = req.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    requestBuilder.header(headerName, headers.nextElement());
                }
            }

            HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

            resp.setStatus(response.statusCode());

            // Copy response headers
            response.headers().map().forEach((k, v) -> {
                if (!isHopByHopHeader(k) && !k.equalsIgnoreCase("Content-Length")) {
                    v.forEach(val -> resp.addHeader(k, val));
                }
            });

            try (InputStream bodyStream = response.body()) {
                String contentType = response.headers().firstValue("Content-Type").orElse("");

                if (contentType.toLowerCase().contains("text/html")) {
                    String body = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
                    String baseTag = "<base href=\"/proxy/" + projectId + "/\">";

                    if (body.toLowerCase().contains("<head")) {
                        // Very simple regex replacement to insert base tag inside head
                        body = body.replaceFirst("(?i)(<head[^>]*>)", "$1" + baseTag);
                    } else {
                        body = baseTag + body;
                    }

                    byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                    resp.setContentLength(bodyBytes.length);
                    resp.getOutputStream().write(bodyBytes);
                } else {
                    response.headers().firstValue("Content-Length").ifPresent(len -> resp.setContentLengthLong(Long.parseLong(len)));
                    bodyStream.transferTo(resp.getOutputStream());
                }
            }

        } catch (Exception e) {
            log.error("Proxy error", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy error: " + e.getMessage());
        }
    }

    private boolean isHopByHopHeader(String name) {
        return name.equalsIgnoreCase("Connection") ||
                name.equalsIgnoreCase("Keep-Alive") ||
                name.equalsIgnoreCase("Proxy-Authenticate") ||
                name.equalsIgnoreCase("Proxy-Authorization") ||
                name.equalsIgnoreCase("TE") ||
                name.equalsIgnoreCase("Trailers") ||
                name.equalsIgnoreCase("Transfer-Encoding") ||
                name.equalsIgnoreCase("Upgrade");
    }
}
