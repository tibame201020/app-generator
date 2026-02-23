package com.tibame.app_generator;

import com.tibame.app_generator.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(StorageProperties.class)
public class AppGeneratorBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppGeneratorBackendApplication.class, args);
	}

}
