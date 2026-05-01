package com.spring_midterm.midterm.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@Value("${spring.application.name:midterm}")
	private String applicationName;

	@GetMapping("/health")
	public Map<String, Object> health() {
		return Map.of(
				"status", "UP",
				"application", applicationName,
				"timestamp", Instant.now().toString()
		);
	}
}
