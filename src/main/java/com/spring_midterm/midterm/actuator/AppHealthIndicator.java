package com.spring_midterm.midterm.actuator;

import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.entity.Student;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AppHealthIndicator {

    private final IRepository<Student> studentRepository;

    public AppHealthIndicator(IRepository<Student> studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Map<String, Object> health() {
        long studentCount;
        boolean dbReachable;
        try {
            studentCount = studentRepository.count();
            dbReachable = true;
        } catch (Exception e) {
            studentCount = 0;
            dbReachable = false;
        }

        var status = dbReachable ? "UP" : "DOWN";

        return Map.of(
                "status", status,
                "components", Map.of(
                        "db", Map.of(
                                "status", dbReachable ? "UP" : "DOWN",
                                "studentCount", studentCount
                        ),
                        "diskSpace", Map.of(
                                "status", "UP",
                                "total", Runtime.getRuntime().totalMemory(),
                                "free", Runtime.getRuntime().freeMemory()
                        )
                )
        );
    }
}
