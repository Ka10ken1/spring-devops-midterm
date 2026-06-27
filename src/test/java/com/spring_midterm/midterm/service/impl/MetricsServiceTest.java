package com.spring_midterm.midterm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.entity.Note;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private IRepository<Student> studentRepository;

    @Mock
    private IRepository<Task> taskRepository;

    @Mock
    private IRepository<Note> noteRepository;

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService(studentRepository, taskRepository, noteRepository);
    }

    @Test
    void incrementCounter_shouldIncrementFromZero() {
        when(studentRepository.count()).thenReturn(0L);
        when(taskRepository.count()).thenReturn(0L);
        when(noteRepository.count()).thenReturn(0L);

        metricsService.incrementCounter("api.requests");

        @SuppressWarnings("unchecked")
        Map<String, Long> counters = (Map<String, Long>) metricsService.getAllMetrics().get("custom.counters");
        assertEquals(1L, counters.get("api.requests"));
    }

    @Test
    void incrementCounter_shouldIncrementExisting() {
        when(studentRepository.count()).thenReturn(0L);
        when(taskRepository.count()).thenReturn(0L);
        when(noteRepository.count()).thenReturn(0L);

        metricsService.incrementCounter("api.requests");
        metricsService.incrementCounter("api.requests");
        metricsService.incrementCounter("api.requests");

        @SuppressWarnings("unchecked")
        Map<String, Long> counters = (Map<String, Long>) metricsService.getAllMetrics().get("custom.counters");
        assertEquals(3L, counters.get("api.requests"));
    }

    @Test
    void getAllMetrics_shouldReturnAllKeys() {
        when(studentRepository.count()).thenReturn(5L);
        when(taskRepository.count()).thenReturn(10L);
        when(noteRepository.count()).thenReturn(15L);

        Map<String, Object> metrics = metricsService.getAllMetrics();

        assertTrue(metrics.containsKey("jvm.memory.heap.used"));
        assertTrue(metrics.containsKey("jvm.memory.heap.max"));
        assertTrue(metrics.containsKey("jvm.threads.live"));
        assertTrue(metrics.containsKey("system.load.average"));
        assertEquals(5L, metrics.get("db.students.count"));
        assertEquals(10L, metrics.get("db.tasks.count"));
        assertEquals(15L, metrics.get("db.notes.count"));
        assertTrue(metrics.containsKey("custom.counters"));
    }

    @Test
    void getMetricByName_whenExists_shouldReturnNameAndValue() {
        when(studentRepository.count()).thenReturn(3L);
        when(taskRepository.count()).thenReturn(0L);
        when(noteRepository.count()).thenReturn(0L);

        Map<String, Object> result = metricsService.getMetricByName("db.students.count");

        assertNotNull(result);
        assertEquals("db.students.count", result.get("name"));
        assertEquals(3L, result.get("value"));
    }

    @Test
    void getMetricByName_whenNotExists_shouldReturnNull() {
        when(studentRepository.count()).thenReturn(0L);
        when(taskRepository.count()).thenReturn(0L);
        when(noteRepository.count()).thenReturn(0L);

        Map<String, Object> result = metricsService.getMetricByName("nonexistent.metric");

        assertNull(result);
    }
}
