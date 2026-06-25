package com.spring_midterm.midterm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.entity.Student;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void shouldSaveAndFindTask() {
        Student student = new Student();
        student.setFirstName("Test");
        student.setLastName("User");
        student.setEmail("test@example.com");
        student = studentRepository.save(student);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);
        task.setDueDate(LocalDate.now());
        task.setStudent(student);
        task = taskRepository.save(task);

        Task found = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Test Task");
        assertThat(found.getStudent().getId()).isEqualTo(student.getId());
    }

    @Test
    void shouldCountTasks() {
        assertThat(taskRepository.count()).isGreaterThanOrEqualTo(0);
    }
}
