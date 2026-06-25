package com.spring_midterm.midterm.actuator;

import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.entity.Note;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class MetricsService implements IMetricsService {

    private final IRepository<Student> studentRepository;
    private final IRepository<Task> taskRepository;
    private final IRepository<Note> noteRepository;

    private final ConcurrentHashMap<String, AtomicLong> customCounters = new ConcurrentHashMap<>();

    public MetricsService(
            IRepository<Student> studentRepository,
            IRepository<Task> taskRepository,
            IRepository<Note> noteRepository
    ) {
        this.studentRepository = studentRepository;
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
    }

    public void incrementCounter(String name) {
        customCounters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    public Map<String, Object> getAllMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("jvm.memory.heap.used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        metrics.put("jvm.memory.heap.max", Runtime.getRuntime().maxMemory());
        metrics.put("jvm.threads.live", Thread.activeCount());
        metrics.put("system.load.average", ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemLoadAverage());
        metrics.put("db.students.count", studentRepository.count());
        metrics.put("db.tasks.count", taskRepository.count());
        metrics.put("db.notes.count", noteRepository.count());

        Map<String, Long> counters = new LinkedHashMap<>();
        customCounters.forEach((k, v) -> counters.put(k, v.get()));
        metrics.put("custom.counters", counters);

        return metrics;
    }

    public Map<String, Object> getMetricByName(String name) {
        var all = getAllMetrics();
        if (!all.containsKey(name)) {
            return null;
        }
        return Map.of("name", name, "value", all.get(name));
    }
}
