package com.spring_midterm.midterm.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.entity.Student;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppHealthIndicatorTest {

    @Mock
    private IRepository<Student> studentRepository;

    @Test
    void health_whenDbReachable_shouldReturnUp() {
        when(studentRepository.count()).thenReturn(42L);

        AppHealthIndicator indicator = new AppHealthIndicator(studentRepository);
        Map<String, Object> result = indicator.health();

        assertEquals("UP", result.get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> db = (Map<String, Object>) ((Map<String, Object>) result.get("components")).get("db");
        assertEquals("UP", db.get("status"));
        assertEquals(42L, db.get("studentCount"));
    }

    @Test
    void health_whenDbThrows_shouldReturnDown() {
        when(studentRepository.count()).thenThrow(new RuntimeException("DB connection failed"));

        AppHealthIndicator indicator = new AppHealthIndicator(studentRepository);
        Map<String, Object> result = indicator.health();

        assertEquals("DOWN", result.get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> db = (Map<String, Object>) ((Map<String, Object>) result.get("components")).get("db");
        assertEquals("DOWN", db.get("status"));
        assertEquals(0L, db.get("studentCount"));
    }

    @Test
    void health_shouldIncludeDiskSpace() {
        when(studentRepository.count()).thenReturn(1L);

        AppHealthIndicator indicator = new AppHealthIndicator(studentRepository);
        Map<String, Object> result = indicator.health();

        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) result.get("components");
        assertNotNull(components.get("diskSpace"));

        @SuppressWarnings("unchecked")
        Map<String, Object> disk = (Map<String, Object>) components.get("diskSpace");
        assertEquals("UP", disk.get("status"));
        assertTrue(((Number) disk.get("total")).longValue() > 0);
        assertTrue(((Number) disk.get("free")).longValue() >= 0);
    }
}
