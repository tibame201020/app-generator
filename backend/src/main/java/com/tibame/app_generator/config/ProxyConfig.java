package com.tibame.app_generator.config;

import com.tibame.app_generator.service.DockerService;
import com.tibame.app_generator.servlet.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean
    public ServletRegistrationBean<ProxyServlet> proxyServletRegistrationBean(DockerService dockerService, DockerProperties dockerProperties) {
        ProxyServlet servlet = new ProxyServlet(dockerService, dockerProperties);
        ServletRegistrationBean<ProxyServlet> registrationBean = new ServletRegistrationBean<>(servlet, "/proxy/*");
        registrationBean.setName("ProxyServlet");
        registrationBean.setAsyncSupported(true);
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }
}
