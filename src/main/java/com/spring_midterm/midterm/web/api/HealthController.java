package com.spring_midterm.midterm.web.api;

import com.spring_midterm.midterm.config.AppSettings;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

	@Value("${spring.application.name:midterm}")
	private String applicationName;

	private final AppSettings appSettings;

	public HealthController(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	@GetMapping("/health")
	public Map<String, Object> health() {
		log.debug("Health check requested");
		return Map.of(
				"status", "UP",
				"application", applicationName,
				"title", appSettings.getTitle(),
				"contactEmail", appSettings.getContactEmail(),
				"paginationLimit", appSettings.getPaginationLimit(),
				"timestamp", Instant.now().toString()
		);
	}
}
