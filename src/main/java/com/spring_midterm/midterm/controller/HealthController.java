package com.spring_midterm.midterm.controller;

import com.spring_midterm.midterm.config.AppSettings;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
