package com.spring_midterm.midterm.actuator;

import com.spring_midterm.midterm.config.AppSettings;
import com.spring_midterm.midterm.actuator.IMetricsService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/actuator")
public class ActuatorController {

    private final AppHealthIndicator appHealthIndicator;
    private final IMetricsService metricsService;
    private final AppSettings appSettings;

    @Value("${spring.application.name:midterm}")
    private String applicationName;

    @Value("${management.info.app.version:1.0.0}")
    private String appVersion;

    public ActuatorController(
            AppHealthIndicator appHealthIndicator,
            IMetricsService metricsService,
            AppSettings appSettings
    ) {
        this.appHealthIndicator = appHealthIndicator;
        this.metricsService = metricsService;
        this.appSettings = appSettings;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        log.debug("Actuator health check requested");
        return appHealthIndicator.health();
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        log.debug("Actuator info requested");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", applicationName);
        info.put("title", appSettings.getTitle());
        info.put("version", appVersion);
        info.put("contactEmail", appSettings.getContactEmail());
        info.put("paginationLimit", appSettings.getPaginationLimit());
        info.put("timestamp", Instant.now().toString());
        return info;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        log.debug("Actuator metrics list requested");
        return metricsService.getAllMetrics();
    }

    @GetMapping("/metrics/{name}")
    public ResponseEntity<?> metricByName(@PathVariable String name) {
        log.debug("Actuator metric requested: {}", name);
        Map<String, Object> metric = metricsService.getMetricByName(name);
        if (metric == null) {
            log.warn("Unknown metric requested: {}", name);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metric);
    }
}
