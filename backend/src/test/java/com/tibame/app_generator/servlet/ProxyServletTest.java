package com.tibame.app_generator.servlet;

import com.tibame.app_generator.config.DockerProperties;
import com.tibame.app_generator.service.DockerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProxyServletTest {

    @Mock
    private DockerService dockerService;

    @Mock
    private DockerProperties dockerProperties;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private HttpResponse<InputStream> httpResponse;

    @Mock
    private ServletOutputStream outputStream;

    private ProxyServlet proxyServlet;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        // Use the protected constructor for testing
        proxyServlet = new ProxyServlet(dockerService, dockerProperties, httpClient);
        when(dockerProperties.getContainerPort()).thenReturn("8080");
        when(resp.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void service_ShouldInjectBaseTag_WhenResponseIsHtml() throws ServletException, IOException, InterruptedException {
        // Arrange
        String projectId = "test-project";
        String containerIp = "172.17.0.2";
        String htmlContent = "<html><head><title>Test</title></head><body>Hello</body></html>";

        when(req.getPathInfo()).thenReturn("/" + projectId + "/index.html");
        when(req.getMethod()).thenReturn("GET");
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(dockerService.getContainerIp(projectId)).thenReturn(containerIp);

        when(httpResponse.statusCode()).thenReturn(200);

        HttpHeaders headers = HttpHeaders.of(
                Map.of("Content-Type", List.of("text/html")),
                (k, v) -> true
        );
        when(httpResponse.headers()).thenReturn(headers);
        when(httpResponse.body()).thenReturn(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)));

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Act
        proxyServlet.service(req, resp);

        // Assert
        verify(dockerService).getContainerIp(projectId);
        verify(resp).setStatus(200);

        ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(bodyCaptor.capture());

        String responseBody = new String(bodyCaptor.getValue(), StandardCharsets.UTF_8);
        String expectedBaseTag = "<base href=\"/proxy/" + projectId + "/\">";

        // Check if base tag is injected
        assertTrue(responseBody.contains(expectedBaseTag));
        // Verify position (inside head)
        assertTrue(responseBody.contains("<head>" + expectedBaseTag + "<title>Test</title></head>"));
    }

    @Test
    void service_ShouldProxyRequest_ToCorrectUrl() throws ServletException, IOException, InterruptedException {
        // Arrange
        String projectId = "proj1";
        String containerIp = "10.0.0.5";

        when(req.getPathInfo()).thenReturn("/" + projectId + "/api/data");
        when(req.getMethod()).thenReturn("GET");
        when(req.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(dockerService.getContainerIp(projectId)).thenReturn(containerIp);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(HttpHeaders.of(Map.of(), (k, v) -> true));
        when(httpResponse.body()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Act
        proxyServlet.service(req, resp);

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());

        HttpRequest sentRequest = requestCaptor.getValue();
        assertEquals("http://10.0.0.5:8080/api/data", sentRequest.uri().toString());
    }
}
