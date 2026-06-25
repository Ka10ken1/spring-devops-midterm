package com.spring_midterm.midterm.service;

import java.util.Map;

public interface IMetricsService {

    void incrementCounter(String name);

    Map<String, Object> getAllMetrics();

    Map<String, Object> getMetricByName(String name);
}
